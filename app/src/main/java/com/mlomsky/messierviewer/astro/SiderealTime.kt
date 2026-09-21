package com.mlomsky.messierviewer.astro

/** Greenwich/local sidereal time (Meeus, Astronomical Algorithms, ch. 12). */
object SiderealTime {

    /** Greenwich Mean Sidereal Time in degrees, normalized to [0, 360). */
    fun greenwichMeanSiderealTimeDegrees(jd: Double): Double {
        val t = JulianDate.centuriesSinceJ2000(jd)
        val gmst = 280.46061837 +
            360.98564736629 * (jd - 2451545.0) +
            0.000387933 * t * t -
            (t * t * t) / 38710000.0
        return normalizeDegrees(gmst)
    }

    /** Local Mean Sidereal Time in degrees, normalized to [0, 360). [longitudeDeg] is east-positive. */
    fun localMeanSiderealTimeDegrees(jd: Double, longitudeDeg: Double): Double =
        normalizeDegrees(greenwichMeanSiderealTimeDegrees(jd) + longitudeDeg)
}
