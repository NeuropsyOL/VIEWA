package de.uol.neuropsy.viewa.ui.plot

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.LineData
import de.uol.neuropsy.viewa.R
import kotlinx.coroutines.flow.mapNotNull

class FullScreenPlotDialogFragment : DialogFragment(R.layout.fragment_fullscreen_plot) {
    private val viewModel: LivePlotViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL,R.style.AppTheme)
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            window.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    override fun onViewCreated(v: View, savedInstanceState: Bundle?) {
        val chart = v.findViewById<LineChart>(R.id.fullscreenChart)
        val streamName = requireArguments().getString("streamName")!!
        // chart setup (axes, descr, etc)…
        chart.description.isEnabled = false
        chart.axisRight.isEnabled = false

        // observe live data updates for this stream
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState
                .mapNotNull { it[streamName] }
                .collect { ui ->
                    chart.data = LineData(*ui.entries.toTypedArray())
                    chart.axisLeft.axisMaximum = ui.yMax
                    chart.axisLeft.axisMinimum = ui.yMin
                    chart.invalidate()
                }
        }
        val closeBtn=v.findViewById<View>(R.id.plot_popup_close_button)
        closeBtn?.setOnClickListener{dismiss()}
    }

    companion object {
        fun show(fm: FragmentManager, streamName: String) {
            FullScreenPlotDialogFragment().apply {
                arguments = bundleOf("streamName" to streamName)
            }.show(fm, "fullScreen_$streamName")
        }
    }
}