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
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive

class LSLService : LifecycleService() {

    sealed class ServiceEvent {
        data class DataSample(val streamName: String,val timestamp:Double, val sample: FloatArray) : ServiceEvent()
        data class StreamConfig(val streamName:String,val channelCount:Int) : ServiceEvent()
    }

    private val _dataFlow = MutableSharedFlow<ServiceEvent>(extraBufferCapacity = 100, replay=0)
    val dataFlow: SharedFlow<ServiceEvent> = _dataFlow

    private val _configFlow = MutableStateFlow<Map<String,Int>>(emptyMap())
    val configFlow: StateFlow<Map<String,Int>> = _configFlow

    private val inletJobs = mutableMapOf<String, Job>()

    private val localBinder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)    // important for LifecycleService
        return localBinder      // return your binder here
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
            _configFlow.value=_configFlow.value+(streamName to info.channel_count())
            val buf = FloatArray(info.channel_count())
            while (isActive) {
                val timestamp = inlet.pull_sample(buf, LSL.FOREVER)
                _dataFlow.emit(ServiceEvent.DataSample(streamName, timestamp, buf.copyOf()))
            }
        }
        inletJobs[streamName] = job
    }

    fun stopInlet(streamName: String) {
        _configFlow.value =
            _configFlow.value - streamName
        inletJobs.remove(streamName)?.also { job->job.cancel() }
    }

    fun stopAll(){
        //inletJobs.forEach {stopInlet(it.key)
        //    Log.e("LSLService",it.key)}
    }

    // Binder to return the Flow:
    inner class LocalBinder : Binder() {
        fun service(): LSLService = this@LSLService
    }
}