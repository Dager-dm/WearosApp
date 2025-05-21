package com.example.watch.presentation.theme

import androidx.compose.runtime.Composable
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Colors
import androidx.wear.compose.material.Typography

val wearColorPalette = Colors(
primary = Color(0xFF008577),          // Un verde azulado como primario
primaryVariant = Color(0xFF00574B),    // Una variante más oscura del primario
secondary = Color(0xFFD81B60),        // Un rosa como secundario
secondaryVariant = Color(0xFFA00037),  // Una variante más oscura del secundario
background = Color.Black,             // Fondo negro, común en Wear OS
surface = Color(0xFF333333),          // Color de superficie para tarjetas, etc. (gris oscuro)
error = Color(0xFFB00020),            // Color para errores (rojo)
onPrimary = Color.Black,              // Color del texto/iconos sobre el color primario
onSecondary = Color.Black,            // Color del texto/iconos sobre el color secundario
onBackground = Color.White,           // Color del texto/iconos sobre el color de fondo
onSurface = Color.White,              // Color del texto/iconos sobre el color de superficie
onSurfaceVariant = Color.LightGray,   // Color para elementos menos prominentes en superficies
onError = Color.White                 // Color del texto/iconos sobre el color de error
)


val Typography = Typography(
    // -- Estilos de Display --
    // Para texto muy grande, como el tiempo en una watch face principal.
    // display1 = TextStyle(...),
    // display2 = TextStyle(...),
    // display3 = TextStyle(...),

    // -- Estilos de Título --
    // Para títulos de pantalla o secciones importantes.
    title1 = TextStyle(
        fontFamily = FontFamily.Default, // Puedes usar FontFamily.SansSerif, .Serif o fuentes personalizadas
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp // Ajusta según el diseño de Wear OS
    ),
    title2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp
    ),
    // title3 = TextStyle(...),

    // -- Estilos de Cuerpo de Texto --
    // Para el texto principal y listas.
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    body2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),

    // -- Estilos de Botón y Etiqueta --
    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        letterSpacing = 0.5.sp // Un poco de espaciado para botones
    ),
    caption1 = TextStyle( // Para texto más pequeño, como leyendas o metadatos
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    caption2 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Light, // Más ligero
        fontSize = 12.sp
    )
    // caption3 = TextStyle(...) // Si necesitas aún más pequeño

    // Puedes definir más estilos o sobrescribir los que necesites.
    // Los estilos no definidos aquí usarán los valores predeterminados de la Typography de Wear.
)





@Composable
fun WatchTestTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = wearColorPalette, // Ensure wearColorPalette is defined
        typography = Typography,    // Ensure Typography is defined
        // For shapes, use MaterialTheme.shapes if providing custom shapes is not supported
        content = content
    )
}