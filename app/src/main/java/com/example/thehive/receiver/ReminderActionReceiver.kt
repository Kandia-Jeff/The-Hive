package com.example.thehive.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.thehive.HiveApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: return
        val listingId = intent.getLongExtra(EXTRA_LISTING_ID, -1L)
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1)

        if (listingId == -1L) return

        val pendingResult = goAsync()
        val app = context.applicationContext as HiveApplication
        val listingRepository = app.container.listingRepository

        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_STILL_AVAILABLE -> {
                        listingRepository.confirmStillAvailable(listingId)
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Listing confirmed as available", Toast.LENGTH_SHORT).show()
                        }
                    }
                    ACTION_MARK_TAKEN -> {
                        listingRepository.setAvailability(listingId, false)
                        CoroutineScope(Dispatchers.Main).launch {
                            Toast.makeText(context, "Listing marked as taken", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                if (notificationId != -1) {
                    NotificationManagerCompat.from(context).cancel(notificationId)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_STILL_AVAILABLE = "com.example.thehive.ACTION_STILL_AVAILABLE"
        const val ACTION_MARK_TAKEN = "com.example.thehive.ACTION_MARK_TAKEN"

        const val EXTRA_LISTING_ID = "extra_listing_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
    }
}
