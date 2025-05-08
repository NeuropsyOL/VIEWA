package de.uol.viewa.ui.plot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import de.uol.viewa.LSLService
import de.uol.viewa.ui.main.utils.ColorPalette
import edu.ucsd.sccn.LSL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch
import kotlin.math.max
import kotlin.math.min

// A simple UI state holding the latest list of chart Entries
// and the all time max/min values used for axis scaling
//
data class ChartUiState(
    val entries: List<ILineDataSet> = emptyList(),
    val yMin: Float = Float.POSITIVE_INFINITY,
    val yMax: Float = Float.NEGATIVE_INFINITY
)

class LivePlotViewModel : ViewModel() {

    private val maxPoints = 500
    val bufferSizeInSeconds = 10.0
    private var pausePlotting = false
    private var allTimeMin = mutableMapOf<String, Float>()
    private var allTimeMax = mutableMapOf<String, Float>()
    private var job: Job? = null
    private val buffers = mutableMapOf<String, Map<Int, ArrayDeque<Entry>>>()
    private val _uiState =
        MutableStateFlow<Map<String, ChartUiState>>(emptyMap())
    val uiState: StateFlow<Map<String, ChartUiState>> = _uiState.asStateFlow()

    /* Collects all config events and new samples, sends the new LineData to the UI
     *(i.e. the StreamPlotAdapter)
     * */
    fun startCollecting(streams: List<String>, dataFlow: SharedFlow<LSLService.ServiceEvent>) {
        // Cancel any previous collection
        job?.cancel()
        buffers.clear()
        allTimeMin.clear()
        allTimeMax.clear()

        job = viewModelScope.launch(Dispatchers.Default) {
            dataFlow.filterIsInstance<LSLService.ServiceEvent>()
                .collect { ev ->
                    when (ev) {
                        is LSLService.ServiceEvent.StreamConfig -> handleConfigurationEvent(ev)
                        is LSLService.ServiceEvent.DataSample -> handleDataEvent(ev)
                    }
                }
        }
    }

    private fun handleDataEvent(sampleEv: LSLService.ServiceEvent.DataSample) {
        val name = sampleEv.streamName
        val t = sampleEv.timestamp.toFloat()
        val ys = sampleEv.sample
        val chBufs = buffers[name]
            ?: return       // If this stream is not yet configured, drop the sample


        // add each channel’s new Entry
        chBufs.forEach { (i, buf) ->
            buf.addLast(Entry(t, ys[i]))
            // Remove every entry older than 5 seconds
            // TODO make the timeout configurable
            buf.removeIf { t -> buf.last().x - t.x > bufferSizeInSeconds }
        }

        // Compute this stream's current min/max
        val allValues = chBufs.flatMap { it.value.toList() }.map { it.y }
        val currentMin = allValues.minOrNull() ?: 0f
        val currentMax = allValues.maxOrNull() ?: 0f

        allTimeMin[name] = min(allTimeMin[name]!!, currentMin)
        allTimeMax[name] = max(allTimeMax[name]!!, currentMax)

        val cp = ColorPalette()

        // 7) Build a ChartUiState for each stream
        if (!pausePlotting) {
            val stateMap = buffers.mapValues { (stream, bufMap) ->
                // create one DataSet per channel
                val sets = bufMap.map { (i, buf) ->
                    LineDataSet(buf.toList(), "Ch-$i").apply {
                        setDrawCircles(false)
                        lineWidth = 1f
                        color = cp.nextColor()
                    }
                }
                ChartUiState(
                    entries = sets,
                    yMin = allTimeMin[stream]!!,
                    yMax = allTimeMax[stream]!!
                )
            }
            _uiState.value = stateMap
        }
    }

    private fun handleConfigurationEvent(configEvent: LSLService.ServiceEvent.StreamConfig) {
        val channelCount = configEvent.channelCount!!
        val streamName = configEvent.streamName
        val bufferSize =
            if (configEvent.samplingRate == LSL.IRREGULAR_RATE) maxPoints else (bufferSizeInSeconds * configEvent.samplingRate + 1).toInt()
        buffers[streamName] = (0 until channelCount)
            .associateWith { ArrayDeque<Entry>(bufferSize) }
            .toMutableMap()
        allTimeMin[streamName] = Float.POSITIVE_INFINITY
        allTimeMax[streamName] = Float.NEGATIVE_INFINITY
    }

    fun stopCollecting() {
        _uiState.value = emptyMap()
    }

    fun resetRange() {
        // reset to some sensible defaults, e.g. ±1
        val defaultMin = Float.POSITIVE_INFINITY
        val defaultMax = Float.NEGATIVE_INFINITY
        //_uiState.value = _uiState.value.copy(yMin = defaultMin, yMax = defaultMax)
    }
}