package com.example.watch.services

import android.content.Context
import android.util.Log
import com.example.watch.data.SpO2Repository
import com.example.watch.data.SpO2Message
import com.samsung.android.service.health.tracking.HealthTracker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.atomic.AtomicBoolean

class SpO2Measurer(private val context: Context) {
    private val repo = SpO2Repository(context)
    private val TAG = "SpO2Measurer"
    private val isMeasurementRunning = AtomicBoolean(false)

    suspend fun measureSpO2(timeoutMillis: Long = 35000): Int? = withContext(Dispatchers.IO) {
        if (isMeasurementRunning.get()) {
            Log.w(TAG, "Ya hay una medición en curso")
            return@withContext null
        }

        var finalValue: Int? = null
        var measurementError: HealthTracker.TrackerError? = null
        
        try {
            isMeasurementRunning.set(true)
            Log.d(TAG, "Iniciando medición de SpO2")

            withTimeoutOrNull(timeoutMillis) {
                repo.spO2MeasureFlow().takeWhile { message ->
                    when (message) {
                        is SpO2Message.MeasureData -> {
                            finalValue = message.value
                            Log.d(TAG, "SpO2 medido: ${message.value}%")
                            false // Terminar la recolección cuando tengamos un valor
                        }
                        is SpO2Message.MeasureStatus -> {
                            when (message.status) {
                                -5 -> Log.w(TAG, "Señal débil detectada")
                                -4 -> Log.w(TAG, "Dispositivo en movimiento")
                                0 -> Log.d(TAG, "Calculando medición...")
                                2 -> Log.d(TAG, "Medición completada")
                                else -> Log.d(TAG, "Estado de medición: ${message.status}")
                            }
                            isMeasurementRunning.get() && finalValue == null // Continuar si la medición sigue activa y no tenemos valor
                        }
                        is SpO2Message.Error -> {
                            measurementError = message.error
                            when (message.error) {
                                HealthTracker.TrackerError.SDK_POLICY_ERROR -> 
                                    Log.e(TAG, "Error de política del SDK. Verifica que la app tenga todos los permisos necesarios")
                                HealthTracker.TrackerError.PERMISSION_ERROR -> 
                                    Log.e(TAG, "Error de permisos. La app no tiene los permisos necesarios")
                            }
                            false // Terminar en caso de error
                        }
                    }
                }.collect()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado durante la medición", e)
        } finally {
            isMeasurementRunning.set(false)
            repo.disconnect()
            Log.d(TAG, "Medición finalizada")
        }

        if (measurementError != null) {
            Log.e(TAG, "La medición falló debido a: $measurementError")
        }
        
        finalValue
    }

    fun stopMeasurement() {
        if (isMeasurementRunning.get()) {
            Log.d(TAG, "Deteniendo medición de SpO2")
            isMeasurementRunning.set(false)
            repo.disconnect()
        }
    }
} 