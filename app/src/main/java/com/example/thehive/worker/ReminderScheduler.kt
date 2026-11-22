package com.example.thehive.worker

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Manages WorkManager task scheduling and cancellation for availability reminders.
 */
object ReminderScheduler {

    const val WORK_NAME = "availability_reminder_periodic_work"

    /**
     * Enqueues or updates unique periodic work to check stale listings according to [frequencyDays].
     */
    fun schedulePeriodicReminder(context: Context, frequencyDays: Int) {
        val days = frequencyDays.coerceAtLeast(1).toLong()
        val workRequest = PeriodicWorkRequestBuilder<AvailabilityReminderWorker>(
            days, TimeUnit.DAYS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    /**
     * Cancels scheduled periodic availability reminder work.
     */
    fun cancelReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    /**
     * Triggers an immediate one-time check (useful for manual testing from Settings).
     */
    fun triggerImmediateCheck(context: Context) {
        val workRequest = OneTimeWorkRequestBuilder<AvailabilityReminderWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}
