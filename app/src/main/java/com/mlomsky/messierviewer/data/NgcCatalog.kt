package com.mlomsky.messierviewer.data

import android.content.Context
import org.json.JSONArray

/**
 * The NGC (New General Catalogue) entries bundled as assets/ngc_catalog.json, scraped from
 * Wikipedia's "List of NGC objects" series. ~4,169 objects with valid single-number designations
 * and parseable coordinates (duplicate/lettered sub-designations like "1427A" are left out, as
 * are a handful of rows missing full coordinates).
 */
data class NgcEntry(
    val number: Int,
    val objectType: String,
    val raDeg: Double,
    val decDeg: Double,
    val apparentMagnitude: Double?,
    val commonName: String?
)

private val parentheticalPattern = Regex("\\s*-?\\s*\\([^)]*\\)")
private val footnotePattern = Regex("\\s*\\[[^]]*]")

/**
 * Wikipedia's "Other names" column often holds a pure annotation rather than a real name, e.g.
 * "(Located in Small Magellanic Cloud )" or "(Duplicate of NGC 20 ) [5]". Strips any
 * parenthesized/bracketed annotation (and a leading "-" separator) from a raw name, returning
 * null if nothing but the annotation was there.
 */
private fun cleanCommonName(raw: String): String? {
    val cleaned = raw.replace(parentheticalPattern, "").replace(footnotePattern, "").trim()
    return cleaned.ifBlank { null }
}

object NgcCatalog {

    @Volatile
    private var cached: List<NgcEntry>? = null

    fun load(context: Context): List<NgcEntry> {
        cached?.let { return it }
        val json = context.assets.open("ngc_catalog.json").bufferedReader(Charsets.UTF_8).use { it.readText() }
        val array = JSONArray(json)
        val entries = ArrayList<NgcEntry>(array.length())
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            entries.add(
                NgcEntry(
                    number = obj.getInt("number"),
                    objectType = obj.getString("objectType"),
                    raDeg = obj.getDouble("raDeg"),
                    decDeg = obj.getDouble("decDeg"),
                    apparentMagnitude = if (obj.isNull("magnitude")) null else obj.getDouble("magnitude"),
                    commonName = if (obj.isNull("commonName")) null else cleanCommonName(obj.getString("commonName"))
                )
            )
        }
        cached = entries
        return entries
    }
}
