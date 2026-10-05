package com.mlomsky.messierviewer.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mlomsky.messierviewer.astro.AltitudeSample
import com.mlomsky.messierviewer.astro.RiseSetThresholds
import com.mlomsky.messierviewer.viewmodel.SunMoonChartState
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val hourFormatter = DateTimeFormatter.ofPattern("h a")
private const val MIN_ALT_DEG = -24.0
private const val MAX_ALT_DEG = 90.0
private const val NAUTICAL_TWILIGHT_DEG = -12.0
private const val ASTRONOMICAL_TWILIGHT_DEG = -18.0

@Composable
fun SunMoonChartDialog(
    state: SunMoonChartState,
    currentTime: Instant,
    nightMode: Boolean,
    onDismiss: () -> Unit
) {
    val sunColor = if (nightMode) Color(0xFFFF3B30) else Color(0xFFFB8C00)
    val moonColor = if (nightMode) Color(0xFF8B0000) else Color(0xFF78909C)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sun & Moon Tonight") },
        text = {
            Column {
                Text("Altitude by hour (6pm - 6am)", style = MaterialTheme.typography.bodySmall)
                SunMoonChart(
                    sunSamples = state.sunSamples,
                    moonSamples = state.moonSamples,
                    zone = state.zone,
                    currentTime = currentTime,
                    sunColor = sunColor,
                    moonColor = moonColor,
                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = 12.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                LegendRow(sunColor, moonColor)
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun LegendRow(sunColor: Color, moonColor: Color) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendSwatch(sunColor)
            Text(" Sun   ", style = MaterialTheme.typography.labelSmall)
            LegendSwatch(moonColor)
            Text(" Moon", style = MaterialTheme.typography.labelSmall)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LegendSwatch(Color.Gray.copy(alpha = 0.25f))
            Text(" Twilight   ", style = MaterialTheme.typography.labelSmall)
            LegendSwatch(Color.Gray.copy(alpha = 0.45f))
            Text(" Astro. twilight   ", style = MaterialTheme.typography.labelSmall)
            LegendSwatch(Color.Gray.copy(alpha = 0.65f))
            Text(" Night", style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun LegendSwatch(color: Color) {
    Canvas(modifier = Modifier.size(10.dp).padding(end = 2.dp)) {
        drawRect(color = color)
    }
}

/**
 * Shades the sky-darkness band behind the Sun/Moon lines using the Sun's own altitude:
 * daylight (>= -0.83deg) is left clear, civil+nautical twilight (down to -12deg) gets a light
 * overlay, astronomical twilight (-12 to -18deg) a medium one, and full night (below -18deg) the
 * darkest - all as alpha on a single neutral color so it reads correctly in both themes.
 */
private fun bandAlphaFor(sunAltitudeDeg: Double): Float = when {
    sunAltitudeDeg >= RiseSetThresholds.SUN -> 0f
    sunAltitudeDeg >= NAUTICAL_TWILIGHT_DEG -> 0.25f
    sunAltitudeDeg >= ASTRONOMICAL_TWILIGHT_DEG -> 0.45f
    else -> 0.65f
}

@Composable
private fun SunMoonChart(
    sunSamples: List<AltitudeSample>,
    moonSamples: List<AltitudeSample>,
    zone: ZoneId,
    currentTime: Instant,
    sunColor: Color,
    moonColor: Color,
    modifier: Modifier = Modifier
) {
    if (sunSamples.isEmpty()) return
    val start = sunSamples.first().time
    val end = sunSamples.last().time
    val totalMillis = (end.toEpochMilli() - start.toEpochMilli()).coerceAtLeast(1)
    val labelColor = Color.Gray.toArgb()

    Canvas(modifier = modifier) {
        val leftPad = 34.dp.toPx()
        val bottomPad = 20.dp.toPx()
        val topPad = 6.dp.toPx()
        val chartWidth = size.width - leftPad
        val chartHeight = size.height - bottomPad - topPad

        fun xFor(time: Instant): Float {
            val frac = (time.toEpochMilli() - start.toEpochMilli()).toFloat() / totalMillis
            return leftPad + frac * chartWidth
        }
        fun yFor(altDeg: Double): Float {
            val frac = ((altDeg - MIN_ALT_DEG) / (MAX_ALT_DEG - MIN_ALT_DEG)).toFloat().coerceIn(0f, 1f)
            return topPad + (1f - frac) * chartHeight
        }

        for (i in 0 until sunSamples.size - 1) {
            val a = sunSamples[i]
            val b = sunSamples[i + 1]
            val midAlt = (a.altitudeDeg + b.altitudeDeg) / 2.0
            val alpha = bandAlphaFor(midAlt)
            if (alpha > 0f) {
                drawRect(
                    color = Color.Black.copy(alpha = alpha),
                    topLeft = Offset(xFor(a.time), topPad),
                    size = Size(xFor(b.time) - xFor(a.time), chartHeight)
                )
            }
        }

        val gridColor = Color.Gray.copy(alpha = 0.4f)
        val textSizePx = 10.sp.toPx()
        listOf(0.0, 30.0, 60.0, 90.0).forEach { alt ->
            val y = yFor(alt)
            drawLine(gridColor, Offset(leftPad, y), Offset(leftPad + chartWidth, y), strokeWidth = 1f)
            drawContext.canvas.nativeCanvas.drawText(
                "${alt.toInt()}°",
                2f,
                y + textSizePx / 3,
                Paint().apply {
                    color = labelColor
                    textSize = textSizePx
                }
            )
        }

        var tickTime = start
        while (!tickTime.isAfter(end)) {
            val x = xFor(tickTime)
            drawContext.canvas.nativeCanvas.drawText(
                tickTime.atZone(zone).format(hourFormatter),
                x - 14f,
                size.height - 4f,
                Paint().apply {
                    color = labelColor
                    textSize = textSizePx
                }
            )
            tickTime = tickTime.plus(Duration.ofHours(2))
        }

        fun pathFor(samples: List<AltitudeSample>): Path {
            val path = Path()
            samples.forEachIndexed { index, sample ->
                val x = xFor(sample.time)
                val y = yFor(sample.altitudeDeg)
                if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            return path
        }

        drawPath(pathFor(moonSamples), color = moonColor, style = Stroke(width = 3f))
        drawPath(pathFor(sunSamples), color = sunColor, style = Stroke(width = 3f))

        if (!currentTime.isBefore(start) && !currentTime.isAfter(end)) {
            val x = xFor(currentTime)
            drawLine(
                color = Color.Gray.copy(alpha = 0.7f),
                start = Offset(x, topPad),
                end = Offset(x, topPad + chartHeight),
                strokeWidth = 2f
            )
        }
    }
}
