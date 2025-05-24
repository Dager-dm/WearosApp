package com.example.maternapp.presentation


import android.Manifest
import androidx.wear.compose.material.dialog.Alert
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.graphicsLayer
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.wear.compose.material.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.BubbleChart
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.example.maternapp.services.MessageSender
import com.example.maternapp.data.spo2.SpO2Measurer
import kotlinx.coroutines.delay
import kotlinx.coroutines.channels.Channel
import com.example.maternapp.R
import androidx.compose.animation.core.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import kotlin.text.toFloat

class MainActivity : ComponentActivity() {
    companion object {
        private const val APP_TAG = "Maternapp"
    }

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
    var showRetryDialog by remember { mutableStateOf(false) }
    var lastSpO2Value by remember { mutableStateOf<Int?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val activity = LocalActivity.current
    val retryChannel = remember { Channel<Boolean>() }

    LaunchedEffect(measuring) {
        onMeasuringStateChanged(measuring)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            onMeasuringStateChanged(true)
            measuring = true

            // Medir HR y SpO2 en paralelo
            val hrAverager = HeartRateAverager(context)
            val hrPromedio = hrAverager.measureAverage()
            
            // Iniciar medición de SpO2
            val spo2Measurer = SpO2Measurer(context)
            var spo2Value: Int? = null
            
            while (spo2Value == null && measuring) {
                try {
                    spo2Value = spo2Measurer.measureSpO2()
                } catch (e: Exception) {
                    Log.e("MainActivity", "Error en medición de SpO2", e)
                    showRetryDialog = true
                    val shouldRetry = retryChannel.receive()
                    if (!shouldRetry) {
                        measuring = false
                        break
                    }
                }
            }

            hr = hrPromedio?.let { "$it bpm" } ?: "No disponible"
            spo2 = spo2Value?.let { "$it%" } ?: "No disponible"
            val resultado = "$hr ; $spo2"
            MessageSender.sendMessageToPhone(context, resultado)
            
            measuring = false
            onMeasuringStateChanged(false)

            delay(7000L)
            activity?.finishAndRemoveTask()
        }
    }

    WatchTestTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                HeartbeatAnimationIcon()
                Spacer(modifier = Modifier.height(12.dp))
                if (measuring) {
                    AnimatedMeasuringText()
                } else {
                    Row{
                        HeartIcon()
                        Text("Hr: $hr", textAlign = TextAlign.Center, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Row{
                        SpO2Icon()
                        Text("SpO2: $spo2", textAlign = TextAlign.Center, fontSize = 20.sp)
                    }
                }
            }
        }

        if (showRetryDialog) {
            Alert(
                icon = {
                    SpO2Icon()
                },
                title = { Text("¿Desea reintentar la medición de SpO2?", textAlign = TextAlign.Center) },
                negativeButton = {
                    Button(
                        colors = ButtonDefaults.secondaryButtonColors(),
                        onClick = {
                            showRetryDialog = false
                            scope.launch {
                                retryChannel.send(false)
                            }
                        }
                    ) {
                        Text("No")
                    }
                },
                positiveButton = {
                    Button(
                        onClick = {
                            showRetryDialog = false
                            scope.launch {
                                retryChannel.send(true)
                            }
                        }
                    ) {
                        Text("Sí")
                    }
                },
                contentPadding = PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
            )
        }
    }
}

@Composable
fun AnimatedMeasuringText(modifier: Modifier = Modifier) {
    val text = "Midiendo signos vitales..."
    val baseColor = Color.Gray // Color base del texto
    val highlightColor = Color.White // Color que se mueve
    val animationDurationMillis = 2000 // Duración de una pasada de la animación

    // Estado para controlar la animación de traslación del gradiente
    val infiniteTransition = rememberInfiniteTransition(label = "text_highlight_transition")
    val translationX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f, // Representa el 100% del ancho del texto + ancho del gradiente
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = animationDurationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "text_highlight_translation"
    )

    // Necesitamos el ancho del texto para calcular el desplazamiento del gradiente.
    // Usaremos onTextLayout para obtenerlo.
    var textWidthPx by remember { mutableStateOf(0f) }
    val density = LocalDensity.current

    val brush = remember(translationX, textWidthPx) {
        if (textWidthPx == 0f) {
            // Aún no se conoce el ancho del texto, usa un color sólido temporalmente
            Brush.linearGradient(listOf(baseColor, baseColor))
        } else {
            // Ancho del gradiente (puedes ajustarlo)
            val gradientWidth = textWidthPx / 3 // Por ejemplo, 1/3 del ancho del texto

            // Calcula el desplazamiento real en píxeles
            // El rango de translationX es 0f a 1f.
            // Queremos que el gradiente se mueva desde -gradientWidth hasta textWidthPx.
            val actualTranslation = -gradientWidth + (textWidthPx + gradientWidth) * translationX

            Brush.linearGradient(
                colors = listOf(baseColor, highlightColor, baseColor),
                // Ajusta los 'stops' si quieres que el blanco sea más o menos predominante
                // stops = listOf(0.4f, 0.5f, 0.6f), // Ejemplo de stops
                start = Offset(x = actualTranslation - gradientWidth / 2, y = 0f),
                end = Offset(x = actualTranslation + gradientWidth / 2, y = 0f),
                tileMode = TileMode.Clamp // O TileMode.Mirror / TileMode.Repeated si prefieres otro efecto en los bordes
            )
        }
    }

    Text(
        text = text,
        style = TextStyle(
            brush = brush, // Aplicamos el ShaderBrush aquí
            fontSize = 14.sp, // Ajusta el tamaño según necesites
            fontWeight = FontWeight.Bold,
            // textAlign = TextAlign.Center // Si lo necesitas centrado
        ),
        onTextLayout = { textLayoutResult ->
            // Actualiza el ancho del texto cuando se conoce
            textWidthPx = textLayoutResult.size.width.toFloat()
        },
        modifier = modifier
    )
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
        tint = Color(red = 239, green = 74, blue = 138)
    )
}

@Composable
fun SpO2Icon() {
    Icon(
        imageVector = Icons.Filled.BubbleChart,
        contentDescription = "Nivel de Oxígeno en Sangre",
        tint = Color(red =117, green =207, blue = 255)
    )
}

@Composable
fun HeartIcon() {
    Icon(
        imageVector = Icons.Default.Favorite,
        contentDescription = "Hr",
        tint = Color(red = 255, green = 0, blue = 0, alpha = 215)
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DiagnosticPreview() {
    DiagnosticScreen(onMeasuringStateChanged = {})
}
