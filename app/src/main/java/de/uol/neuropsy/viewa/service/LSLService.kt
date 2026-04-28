package de.uol.neuropsy.viewa.service

import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import edu.ucsd.sccn.LSL
import edu.ucsd.sccn.LSL.StreamInlet
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import java.lang.Exception

class LSLService : LifecycleService() {

    private val timeoutMs = 500.0    // half-second

    sealed class ServiceEvent {
        data class DataSample(val streamName: String,val timestamp:Double, val sample: FloatArray) : ServiceEvent() {
            override fun equals(other: Any?): Boolean {
                if (this === other) return true
                if (javaClass != other?.javaClass) return false

                other as DataSample

                if (streamName != other.streamName) return false
                if (timestamp != other.timestamp) return false
                if (!sample.contentEquals(other.sample)) return false

                return true
            }

            override fun hashCode(): Int {
                var result = streamName.hashCode()
                result = 31 * result + timestamp.hashCode()
                result = 31 * result + sample.contentHashCode()
                return result
            }
        }

        data class StreamConfig(val streamName:String,val channelCount:Int, val samplingRate : Double) : ServiceEvent()
    }

    // This SharedFlow handles all data flow from the service to the view model.
    // Whenever a new inlet is opened, a StreamConfig is emitted to the flow which contains all
    // information needed to set up a live chart
    private val _dataFlow = MutableSharedFlow<ServiceEvent>(extraBufferCapacity = 100, replay=0)
    val dataFlow: SharedFlow<ServiceEvent> = _dataFlow

    data class StreamSubscription(val job : Job, val inlet: StreamInlet){
        fun stop(){
            job.cancel()
            inlet.close()
        }
    }
    private val inletJobs = mutableMapOf<String, StreamSubscription>()

    private val localBinder = LocalBinder()

    override fun onBind(intent: Intent): IBinder {
        super.onBind(intent)    // important for LifecycleService
        return localBinder      // return your binder here
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return super.onUnbind(intent)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopAll()
        stopSelf()
    }

    fun startInlet(streamName : String){
        // Resolve with a 5-second timeout. Prefer matching by source_id (unique per device)
        // so that two sensors with the same name are handled correctly. Fall back to name only.
        val candidates = LSL.resolve_stream("name", streamName, 1, 5.0)
        val info = candidates.firstOrNull() ?: run {
            Log.w("LSLService", "Could not find stream '$streamName' within timeout")
            return
        }
        val inlet = StreamInlet(info)
        val job = lifecycleScope.launch(Dispatchers.IO) {
            Log.i("LSLService","Emitting config for ${info.name()}")
            _dataFlow.emit(
                ServiceEvent.StreamConfig(
                    info.name(),
                    info.channel_count(),
                    info.nominal_srate()
                )
            )
            val buf = FloatArray(info.channel_count())
            try {
                while (isActive) {
                    val timestamp = inlet.pull_sample(buf, timeoutMs)
                    if (timestamp > 0) {
                        _dataFlow.tryEmit(
                            ServiceEvent.DataSample(
                                streamName,
                                timestamp,
                                buf.copyOf()
                            )
                        )
                    }
                }
            } catch (e: Exception){
                Log.e("LSLService", "Stream $streamName failed", e)
            } finally {
                inlet.close()
            }
        }
        inletJobs[streamName] = StreamSubscription(job,inlet)
    }

    fun stopInlet(streamName: String) {
        inletJobs.remove(streamName)?.stop()
    }

    private fun stopAll() {
        inletJobs.values.forEach { it.stop() }
    }

    // Binder to return the Flow:
    inner class LocalBinder : Binder() {
        fun service(): LSLService = this@LSLService
    }
}