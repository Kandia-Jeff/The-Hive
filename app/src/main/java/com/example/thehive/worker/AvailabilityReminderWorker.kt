package com.example.thehive.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.thehive.HiveApplication
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.util.NotificationHelper
import com.example.thehive.util.ReminderPreferences
import com.example.thehive.util.SessionManager

/**
 * WorkManager worker that runs periodically to find the logged-in landlord's listings
 * whose lastConfirmedAt is older than N days, triggering a notification if any are found.
 */
class AvailabilityReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val preferences = ReminderPreferences(context)

        // If reminders are turned off, exit early
        if (!preferences.isReminderEnabled) {
            return Result.success()
        }

        val app = context as? HiveApplication ?: return Result.success()
        val sessionManager = app.container.sessionManager
        val userId = sessionManager.getUserId()

        if (userId == SessionManager.NO_USER) {
            return Result.success()
        }

        val userRepository = app.container.userRepository
        val listingRepository = app.container.listingRepository

        val user = userRepository.findById(userId) ?: return Result.success()
        if (user.role != UserRole.LANDLORD) {
            return Result.success()
        }

        val frequencyDays = preferences.reminderFrequencyDays
        val staleListings = listingRepository.getStaleListings(userId, frequencyDays)

        if (staleListings.isNotEmpty()) {
            NotificationHelper.showAvailabilityReminder(context, staleListings)
        }

        return Result.success()
    }
}
