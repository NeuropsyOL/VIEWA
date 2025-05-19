package de.uol.neuropsy.viewa.ui.plot

//import de.uol.neuropsy.viewa.ui.settings.SettingsDialog

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.uol.neuropsy.viewa.R
import de.uol.neuropsy.viewa.service.LSLService
import de.uol.neuropsy.viewa.ui.selection.StreamSelectionFragment
import de.uol.neuropsy.viewa.ui.settings.SettingsDialog
import kotlinx.coroutines.flow.sample


class LivePlotFragment : Fragment(R.layout.fragment_live_plot),
    StreamPlotAdapter.OnPlotClickListener {
    private val viewModel: LivePlotViewModel by activityViewModels()
    private lateinit var adapter: StreamPlotAdapter
    private var service: LSLService? = null
    private lateinit var wakeLock : PowerManager.WakeLock


    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as LSLService.LocalBinder).service()
            viewModel.bindService(service!!)
        }
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
        }
    }

    override fun onPlotClicked(item: String) {
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container,
                FullScreenPlotFragment::class.java,
                bundleOf(FullScreenPlotFragment.ARG_STREAM to item)
            )
            .addToBackStack("Fullscreen")          // <-- this makes “Back” undo this replace
            .commit()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wakeLock=(context.getSystemService(Context.POWER_SERVICE) as PowerManager).run { newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"LivePlotFragment::Wakelock") }
    }

    override fun onStart() {
        super.onStart()
        requireActivity().bindService(
            Intent(requireContext(), LSLService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
        wakeLock.acquire(10*60*1000L /*10 minutes*/)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unbindService(connection)
        wakeLock.release()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.plotsRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = StreamPlotAdapter(viewModel) { name -> onPlotClicked(name) }
        recycler.adapter = adapter
        adapter.submitList(viewModel.activeStreams.toList())

        // When *any* chartData updates, ask the adapter to re-bind visible ViewHolders
        // Use sample() to throttle to 60 FPS
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.sample(16).collect { _ ->
                adapter.notifyDataSetChanged()
            }
        }

        childFragmentManager.setFragmentResultListener(
            "streamSelection",     // requestKey
            this                   // lifecycleOwner
        ) { _, bundle ->
            val selectedStreams = bundle.getStringArray("selectedStreams")!!.toSet()
            adapter.submitList(selectedStreams.toSet().toList())
            viewModel.updateSelection(selectedStreams.toSet())
        }

        // Add a MenuProvider
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // inflate the fragment-specific menu
                menuInflater.inflate(R.menu.menu_live_plot, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_add_stream -> {
                        StreamSelectionFragment().show(childFragmentManager, "StreamSelectionTag")
                        true
                    }
                    R.id.action_settings -> {
                        SettingsDialog().show(childFragmentManager, "settings")
                        true
                    }
                    R.id.action_about -> {
                        showAboutDialog(requireContext())
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun showAboutDialog(ctx: Context) {
        val pm: PackageManager = ctx.packageManager
        val pkg: String = ctx.packageName
        val appName: String = ctx.applicationInfo.loadLabel(pm).toString()
        val version: String? = try {
            pm.getPackageInfo(pkg, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            "?.?.?"
        }

        val html = """
        <h1>About $appName</h1>
        <p>Version $version<br>© 2025 Carl von Ossietzky Universität Oldenburg.</p>
        <p>For more information, visit
          <a href="https://github.com/NeuropsyOL">our Github repo</a>.
        </p>
        <h2>Credits</h2>
        <ul>
          <li><a href="https://github.com/labstreaminglayer/liblsl-Java">liblsl-Java</a> — MIT License</li>
          <li><a href="https://github.com/sccn/liblsl">liblsl</a> — MIT License</li>
          <li><a href="https://github.com/PhilJay/MPAndroidChart">MPAndroidChart</a> — Apache 2.0</li>
        </ul>
    """.trimIndent()

        // 2) Inflate a TextView in code, apply padding and HTML
        val tv = TextView(context).apply {
            val pad = (resources.displayMetrics.density * 16).toInt()
            setPadding(pad, pad, pad, pad)
            text = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }

        // 3) Build and show
        AlertDialog.Builder(ctx)
            .setView(tv)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }
}