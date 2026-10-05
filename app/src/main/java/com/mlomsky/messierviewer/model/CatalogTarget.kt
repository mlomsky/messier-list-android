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
        val declinationDeg: Double,
        val apparentMagnitude: Double
    ) : CatalogTarget {
        override val id: String get() = "M$number"
        override val displayName: String get() = if (commonName != null) "M$number - $commonName" else "M$number"
    }

    data class PlanetTarget(val planet: Planet) : CatalogTarget {
        override val id: String get() = planet.name
        override val displayName: String get() = planet.displayName
    }

    data class Ngc(
        val number: Int,
        val commonName: String?,
        val objectType: String,
        val rightAscensionDeg: Double,
        val declinationDeg: Double,
        val apparentMagnitude: Double?
    ) : CatalogTarget {
        override val id: String get() = "NGC$number"
        override val displayName: String get() = if (commonName != null) "NGC $number - $commonName" else "NGC $number"
    }
}

/** A plain-text object type label usable for search/filtering, uniform across all catalog kinds. */
val CatalogTarget.typeLabel: String
    get() = when (this) {
        is CatalogTarget.Messier -> objectType
        is CatalogTarget.Ngc -> objectType
        is CatalogTarget.PlanetTarget -> "Planet"
    }
