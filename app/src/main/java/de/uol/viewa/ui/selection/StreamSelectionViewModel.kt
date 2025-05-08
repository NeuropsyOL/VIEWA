package de.uol.viewa.ui.selection

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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

    // LiveData of currently selected stream names
    private val _selectedStreams = MutableStateFlow(mutableSetOf<String>())
    val selectedStreams: StateFlow<MutableSet<String>> = _selectedStreams

    // Recording toggle state
    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    // Combined LiveData indicating whether Start Recording can be enabled
    private val _canStartRecording = MediatorLiveData<Boolean>().apply {

    }
    val canStartRecording: LiveData<Boolean> = _canStartRecording
    /**
     * Called when user checks/unchecks a stream.
     */
    fun toggleStream(name: String, isChecked: Boolean) {
        Log.e("StreamSelectionViewModel","$name $isChecked")
        if (isChecked) _selectedStreams.value+=name else _selectedStreams.value-=name
    }

    /**
     * Performs LSL discovery on a background thread,
     * updating availableStreams when done.
     */
    fun refreshAvailableStreams() {
        // Show spinner immediately
        //_availableStreams.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            val infos=LSL.resolve_streams()
            val items = infos.map { info ->
                StreamItem(
                    name = info.name(),
                    isChecked = _selectedStreams.value.contains(info.name())
                )
            }
            withContext(Dispatchers.Main) {
                _availableStreams.value = items
            }
        }
    }

    /**
     * Call when recording starts/stops to disable UI.
     */
    fun setRecording(active: Boolean) {
        _isRecording.value = active
    }
}
