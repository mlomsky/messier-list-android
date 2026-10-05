package com.mlomsky.messierviewer.ui

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.mlomsky.messierviewer.viewmodel.ElevationChartState
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val hourFormatter = DateTimeFormatter.ofPattern("h a")
private const val MIN_ALT_DEG = -20.0
private const val MAX_ALT_DEG = 90.0

@Composable
fun ElevationChartDialog(
    state: ElevationChartState,
    currentTime: Instant,
    nightMode: Boolean,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(state.target.displayName) },
        text = {
            Column {
                Text("Elevation tonight (6pm - 6am)", style = MaterialTheme.typography.bodySmall)
                ElevationChart(
                    samples = state.samples,
                    zone = state.zone,
                    currentTime = currentTime,
                    lineColor = if (nightMode) Color(0xFFFF3B30) else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth().height(220.dp).padding(top = 12.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Composable
private fun ElevationChart(
    samples: List<AltitudeSample>,
    zone: ZoneId,
    currentTime: Instant,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (samples.isEmpty()) return
    val start = samples.first().time
    val end = samples.last().time
    val totalMillis = (end.toEpochMilli() - start.toEpochMilli()).coerceAtLeast(1)
    val labelColor = lineColor.copy(alpha = 0.8f).toArgb()

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

        // Shade the below-horizon band so "not visible" stretches read at a glance.
        val horizonY = yFor(0.0)
        drawRect(
            color = Color.Gray.copy(alpha = 0.15f),
            topLeft = Offset(leftPad, horizonY),
            size = Size(chartWidth, (topPad + chartHeight) - horizonY)
        )

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
            drawLine(gridColor, Offset(x, topPad), Offset(x, topPad + chartHeight), strokeWidth = 1f)
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

        val path = Path()
        samples.forEachIndexed { index, sample ->
            val x = xFor(sample.time)
            val y = yFor(sample.altitudeDeg)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 3f))

        if (!currentTime.isBefore(start) && !currentTime.isAfter(end)) {
            val x = xFor(currentTime)
            drawLine(
                color = lineColor.copy(alpha = 0.5f),
                start = Offset(x, topPad),
                end = Offset(x, topPad + chartHeight),
                strokeWidth = 2f
            )
        }
    }
}
