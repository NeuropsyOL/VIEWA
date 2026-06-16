package de.uol.neuropsy.viewa.ui.plot

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.LineData
import de.uol.neuropsy.viewa.R
import de.uol.neuropsy.viewa.databinding.ItemMarkerPlotBinding
import de.uol.neuropsy.viewa.databinding.ItemStreamPlotBinding

class StreamPlotAdapter(
    private val viewModel: LivePlotViewModel, private val listener: (String)->Unit
) : ListAdapter<String, RecyclerView.ViewHolder>(DiffCallback) {

    interface OnPlotClickListener {
        fun onPlotClicked(item: String)
    }

    companion object {
        private const val VIEW_DATA = 0
        private const val VIEW_MARKER = 1

        private val DiffCallback = object : DiffUtil.ItemCallback<String>() {
            override fun areItemsTheSame(old: String, new: String) = old == new
            override fun areContentsTheSame(old: String, new: String) = true
        }
    }

    override fun getItemViewType(position: Int): Int {
        val streamName = getItem(position)
        return if (viewModel.markerStreams.contains(streamName)) VIEW_MARKER else VIEW_DATA
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

    inner class MarkerVH(val binding: ItemMarkerPlotBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_MARKER -> MarkerVH(ItemMarkerPlotBinding.inflate(inflater, parent, false))
            else        -> PlotVH(ItemStreamPlotBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val streamName = getItem(position)
        when (holder) {
            is PlotVH   -> bindDataHolder(holder, streamName)
            is MarkerVH -> bindMarkerHolder(holder, streamName)
        }
    }

    private fun bindDataHolder(holder: PlotVH, streamName: String) {
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
        // Pick label colour based on current night-mode setting
        val nightMask = holder.itemView.context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val labelColor = if (nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            0xFFCCCCCC.toInt()   // light grey for dark mode
        else
            android.graphics.Color.DKGRAY  // dark grey for light mode
        binding.streamChart.xAxis.textColor = labelColor
        binding.streamChart.axisLeft.textColor = labelColor
        binding.streamChart.legend.textColor = labelColor
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

    private fun bindMarkerHolder(holder: MarkerVH, streamName: String) {
        val binding = holder.binding
        binding.markerStreamTitle.text = streamName

        val ui = viewModel.markerUiState.value[streamName] ?: return

        val nightMask = holder.itemView.context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val labelColor = if (nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            0xFFCCCCCC.toInt() else android.graphics.Color.DKGRAY
        val markerLineColor = if (nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            0xFFFF6B6B.toInt() else 0xFFCC0000.toInt()

        binding.markerChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            axisLeft.isEnabled = false
            axisRight.isEnabled = false

            xAxis.textColor = labelColor
            xAxis.removeAllLimitLines()

            // Set the visible window to match the 10-second buffer
            val windowEnd = if (ui.latestX > 0f) ui.latestX else 10f
            val windowStart = windowEnd - 10f
            xAxis.axisMinimum = windowStart
            xAxis.axisMaximum = windowEnd
            xAxis.setDrawLimitLinesBehindData(false)

            ui.markers.forEach { (x, label) ->
                val ll = LimitLine(x, label).apply {
                    lineColor = markerLineColor
                    lineWidth = 1.5f
                    textColor = labelColor
                    textSize = 9f
                    labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                }
                xAxis.addLimitLine(ll)
            }

            // Provide empty data so the chart renders the x-axis and limit lines
            if (data == null) data = LineData()
            notifyDataSetChanged()
            invalidate()
        }
    }
}