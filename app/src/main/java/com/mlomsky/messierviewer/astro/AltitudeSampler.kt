package com.mlomsky.messierviewer.astro

import java.time.Duration
import java.time.Instant

data class AltitudeSample(val time: Instant, val altitudeDeg: Double, val azimuthDeg: Double)

data class VisibilityWindow(
    val risesAboveThreshold: Boolean,
    val alwaysAboveThreshold: Boolean,
    val riseTime: Instant?,
    val setTime: Instant?,
    val maxAltitudeTime: Instant,
    val maxAltitudeDeg: Double,
    val azimuthAtMax: Double
)

/**
 * Samples an object's altitude across [start, end] at [stepMinutes] resolution to find when it
 * crosses [thresholdDeg], and its peak altitude in the window. A sampling approach (rather than a
 * closed-form rise/set formula) was chosen because it works uniformly for fixed (Messier) and
 * moving (Sun/Moon/planet) targets, and degrades gracefully for circumpolar or never-rising objects.
 */
object AltitudeSampler {

    fun sample(
        observer: Observer,
        position: EquatorialPositionProvider,
        start: Instant,
        end: Instant,
        thresholdDeg: Double,
        stepMinutes: Long = 2
    ): VisibilityWindow {
        val samples = mutableListOf<AltitudeSample>()
        var t = start
        while (!t.isAfter(end)) {
            val jd = JulianDate.fromInstant(t)
            val horizontal = position.at(jd).toHorizontal(observer, jd)
            samples.add(AltitudeSample(t, horizontal.altitudeDeg, horizontal.azimuthDeg))
            t = t.plus(Duration.ofMinutes(stepMinutes))
        }
        if (samples.last().time != end) {
            val jd = JulianDate.fromInstant(end)
            val horizontal = position.at(jd).toHorizontal(observer, jd)
            samples.add(AltitudeSample(end, horizontal.altitudeDeg, horizontal.azimuthDeg))
        }

        var maxSample = samples[0]
        for (s in samples) if (s.altitudeDeg > maxSample.altitudeDeg) maxSample = s

        var riseTime: Instant? = null
        var setTime: Instant? = null
        for (i in 1 until samples.size) {
            val prev = samples[i - 1]
            val curr = samples[i]
            if (prev.altitudeDeg < thresholdDeg && curr.altitudeDeg >= thresholdDeg && riseTime == null) {
                riseTime = interpolateCrossing(prev, curr, thresholdDeg)
            }
            if (prev.altitudeDeg >= thresholdDeg && curr.altitudeDeg < thresholdDeg) {
                setTime = interpolateCrossing(prev, curr, thresholdDeg)
            }
        }

        val alwaysAbove = samples.all { it.altitudeDeg >= thresholdDeg }
        val everAbove = samples.any { it.altitudeDeg >= thresholdDeg }

        return VisibilityWindow(
            risesAboveThreshold = everAbove,
            alwaysAboveThreshold = alwaysAbove,
            riseTime = riseTime,
            setTime = setTime,
            maxAltitudeTime = maxSample.time,
            maxAltitudeDeg = maxSample.altitudeDeg,
            azimuthAtMax = maxSample.azimuthDeg
        )
    }

    private fun interpolateCrossing(a: AltitudeSample, b: AltitudeSample, thresholdDeg: Double): Instant {
        val span = b.altitudeDeg - a.altitudeDeg
        if (span == 0.0) return a.time
        val fraction = (thresholdDeg - a.altitudeDeg) / span
        val durationMillis = Duration.between(a.time, b.time).toMillis()
        return a.time.plusMillis((durationMillis * fraction).toLong())
    }
}
