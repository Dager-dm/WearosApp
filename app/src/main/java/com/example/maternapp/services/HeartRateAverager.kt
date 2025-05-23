package com.example.maternapp.services

import android.content.Context
import android.util.Log
import com.example.maternapp.data.HealthServicesRepository
import com.example.maternapp.data.MeasureMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withContext

class HeartRateAverager(private val context: Context) {
    private val repo = HealthServicesRepository(context)

    suspend fun measureAverage(targetCount: Int = 10): Int? = withContext(Dispatchers.IO) {
        val values = mutableListOf<Float>()

        repo.heartRateMeasureFlow().takeWhile {
            values.size < targetCount
        }.collect { message ->
            when (message) {
                is MeasureMessage.MeasureData -> {
                    val bpm = message.data.firstOrNull()?.value?.toFloat()
                    if (bpm != null && bpm > 0) {
                        values.add(bpm)
                        Log.d("HeartRateAverager", "Dato registrado: $bpm")
                    }
                }

                is MeasureMessage.MeasureAvailability -> {
                    Log.d("HeartRateAverager", message.availability.toString())
                }

            }
        }

        if (values.isNotEmpty()) {
            val avg = values.average().toInt()
            Log.d("HeartRateAverager", "Promedio final: $avg bpm")
            avg
        } else null
    }
}




