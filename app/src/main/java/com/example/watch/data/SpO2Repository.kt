package com.example.watch.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.samsung.android.service.health.tracking.ConnectionListener
import com.samsung.android.service.health.tracking.HealthTracker
import com.samsung.android.service.health.tracking.HealthTrackerException
import com.samsung.android.service.health.tracking.HealthTrackingService
import com.samsung.android.service.health.tracking.data.DataPoint
import com.samsung.android.service.health.tracking.data.HealthTrackerType
import com.samsung.android.service.health.tracking.data.ValueKey
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.runBlocking

class SpO2Repository(context: Context) {
    private val TAG = "SpO2Repository"
    private var healthTrackingService: HealthTrackingService? = null
    private var spo2Tracker: HealthTracker? = null
    private var isSpO2Available = false
    private val spo2Handler = Handler(Looper.getMainLooper())
    private var isHandlerRunning = false

    init {
        healthTrackingService = HealthTrackingService(object : ConnectionListener {
            override fun onConnectionSuccess() {
                Log.d(TAG, "Conexión exitosa con Samsung Health")
                checkSpO2Availability()
                if (isSpO2Available) {
                    initSpO2Tracker()
                } else {
                    Log.e(TAG, "SpO2 no está disponible en este dispositivo o no se tienen los permisos necesarios")
                }
            }

            override fun onConnectionEnded() {
                Log.d(TAG, "Conexión terminada con Samsung Health")
            }

            override fun onConnectionFailed(e: HealthTrackerException) {
                Log.e(TAG, "Error de conexión con Samsung Health: ${e.message}")
            }
        }, context)
        healthTrackingService?.connectService()
    }

    private fun checkSpO2Availability() {
        try {
            val supportedTypes =
                healthTrackingService!!.getTrackingCapability().getSupportHealthTrackerTypes()
            isSpO2Available = supportedTypes?.contains(HealthTrackerType.SPO2_ON_DEMAND) == true
            Log.d(TAG, "Tipos de sensores soportados: $supportedTypes")
        } catch (e: Exception) {
            Log.e(TAG, "Error al verificar disponibilidad de SpO2", e)
            isSpO2Available = false
        }
    }

    private fun initSpO2Tracker() {
        try {
            if (!isSpO2Available) {
                Log.e(TAG, "No se puede inicializar el tracker de SpO2 porque no está disponible")
                return
            }
            
            spo2Tracker = healthTrackingService?.getHealthTracker(HealthTrackerType.SPO2_ON_DEMAND)
            Log.d(TAG, "SpO2 tracker inicializado correctamente")
        } catch (e: Exception) {
            Log.e(TAG, "Error al inicializar SpO2 tracker", e)
        }
    }

    fun spO2MeasureFlow() = callbackFlow {
        if (!isSpO2Available) {
            trySendBlocking(SpO2Message.Error(HealthTracker.TrackerError.PERMISSION_ERROR))
            close()
            return@callbackFlow
        }

        val eventListener = object : HealthTracker.TrackerEventListener {
            override fun onDataReceived(dataPoints: List<DataPoint>) {
                for (data in dataPoints) {
                    try {
                        val status = data.getValue(ValueKey.SpO2Set.STATUS)
                        Log.d(TAG, "Estado de SpO2 recibido: $status")
                        
                        if (status == 2) { // MEASUREMENT_COMPLETED
                            val spO2Value = data.getValue(ValueKey.SpO2Set.SPO2)
                            Log.d(TAG, "Valor de SpO2 medido: $spO2Value")
                            trySendBlocking(SpO2Message.MeasureData(spO2Value))
                        }
                        trySendBlocking(SpO2Message.MeasureStatus(status))
                    } catch (e: Exception) {
                        Log.e(TAG, "Error al procesar datos de SpO2", e)
                        trySendBlocking(SpO2Message.Error(HealthTracker.TrackerError.SDK_POLICY_ERROR))
                    }
                }
            }

            override fun onFlushCompleted() {
                Log.d(TAG, "Flush completado")
            }

            override fun onError(error: HealthTracker.TrackerError) {
                Log.e(TAG, "Error en SpO2 tracker: $error")
                trySendBlocking(SpO2Message.Error(error))
            }
        }

        try {
            if (!isHandlerRunning) {
                Log.d(TAG, "Configurando event listener para SpO2")
                spo2Handler.post {
                    spo2Tracker?.setEventListener(eventListener)
                    isHandlerRunning = true
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al configurar event listener", e)
            trySendBlocking(SpO2Message.Error(HealthTracker.TrackerError.SDK_POLICY_ERROR))
        }

        awaitClose {
            runBlocking {
                try {
                    spo2Handler.post {
                        spo2Tracker?.unsetEventListener()
                        isHandlerRunning = false
                    }
                    spo2Handler.removeCallbacksAndMessages(null)
                    Log.d(TAG, "Event listener de SpO2 eliminado")
                } catch (e: Exception) {
                    Log.e(TAG, "Error al eliminar event listener", e)
                }
            }
        }
    }

    fun disconnect() {
        try {
            spo2Handler.post {
                spo2Tracker?.unsetEventListener()
                isHandlerRunning = false
            }
            spo2Handler.removeCallbacksAndMessages(null)
            healthTrackingService?.disconnectService()
            Log.d(TAG, "Desconexión completada")
        } catch (e: Exception) {
            Log.e(TAG, "Error durante la desconexión", e)
        }
    }
}

sealed class SpO2Message {
    data class MeasureStatus(val status: Int) : SpO2Message()
    data class MeasureData(val value: Int) : SpO2Message()
    data class Error(val error: HealthTracker.TrackerError) : SpO2Message()
} 