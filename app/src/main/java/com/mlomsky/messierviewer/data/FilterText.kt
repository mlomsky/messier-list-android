package com.mlomsky.messierviewer.data

/** The UHC/OIII/H-Beta/Light Pollution/Other selection backing a filter edit dialog. */
data class FilterSelection(
    val uhc: Boolean,
    val oiii: Boolean,
    val hBeta: Boolean,
    val lightPollution: Boolean,
    val otherChecked: Boolean,
    val otherText: String
)

private val uhcPattern = Regex("UHC", RegexOption.IGNORE_CASE)
private val oiiiPattern = Regex("OIII", RegexOption.IGNORE_CASE)
private val hBetaPattern = Regex("H-?BETA", RegexOption.IGNORE_CASE)
private val lightPollutionPattern = Regex("LIGHT\\s*POLLUTION", RegexOption.IGNORE_CASE)

/** Recovers checkbox state from a stored/default filter string (e.g. "UHC/OIII (near-tie)*"). */
fun parseFilterText(text: String): FilterSelection {
    if (text.isBlank()) return FilterSelection(false, false, false, false, false, "")
    val uhc = uhcPattern.containsMatchIn(text)
    val oiii = oiiiPattern.containsMatchIn(text)
    val hBeta = hBetaPattern.containsMatchIn(text)
    val lightPollution = lightPollutionPattern.containsMatchIn(text)
    val otherText = text
        .replace(uhcPattern, "")
        .replace(oiiiPattern, "")
        .replace(hBetaPattern, "")
        .replace(lightPollutionPattern, "")
        .replace("/", " ")
        .trim(' ', ',', '.', '-')
        .trim()
    return FilterSelection(uhc, oiii, hBeta, lightPollution, otherText.isNotBlank(), otherText)
}

/** Builds the stored filter string from checkbox state, in UHC/OIII/H-BETA/Light Pollution/other order. */
fun buildFilterText(
    uhc: Boolean,
    oiii: Boolean,
    hBeta: Boolean,
    lightPollution: Boolean,
    otherChecked: Boolean,
    otherText: String
): String {
    val parts = mutableListOf<String>()
    if (uhc) parts.add("UHC")
    if (oiii) parts.add("OIII")
    if (hBeta) parts.add("H-BETA")
    if (lightPollution) parts.add("Light Pollution")
    if (otherChecked && otherText.isNotBlank()) parts.add(otherText.trim())
    return parts.joinToString("/")
}
