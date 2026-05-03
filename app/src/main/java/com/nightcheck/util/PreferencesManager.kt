package com.nightcheck.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.booleanPreferencesKey

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    // ── Keys ──────────────────────────────────────────────────────────────────

    private object Keys {
        val EOD_HOUR   = intPreferencesKey("eod_hour")
        val EOD_MINUTE = intPreferencesKey("eod_minute")
    }

    // ── End-of-Day review time ────────────────────────────────────────────────

    /** Emits the currently saved End-of-Day hour (default 21 = 9 PM) */
    val endOfDayHour: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.EOD_HOUR] ?: DEFAULT_EOD_HOUR
    }

    val isDarkTheme: Flow<Boolean> = dataStore.data
        .map { prefs -> prefs[IS_DARK_THEME_KEY] ?: true } // default: dark

    suspend fun setDarkTheme(dark: Boolean) {
        dataStore.edit { prefs -> prefs[IS_DARK_THEME_KEY] = dark }
    }


    /** Emits the currently saved End-of-Day minute (default 0) */
    val endOfDayMinute: Flow<Int> = dataStore.data.map { prefs ->
        prefs[Keys.EOD_MINUTE] ?: DEFAULT_EOD_MINUTE
    }

    suspend fun setEndOfDayTime(hour: Int, minute: Int) {
        dataStore.edit { prefs ->
            prefs[Keys.EOD_HOUR]   = hour
            prefs[Keys.EOD_MINUTE] = minute
        }
    }

    companion object {
        const val DEFAULT_EOD_HOUR   = 21   // 9 PM
        const val DEFAULT_EOD_MINUTE = 0

        private val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
    }
}
