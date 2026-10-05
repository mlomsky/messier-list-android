package com.mlomsky.messierviewer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mlomsky.messierviewer.model.CatalogSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")
private const val FILTER_KEY_PREFIX = "filter_"

val DEFAULT_ENABLED_CATALOGS: Set<CatalogSource> = setOf(CatalogSource.PLANETS, CatalogSource.MESSIER)
const val DEFAULT_NGC_MAGNITUDE_LIMIT = 12.0

class PreferencesRepository(private val context: Context) {
    private val keyNightMode = booleanPreferencesKey("night_mode")
    private val keyEnabledCatalogs = stringSetPreferencesKey("enabled_catalogs")
    private val keyNgcMagnitudeLimit = doublePreferencesKey("ngc_magnitude_limit")
    private val keyHideBelowHorizon = booleanPreferencesKey("hide_below_horizon")
    private val keyFavorites = stringSetPreferencesKey("favorites")

    val nightModeEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[keyNightMode] ?: false }

    suspend fun setNightMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[keyNightMode] = enabled }
    }

    val enabledCatalogs: Flow<Set<CatalogSource>> = context.settingsDataStore.data.map { prefs ->
        val stored = prefs[keyEnabledCatalogs]
        if (stored == null) {
            DEFAULT_ENABLED_CATALOGS
        } else {
            stored.mapNotNull { name -> runCatching { CatalogSource.valueOf(name) }.getOrNull() }.toSet()
        }
    }

    suspend fun setCatalogEnabled(source: CatalogSource, enabled: Boolean) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[keyEnabledCatalogs] ?: DEFAULT_ENABLED_CATALOGS.map { it.name }.toSet()
            prefs[keyEnabledCatalogs] = if (enabled) current + source.name else current - source.name
        }
    }

    val ngcMagnitudeLimit: Flow<Double> =
        context.settingsDataStore.data.map { it[keyNgcMagnitudeLimit] ?: DEFAULT_NGC_MAGNITUDE_LIMIT }

    suspend fun setNgcMagnitudeLimit(limit: Double) {
        context.settingsDataStore.edit { it[keyNgcMagnitudeLimit] = limit }
    }

    val hideBelowHorizon: Flow<Boolean> = context.settingsDataStore.data.map { it[keyHideBelowHorizon] ?: false }

    suspend fun setHideBelowHorizon(enabled: Boolean) {
        context.settingsDataStore.edit { it[keyHideBelowHorizon] = enabled }
    }

    /** Favorited object ids ("M1", "NGC220", "Jupiter", ...). */
    val favorites: Flow<Set<String>> = context.settingsDataStore.data.map { it[keyFavorites] ?: emptySet() }

    suspend fun toggleFavorite(objectId: String) {
        context.settingsDataStore.edit { prefs ->
            val current = prefs[keyFavorites] ?: emptySet()
            prefs[keyFavorites] = if (objectId in current) current - objectId else current + objectId
        }
    }

    /** User-set filter overrides, keyed by catalog id ("M1", "M8", ...). */
    val filterOverrides: Flow<Map<String, String>> = context.settingsDataStore.data.map { prefs ->
        prefs.asMap().entries
            .filter { (key, _) -> key.name.startsWith(FILTER_KEY_PREFIX) }
            .associate { (key, value) -> key.name.removePrefix(FILTER_KEY_PREFIX) to value.toString() }
    }

    suspend fun setObjectFilter(objectId: String, filterText: String) {
        context.settingsDataStore.edit { prefs ->
            val key = stringPreferencesKey("$FILTER_KEY_PREFIX$objectId")
            if (filterText.isBlank()) prefs.remove(key) else prefs[key] = filterText
        }
    }
}
