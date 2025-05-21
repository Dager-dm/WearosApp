
package com.example.maternapp.presentation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.maternapp.presentation.theme.WatchTestTheme
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.example.maternapp.services.HeartRateAverager
import androidx.wear.compose.material.MaterialTheme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Icon
import androidx.compose.material.icons.filled.Favorite


import androidx.compose.ui.graphics.graphicsLayer


import androidx.compose.foundation.layout.size

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.Icons
import androidx.compose.ui.unit.sp
import com.example.maternapp.services.MessageSender
import kotlinx.coroutines.delay


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    startDiagnostics()
                }
            }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BODY_SENSORS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startDiagnostics()
        } else {
            permissionLauncher.launch(Manifest.permission.BODY_SENSORS)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // se limpia el flag si la actividad se destruye mientras la pantalla se mantenía encendida
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startDiagnostics() {
        setContent {
            DiagnosticScreen (
                onMeasuringStateChanged = { currentlyMeasuring ->
                    if (currentlyMeasuring) {
                        //mientras que se está midiendo se mantiene la pantalla encendida
                        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    } else {
                        //cuando se detiene la medición se limpia el flag
                        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                    }
                },
                onMeasurementFinishedAndSent={

                }

            )
    }

}

@Composable
fun DiagnosticScreen(
    onMeasuringStateChanged: (Boolean) -> Unit,
    onMeasurementFinishedAndSent: () -> Unit) {
    var measuring by remember { mutableStateOf(true) }
    var hr by remember { mutableStateOf("--") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current // Obtener la actividad para cerrarla

    LaunchedEffect(measuring) {
        onMeasuringStateChanged(measuring)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            onMeasuringStateChanged(true) // Asegura que se activa al inicio
            measuring = true // se actualiza el estado local también

            val averager = HeartRateAverager(context)
            val promedio = averager.measureAverage()
            hr = promedio?.let { "$it bpm" } ?: "No disponible"
            MessageSender.sendMessageToPhone(applicationContext, hr)
            measuring = false // se actualiza el estado local
            onMeasuringStateChanged(false) // Notifica que la medición terminó

            delay(7000L)

            activity?.finishAndRemoveTask() // Cierra la actividad
        }
    }

    WatchTestTheme {
        Box(modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colors.background), contentAlignment = Alignment.Center) {

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HeartbeatAnimationIcon()
                Spacer(modifier = Modifier.height(12.dp))
                if (measuring) {

                    Text("Midiendo frecuencia cardíaca...", textAlign = TextAlign.Center)

                } else {
                    Text(hr, textAlign = TextAlign.Center, fontSize = 24.sp)
                }
            }

        }
    }
}


    @Composable
    fun HeartbeatAnimationIcon() {
        val infiniteTransition = rememberInfiniteTransition()
        val scale by infiniteTransition.animateFloat(
            initialValue = 0.85f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )

        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Heart Beat",
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer(scaleX = scale, scaleY = scale),
            tint = MaterialTheme.colors.primary
        )
    }


    @Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DiagnosticPreview() {
    DiagnosticScreen(onMeasuringStateChanged = {}, onMeasurementFinishedAndSent = {})
}}
