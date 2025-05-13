package de.uol.neuropsy.viewa.ui.selection

import android.util.Log
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

    private var selectedStreams: Set<String> = emptySet()

    fun setSelectedStreams(streams: Set<String>) {
        selectedStreams = streams
        Log.e("StreamListAdapter","Selected streams: $selectedStreams")
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // Inflate a single MaterialCheckBox as the row view
        val checkBox = inflater.inflate(R.layout.item_stream, parent, false) as MaterialCheckBox
        return ViewHolder(checkBox)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var item =getItem(position)
        item.isChecked=selectedStreams.contains(item.name)
        holder.bind(item,onCheckChanged)
    }

    inner class ViewHolder(private val checkBox: MaterialCheckBox) : RecyclerView.ViewHolder(checkBox) {
        fun bind(item: StreamItem, onCheckChanged: (streamName: String, isChecked: Boolean) -> Unit) {
            checkBox.text = item.name
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.isChecked
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
    var isChecked: Boolean
)
