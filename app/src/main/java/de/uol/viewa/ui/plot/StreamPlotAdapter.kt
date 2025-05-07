package de.uol.viewa.ui.plot

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.LineData
import de.uol.neuropsy.viewa.databinding.ItemStreamPlotBinding

class StreamPlotAdapter(
    private val viewModel: LivePlotViewModel
) : ListAdapter<String, StreamPlotAdapter.PlotVH>(DiffCallback) {

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(old: String, new: String) = old == new
            override fun areContentsTheSame(old: String, new: String) = true
        }
    }

    inner class PlotVH(val binding: ItemStreamPlotBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlotVH {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemStreamPlotBinding.inflate(inflater, parent, false)
        return PlotVH(binding)
    }

    override fun onBindViewHolder(holder: PlotVH, position: Int) {
        val streamName = getItem(position)
        val binding = holder.binding
        // Set the text of the title TV
        binding.streamTitle.text = streamName
        // Fetch the latest DataSets for this stream
        val dataSets = viewModel.uiState.value[streamName]?.entries?: emptyList()
        binding.streamChart.description=Description().apply {isEnabled=false}
        binding.streamChart.axisRight.isEnabled=false

        // Apply to the chart
        binding.streamChart.apply {
            data = LineData(*dataSets.toTypedArray())
            viewModel.uiState.value[streamName]?.yMax?.let { axisLeft.axisMaximum=it }
            viewModel.uiState.value[streamName]?.yMin?.let { axisLeft.axisMinimum=it }
            notifyDataSetChanged()
            invalidate()
        }
    }
}