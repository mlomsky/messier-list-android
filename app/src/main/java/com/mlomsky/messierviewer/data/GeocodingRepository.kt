package com.mlomsky.messierviewer.data

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.Locale
import kotlin.coroutines.resume

class GeocodingRepository(
    private val context: Context,
    private val elevationRepository: ElevationRepository = ElevationRepository()
) {

    /** Resolves a free-text address/place name to a location, or null if not found. */
    suspend fun search(query: String): AppLocation? {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = if (Build.VERSION.SDK_INT >= 33) {
            geocodeAsync(geocoder, query)
        } else {
            @Suppress("DEPRECATION")
            geocoder.getFromLocationName(query, 1)?.firstOrNull()
        }
        val location = address?.toAppLocation(query) ?: return null
        val elevation = elevationRepository.lookupMeters(location.latitudeDeg, location.longitudeDeg)
        return if (elevation != null) location.copy(elevationMeters = elevation) else location
    }

    private suspend fun geocodeAsync(geocoder: Geocoder, query: String): Address? =
        suspendCancellableCoroutine { continuation ->
            geocoder.getFromLocationName(query, 1) { results ->
                if (continuation.isActive) continuation.resume(results.firstOrNull())
            }
        }

    private fun Address.toAppLocation(query: String) = AppLocation(
        label = if (!locality.isNullOrBlank()) locality else query,
        latitudeDeg = latitude,
        longitudeDeg = longitude,
        elevationMeters = 0.0,
        isCustom = true
    )
}
