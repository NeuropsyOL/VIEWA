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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch

class StreamPlotAdapter(
    private val viewModel: LivePlotViewModel,
    private val scope: CoroutineScope,
    private val listener: (String) -> Unit
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

    inner class PlotVH(val binding: ItemStreamPlotBinding) : RecyclerView.ViewHolder(binding.root) {
        var streamName: String? = null
        var updateJob: Job? = null
        private val button: AppCompatImageButton = itemView.findViewById(R.id.show_fullscreen_btn)

        init {
            button.setOnTouchListener { v, _ ->
                v.parent?.requestDisallowInterceptTouchEvent(true)
                v.performClick()
                false
            }
            button.setOnClickListener {
                Log.e("PlotVH", "Button clicked: $adapterPosition")
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    listener(getItem(adapterPosition))
                }
            }
        }
    }

    inner class MarkerVH(val binding: ItemMarkerPlotBinding) : RecyclerView.ViewHolder(binding.root) {
        var streamName: String? = null
        var updateJob: Job? = null
    }

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

    // Start per-ViewHolder coroutines once the view is attached to the window (layout complete).
    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder) {
        super.onViewAttachedToWindow(holder)
        when (holder) {
            is PlotVH   -> startDataUpdates(holder)
            is MarkerVH -> startMarkerUpdates(holder)
        }
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        when (holder) {
            is PlotVH   -> holder.updateJob?.cancel()
            is MarkerVH -> holder.updateJob?.cancel()
        }
    }

    // onBindViewHolder only sets up static chart config (colors, axes). Data updates happen in
    // the per-ViewHolder coroutine started in onViewAttachedToWindow, which avoids calling
    // notifyDataSetChanged() on the whole adapter at 60 fps and prevents animation conflicts.
    private fun bindDataHolder(holder: PlotVH, streamName: String) {
        holder.streamName = streamName
        val binding = holder.binding
        binding.streamTitle.text = streamName

        binding.streamChart.description = Description().apply { isEnabled = false }
        binding.streamChart.axisRight.isEnabled = false

        val nightMask = holder.itemView.context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val labelColor = if (nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES)
            0xFFCCCCCC.toInt()
        else
            android.graphics.Color.DKGRAY
        binding.streamChart.xAxis.textColor = labelColor
        binding.streamChart.axisLeft.textColor = labelColor
        binding.streamChart.legend.textColor = labelColor
        // Initialise with empty data so MPAndroidChart renders axes immediately
        // instead of showing "No chart data available" while the coroutine starts up.
        if (binding.streamChart.data == null) binding.streamChart.data = LineData()
    }

    private fun startDataUpdates(holder: PlotVH) {
        holder.updateJob?.cancel()
        val name = holder.streamName ?: return
        holder.updateJob = scope.launch {
            viewModel.uiState
                .mapNotNull { it[name] }
                .conflate()   // drop intermediate values if the collector is busy
                .collect { state ->
                    val binding = holder.binding
                    val dataSets = state.entries
                    Log.d("LivePlot", "[Adapter] $name  datasets=${dataSets.size}  " +
                        dataSets.mapIndexed { i, ds ->
                            "Ch$i: entries=${ds.entryCount}  visible=${ds.isVisible}  " +
                            "yRange=[${if (ds.entryCount > 0) ds.yMin else Float.NaN}, " +
                            "${if (ds.entryCount > 0) ds.yMax else Float.NaN}]"
                        }.joinToString(" | ")
                    )
                    binding.streamChart.apply {
                        data = LineData(*dataSets.toTypedArray())
                        if (state.yMin.isFinite() && state.yMax.isFinite()) {
                            val range = state.yMax - state.yMin
                            val padding = if (range > 0f) range * 0.1f
                                          else Math.abs(state.yMax) * 0.1f + 1f
                            axisLeft.axisMaximum = state.yMax + padding
                            axisLeft.axisMinimum = state.yMin - padding
                        } else {
                            axisLeft.resetAxisMaximum()
                            axisLeft.resetAxisMinimum()
                        }
                        notifyDataSetChanged()
                        val xMax = data?.xMax ?: 0f
                        if (width > 0) {
                            moveViewToX(xMax)
                            invalidate()
                        } else {
                            post {
                                moveViewToX(xMax)
                                invalidate()
                            }
                        }
                    }
                    delay(16) // throttle to ~60 fps; conflate() drops values we can't keep up with
                }
        }
    }

    private fun bindMarkerHolder(holder: MarkerVH, streamName: String) {
        holder.streamName = streamName
        val binding = holder.binding
        binding.markerStreamTitle.text = streamName

        binding.markerChart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            setTouchEnabled(false)
            axisLeft.isEnabled = false
            axisRight.isEnabled = false
        }

        val nightMask = holder.itemView.context.resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        binding.markerChart.xAxis.textColor =
            if (nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                0xFFCCCCCC.toInt() else android.graphics.Color.DKGRAY
    }

    private fun startMarkerUpdates(holder: MarkerVH) {
        holder.updateJob?.cancel()
        val name = holder.streamName ?: return
        holder.updateJob = scope.launch {
            viewModel.markerUiState
                .mapNotNull { it[name] }
                .conflate()
                .collect { ui ->
                    val binding = holder.binding
                    val nightMask = holder.itemView.context.resources.configuration.uiMode and
                            android.content.res.Configuration.UI_MODE_NIGHT_MASK
                    val labelColor = if (nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                        0xFFCCCCCC.toInt() else android.graphics.Color.DKGRAY
                    val markerLineColor = if (nightMask == android.content.res.Configuration.UI_MODE_NIGHT_YES)
                        0xFFFF6B6B.toInt() else 0xFFCC0000.toInt()

                    binding.markerChart.apply {
                        xAxis.textColor = labelColor
                        xAxis.removeAllLimitLines()

                        val windowEnd = if (ui.latestX > 0f) ui.latestX else 10f
                        val windowStart = windowEnd - 10f
                        xAxis.axisMinimum = windowStart
                        xAxis.axisMaximum = windowEnd
                        xAxis.setDrawLimitLinesBehindData(false)

                        ui.markers.forEach { (x, label) ->
                            xAxis.addLimitLine(LimitLine(x, label).apply {
                                lineColor = markerLineColor
                                lineWidth = 1.5f
                                textColor = labelColor
                                textSize = 9f
                                labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
                            })
                        }

                        if (data == null) data = LineData()
                        notifyDataSetChanged()
                        invalidate()
                    }
                    delay(16)
                }
        }
    }
}