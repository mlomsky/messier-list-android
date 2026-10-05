package com.mlomsky.messierviewer.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mlomsky.messierviewer.astro.AltitudeSampler
import com.mlomsky.messierviewer.astro.EquatorialCoordinates
import com.mlomsky.messierviewer.astro.EquatorialPositionProvider
import com.mlomsky.messierviewer.astro.JulianDate
import com.mlomsky.messierviewer.astro.MoonPosition
import com.mlomsky.messierviewer.astro.Observer
import com.mlomsky.messierviewer.astro.Planet
import com.mlomsky.messierviewer.astro.PlanetPositions
import com.mlomsky.messierviewer.astro.RiseSetThresholds
import com.mlomsky.messierviewer.astro.SunPosition
import com.mlomsky.messierviewer.astro.toCardinalDirection
import com.mlomsky.messierviewer.astro.toHorizontal
import com.mlomsky.messierviewer.data.AppLocation
import com.mlomsky.messierviewer.data.DEFAULT_ENABLED_CATALOGS
import com.mlomsky.messierviewer.data.DEFAULT_NGC_MAGNITUDE_LIMIT
import com.mlomsky.messierviewer.data.DefaultFilters
import com.mlomsky.messierviewer.data.DefaultLocation
import com.mlomsky.messierviewer.data.GeocodingRepository
import com.mlomsky.messierviewer.data.LocationRepository
import com.mlomsky.messierviewer.data.MessierCatalog
import com.mlomsky.messierviewer.data.NgcCatalog
import com.mlomsky.messierviewer.data.PreferencesRepository
import com.mlomsky.messierviewer.model.CatalogSource
import com.mlomsky.messierviewer.model.CatalogTarget
import com.mlomsky.messierviewer.model.ObjectVisibility
import com.mlomsky.messierviewer.model.SortMode
import com.mlomsky.messierviewer.model.ViewingSession
import com.mlomsky.messierviewer.model.typeLabel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class SunMoonTimes(
    val sunrise: Instant?,
    val sunset: Instant?,
    val moonrise: Instant?,
    val moonset: Instant?,
    val moonIlluminationPercent: Double
)

data class ObjectSearchFilter(
    val nameQuery: String = "",
    val typeQuery: String = "",
    val direction: String? = null,
    val minAltitudeDeg: Double? = null
) {
    val isActive: Boolean
        get() = nameQuery.isNotBlank() || typeQuery.isNotBlank() || direction != null || minAltitudeDeg != null
}

data class MainUiState(
    val location: AppLocation = DefaultLocation.NEW_YORK_CITY,
    val session: ViewingSession? = null,
    val sunMoonTimes: SunMoonTimes = SunMoonTimes(null, null, null, null, 0.0),
    val allObjects: List<ObjectVisibility> = emptyList(),
    val objects: List<ObjectVisibility> = emptyList(),
    val sortMode: SortMode = SortMode.NAME,
    val nightMode: Boolean = false,
    val filters: Map<String, String> = DefaultFilters.byId,
    val enabledCatalogs: Set<CatalogSource> = DEFAULT_ENABLED_CATALOGS,
    val ngcMagnitudeLimit: Double = DEFAULT_NGC_MAGNITUDE_LIMIT,
    val hideBelowHorizon: Boolean = false,
    val searchFilter: ObjectSearchFilter = ObjectSearchFilter(),
    val favorites: Set<String> = emptySet(),
    val isLoading: Boolean = true
)

private const val RECOMPUTE_INTERVAL_MS = 5 * 60_000L
private const val CLOCK_TICK_MS = 15_000L

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val locationRepository = LocationRepository(application)
    private val geocodingRepository = GeocodingRepository(application)
    private val preferencesRepository = PreferencesRepository(application)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _currentTime = MutableStateFlow(Instant.now())
    val currentTime: StateFlow<Instant> = _currentTime.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesRepository.nightModeEnabled.collect { enabled ->
                _uiState.value = _uiState.value.copy(nightMode = enabled)
            }
        }
        viewModelScope.launch {
            preferencesRepository.filterOverrides.collect { overrides ->
                _uiState.value = _uiState.value.copy(filters = DefaultFilters.byId + overrides)
            }
        }
        viewModelScope.launch {
            preferencesRepository.enabledCatalogs.collect { catalogs ->
                _uiState.value = _uiState.value.copy(enabledCatalogs = catalogs)
                recomputeForCurrentLocation()
            }
        }
        viewModelScope.launch {
            preferencesRepository.ngcMagnitudeLimit.collect { limit ->
                _uiState.value = _uiState.value.copy(ngcMagnitudeLimit = limit)
                recomputeForCurrentLocation()
            }
        }
        viewModelScope.launch {
            preferencesRepository.hideBelowHorizon.collect { hide ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    hideBelowHorizon = hide,
                    objects = sortObjects(current.allObjects, current.sortMode, hide, current.searchFilter, current.favorites)
                )
            }
        }
        viewModelScope.launch {
            preferencesRepository.favorites.collect { favorites ->
                val current = _uiState.value
                _uiState.value = current.copy(
                    favorites = favorites,
                    objects = sortObjects(current.allObjects, current.sortMode, current.hideBelowHorizon, current.searchFilter, favorites)
                )
            }
        }
        viewModelScope.launch {
            while (true) {
                _currentTime.value = Instant.now()
                delay(CLOCK_TICK_MS)
            }
        }
        viewModelScope.launch {
            while (true) {
                delay(RECOMPUTE_INTERVAL_MS)
                recomputeForCurrentLocation()
            }
        }
        refreshLocation()
    }

    fun refreshLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val custom = locationRepository.customLocation.first()
            val resolved = custom ?: locationRepository.getGpsLocation() ?: DefaultLocation.NEW_YORK_CITY
            _uiState.value = _uiState.value.copy(location = resolved)
            recomputeForCurrentLocation()
        }
    }

    fun setSortMode(mode: SortMode) {
        val current = _uiState.value
        val next = when {
            current.sortMode == SortMode.START_TIME && mode == SortMode.START_TIME -> SortMode.END_TIME
            current.sortMode == SortMode.END_TIME && mode == SortMode.START_TIME -> SortMode.START_TIME
            current.sortMode == SortMode.FAVORITES && mode == SortMode.FAVORITES -> SortMode.NAME
            else -> mode
        }
        _uiState.value = current.copy(
            sortMode = next,
            objects = sortObjects(current.allObjects, next, current.hideBelowHorizon, current.searchFilter, current.favorites)
        )
    }

    fun toggleNightMode() {
        viewModelScope.launch {
            preferencesRepository.setNightMode(!_uiState.value.nightMode)
        }
    }

    fun setCustomLocation(location: AppLocation) {
        viewModelScope.launch {
            locationRepository.saveCustomLocation(location)
            _uiState.value = _uiState.value.copy(location = location)
            recomputeForCurrentLocation()
        }
    }

    fun useGpsLocation() {
        viewModelScope.launch {
            locationRepository.clearCustomLocation()
            refreshLocation()
        }
    }

    suspend fun searchLocation(query: String): AppLocation? = geocodingRepository.search(query)

    fun setObjectFilter(objectId: String, filterText: String) {
        viewModelScope.launch {
            preferencesRepository.setObjectFilter(objectId, filterText)
        }
    }

    fun setCatalogEnabled(source: CatalogSource, enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setCatalogEnabled(source, enabled)
        }
    }

    fun setNgcMagnitudeLimit(limit: Double) {
        viewModelScope.launch {
            preferencesRepository.setNgcMagnitudeLimit(limit)
        }
    }

    fun setHideBelowHorizon(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setHideBelowHorizon(enabled)
        }
    }

    fun setSearchFilter(filter: ObjectSearchFilter) {
        val current = _uiState.value
        _uiState.value = current.copy(
            searchFilter = filter,
            objects = sortObjects(current.allObjects, current.sortMode, current.hideBelowHorizon, filter, current.favorites)
        )
    }

    fun toggleFavorite(objectId: String) {
        viewModelScope.launch {
            preferencesRepository.toggleFavorite(objectId)
        }
    }

    private suspend fun recomputeForCurrentLocation() {
        val location = _uiState.value.location
        val sortMode = _uiState.value.sortMode
        val enabledCatalogs = _uiState.value.enabledCatalogs
        val ngcMagnitudeLimit = _uiState.value.ngcMagnitudeLimit
        val application = getApplication<Application>()

        val (session, sunMoonTimes, objects) = withContext(Dispatchers.Default) {
            val zone = ZoneId.systemDefault()
            val session = ViewingSession.forNow(ZonedDateTime.now(zone))
            val observer = Observer(location.latitudeDeg, location.longitudeDeg, location.elevationMeters)

            // Sun/moon rise-set are searched over the "observing night" (local noon to local noon,
            // centered on the 6pm-6am session) rather than just the session window itself, so a
            // sunrise/sunset/moonrise/moonset that falls outside 6pm-6am still gets reported instead
            // of showing as missing.
            val sunMoonWindowStart = session.start.minus(Duration.ofHours(6))
            val sunMoonWindowEnd = session.end.plus(Duration.ofHours(6))

            val sunProvider = EquatorialPositionProvider { jd -> SunPosition.geocentricEquatorial(jd) }
            val sunWindow = AltitudeSampler.sample(
                observer, sunProvider, sunMoonWindowStart, sunMoonWindowEnd, RiseSetThresholds.SUN
            )

            val moonParallax = MoonPosition.geocentric(JulianDate.fromInstant(session.start)).horizontalParallaxDeg
            val moonProvider = EquatorialPositionProvider { jd -> MoonPosition.geocentric(jd).equatorial }
            val moonWindow = AltitudeSampler.sample(
                observer, moonProvider, sunMoonWindowStart, sunMoonWindowEnd, RiseSetThresholds.moon(moonParallax)
            )

            val nowJd = JulianDate.fromInstant(Instant.now())

            val sunMoonTimes = SunMoonTimes(
                sunrise = sunWindow.riseTime,
                sunset = sunWindow.setTime,
                moonrise = moonWindow.riseTime,
                moonset = moonWindow.setTime,
                moonIlluminationPercent = MoonPosition.illuminatedFraction(nowJd) * 100.0
            )

            val objects = mutableListOf<ObjectVisibility>()
            if (CatalogSource.MESSIER in enabledCatalogs) {
                for (entry in MessierCatalog.entries) {
                    val target = CatalogTarget.Messier(
                        entry.number, entry.commonName, entry.objectType, entry.raDeg, entry.decDeg, entry.apparentMagnitude
                    )
                    val provider = EquatorialPositionProvider { EquatorialCoordinates(entry.raDeg, entry.decDeg) }
                    val window = AltitudeSampler.sample(observer, provider, session.start, session.end, RiseSetThresholds.STAR_OR_PLANET)
                    val nowHorizontal = provider.at(nowJd).toHorizontal(observer, nowJd)
                    objects.add(ObjectVisibility(target, window, nowHorizontal.altitudeDeg, nowHorizontal.azimuthDeg))
                }
            }
            if (CatalogSource.PLANETS in enabledCatalogs) {
                for (planet in Planet.entries) {
                    val target = CatalogTarget.PlanetTarget(planet)
                    val provider = EquatorialPositionProvider { jd -> PlanetPositions.geocentricEquatorial(planet, jd) }
                    val window = AltitudeSampler.sample(observer, provider, session.start, session.end, RiseSetThresholds.STAR_OR_PLANET)
                    val nowHorizontal = provider.at(nowJd).toHorizontal(observer, nowJd)
                    objects.add(ObjectVisibility(target, window, nowHorizontal.altitudeDeg, nowHorizontal.azimuthDeg))
                }
            }
            if (CatalogSource.NGC in enabledCatalogs) {
                // Filtered by magnitude *before* sampling, since the NGC catalog (~4,169 entries)
                // is large enough that running AltitudeSampler on all of it every recompute would
                // be noticeably slow; a sensible brightness limit keeps this to a few hundred.
                for (entry in NgcCatalog.load(application)) {
                    if (entry.apparentMagnitude != null && entry.apparentMagnitude > ngcMagnitudeLimit) continue
                    val target = CatalogTarget.Ngc(
                        entry.number, entry.commonName, entry.objectType, entry.raDeg, entry.decDeg, entry.apparentMagnitude
                    )
                    val provider = EquatorialPositionProvider { EquatorialCoordinates(entry.raDeg, entry.decDeg) }
                    val window = AltitudeSampler.sample(observer, provider, session.start, session.end, RiseSetThresholds.STAR_OR_PLANET)
                    val nowHorizontal = provider.at(nowJd).toHorizontal(observer, nowJd)
                    objects.add(ObjectVisibility(target, window, nowHorizontal.altitudeDeg, nowHorizontal.azimuthDeg))
                }
            }

            Triple(session, sunMoonTimes, objects.toList())
        }

        val current = _uiState.value
        _uiState.value = current.copy(
            session = session,
            sunMoonTimes = sunMoonTimes,
            allObjects = objects,
            objects = sortObjects(objects, sortMode, current.hideBelowHorizon, current.searchFilter, current.favorites),
            isLoading = false
        )
    }

    private fun sortObjects(
        objects: List<ObjectVisibility>,
        mode: SortMode,
        hideBelowHorizon: Boolean,
        searchFilter: ObjectSearchFilter,
        favorites: Set<String>
    ): List<ObjectVisibility> {
        var base = if (hideBelowHorizon) objects.filter { it.isVisibleNow } else objects
        if (searchFilter.nameQuery.isNotBlank()) {
            base = base.filter { it.target.displayName.contains(searchFilter.nameQuery.trim(), ignoreCase = true) }
        }
        if (searchFilter.typeQuery.isNotBlank()) {
            base = base.filter { it.target.typeLabel.contains(searchFilter.typeQuery.trim(), ignoreCase = true) }
        }
        searchFilter.direction?.let { dir ->
            base = base.filter { it.currentAzimuthDeg.toCardinalDirection() == dir }
        }
        searchFilter.minAltitudeDeg?.let { minAlt ->
            base = base.filter { it.currentAltitudeDeg >= minAlt }
        }
        return when (mode) {
            SortMode.NAME -> base.sortedWith(compareBy(::naturalCompare) { it.target.displayName })
            SortMode.MAX_ELEVATION -> base.sortedByDescending { it.window.maxAltitudeDeg }
            SortMode.START_TIME -> base.sortedBy { it.sortableStartTime }
            SortMode.END_TIME -> base.sortedBy { it.sortableEndTime }
            SortMode.NOW -> base.filter { it.isVisibleNow }.sortedByDescending { it.currentAltitudeDeg }
            SortMode.FAVORITES -> base.filter { it.target.id in favorites }
                .sortedWith(compareBy(::naturalCompare) { it.target.displayName })
        }
    }

    /** Compares strings so embedded digit runs sort numerically (M2 < M10) instead of lexically (M10 < M2). */
    private fun naturalCompare(a: String, b: String): Int {
        var i = 0
        var j = 0
        while (i < a.length && j < b.length) {
            val ca = a[i]
            val cb = b[j]
            if (ca.isDigit() && cb.isDigit()) {
                var iEnd = i
                while (iEnd < a.length && a[iEnd].isDigit()) iEnd++
                var jEnd = j
                while (jEnd < b.length && b[jEnd].isDigit()) jEnd++
                val cmp = a.substring(i, iEnd).toLong().compareTo(b.substring(j, jEnd).toLong())
                if (cmp != 0) return cmp
                i = iEnd
                j = jEnd
            } else {
                val cmp = ca.compareTo(cb)
                if (cmp != 0) return cmp
                i++
                j++
            }
        }
        return (a.length - i) - (b.length - j)
    }
}
