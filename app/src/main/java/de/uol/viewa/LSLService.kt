package de.uol.viewa

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import edu.ucsd.sccn.LSL
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel.Factory.UNLIMITED
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class LSLService : LifecycleService() {

    sealed class ServiceEvent {
        data class DataSample(val streamName: String,val timestamp:Double, val sample: FloatArray) : ServiceEvent()
        data class StreamConfig(val streamName:String,val channelCount:Int) : ServiceEvent()
    }

    // This SharedFlow handles all data flow from the service to the view model.
    // Whenever a new inlet is opened, a StreamConfig is emitted to the flow which contains all
    // information needed to set up a live chart
    private val _dataFlow = MutableSharedFlow<ServiceEvent>(extraBufferCapacity = 100, replay=0)
    val dataFlow: SharedFlow<ServiceEvent> = _dataFlow

    private val inletJobs = mutableMapOf<String, Job>()

    private val localBinder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)    // important for LifecycleService
        return localBinder      // return your binder here
    }

    override fun onUnbind(intent: Intent?): Boolean {
        stopAll()
        return super.onUnbind(intent)
    }

    override fun onCreate() {
        super.onCreate()
    }

    fun startInlet(streamName : String){
        if (inletJobs.containsKey(streamName)) return
        val info = LSL.resolve_stream("name", streamName).firstOrNull() ?: return
        val inlet = LSL.StreamInlet(info)
        val job = lifecycleScope.launch(Dispatchers.IO) {
            Log.i("LSLService","Emitting config for ${info.name()}")
            _dataFlow.emit(ServiceEvent.StreamConfig(info.name(),info.channel_count()))
            val buf = FloatArray(info.channel_count())
            while (isActive) {
                val timestamp = inlet.pull_sample(buf, 1.0)
                if(timestamp>0)
                    _dataFlow.tryEmit(ServiceEvent.DataSample(streamName, timestamp, buf.copyOf()))
            }
        }
        inletJobs[streamName] = job

    }

    fun stopInlet(streamName: String) {
        inletJobs[streamName]?.cancel()
        inletJobs.remove(streamName)
    }

    fun stopAll() {
        inletJobs.values.forEach { job -> job.cancel() }
        inletJobs.clear()
    }

        // Binder to return the Flow:
        inner class LocalBinder : Binder() {
            fun service(): LSLService = this@LSLService
        }
    }