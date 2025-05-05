package de.uol.viewa.ui.selection

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import de.uol.neuropsy.viewa.R

class StreamSelectionFragment : Fragment(R.layout.fragment_selection) {
    private val viewModel: StreamSelectionViewModel by viewModels()
    private lateinit var adapter: StreamListAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // UI references
        val swipe = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefresh)
        val recycler = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.streamsRecyclerView)
        val startBtn = view.findViewById<MaterialButton>(R.id.toLiveBtn)

        // RecyclerView setup
        adapter = StreamListAdapter { name, checked -> viewModel.toggleStream(name, checked) }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        // Observe available streams and stop refresh spinner
        viewModel.availableStreams.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            swipe.isRefreshing = false
        }

        // Observe recording state to disable list interactions
        viewModel.isRecording.observe(viewLifecycleOwner) { recording ->
            adapter.setRecording(recording)
            swipe.isEnabled = !recording
        }

        // Observe whether Start can be enabled
        viewModel.canStartRecording.observe(viewLifecycleOwner) { canStart ->
            startBtn.isEnabled = canStart
        }

        // Pull-to-refresh triggers discovery
        swipe.setOnRefreshListener {
            viewModel.refreshAvailableStreams()
        }

        // Start Recording button navigates to LivePlotFragment
        startBtn.setOnClickListener {
            val selected = viewModel.selectedStreams.value
                .orEmpty()
            val args = bundleOf(
                "selectedStreams" to selected.toTypedArray()
            )
            findNavController().navigate(
                R.id.action_selection_to_live,
                args
            )
        }
        // Initial discovery
        swipe.isRefreshing = true
        viewModel.refreshAvailableStreams()
    }
}
