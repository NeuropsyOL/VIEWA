package de.uol.neuropsy.viewa.ui.plot

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.LineData
import de.uol.neuropsy.viewa.R
import de.uol.neuropsy.viewa.databinding.ItemStreamPlotBinding

class StreamPlotAdapter(
    private val viewModel: LivePlotViewModel, private val listener: (String)->Unit
) : ListAdapter<String, StreamPlotAdapter.PlotVH>(DiffCallback) {

    interface OnPlotClickListener {
        fun onPlotClicked(item: String)
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(old: String, new: String) = old == new
            override fun areContentsTheSame(old: String, new: String) = true
        }
    }

    inner class PlotVH(val binding: ItemStreamPlotBinding)
        : RecyclerView.ViewHolder(binding.root){
        private val button: AppCompatImageButton = itemView.findViewById(R.id.show_fullscreen_btn)

        init {
            button.setOnTouchListener { v, _ ->
                v.parent?.requestDisallowInterceptTouchEvent(true)
                v.performClick()
                false
            }
            button.setOnClickListener {
                Log.e("PlotVH","Button clicked: $adapterPosition")
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener(getItem(adapterPosition))
                }
            }
        }
    }

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
        val dataSets = viewModel.uiState.value[streamName]?.entries ?: emptyList()
        Log.d("LivePlot", "[Adapter] $streamName  datasets=${dataSets.size}  " +
            dataSets.mapIndexed { i, ds ->
                "Ch$i: entries=${ds.entryCount}  visible=${ds.isVisible}  " +
                "yRange=[${if (ds.entryCount > 0) ds.yMin else Float.NaN}, ${if (ds.entryCount > 0) ds.yMax else Float.NaN}]"
            }.joinToString(" | ")
        )
        binding.streamChart.description=Description().apply {isEnabled=false}
        binding.streamChart.axisRight.isEnabled=false
        binding.streamChart.apply {
            data = LineData(*dataSets.toTypedArray())
            // Only apply axis limits when we have finite values (guard against initial ±Infinity)
            val yMin = viewModel.uiState.value[streamName]?.yMin ?: Float.NaN
            val yMax = viewModel.uiState.value[streamName]?.yMax ?: Float.NaN
            if (yMin.isFinite() && yMax.isFinite()) {
                val range = yMax - yMin
                val padding = if (range > 0f) range * 0.1f else Math.abs(yMax) * 0.1f + 1f
                axisLeft.axisMaximum = yMax + padding
                axisLeft.axisMinimum = yMin - padding
            } else {
                axisLeft.resetAxisMaximum()
                axisLeft.resetAxisMinimum()
            }
            // Auto-scroll to the latest data so the chart viewport follows incoming samples
            moveViewToX(data?.xMax ?: 0f)
            notifyDataSetChanged()
            invalidate()
        }
    }
}