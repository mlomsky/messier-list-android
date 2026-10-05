@file:OptIn(ExperimentalMaterial3Api::class)

package com.mlomsky.messierviewer.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Brightness2
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mlomsky.messierviewer.BuildConfig
import com.mlomsky.messierviewer.astro.toCardinalDirection
import com.mlomsky.messierviewer.data.AppLocation
import com.mlomsky.messierviewer.model.CatalogSource
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
    onSetLocationClick: () -> Unit,
    onAboutClick: () -> Unit,
    onHelpClick: () -> Unit,
    onFilterClick: (CatalogTarget) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onCatalogToggle: (CatalogSource, Boolean) -> Unit,
    onDisplayFiltersClick: () -> Unit,
    onSearchClick: () -> Unit,
    onObjectClick: (CatalogTarget) -> Unit,
    onSunMoonClick: () -> Unit
) {
    val zone = ZoneId.systemDefault()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Messier Tonight  V${BuildConfig.VERSION_NAME}")
                },
                actions = {
                    IconButton(onClick = onHelpClick) {
                        Icon(Icons.Filled.Help, contentDescription = "Help")
                    }
                    IconButton(onClick = onSetLocationClick) {
                        Icon(Icons.Filled.EditLocation, contentDescription = "Set location")
                    }
                    IconButton(onClick = onNightModeToggle) {
                        Icon(Icons.Filled.Brightness2, contentDescription = "Toggle night mode")
                    }
                    IconButton(onClick = onAboutClick) {
                        Icon(Icons.Filled.Info, contentDescription = "About")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CatalogToggleRow(
                state.enabledCatalogs,
                state.nightMode,
                state.searchFilter.isActive,
                onCatalogToggle,
                onDisplayFiltersClick,
                onSearchClick
            )
            HeaderSection(state.location, currentTime, zone)
            SunMoonSection(state.sunMoonTimes, zone, onSunMoonClick)
            SortButtonsRow(state.sortMode, state.nightMode, onSortSelected)
            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ObjectListSection(
                    state.objects,
                    zone,
                    state.filters,
                    state.favorites,
                    onFilterClick,
                    onFavoriteClick,
                    onObjectClick,
                    state.nightMode
                )
            }
        }
    }
}

@Composable
private fun HeaderSection(location: AppLocation, currentTime: Instant, zone: ZoneId) {
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
        Text(location.label, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        val elevationFeet = location.elevationMeters * 3.28084
        val coordsText = "${location.latitudeDeg.toDms("N", "S")}  ${location.longitudeDeg.toDms("E", "W")}  " +
            "%.0f ft elev".format(elevationFeet)
        Text(coordsText, style = MaterialTheme.typography.titleMedium)
        val zoned = currentTime.atZone(zone)
        Text(zoned.format(dateFormatter) + "   " + zoned.format(timeFormatter), style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun SunMoonSection(times: SunMoonTimes, zone: ZoneId, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SunMoonStat("Sunset", times.sunset.formatTime(zone), onClick)
        SunMoonStat("Sunrise", times.sunrise.formatTime(zone), onClick)
        SunMoonStat("Moonrise", times.moonrise.formatTime(zone), onClick)
        SunMoonStat("Moonset", times.moonset.formatTime(zone), onClick)
        SunMoonStat("Illumination", "%.0f%%".format(times.moonIlluminationPercent), onClick)
    }
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}

@Composable
private fun SunMoonStat(label: String, value: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
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
        FavoritesSortButton(sortMode == SortMode.FAVORITES, nightMode) { onSortSelected(SortMode.FAVORITES) }
    }
}

@Composable
private fun RowScope.FavoritesSortButton(selected: Boolean, nightMode: Boolean, onClick: () -> Unit) {
    FilterChip(
        modifier = Modifier.weight(1f),
        selected = selected,
        onClick = onClick,
        label = {
            Icon(
                imageVector = if (selected) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "Favorites",
                modifier = Modifier.size(18.dp)
            )
        },
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
private fun CatalogToggleRow(
    enabledCatalogs: Set<CatalogSource>,
    nightMode: Boolean,
    searchActive: Boolean,
    onCatalogToggle: (CatalogSource, Boolean) -> Unit,
    onDisplayFiltersClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SortButton("Planets", CatalogSource.PLANETS in enabledCatalogs, nightMode) {
            onCatalogToggle(CatalogSource.PLANETS, CatalogSource.PLANETS !in enabledCatalogs)
        }
        SortButton("Messier", CatalogSource.MESSIER in enabledCatalogs, nightMode) {
            onCatalogToggle(CatalogSource.MESSIER, CatalogSource.MESSIER !in enabledCatalogs)
        }
        SortButton("NGC", CatalogSource.NGC in enabledCatalogs, nightMode) {
            onCatalogToggle(CatalogSource.NGC, CatalogSource.NGC !in enabledCatalogs)
        }
        IconButton(onClick = onSearchClick) {
            Icon(
                Icons.Filled.Search,
                contentDescription = "Search objects",
                tint = if (searchActive) MaterialTheme.colorScheme.primary else LocalContentColor.current
            )
        }
        IconButton(onClick = onDisplayFiltersClick) {
            Icon(Icons.Filled.Tune, contentDescription = "Display filters")
        }
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
private fun ObjectListSection(
    objects: List<ObjectVisibility>,
    zone: ZoneId,
    filters: Map<String, String>,
    favorites: Set<String>,
    onFilterClick: (CatalogTarget) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onObjectClick: (CatalogTarget) -> Unit,
    nightMode: Boolean
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(objects, key = { it.target.id }) { visibility ->
            ObjectRow(
                visibility,
                zone,
                filters[visibility.target.id],
                visibility.target.id in favorites,
                onFilterClick,
                onFavoriteClick,
                onObjectClick,
                nightMode
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun ObjectRow(
    visibility: ObjectVisibility,
    zone: ZoneId,
    filterText: String?,
    isFavorite: Boolean,
    onFilterClick: (CatalogTarget) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onObjectClick: (CatalogTarget) -> Unit,
    nightMode: Boolean
) {
    val window = visibility.window
    val target = visibility.target
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FavoriteButton(isFavorite = isFavorite, nightMode = nightMode, onClick = { onFavoriteClick(target.id) })
        Spacer(modifier = Modifier.width(4.dp))
        FilterButton(hasFilter = !filterText.isNullOrBlank(), nightMode = nightMode, onClick = { onFilterClick(target) })
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.Bottom) {
                Text(
                    target.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f, fill = false).clickable { onObjectClick(target) }
                )
                val belowHorizon = visibility.currentAltitudeDeg <= 0.0
                val altAzColor = when {
                    belowHorizon && nightMode -> Color.Gray
                    belowHorizon -> Color.Red
                    else -> Color.Unspecified
                }
                Text(
                    "%.0f°/%.0f° %s".format(
                        visibility.currentAltitudeDeg,
                        visibility.currentAzimuthDeg,
                        visibility.currentAzimuthDeg.toCardinalDirection()
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = altAzColor
                )
            }
            val subtitle = when (target) {
                is CatalogTarget.Messier -> "${target.objectType}  •  Mag %.1f".format(target.apparentMagnitude)
                is CatalogTarget.PlanetTarget -> "Planet  •  Mag %.1f".format(target.planet.meanApparentMagnitude)
                is CatalogTarget.Ngc -> "${target.objectType}  •  Mag " +
                    (target.apparentMagnitude?.let { "%.1f".format(it) } ?: "--")
            }
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
            if (!filterText.isNullOrBlank()) {
                Text("Filter: $filterText", style = MaterialTheme.typography.bodySmall)
            }
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

@Composable
private fun FilterButton(hasFilter: Boolean, nightMode: Boolean, onClick: () -> Unit) {
    val backgroundColor = when {
        nightMode && hasFilter -> NightRed
        nightMode -> NightBlack
        hasFilter -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = when {
        nightMode && hasFilter -> NightBlack
        nightMode -> NightRed
        else -> Color.Unspecified
    }
    val border = if (nightMode && !hasFilter) BorderStroke(1.dp, NightRed) else null
    Surface(
        modifier = Modifier.size(28.dp).clickable(onClick = onClick),
        shape = CircleShape,
        color = backgroundColor,
        border = border
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text("F", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}

@Composable
private fun FavoriteButton(isFavorite: Boolean, nightMode: Boolean, onClick: () -> Unit) {
    val tint = if (nightMode) NightRed else MaterialTheme.colorScheme.primary
    Box(
        modifier = Modifier.size(28.dp).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}
