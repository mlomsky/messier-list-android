package com.mlomsky.messierviewer.astro

val CompassPoints = listOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")

fun Double.toCardinalDirection(): String {
    val normalized = ((this % 360) + 360) % 360
    val index = ((normalized / 45.0) + 0.5).toInt() % 8
    return CompassPoints[index]
}
