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
import com.example.watch.services.SpO2Measurer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer

class MainActivity : ComponentActivity() {

    private val permissionsToRequest = arrayOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACTIVITY_RECOGNITION
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.all { it.value }) {
                    startDiagnostics()
                }
            }

        val allPermissionsGranted = listOf(
            Manifest.permission.BODY_SENSORS,
            Manifest.permission.ACTIVITY_RECOGNITION
        ).all { perm ->
            ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED
        }

        if (allPermissionsGranted) {
            startDiagnostics()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BODY_SENSORS,
                    Manifest.permission.ACTIVITY_RECOGNITION
                )
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun startDiagnostics() {
        setContent {
            DiagnosticScreen { currentlyMeasuring ->
                if (currentlyMeasuring) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
            }
        }
    }
}

@Composable
fun DiagnosticScreen(onMeasuringStateChanged: (Boolean) -> Unit) {
    var measuring by remember { mutableStateOf(true) }
    var hr by remember { mutableStateOf("--") }
    var spo2 by remember { mutableStateOf("--") }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(measuring) {
        onMeasuringStateChanged(measuring)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            onMeasuringStateChanged(true)
            measuring = true

            // Medir HR y SpO2 en paralelo
            val hrAverager = HeartRateAverager(context)
            val spo2Measurer = SpO2Measurer(context)

            val hrPromedio = hrAverager.measureAverage()
            val spo2Valor = spo2Measurer.measureSpO2()

            hr = hrPromedio?.let { "$it bpm" } ?: "No disponible"
            spo2 = spo2Valor?.let { "$it%" } ?: "No disponible"

            measuring = false
            onMeasuringStateChanged(false)
        }
    }

    WatchTestTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            if (measuring) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HeartbeatAnimationIcon()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Midiendo signos vitales...",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body2
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "❤️ HR: $hr",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "\uD83E\uDEC0 SpO2: $spo2",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1
                    )
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
    DiagnosticScreen(onMeasuringStateChanged = {})
}
