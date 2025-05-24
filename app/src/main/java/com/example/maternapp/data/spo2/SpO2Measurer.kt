package com.example.maternapp.data.spo2

import android.content.Context
import android.os.CountDownTimer
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
    companion object {
        private const val MEASUREMENT_DURATION = 35000L // 35 segundos
        private const val MEASUREMENT_TICK = 250L
    }

    private var connectionManager: ConnectionManager? = null
    private var spo2Listener: SpO2Listener? = null
    private var isMeasuring = false
    private val APP_TAG = "SpO2Measurer"
    private var countDownTimer: CountDownTimer? = null
    private var hasResumed = false

    suspend fun measureSpO2(): Int? = suspendCancellableCoroutine { continuation ->
        try {
            hasResumed = false
            
            // Configurar el observador del tracker
            val trackerObserver = object : TrackerObserver {
                override fun onTrackerDataChanged(status: Int, spo2Value: Int) {
                    if (status == Status.MEASUREMENT_COMPLETED && isMeasuring && !hasResumed) {
                        hasResumed = true
                        countDownTimer?.cancel()
                        continuation.resume(spo2Value)
                        stopMeasurement()
                    }
                }

                override fun onError(errorResourceId: Int) {
                    Log.e(APP_TAG, "Error in SpO2 measurement")
                    if (isMeasuring && !hasResumed) {
                        hasResumed = true
                        countDownTimer?.cancel()
                        continuation.resume(null)
                        stopMeasurement()
                    }
                }
            }

            // Configurar el observador de conexión
            val connectionObserver = object : ConnectionObserver {
                override fun onConnectionResult(stringResourceId: Int) {
                    if (stringResourceId == R.string.ConnectedToHS && !hasResumed) {
                        // Cancelar cualquier timer existente
                        countDownTimer?.cancel()
                        
                        spo2Listener = SpO2Listener()
                        connectionManager?.initSpO2(spo2Listener!!)
                        ObserverUpdater.getObserverUpdater().addTrackerObserver(trackerObserver)
                        spo2Listener?.startTracker()
                        isMeasuring = true
                        
                        // Iniciar el timer cuando comienza la medición
                        countDownTimer = object : CountDownTimer(MEASUREMENT_DURATION, MEASUREMENT_TICK) {
                            override fun onTick(millisUntilFinished: Long) {
                                // No necesitamos hacer nada en cada tick
                            }

                            override fun onFinish() {
                                if (isMeasuring && !hasResumed) {
                                    Log.e(APP_TAG, "Measurement timeout")
                                    hasResumed = true
                                    continuation.resume(null)
                                    stopMeasurement()
                                }
                            }
                        }.start()
                    }
                }
            }

            // Iniciar la conexión
            ObserverUpdater.getObserverUpdater().addConnectionObserver(connectionObserver)
            connectionManager = ConnectionManager()
            connectionManager?.connect(null, context)

        } catch (e: Exception) {
            Log.e(APP_TAG, "Error starting SpO2 measurement", e)
            if (!hasResumed) {
                hasResumed = true
                continuation.resume(null)
            }
        }
    }

    fun stopMeasurement() {
        isMeasuring = false
        countDownTimer?.cancel()
        spo2Listener?.stopTracker()
        connectionManager?.disconnect()
    }
}