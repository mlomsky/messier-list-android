package com.mlomsky.messierviewer.model

import com.mlomsky.messierviewer.astro.RiseSetThresholds
import com.mlomsky.messierviewer.astro.VisibilityWindow
import java.time.Instant

data class ObjectVisibility(
    val target: CatalogTarget,
    val window: VisibilityWindow,
    val currentAltitudeDeg: Double
) {
    val sortableStartTime: Instant
        get() = window.riseTime ?: Instant.MIN

    val sortableEndTime: Instant
        get() = window.setTime ?: Instant.MAX

    val isVisibleNow: Boolean
        get() = currentAltitudeDeg >= RiseSetThresholds.STAR_OR_PLANET
}

enum class SortMode { NAME, MAX_ELEVATION, START_TIME, END_TIME, NOW }
