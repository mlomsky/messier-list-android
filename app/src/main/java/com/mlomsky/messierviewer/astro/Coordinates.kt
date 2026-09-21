package com.mlomsky.messierviewer.astro

import kotlin.math.abs

data class Observer(
    val latitudeDeg: Double,
    val longitudeDeg: Double, // east-positive
    val elevationMeters: Double = 0.0
)

/** Right ascension and declination, in degrees (equinox of date is close enough to J2000 for this app's purposes). */
data class EquatorialCoordinates(val rightAscensionDeg: Double, val declinationDeg: Double)

/** Azimuth measured from North, through East (0=N, 90=E, 180=S, 270=W). */
data class HorizontalCoordinates(val altitudeDeg: Double, val azimuthDeg: Double)

/** Supplies an object's equatorial position at a given Julian Date (constant for fixed deep-sky objects). */
fun interface EquatorialPositionProvider {
    fun at(jd: Double): EquatorialCoordinates
}

/**
 * Converts equatorial coordinates to horizontal (altitude/azimuth) coordinates for an observer,
 * at the given Julian Date.
 *
 * Algorithm: Duffett-Smith & Zwart, "Practical Astronomy with your Calculator or Spreadsheet".
 */
fun EquatorialCoordinates.toHorizontal(observer: Observer, jd: Double): HorizontalCoordinates {
    val lst = SiderealTime.localMeanSiderealTimeDegrees(jd, observer.longitudeDeg)
    val hourAngle = normalizeDegrees(lst - rightAscensionDeg)

    val sinAlt = sinDeg(declinationDeg) * sinDeg(observer.latitudeDeg) +
        cosDeg(declinationDeg) * cosDeg(observer.latitudeDeg) * cosDeg(hourAngle)
    val altitude = asinDeg(sinAlt)

    val cosAltRaw = cosDeg(altitude)
    val cosAlt = if (abs(cosAltRaw) < 1e-9) 1e-9 else cosAltRaw

    val cosAz = (sinDeg(declinationDeg) - sinDeg(altitude) * sinDeg(observer.latitudeDeg)) /
        (cosAlt * cosDeg(observer.latitudeDeg))
    var azimuth = acosDeg(cosAz)
    if (sinDeg(hourAngle) > 0) {
        azimuth = 360.0 - azimuth
    }

    return HorizontalCoordinates(altitude, azimuth)
}

/** Standard altitude thresholds (degrees) for rise/set events, accounting for refraction and apparent size. */
object RiseSetThresholds {
    const val STAR_OR_PLANET = -0.5667
    const val SUN = -0.8333

    /** Meeus 15.1: the Moon's threshold depends on its horizontal parallax (degrees). */
    fun moon(horizontalParallaxDeg: Double): Double = 0.7275 * horizontalParallaxDeg - 0.5667
}
