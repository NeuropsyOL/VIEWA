package de.uol.neuropsy.viewa.ui.plot

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import android.view.GestureDetector
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.uol.neuropsy.viewa.R
import de.uol.neuropsy.viewa.LSLService
import de.uol.neuropsy.viewa.ui.selection.StreamSelectionFragment
//import de.uol.neuropsy.viewa.ui.settings.SettingsDialog
import kotlinx.coroutines.flow.sample


class LivePlotFragment : Fragment(R.layout.fragment_live_plot) {
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
        wakeLock.acquire()
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unbindService(connection)
        wakeLock.release()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        var recycler = view.findViewById<RecyclerView>(R.id.plotsRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = StreamPlotAdapter(viewModel)
        recycler.adapter = adapter
        adapter.submitList(viewModel.activeStreams.toList())

        // When *any* chartData updates, ask the adapter to re-bind visible ViewHolders
        // Use sample() to throttle to 60 FPS
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.sample(16).collect { _ ->
                adapter.notifyDataSetChanged()
            }
        }
        val tapDetector = GestureDetector(requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    // Returning true means “I’ve recognized the tap”
                    return true
                }
            }
        )

        // Using MPAndroidChart's gesture handlers is janky, in order to open
        // the fullscreen popup on touch, we need to intercept all gestures on the
        // fragment.
        recycler.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                // Feed every event to the detector
                if (tapDetector.onTouchEvent(e)) {
                    // It was a tap, find which child was tapped:
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null) {
                        val pos = rv.getChildAdapterPosition(child)
                        if (pos != RecyclerView.NO_POSITION) {
                            val streamName = adapter.currentList[pos]
                            FullScreenPlotDialogFragment.show(
                                childFragmentManager,
                                streamName
                            )
                        }
                    }
                }
                // Return false so normal RecyclerView scrolling still works
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                // No-op
            }

            override fun onRequestDisallowInterceptTouchEvent(disallow: Boolean) {
                // No-op
            }
        })

        childFragmentManager.setFragmentResultListener(
            "streamSelection",     // requestKey
            this                   // lifecycleOwner
        ) { _, bundle ->
            val selectedStreams = bundle.getStringArray("selectedStreams")!!.toSet()
            adapter.submitList(selectedStreams!!.toSet().toList())
            viewModel.updateSelection(selectedStreams!!.toSet())
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
                        //SettingsDialog().show(childFragmentManager, "settings")
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}