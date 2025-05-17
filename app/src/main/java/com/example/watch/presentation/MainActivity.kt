/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.example.watch.presentation


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
import com.example.watch.data.HealthServicesRepository
import com.example.watch.data.MeasureMessage
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

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

    private fun startDiagnostics() {
        setContent {
            DiagnosticScreen()
        }
    }
}

@Composable
fun DiagnosticScreen() {
    var hr by remember { mutableStateOf("--") }
    var availability by remember { mutableStateOf("--") }
    val context = LocalContext.current
    val repo = remember { HealthServicesRepository(context) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            repo.heartRateMeasureFlow().collect { message ->
                when (message) {
                    is MeasureMessage.MeasureAvailability -> {
                        availability = message.availability.toString()
                    }
                    is MeasureMessage.MeasureData -> {
                        val bpm = message.data.firstOrNull()?.value?.toInt()
                        if (bpm != null) hr = "$bpm bpm"
                    }
                }
            }
        }
    }

    WatchTestTheme {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "❤️ HR: $hr", textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "📶 Disponibilidad: $availability", textAlign = TextAlign.Center)
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DiagnosticPreview() {
    DiagnosticScreen()
}

