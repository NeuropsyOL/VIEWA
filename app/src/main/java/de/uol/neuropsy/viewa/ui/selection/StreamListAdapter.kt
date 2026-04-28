package de.uol.neuropsy.viewa.ui.selection

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_stream, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item =getItem(position)
        item.isChecked=selectedStreams.contains(item.name)
        holder.bind(item,onCheckChanged)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val checkBox    = itemView.findViewById<MaterialCheckBox>(R.id.checkBox)
        private val subTitleTxt = itemView.findViewById<TextView>(R.id.streamSubTitle)

        fun bind(item: StreamItem, onCheckChanged: (streamName: String, isChecked: Boolean) -> Unit) {
            checkBox.text = item.name
            checkBox.setOnCheckedChangeListener(null)
            checkBox.isChecked = item.isChecked
            checkBox.setOnCheckedChangeListener { _, checked ->
                onCheckChanged(item.name, checked)
            }
            subTitleTxt.text   = item.subtitle
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StreamItem>() {
            // Use uid — not name — so two outlets sharing the same name are distinct rows
            override fun areItemsTheSame(old: StreamItem, new: StreamItem) =
                old.uid == new.uid
            override fun areContentsTheSame(old: StreamItem, new: StreamItem) =
                old == new
        }
    }
}

/**
 * Data class representing a single stream in the list.
 * [uid] is the LSL outlet UID — guaranteed unique per outlet instance — used
 * as the DiffUtil identity key so that two streams sharing the same name are
 * still treated as distinct rows.
 */
data class StreamItem(
    val uid: String,
    val name: String,
    var isChecked: Boolean,
    val subtitle : String
)
