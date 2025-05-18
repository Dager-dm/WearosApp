
package com.example.watch.presentation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
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
import com.example.watch.presentation.theme.WatchTestTheme
import androidx.wear.compose.material.*
import androidx.wear.tooling.preview.devices.WearDevices
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.launch
import com.example.watch.services.HeartRateAverager
import androidx.wear.compose.material.MaterialTheme


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
            DiagnosticScreen { currentlyMeasuring ->
                if (currentlyMeasuring) {
                    //mientras que se está midiendo se mantiene la pantalla encendida
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    //cuando se detiene la medición se limpia el flag
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
    }

}

@Composable
fun DiagnosticScreen(onMeasuringStateChanged: (Boolean) -> Unit) {
    var measuring by remember { mutableStateOf(true) }
    var hr by remember { mutableStateOf("--") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

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

            measuring = false // se actualiza el estado local
            onMeasuringStateChanged(false) // Notifica que la medición terminó
        }
    }

    WatchTestTheme {
        Box(modifier = Modifier.fillMaxSize()
            .background(MaterialTheme.colors.background), contentAlignment = Alignment.Center) {
            if (measuring) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Midiendo frecuencia cardíaca...", textAlign = TextAlign.Center)
                }
            } else {
                Text("❤️ HR Promedio: $hr", textAlign = TextAlign.Center)
            }
        }
    }
}


@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DiagnosticPreview() {
    DiagnosticScreen(onMeasuringStateChanged = {})
}}
