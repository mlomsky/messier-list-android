package com.mlomsky.messierviewer.model

import com.mlomsky.messierviewer.astro.Planet

sealed interface CatalogTarget {
    val id: String
    val displayName: String

    data class Messier(
        val number: Int,
        val commonName: String?,
        val objectType: String,
        val rightAscensionDeg: Double,
        val declinationDeg: Double
    ) : CatalogTarget {
        override val id: String get() = "M$number"
        override val displayName: String get() = if (commonName != null) "M$number - $commonName" else "M$number"
    }

    data class PlanetTarget(val planet: Planet) : CatalogTarget {
        override val id: String get() = planet.name
        override val displayName: String get() = planet.displayName
    }
}
