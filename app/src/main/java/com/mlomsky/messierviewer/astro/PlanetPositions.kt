package com.mlomsky.messierviewer.astro

import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Approximate geocentric planetary positions using mean Keplerian elements (Standish/JPL,
 * "Keplerian Elements for Approximate Positions of the Major Planets", valid 1800-2050),
 * following the method in Meeus, Astronomical Algorithms, ch. 33. No light-time or aberration
 * correction is applied; expect accuracy on the order of a few arcminutes, which is fine for
 * telling whether a planet is above the horizon but not for precision pointing.
 */
object PlanetPositions {

    fun geocentricEquatorial(planet: Planet, jd: Double): EquatorialCoordinates {
        val t = JulianDate.centuriesSinceJ2000(jd)
        val (xEarth, yEarth, zEarth) = heliocentricEcliptic(EARTH_ELEMENTS, t)
        val (xPlanet, yPlanet, zPlanet) = heliocentricEcliptic(planet.elements, t)

        val x = xPlanet - xEarth
        val y = yPlanet - yEarth
        val z = zPlanet - zEarth

        val obliquity = 23.439291 - 0.0130042 * t
        val xEq = x
        val yEq = y * cosDeg(obliquity) - z * sinDeg(obliquity)
        val zEq = y * sinDeg(obliquity) + z * cosDeg(obliquity)

        val ra = normalizeDegrees(atan2Deg(yEq, xEq))
        val r = sqrt(xEq * xEq + yEq * yEq + zEq * zEq)
        val dec = asinDeg(zEq / r)

        return EquatorialCoordinates(ra, dec)
    }

    private fun heliocentricEcliptic(elements: OrbitalElements, t: Double): Triple<Double, Double, Double> {
        val resolved = elements.at(t)
        val meanAnomaly = normalizeSigned(resolved.l - resolved.longPeri)
        val eccentricAnomaly = solveKepler(meanAnomaly, resolved.e)

        val xOrbit = resolved.a * (cosDeg(eccentricAnomaly) - resolved.e)
        val yOrbit = resolved.a * sqrt(1 - resolved.e * resolved.e) * sinDeg(eccentricAnomaly)

        val argPeri = resolved.longPeri - resolved.longNode
        val bigOmega = resolved.longNode
        val incl = resolved.i

        val cosO = cosDeg(bigOmega)
        val sinO = sinDeg(bigOmega)
        val cosW = cosDeg(argPeri)
        val sinW = sinDeg(argPeri)
        val cosI = cosDeg(incl)
        val sinI = sinDeg(incl)

        val x = (cosW * cosO - sinW * sinO * cosI) * xOrbit + (-sinW * cosO - cosW * sinO * cosI) * yOrbit
        val y = (cosW * sinO + sinW * cosO * cosI) * xOrbit + (-sinW * sinO + cosW * cosO * cosI) * yOrbit
        val z = (sinW * sinI) * xOrbit + (cosW * sinI) * yOrbit

        return Triple(x, y, z)
    }

    /** Solves Kepler's equation M = E - e*sin(E) for E, in degrees, via Newton-Raphson. */
    private fun solveKepler(meanAnomalyDeg: Double, eccentricity: Double): Double {
        val m = Math.toRadians(meanAnomalyDeg)
        var e = m + eccentricity * sin(m)
        repeat(8) {
            val delta = (e - eccentricity * sin(e) - m) / (1 - eccentricity * cos(e))
            e -= delta
        }
        return Math.toDegrees(e)
    }

    private fun normalizeSigned(deg: Double): Double {
        var d = normalizeDegrees(deg)
        if (d > 180.0) d -= 360.0
        return d
    }
}
