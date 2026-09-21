@file:OptIn(ExperimentalMaterial3Api::class)

package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mlomsky.messierviewer.data.AppLocation
import com.mlomsky.messierviewer.model.CatalogTarget
import com.mlomsky.messierviewer.model.ObjectVisibility
import com.mlomsky.messierviewer.model.SortMode
import com.mlomsky.messierviewer.ui.theme.NightBlack
import com.mlomsky.messierviewer.ui.theme.NightRed
import com.mlomsky.messierviewer.viewmodel.MainUiState
import com.mlomsky.messierviewer.viewmodel.SunMoonTimes
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMM d")

private fun Instant?.formatTime(zone: ZoneId): String =
    this?.atZone(zone)?.format(timeFormatter) ?: "--"

private fun Double.toDms(positiveSuffix: String, negativeSuffix: String): String {
    val hemisphere = if (this >= 0) positiveSuffix else negativeSuffix
    val abs = kotlin.math.abs(this)
    val degrees = abs.toInt()
    val minutesFull = (abs - degrees) * 60
    val minutes = minutesFull.toInt()
    val seconds = (minutesFull - minutes) * 60
    return "%d°%d'%.0f\"%s".format(degrees, minutes, seconds, hemisphere)
}

@Composable
fun MainScreen(
    state: MainUiState,
    currentTime: Instant,
    onSortSelected: (SortMode) -> Unit,
    onNightModeToggle: () -> Unit,
    onSetLocationClick: () -> Unit
) {
    val zone = ZoneId.systemDefault()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Tonight's Sky")
                        Text(
                            currentTime.atZone(zone).format(timeFormatter),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSetLocationClick) {
                        Icon(Icons.Filled.EditLocation, contentDescription = "Set location")
                    }
                    IconButton(onClick = onNightModeToggle) {
                        Icon(Icons.Filled.Brightness2, contentDescription = "Toggle night mode")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            HeaderSection(state.location, currentTime, zone)
            SunMoonSection(state.sunMoonTimes, zone)
            SortButtonsRow(state.sortMode, state.nightMode, onSortSelected)
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ObjectListSection(state.objects, zone)
            }
        }
    }
}

@Composable
private fun HeaderSection(location: AppLocation, currentTime: Instant, zone: ZoneId) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(location.label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        val elevationFeet = location.elevationMeters * 3.28084
        val coordsText = "${location.latitudeDeg.toDms("N", "S")}  ${location.longitudeDeg.toDms("E", "W")}  " +
            "%.0f ft elev".format(elevationFeet)
        Text(coordsText, style = MaterialTheme.typography.bodySmall)
        val zoned = currentTime.atZone(zone)
        Text(zoned.format(dateFormatter) + "   " + zoned.format(timeFormatter), style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun SunMoonSection(times: SunMoonTimes, zone: ZoneId) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SunMoonStat("Sunset", times.sunset.formatTime(zone))
        SunMoonStat("Sunrise", times.sunrise.formatTime(zone))
        SunMoonStat("Moonrise", times.moonrise.formatTime(zone))
        SunMoonStat("Moonset", times.moonset.formatTime(zone))
        SunMoonStat("Illumination", "%.0f%%".format(times.moonIlluminationPercent))
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun SunMoonStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium)
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun SortButtonsRow(sortMode: SortMode, nightMode: Boolean, onSortSelected: (SortMode) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SortButton("Name", sortMode == SortMode.NAME, nightMode) { onSortSelected(SortMode.NAME) }
        SortButton("Max Elevation", sortMode == SortMode.MAX_ELEVATION, nightMode) { onSortSelected(SortMode.MAX_ELEVATION) }
        val startEndLabel = if (sortMode == SortMode.END_TIME) "Set Time" else "Rise Time"
        SortButton(startEndLabel, sortMode == SortMode.START_TIME || sortMode == SortMode.END_TIME, nightMode) {
            onSortSelected(if (sortMode == SortMode.START_TIME) SortMode.END_TIME else SortMode.START_TIME)
        }
        SortButton("Now", sortMode == SortMode.NOW, nightMode) { onSortSelected(SortMode.NOW) }
    }
}

@Composable
private fun RowScope.SortButton(label: String, selected: Boolean, nightMode: Boolean, onClick: () -> Unit) {
    FilterChip(
        modifier = Modifier.weight(1f),
        selected = selected,
        onClick = onClick,
        label = { Text(label, maxLines = 1) },
        colors = if (nightMode) {
            FilterChipDefaults.filterChipColors(
                selectedContainerColor = NightRed,
                selectedLabelColor = NightBlack
            )
        } else {
            FilterChipDefaults.filterChipColors()
        }
    )
}

@Composable
private fun ObjectListSection(objects: List<ObjectVisibility>, zone: ZoneId) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(objects, key = { it.target.id }) { visibility ->
            ObjectRow(visibility, zone)
            HorizontalDivider()
        }
    }
}

@Composable
private fun ObjectRow(visibility: ObjectVisibility, zone: ZoneId) {
    val window = visibility.window
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(visibility.target.displayName, style = MaterialTheme.typography.titleMedium)
            val subtitle = when (val target = visibility.target) {
                is CatalogTarget.Messier -> target.objectType
                is CatalogTarget.PlanetTarget -> "Planet"
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text("Max ${window.maxAltitudeDeg.toInt()}°", style = MaterialTheme.typography.bodyMedium)
            val rangeText = when {
                !window.risesAboveThreshold -> "Not visible"
                window.alwaysAboveThreshold -> "All night"
                else -> "${window.riseTime.formatTime(zone)} - ${window.setTime.formatTime(zone)}"
            }
            Text(rangeText, style = MaterialTheme.typography.bodySmall)
        }
    }
}
