package com.example.maternapp.data.spo2

import android.content.Context
import android.util.Log
import com.example.maternapp.data.spo2.ConnectionManager
import com.example.maternapp.data.spo2.SpO2Listener
import com.example.maternapp.data.spo2.Status
import com.example.maternapp.data.spo2.TrackerObserver
import com.example.maternapp.data.spo2.ConnectionObserver
import com.example.maternapp.data.spo2.ObserverUpdater
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.example.maternapp.R

class SpO2Measurer(private val context: Context) {
    private var connectionManager: ConnectionManager? = null
    private var spo2Listener: SpO2Listener? = null
    private var isMeasuring = false
    private val APP_TAG = "SpO2Measurer"

    suspend fun measureSpO2(): Int? = suspendCancellableCoroutine { continuation ->
        try {
            // Configurar el observador del tracker
            val trackerObserver = object : TrackerObserver {
                override fun onTrackerDataChanged(status: Int, spo2Value: Int) {
                    if (status == Status.MEASUREMENT_COMPLETED && isMeasuring) {
                        continuation.resume(spo2Value)
                        stopMeasurement()
                        Log.d(APP_TAG, "SpO2 measurement completed")
                    }
                }

                override fun onError(errorResourceId: Int) {
                    Log.e(APP_TAG, "Error in SpO2 measurement")
                    if (isMeasuring) {
                        continuation.resume(null)
                        stopMeasurement()
                    }
                }
            }

            // Configurar el observador de conexión
            val connectionObserver = object : ConnectionObserver {
                override fun onConnectionResult(stringResourceId: Int) {
                    if (stringResourceId == R.string.ConnectedToHS) {
                        spo2Listener = SpO2Listener()
                        connectionManager?.initSpO2(spo2Listener!!)
                        ObserverUpdater.getObserverUpdater().addTrackerObserver(trackerObserver)
                        spo2Listener?.startTracker()
                        isMeasuring = true
                    }
                }
            }

            // Iniciar la conexión
            ObserverUpdater.getObserverUpdater().addConnectionObserver(connectionObserver)
            connectionManager = ConnectionManager()
            connectionManager?.connect(null, context)

        } catch (e: Exception) {
            Log.e(APP_TAG, "Error starting SpO2 measurement", e)
            continuation.resume(null)
        }
    }

    fun stopMeasurement() {
        isMeasuring = false
        spo2Listener?.stopTracker()
        connectionManager?.disconnect()
    }
} 