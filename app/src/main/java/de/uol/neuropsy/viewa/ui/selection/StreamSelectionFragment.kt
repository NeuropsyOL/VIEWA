package de.uol.neuropsy.viewa.ui.selection

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import de.uol.neuropsy.viewa.R

class StreamSelectionFragment : DialogFragment(R.layout.fragment_selection) {
    private val viewModel: StreamSelectionViewModel by lazy {
        // grabs the parent’s ViewModelStore, not the dialog’s
        ViewModelProvider(requireParentFragment())[StreamSelectionViewModel::class.java]
    }

    private lateinit var adapter: StreamListAdapter
    private lateinit var swipe: SwipeRefreshLayout

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        Log.e("StreamSelectionFragment","onCreateDialog()")
        val view = requireActivity().layoutInflater
            .inflate(R.layout.fragment_selection, null)

        // Grab UI references needed later
        swipe = view.findViewById(R.id.swipeRefresh)
        // Pull-to-refresh triggers discovery
        swipe.setOnRefreshListener {
            viewModel.refreshAvailableStreams()
        }

        // RecyclerView setup
        val recycler =
            view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.streamsRecyclerView)
        adapter = StreamListAdapter { name, checked -> viewModel.toggleStream(name, checked) }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Initial discovery
        viewModel.refreshAvailableStreams()
        swipe.isRefreshing=true

        var dialog = AlertDialog.Builder(requireContext())
            .setView(view).setPositiveButton("Apply changes") { _: DialogInterface, _: Int ->
                val selected = viewModel.selectedStreams.value
                parentFragmentManager.setFragmentResult(
                    "streamSelection", bundleOf(
                        "selectedStreams" to selected.toTypedArray()
                    )
                )
            }.setNegativeButton("Cancel"){ _: DialogInterface, _: Int -> }.setTitle("Stream selection").create()
        adapter.setSelectedStreams(viewModel.selectedStreams.value)
        return dialog
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e("StreamSelectionFragment","onDestroy")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.AppTheme)
    }

    override fun onStart() {
        super.onStart()
        Log.e("StreamSelectionFragment","onStart()")
        adapter.setSelectedStreams(viewModel.selectedStreams.value)
        viewModel.availableStreams.observe(this) { list ->
            adapter.submitList(list) {
                swipe.isRefreshing = false
            }
        }
    }
}
