package com.example.thehive.util

import android.content.Context

/**
 * Manages SharedPreferences for Landlord Availability Reminder settings.
 */
class ReminderPreferences(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE)

    var isReminderEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMINDER_ENABLED, DEFAULT_ENABLED)
        set(value) = prefs.edit().putBoolean(KEY_REMINDER_ENABLED, value).apply()

    var reminderFrequencyDays: Int
        get() = prefs.getInt(KEY_REMINDER_FREQUENCY_DAYS, DEFAULT_FREQUENCY_DAYS)
        set(value) = prefs.edit().putInt(KEY_REMINDER_FREQUENCY_DAYS, value).apply()

    fun clear() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_REMINDER_ENABLED = "reminder_enabled"
        private const val KEY_REMINDER_FREQUENCY_DAYS = "reminder_frequency_days"

        const val DEFAULT_ENABLED = true
        const val DEFAULT_FREQUENCY_DAYS = 7
    }
}
