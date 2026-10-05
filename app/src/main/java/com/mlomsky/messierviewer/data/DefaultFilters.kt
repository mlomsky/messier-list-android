package com.mlomsky.messierviewer.data

/**
 * Starting filter recommendations for the Messier objects where a specific nebula filter
 * (UHC/OIII/H-beta/Deep-Sky) makes a real difference, ported from the user's notes in the
 * Python Astronomy tool. Keyed by catalog id ("M1", "M8", ...). Objects not listed here have
 * no default — the user can still set their own via the filter button.
 */
object DefaultFilters {
    val byId: Map<String, String> = mapOf(
        "M1" to "UHC/DEEP-SKY (H-beta *not* recommended).",
        "M8" to "UHC/OIII",
        "M16" to "UHC/OIII, but H-BETA hurts the view",
        "M17" to "OIII/UHC (H-BETA not recommended)",
        "M20" to "UHC/H-BETA",
        "M27" to "UHC (OIII also useful in showing some inner detail)",
        "M42" to "UHC/OIII (near-tie)*",
        "M43" to "H-BETA (UHC and Deep-Sky also help)",
        "M57" to "UHC/OIII.  UHC does improve it to a degree",
        "M76" to "UHC/OIII (H-BETA NOT recommended!)",
        "M97" to "OIII/UHC (H-beta *not* recommended)"
    )
}
