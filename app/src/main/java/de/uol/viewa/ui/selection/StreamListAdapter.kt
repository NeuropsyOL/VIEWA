package de.uol.viewa.ui.selection

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import de.uol.neuropsy.viewa.R

/**
 * Adapter for displaying available LSL streams with checkboxes.
 * @param onCheckChanged invoked when the user toggles a stream.
 */
class StreamListAdapter(
    private val onCheckChanged: (streamName: String, isChecked: Boolean) -> Unit
) : ListAdapter<StreamItem, StreamListAdapter.ViewHolder>(DIFF_CALLBACK) {

    private var isRecording: Boolean = false

    /**
     * Disable checkboxes while recording is active.
     */
    fun setRecording(recording: Boolean) {
        isRecording = recording
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // Inflate a single MaterialCheckBox as the row view
        val checkBox = inflater.inflate(R.layout.item_stream, parent, false) as MaterialCheckBox
        return ViewHolder(checkBox)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), isRecording)
    }

    inner class ViewHolder(private val checkBox: MaterialCheckBox) : RecyclerView.ViewHolder(checkBox) {
        fun bind(item: StreamItem, recording: Boolean) {
            checkBox.text = item.name
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.isChecked
            checkBox.isEnabled = !recording
            checkBox.setOnCheckedChangeListener { _, checked ->
                onCheckChanged(item.name, checked)
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StreamItem>() {
            override fun areItemsTheSame(old: StreamItem, new: StreamItem) =
                old.name == new.name
            override fun areContentsTheSame(old: StreamItem, new: StreamItem) =
                old == new
        }
    }
}

/**
 * Data class representing a single stream in the list.
 */
data class StreamItem(
    val name: String,
    val isChecked: Boolean
)
