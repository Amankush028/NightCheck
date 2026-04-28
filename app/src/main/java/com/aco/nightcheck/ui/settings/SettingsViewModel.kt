package com.nightcheck.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aco.nightcheck.util.AlarmScheduler
import com.nightcheck.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val endOfDayHour: Int = PreferencesManager.DEFAULT_EOD_HOUR,
    val endOfDayMinute: Int = PreferencesManager.DEFAULT_EOD_MINUTE
) {
    /** Formatted as "9:00 PM" style */
    val formattedTime: String
        get() {
            val amPm = if (endOfDayHour < 12) "AM" else "PM"
            val displayHour = when {
                endOfDayHour == 0  -> 12
                endOfDayHour > 12  -> endOfDayHour - 12
                else               -> endOfDayHour
            }
            return "$displayHour:${endOfDayMinute.toString().padStart(2, '0')} $amPm"
        }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val alarmScheduler: AlarmScheduler
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.endOfDayHour,
        preferencesManager.endOfDayMinute
    ) { hour, minute -> SettingsUiState(endOfDayHour = hour, endOfDayMinute = minute) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun updateEndOfDayTime(hour: Int, minute: Int) = viewModelScope.launch {
        preferencesManager.setEndOfDayTime(hour, minute)
        // Reschedule the alarm with the new time
        alarmScheduler.scheduleEndOfDay(hour, minute)
    }
}
