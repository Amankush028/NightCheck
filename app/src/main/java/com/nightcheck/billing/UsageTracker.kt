package com.nightcheck.billing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Tracks free-tier usage and enforces limits.
 *
 * Free limits:
 *  - MAX_FREE_TASKS  = 7 (total active tasks + recurring tasks in DB)
 *  - MAX_FREE_NOTES  = 10
 *  - SESSION_INTERSTITIAL_THRESHOLD = 10 cumulative adds per session
 *
 * Session counter resets on a new calendar day (first app open of the day).
 * The actual count queries run against Room via [TaskRepository] / [NoteRepository]
 * at the call site — this class only tracks the *session* interstitial counter
 * and exposes helper constants.
 */
@Singleton
class UsageTracker @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        const val MAX_FREE_TASKS                   = 7
        const val MAX_FREE_NOTES                   = 10
        const val SESSION_INTERSTITIAL_THRESHOLD   = 10
    }

    private object Keys {
        val SESSION_ADDS_COUNT  = intPreferencesKey("session_adds_count")
        val SESSION_EPOCH_DAY   = longPreferencesKey("session_epoch_day")
    }

    // ── Session interstitial counter ──────────────────────────────────────────

    /** Returns the current session add count, resetting if it's a new day. */
    val sessionAddsFlow: Flow<Int> = dataStore.data.map { prefs ->
        val savedDay = prefs[Keys.SESSION_EPOCH_DAY] ?: 0L
        val today    = LocalDate.now().toEpochDay()
        if (savedDay < today) 0 else (prefs[Keys.SESSION_ADDS_COUNT] ?: 0)
    }

    /**
     * Increments the session counter. Returns the new count AFTER incrementing.
     * Call this any time the user successfully adds a task or note (free or premium).
     */
    suspend fun incrementSessionAdds(): Int {
        val today = LocalDate.now().toEpochDay()
        var newCount = 0
        dataStore.edit { prefs ->
            val savedDay = prefs[Keys.SESSION_EPOCH_DAY] ?: 0L
            val current  = if (savedDay < today) 0 else (prefs[Keys.SESSION_ADDS_COUNT] ?: 0)
            newCount = current + 1
            prefs[Keys.SESSION_ADDS_COUNT] = newCount
            prefs[Keys.SESSION_EPOCH_DAY]  = today
        }
        return newCount
    }

    /** True if this particular increment crossed the interstitial threshold. */
    suspend fun shouldShowSessionInterstitial(): Boolean {
        val count = dataStore.data.first().let { prefs ->
            val savedDay = prefs[Keys.SESSION_EPOCH_DAY] ?: 0L
            val today    = LocalDate.now().toEpochDay()
            if (savedDay < today) 0 else (prefs[Keys.SESSION_ADDS_COUNT] ?: 0)
        }
        // Fire at exactly the threshold (not every add beyond it)
        return count == SESSION_INTERSTITIAL_THRESHOLD
    }
}