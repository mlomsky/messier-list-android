package com.mlomsky.messierviewer.astro

/**
 * Reduced-accuracy geocentric lunar position (Meeus, Astronomical Algorithms, ch. 47 intro),
 * accurate to about 10' in longitude and 4' in latitude - plenty for rise/set/altitude purposes.
 */
object MoonPosition {

    data class MoonPositionResult(
        val equatorial: EquatorialCoordinates,
        val horizontalParallaxDeg: Double
    )

    fun geocentric(jd: Double): MoonPositionResult {
        val t = JulianDate.centuriesSinceJ2000(jd)

        // Fundamental arguments (degrees).
        val lMoon = normalizeDegrees(218.3164591 + 481267.88134236 * t)      // mean longitude
        val d = normalizeDegrees(297.8502042 + 445267.1115168 * t)          // mean elongation from Sun
        val mSun = normalizeDegrees(357.5291092 + 35999.0502909 * t)        // Sun's mean anomaly
        val mMoon = normalizeDegrees(134.9634114 + 477198.8676313 * t)      // Moon's mean anomaly
        val f = normalizeDegrees(93.2720993 + 483202.0175273 * t)           // argument of latitude

        val longitude = lMoon +
            6.289 * sinDeg(mMoon) -
            1.274 * sinDeg(mMoon - 2 * d) +
            0.658 * sinDeg(2 * d) -
            0.186 * sinDeg(mSun) -
            0.059 * sinDeg(2 * mMoon - 2 * d) -
            0.057 * sinDeg(mMoon - 2 * d + mSun) +
            0.053 * sinDeg(mMoon + 2 * d) +
            0.046 * sinDeg(2 * d - mSun) +
            0.041 * sinDeg(mMoon - mSun) -
            0.035 * sinDeg(d) -
            0.031 * sinDeg(mMoon + mSun) -
            0.015 * sinDeg(2 * f - 2 * d) +
            0.011 * sinDeg(mMoon - 4 * d)

        val latitude = 5.128 * sinDeg(f) +
            0.281 * sinDeg(mMoon + f) +
            0.278 * sinDeg(mMoon - f) +
            0.173 * sinDeg(2 * d - f) +
            0.055 * sinDeg(2 * d - mMoon + f) +
            0.046 * sinDeg(2 * d - mMoon - f) +
            0.033 * sinDeg(2 * d + f) +
            0.017 * sinDeg(2 * mMoon + f)

        val parallax = 0.9508 +
            0.0518 * cosDeg(mMoon) +
            0.0095 * cosDeg(mMoon - 2 * d) +
            0.0078 * cosDeg(2 * d) +
            0.0028 * cosDeg(2 * mMoon)

        val obliquity = 23.439291 - 0.0130042 * t

        val ra = normalizeDegrees(
            atan2Deg(
                sinDeg(longitude) * cosDeg(obliquity) - tanDeg(latitude) * sinDeg(obliquity),
                cosDeg(longitude)
            )
        )
        val dec = asinDeg(
            sinDeg(latitude) * cosDeg(obliquity) + cosDeg(latitude) * sinDeg(obliquity) * sinDeg(longitude)
        )

        return MoonPositionResult(EquatorialCoordinates(ra, dec), parallax)
    }
}
