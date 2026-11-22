package com.example.thehive.util

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.thehive.MainActivity
import com.example.thehive.R
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.receiver.ReminderActionReceiver

/**
 * Manages creation of notification channels, checking POST_NOTIFICATIONS permission,
 * and dispatching availability reminder notifications with inline actions.
 */
object NotificationHelper {

    const val CHANNEL_ID = "landlord_availability_reminders"
    const val CHANNEL_NAME = "Availability Reminders"
    const val NOTIFICATION_ID_BASE = 1001

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for landlords to confirm listing availability"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    @SuppressLint("MissingPermission")
    fun showAvailabilityReminder(context: Context, staleListings: List<ListingEntity>) {
        if (staleListings.isEmpty()) return
        createNotificationChannel(context)

        if (!hasNotificationPermission(context)) {
            return
        }

        val notificationManager = NotificationManagerCompat.from(context)

        if (staleListings.size == 1) {
            val listing = staleListings.first()
            val notificationId = NOTIFICATION_ID_BASE + listing.id.toInt()

            val openIntent = Intent(context, MainActivity::class.java)
            val openPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // "Still Available" action
            val stillAvailableIntent = Intent(context, ReminderActionReceiver::class.java).apply {
                action = ReminderActionReceiver.ACTION_STILL_AVAILABLE
                putExtra(ReminderActionReceiver.EXTRA_LISTING_ID, listing.id)
                putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val stillAvailablePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 1,
                stillAvailableIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // "Mark Taken" action
            val markTakenIntent = Intent(context, ReminderActionReceiver::class.java).apply {
                action = ReminderActionReceiver.ACTION_MARK_TAKEN
                putExtra(ReminderActionReceiver.EXTRA_LISTING_ID, listing.id)
                putExtra(ReminderActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val markTakenPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 2,
                markTakenIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Is your listing still available?")
                .setContentText("\"${listing.title}\" has not been updated recently. Is it still available?")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("\"${listing.title}\" in ${listing.location} has not been updated recently. Please confirm if it is still available.")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)
                .addAction(R.drawable.ic_notifications, "Still Available", stillAvailablePendingIntent)
                .addAction(R.drawable.ic_notifications, "Mark Taken", markTakenPendingIntent)

            try {
                notificationManager.notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Permission revoked
            }
        } else {
            // Summary notification for multiple listings
            val notificationId = NOTIFICATION_ID_BASE
            val openIntent = Intent(context, MainActivity::class.java)
            val openPendingIntent = PendingIntent.getActivity(
                context,
                notificationId,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle("Listing Availability Check")
                .setContentText("You have ${staleListings.size} listings that need availability confirmation.")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(openPendingIntent)
                .setAutoCancel(true)

            try {
                notificationManager.notify(notificationId, builder.build())
            } catch (e: SecurityException) {
                // Permission revoked
            }
        }
    }
}
