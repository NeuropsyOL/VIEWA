package de.uol.neuropsy.viewa.ui.plot

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import de.uol.neuropsy.viewa.R
import kotlinx.coroutines.flow.mapNotNull

class FullScreenPlotFragment : Fragment(R.layout.fragment_fullscreen_plot) {
    companion object {
        const val ARG_STREAM = "arg_stream"
    }
    private val viewModel: LivePlotViewModel by activityViewModels()

    override fun onStart() {
        // Set title on launch
        (requireActivity() as AppCompatActivity)
            .supportActionBar
            ?.title = streamName
        super.onStart()
    }

    // lazy so we only crash if someone forgot to pass it
    private val streamName: String by lazy {
        requireArguments().getString(ARG_STREAM)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_fullscreen_plot, container, false)


    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        val chart = v.findViewById<LineChart>(R.id.fullscreenChart)
        // chart setup (axes, descr, etc)…
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false
        var channelNames : List<String> = emptyList()
        // observe live data updates for this stream
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState
                .mapNotNull { it[streamName] }
                .collect { ui ->
                    val oldState=chart?.data?.dataSets?.map { d->d.isVisible }.orEmpty()
                    chart.data = LineData(*ui.entries.toTypedArray())
                    chart.data.isHighlightEnabled=false
                    chart.data.dataSets.zip(oldState).forEach{(ds,checked)->ds.isVisible=checked}
                    chart.axisLeft.axisMaximum = ui.yMax
                    chart.axisLeft.axisMinimum = ui.yMin
                    channelNames=chart.data.dataSetLabels.toList()
                    chart.invalidate()
                }
        }
        // Add a MenuProvider
        (requireActivity() as MenuHost).addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // inflate the fragment-specific menu
                menuInflater.inflate(R.menu.menu_fullscreen_plot, menu)
            }

            override fun onMenuItemSelected(item: MenuItem): Boolean {
                return when (item.itemId) {
                    R.id.action_close_fullscreen -> {
                        parentFragmentManager.popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
        // Using MPAndroidChart's gesture handlers is janky, in order to open
        // the fullscreen popup on touch, we need to intercept all gestures on the
        // fragment.

        chart?.setOnLongClickListener {
            viewModel.resetLimits(streamName)
            true
        }

        chart?.setOnClickListener { _ ->
            Log.e("FullScreenPlotFragment","chart clicked")
            val checkArray =
                chart.data?.dataSets?.map { dataSet -> dataSet.isVisible }?.toBooleanArray()
            AlertDialog.Builder(requireContext())
                .setTitle("Select channels")
                .setMultiChoiceItems(
                    channelNames.toTypedArray(),checkArray
                ) { _, which, isChecked ->
                    chart.data?.dataSets?.get(which)?.isVisible=isChecked
                }.setPositiveButton("OK"){_:DialogInterface, _: Int->}.create().show()
            }
    }
}