package com.example.thehive.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.data.repository.UserRepository
import com.example.thehive.util.NotificationHelper
import com.example.thehive.util.ReminderPreferences
import com.example.thehive.util.SessionManager
import com.example.thehive.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SettingsUiState(
    val isLandlord: Boolean = false,
    val isReminderEnabled: Boolean = true,
    val reminderFrequencyDays: Int = 7,
    val hasNotificationPermission: Boolean = false
)

class SettingsViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun loadSettings(context: Context) {
        val preferences = ReminderPreferences(context)
        val hasPermission = NotificationHelper.hasNotificationPermission(context)

        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            var isLandlord = false
            if (userId != SessionManager.NO_USER) {
                val user = userRepository.findById(userId)
                isLandlord = user?.role == UserRole.LANDLORD
            }

            _uiState.value = SettingsUiState(
                isLandlord = isLandlord,
                isReminderEnabled = preferences.isReminderEnabled,
                reminderFrequencyDays = preferences.reminderFrequencyDays,
                hasNotificationPermission = hasPermission
            )
        }
    }

    fun setReminderEnabled(context: Context, enabled: Boolean) {
        val preferences = ReminderPreferences(context)
        preferences.isReminderEnabled = enabled
        _uiState.value = _uiState.value.copy(isReminderEnabled = enabled)

        if (enabled) {
            ReminderScheduler.schedulePeriodicReminder(context, preferences.reminderFrequencyDays)
        } else {
            ReminderScheduler.cancelReminder(context)
        }
    }

    fun setReminderFrequency(context: Context, days: Int) {
        val preferences = ReminderPreferences(context)
        preferences.reminderFrequencyDays = days
        _uiState.value = _uiState.value.copy(reminderFrequencyDays = days)

        if (preferences.isReminderEnabled) {
            ReminderScheduler.schedulePeriodicReminder(context, days)
        }
    }

    fun triggerTestCheck(context: Context) {
        ReminderScheduler.triggerImmediateCheck(context)
    }

    fun refreshPermissionStatus(context: Context) {
        val hasPermission = NotificationHelper.hasNotificationPermission(context)
        _uiState.value = _uiState.value.copy(hasNotificationPermission = hasPermission)
    }
}
