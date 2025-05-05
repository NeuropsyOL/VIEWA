package de.uol.viewa.ui.plot

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import de.uol.viewa.LSLService
import de.uol.viewa.ui.main.utils.ColorPalette
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filter
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
    private var pausePlotting = false
    private var allTimeMin = mutableMapOf<String, Float>()
    private var allTimeMax = mutableMapOf<String, Float>()
    private var collectJob: Job? = null
    private var configJob: Job? = null
    private val buffers = mutableMapOf<String, Map<Int, ArrayDeque<Entry>>>()
    private val _uiState =
        MutableStateFlow<Map<String, ChartUiState>>(emptyMap())
    val uiState: StateFlow<Map<String, ChartUiState>> = _uiState.asStateFlow()

    /* Collects all config events and new samples, sends the new LineData to the UI
     *(i.e. the StreamPlotAdapter)
     * TODO: Simplify into one flow, branch on type of event. This map logic is more than I need
     * TODO: Throttle UI updates
     * */
    fun startCollecting(streams: List<String>, dataFlow: SharedFlow<LSLService.ServiceEvent>,
                        configFlow: StateFlow<Map<String,Int>>) {
        // Cancel any previous collection
        collectJob?.cancel()
        configJob?.cancel()
        buffers.clear()
        allTimeMin.clear()
        allTimeMax.clear()
        configJob = viewModelScope.launch {
            configFlow.collect { configMap ->
                // 1) Allocate buffers for any newly added streams
                (configMap.keys.intersect(streams.toSet()) - buffers.keys).forEach { name ->
                    val channelCount = configMap[name]!!
                    buffers[name] = (0 until channelCount)
                        .associateWith { ArrayDeque<Entry>(maxPoints) }
                        .toMutableMap()
                    allTimeMin[name] = Float.POSITIVE_INFINITY
                    allTimeMax[name] = Float.NEGATIVE_INFINITY
                    Log.i("LivePlotViewModel", "Initialized buffers for $name ($channelCount channels)")
                }

                // 2) remove any buffers for streams no longer selected
                (buffers.keys - streams.toSet()).forEach { name ->
                    buffers.remove(name)
                    allTimeMin.remove(name)
                    allTimeMax.remove(name)
                    Log.i("LivePlotViewModel", "Tore down buffers for $name")
                }
            }
        }

        collectJob = viewModelScope.launch {
            dataFlow.filterIsInstance<LSLService.ServiceEvent.DataSample>()
                .filter { it.streamName in streams }
                .collect { sampleEv ->
                    val name = sampleEv.streamName
                    val t = sampleEv.timestamp.toFloat()
                    val ys = sampleEv.sample
                    val chBufs = buffers[name]
                        ?: return@collect       // If this stream is not yet configured, drop the sample


                    // 2 add each channel’s new Entry
                    chBufs.forEach { (i, buf) ->
                        if (buf.size == maxPoints) buf.removeFirst()
                        buf.addLast(Entry(t, ys[i]))
                        buf.filter { t -> buf.last().x - t.x < 5 }
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
                                LineDataSet(buf.toList(), "$stream Ch-$i").apply {
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
        }
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