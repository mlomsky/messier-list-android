package com.mlomsky.messierviewer.astro

import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

internal fun sinDeg(deg: Double) = sin(Math.toRadians(deg))
internal fun cosDeg(deg: Double) = cos(Math.toRadians(deg))
internal fun tanDeg(deg: Double) = tan(Math.toRadians(deg))
internal fun asinDeg(x: Double) = Math.toDegrees(asin(x.coerceIn(-1.0, 1.0)))
internal fun acosDeg(x: Double) = Math.toDegrees(acos(x.coerceIn(-1.0, 1.0)))
internal fun atan2Deg(y: Double, x: Double) = Math.toDegrees(atan2(y, x))

/** Normalizes an angle in degrees to [0, 360). */
fun normalizeDegrees(deg: Double): Double {
    var d = deg % 360.0
    if (d < 0) d += 360.0
    return d
}
