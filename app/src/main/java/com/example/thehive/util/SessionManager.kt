package com.example.thehive.util

import android.content.Context

/** Stores the logged-in user's id on the device (SharedPreferences). */
class SessionManager(context: Context) {
    private val prefs = context.applicationContext
        .getSharedPreferences("session_prefs", Context.MODE_PRIVATE)

    fun saveUserId(id: Long) = prefs.edit().putLong(KEY_USER_ID, id).apply()
    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)
    fun isLoggedIn(): Boolean = getUserId() != NO_USER
    fun logout() = prefs.edit().remove(KEY_USER_ID).apply()

    companion object {
        private const val KEY_USER_ID = "user_id"
        const val NO_USER = -1L
    }
}
