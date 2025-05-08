package de.uol.viewa.ui.plot

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import de.uol.neuropsy.viewa.R
import de.uol.viewa.LSLService
import kotlinx.coroutines.flow.sample


class LivePlotFragment : Fragment(R.layout.fragment_live_plot) {
    private val viewModel: LivePlotViewModel by activityViewModels()
    private lateinit var adapter: StreamPlotAdapter
    private var service: LSLService? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            service = (binder as LSLService.LocalBinder).service()

            val streams = arguments!!.getStringArray("selectedStreams")!!.toList()
            // Tell VM to collect data and update ui state
            viewModel.startCollecting(streams, service!!.dataFlow)
            // Start all inlets
            streams.forEach { Log.i("LivePlotFragment","Starting inlet $it")
                service!!.startInlet(it)
            }
            // The order is very important here: If we start the inlets before the VM is ready to
            // collect, the configuration events sent by the service can be lost

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

        // When *any* chartData updates, ask the adapter to re-bind visible ViewHolders
        // Use sample() to throttle to 60 FPS
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.uiState.sample(16).collect { _ ->
                adapter.notifyDataSetChanged()
            }
        }
        val tapDetector = GestureDetector(requireContext(),
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onSingleTapUp(e: MotionEvent): Boolean {
                    // Returning true means “I’ve recognized the tap”
                    return true
                }
            }
        )

        // Using MPAndroidChart's gesture handlers is janky, in order to open
        // the fullscreen popup on touch, we need to intercept all gestures on the
        // fragment.
        recycler.addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                // Feed every event to the detector
                if (tapDetector.onTouchEvent(e)) {
                    // It was a tap, find which child was tapped:
                    val child = rv.findChildViewUnder(e.x, e.y)
                    if (child != null) {
                        val pos = rv.getChildAdapterPosition(child)
                        if (pos != RecyclerView.NO_POSITION) {
                            val streamName = adapter.currentList[pos]
                            FullScreenPlotDialogFragment.show(
                                childFragmentManager,
                                streamName
                            )
                        }
                    }
                }
                // Return false so normal RecyclerView scrolling still works
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {
                // No-op
            }

            override fun onRequestDisallowInterceptTouchEvent(disallow: Boolean) {
                // No-op
            }
        })
    }
}