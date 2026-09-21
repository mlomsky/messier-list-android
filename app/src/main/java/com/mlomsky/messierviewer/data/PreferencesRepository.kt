package com.mlomsky.messierviewer.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

class PreferencesRepository(private val context: Context) {
    private val keyNightMode = booleanPreferencesKey("night_mode")

    val nightModeEnabled: Flow<Boolean> = context.settingsDataStore.data.map { it[keyNightMode] ?: false }

    suspend fun setNightMode(enabled: Boolean) {
        context.settingsDataStore.edit { it[keyNightMode] = enabled }
    }
}
