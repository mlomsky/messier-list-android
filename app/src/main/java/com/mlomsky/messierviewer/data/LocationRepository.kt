package com.mlomsky.messierviewer.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private val Context.locationDataStore by preferencesDataStore(name = "location_prefs")

data class AppLocation(
    val label: String,
    val latitudeDeg: Double,
    val longitudeDeg: Double,
    val elevationMeters: Double,
    val isCustom: Boolean
)

object DefaultLocation {
    val NEW_YORK_CITY = AppLocation(
        label = "New York City",
        latitudeDeg = 40.7128,
        longitudeDeg = -74.0060,
        elevationMeters = 10.0,
        isCustom = false
    )
}

class LocationRepository(private val context: Context) {

    private val keyLabel = stringPreferencesKey("custom_label")
    private val keyLat = doublePreferencesKey("custom_lat")
    private val keyLon = doublePreferencesKey("custom_lon")
    private val keyElevation = doublePreferencesKey("custom_elevation")

    val customLocation: Flow<AppLocation?> = context.locationDataStore.data.map { prefs ->
        val lat = prefs[keyLat]
        val lon = prefs[keyLon]
        val label = prefs[keyLabel]
        if (lat != null && lon != null && label != null) {
            AppLocation(label, lat, lon, prefs[keyElevation] ?: 0.0, isCustom = true)
        } else {
            null
        }
    }

    suspend fun saveCustomLocation(location: AppLocation) {
        context.locationDataStore.edit { prefs ->
            prefs[keyLabel] = location.label
            prefs[keyLat] = location.latitudeDeg
            prefs[keyLon] = location.longitudeDeg
            prefs[keyElevation] = location.elevationMeters
        }
    }

    suspend fun clearCustomLocation() {
        context.locationDataStore.edit { it.clear() }
    }

    fun hasLocationPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED

    /** Returns the device's current GPS/network location, or null if unavailable/denied/timed out. */
    suspend fun getGpsLocation(): AppLocation? {
        if (!hasLocationPermission()) return null
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return null

        val lastKnown = bestLastKnownLocation(locationManager)
        if (lastKnown != null) return lastKnown.toAppLocation()

        return requestSingleUpdate(locationManager)?.toAppLocation()
    }

    private fun bestLastKnownLocation(locationManager: LocationManager): Location? {
        val providers = locationManager.getProviders(true)
        return providers.mapNotNull { provider ->
            try {
                locationManager.getLastKnownLocation(provider)
            } catch (_: SecurityException) {
                null
            }
        }.maxByOrNull { it.time }
    }

    private suspend fun requestSingleUpdate(locationManager: LocationManager): Location? =
        suspendCancellableCoroutine { continuation ->
            val provider = when {
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> null
            }
            if (provider == null) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    locationManager.removeUpdates(this)
                    if (continuation.isActive) continuation.resume(location)
                }
            }
            try {
                @Suppress("DEPRECATION")
                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
            } catch (_: SecurityException) {
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }
            continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
        }

    private fun Location.toAppLocation() = AppLocation(
        label = "Current Location",
        latitudeDeg = latitude,
        longitudeDeg = longitude,
        elevationMeters = if (hasAltitude()) altitude else 0.0,
        isCustom = false
    )
}
