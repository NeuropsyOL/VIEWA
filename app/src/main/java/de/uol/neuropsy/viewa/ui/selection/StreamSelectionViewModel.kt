package de.uol.neuropsy.viewa.ui.selection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ucsd.sccn.LSL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for StreamSelectionFragment.
 * Discovers available LSL streams, tracks user selections,
 * and exposes recording state.
 */
class StreamSelectionViewModel : ViewModel() {

    // LiveData of streams to display
    private val _availableStreams = MutableLiveData<List<StreamItem>>(emptyList())
    val availableStreams: LiveData<List<StreamItem>> = _availableStreams

    // StateFlow of currently selected stream names
    private val _selectedStreams = MutableStateFlow(mutableSetOf<String>())
    val selectedStreams: StateFlow<MutableSet<String>> = _selectedStreams

    /**
     * Called when user checks/unchecks a stream.
     */
    fun toggleStream(name: String, isChecked: Boolean) {
        Log.e("StreamSelectionViewModel","$name $isChecked")
        if (isChecked) _selectedStreams.value+=name else _selectedStreams.value-=name
        Log.e("StreamSelectionViewModel","${_selectedStreams.value}")
    }

    /**
     * Performs LSL discovery on a background thread,
     * updating availableStreams when done.
     *
     * wait_time is set to 3 s (instead of the 1 s default) so that all outlets on
     * a WiFi network have time to re-announce themselves before we collect results.
     */
    fun refreshAvailableStreams() {
        viewModelScope.launch(Dispatchers.IO) {
            val infos = LSL.resolve_streams(3.0)
            val items = infos.map { info ->
                val host = info.hostname().takeIf { it.isNotEmpty() } ?: "unknown host"
                StreamItem(
                    uid = info.uid(),
                    name = info.name(),
                    isChecked = _selectedStreams.value.contains(info.name()),
                    subtitle = "${info.channel_count()} ch  ${info.type()}  @ " +
                            "${if (info.nominal_srate() == LSL.IRREGULAR_RATE) "irregular" else info.nominal_srate()} Hz  [$host]"
                )
            }
            withContext(Dispatchers.Main) {
                _availableStreams.value = items
            }
        }
    }
}
