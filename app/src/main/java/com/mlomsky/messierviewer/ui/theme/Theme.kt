package com.mlomsky.messierviewer.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val NightRed = Color(0xFFFF3B30)
val NightBlack = Color(0xFF000000)

/** Night-vision mode: black background, all text/accents red. */
private val nightModeColorScheme = darkColorScheme(
    primary = NightRed,
    onPrimary = NightBlack,
    secondary = NightRed,
    onSecondary = NightBlack,
    background = NightBlack,
    onBackground = NightRed,
    surface = NightBlack,
    onSurface = NightRed,
    surfaceVariant = NightBlack,
    onSurfaceVariant = NightRed,
    outline = NightRed,
    error = NightRed,
    onError = NightBlack
)

private val dayColorScheme = lightColorScheme()

@Composable
fun MessierViewerTheme(nightMode: Boolean, content: @Composable () -> Unit) {
    val colorScheme = if (nightMode) nightModeColorScheme else dayColorScheme
    MaterialTheme(colorScheme = colorScheme, content = content)
}
