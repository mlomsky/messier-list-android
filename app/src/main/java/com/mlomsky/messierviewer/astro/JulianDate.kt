package com.mlomsky.messierviewer.astro

import java.time.Instant

/** Julian Date calculations (Meeus, Astronomical Algorithms, ch. 7). */
object JulianDate {

    /** Julian Date (UT) for the given instant. */
    fun fromInstant(instant: Instant): Double {
        val epochSeconds = instant.epochSecond + instant.nano / 1_000_000_000.0
        // Unix epoch (1970-01-01 00:00:00 UTC) = JD 2440587.5
        return 2440587.5 + epochSeconds / 86400.0
    }

    /** Julian centuries since J2000.0 (JD 2451545.0). */
    fun centuriesSinceJ2000(jd: Double): Double = (jd - 2451545.0) / 36525.0
}
