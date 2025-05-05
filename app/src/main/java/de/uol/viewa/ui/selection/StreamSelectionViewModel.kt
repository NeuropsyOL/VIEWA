package de.uol.viewa.ui.selection

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.ucsd.sccn.LSL
import kotlinx.coroutines.Dispatchers
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

    // Internal set of selected names
    private val selectedSet = mutableSetOf<String>()

    // LiveData of currently selected stream names
    private val _selectedStreams = MutableLiveData<List<String>>(emptyList())
    val selectedStreams: LiveData<List<String>> = _selectedStreams

    // Recording toggle state
    private val _isRecording = MutableLiveData(false)
    val isRecording: LiveData<Boolean> = _isRecording

    // Combined LiveData indicating whether Start Recording can be enabled
    private val _canStartRecording = MediatorLiveData<Boolean>().apply {
        value = false
        // Recompute whenever selectedStreams or isRecording changes
        addSource(_selectedStreams) { selected ->
            value = selected.isNotEmpty() && (_isRecording.value == false)
        }
        addSource(_isRecording) { recording ->
            value = (_selectedStreams.value?.isNotEmpty() == true) && !recording
        }
    }
    val canStartRecording: LiveData<Boolean> = _canStartRecording
    /**
     * Called when user checks/unchecks a stream.
     */
    fun toggleStream(name: String, isChecked: Boolean) {
        if (isChecked) selectedSet.add(name) else selectedSet.remove(name)
        _selectedStreams.value = selectedSet.toList()
    }

    /**
     * Performs LSL discovery on a background thread,
     * updating availableStreams when done.
     */
    fun refreshAvailableStreams() {
        // Show spinner immediately
        _availableStreams.value = emptyList()
        viewModelScope.launch(Dispatchers.IO) {
            val infos=LSL.resolve_streams()
            val items = infos.map { info ->
                StreamItem(
                    name = info.name(),
                    isChecked = selectedSet.contains(info.name())
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
