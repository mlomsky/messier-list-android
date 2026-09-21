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
import com.mlomsky.messierviewer.data.AppLocation
import com.mlomsky.messierviewer.data.DefaultLocation
import com.mlomsky.messierviewer.data.GeocodingRepository
import com.mlomsky.messierviewer.data.LocationRepository
import com.mlomsky.messierviewer.data.MessierCatalog
import com.mlomsky.messierviewer.data.PreferencesRepository
import com.mlomsky.messierviewer.model.CatalogTarget
import com.mlomsky.messierviewer.model.ObjectVisibility
import com.mlomsky.messierviewer.model.SortMode
import com.mlomsky.messierviewer.model.ViewingSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

data class SunMoonTimes(
    val sunrise: Instant?,
    val sunset: Instant?,
    val moonrise: Instant?,
    val moonset: Instant?
)

data class MainUiState(
    val location: AppLocation = DefaultLocation.NEW_YORK_CITY,
    val session: ViewingSession? = null,
    val sunMoonTimes: SunMoonTimes = SunMoonTimes(null, null, null, null),
    val objects: List<ObjectVisibility> = emptyList(),
    val sortMode: SortMode = SortMode.NAME,
    val nightMode: Boolean = false,
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
            else -> mode
        }
        _uiState.value = current.copy(sortMode = next, objects = sortObjects(current.objects, next))
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

    private suspend fun recomputeForCurrentLocation() {
        val location = _uiState.value.location
        val sortMode = _uiState.value.sortMode

        val (session, sunMoonTimes, objects) = withContext(Dispatchers.Default) {
            val zone = ZoneId.systemDefault()
            val session = ViewingSession.forNow(ZonedDateTime.now(zone))
            val observer = Observer(location.latitudeDeg, location.longitudeDeg, location.elevationMeters)

            val sunProvider = EquatorialPositionProvider { jd -> SunPosition.geocentricEquatorial(jd) }
            val sunWindow = AltitudeSampler.sample(
                observer, sunProvider, session.start, session.end, RiseSetThresholds.SUN
            )

            val moonParallax = MoonPosition.geocentric(JulianDate.fromInstant(session.start)).horizontalParallaxDeg
            val moonProvider = EquatorialPositionProvider { jd -> MoonPosition.geocentric(jd).equatorial }
            val moonWindow = AltitudeSampler.sample(
                observer, moonProvider, session.start, session.end, RiseSetThresholds.moon(moonParallax)
            )

            val sunMoonTimes = SunMoonTimes(
                sunrise = sunWindow.riseTime,
                sunset = sunWindow.setTime,
                moonrise = moonWindow.riseTime,
                moonset = moonWindow.setTime
            )

            val objects = mutableListOf<ObjectVisibility>()
            for (entry in MessierCatalog.entries) {
                val target = CatalogTarget.Messier(entry.number, entry.commonName, entry.objectType, entry.raDeg, entry.decDeg)
                val provider = EquatorialPositionProvider { EquatorialCoordinates(entry.raDeg, entry.decDeg) }
                val window = AltitudeSampler.sample(observer, provider, session.start, session.end, RiseSetThresholds.STAR_OR_PLANET)
                objects.add(ObjectVisibility(target, window))
            }
            for (planet in Planet.entries) {
                val target = CatalogTarget.PlanetTarget(planet)
                val provider = EquatorialPositionProvider { jd -> PlanetPositions.geocentricEquatorial(planet, jd) }
                val window = AltitudeSampler.sample(observer, provider, session.start, session.end, RiseSetThresholds.STAR_OR_PLANET)
                objects.add(ObjectVisibility(target, window))
            }

            Triple(session, sunMoonTimes, objects.toList())
        }

        val current = _uiState.value
        _uiState.value = current.copy(
            session = session,
            sunMoonTimes = sunMoonTimes,
            objects = sortObjects(objects, sortMode),
            isLoading = false
        )
    }

    private fun sortObjects(objects: List<ObjectVisibility>, mode: SortMode): List<ObjectVisibility> =
        when (mode) {
            SortMode.NAME -> objects.sortedBy { it.target.displayName }
            SortMode.MAX_ELEVATION -> objects.sortedByDescending { it.window.maxAltitudeDeg }
            SortMode.START_TIME -> objects.sortedBy { it.sortableStartTime }
            SortMode.END_TIME -> objects.sortedBy { it.sortableEndTime }
        }
}
