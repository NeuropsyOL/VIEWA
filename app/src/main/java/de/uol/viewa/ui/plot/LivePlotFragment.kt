package de.uol.viewa.ui.plot

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.lifecycle.lifecycleScope
import de.uol.neuropsy.viewa.R
import de.uol.viewa.LSLService
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class LivePlotFragment : Fragment(R.layout.fragment_live_plot) {
    private val viewModel: LivePlotViewModel by viewModels({ requireActivity() })
    private lateinit var adapter: StreamPlotAdapter
    private var service: LSLService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as LSLService.LocalBinder).service()
            // 1) Start all inlets:
            val streams = arguments!!.getStringArray("selectedStreams")!!.toList()
            streams.forEach { Log.i("LivePlotFragment","Starting inlet $it")
                service!!.startInlet(it) }
            // 2) Tell VM to collect & buffer them
            viewModel.startCollecting(streams, service!!.dataFlow, service!!.configFlow)
        }
        override fun onServiceDisconnected(name: ComponentName) {
            service = null
        }
    }

    override fun onStart() {
        super.onStart()
        Log.i("LivePlotFragment","onStart")
        requireActivity().bindService(
            Intent(requireContext(), LSLService::class.java),
            connection,
            Context.BIND_AUTO_CREATE
        )
    }

    override fun onStop() {
        super.onStop()
        // stop everything
        viewModel.stopCollecting()
        service?.stopAll()
        requireActivity().unbindService(connection)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler = view.findViewById<RecyclerView>(R.id.plotsRecycler)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        adapter = StreamPlotAdapter(viewModel)
        recycler.adapter = adapter

        // Observe chart data map
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { map ->
                // adapter’s list is the stream names in the original order
                val streams = requireArguments().getStringArray("selectedStreams")!!.toList()
                adapter.submitList(streams)
            }
        }

        // 2) When *any* chartData updates, ask the adapter to re-bind visible ViewHolders:
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.collect { _ ->
                adapter.notifyDataSetChanged()
            }
        }

    }
}