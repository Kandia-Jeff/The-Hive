package com.example.thehive.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

/**
 * Helper class for launching contact intents via Email, SMS, and WhatsApp,
 * handling cases where no app can open the intent with friendly error callbacks.
 */
object ContactHelper {

    /**
     * Opens an email client prefilled with recipient [email], [subject], and [body].
     */
    fun sendEmail(
        context: Context,
        email: String,
        subject: String,
        body: String,
        onError: (String) -> Unit
    ) {
        if (email.isBlank()) {
            onError("Email address is missing")
            return
        }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:${email.trim()}")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            onError("No email app found on your device")
        } catch (e: Exception) {
            onError("Unable to open email client: ${e.localizedMessage}")
        }
    }

    /**
     * Opens an SMS app prefilled with recipient [phone] and [message].
     */
    fun sendSms(
        context: Context,
        phone: String,
        message: String,
        onError: (String) -> Unit
    ) {
        if (phone.isBlank()) {
            onError("Phone number is missing")
            return
        }
        val cleanPhone = phone.trim()
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$cleanPhone")
            putExtra("sms_body", message)
            putExtra(Intent.EXTRA_TEXT, message)
        }
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            onError("No SMS app found on your device")
        } catch (e: Exception) {
            onError("Unable to open SMS app: ${e.localizedMessage}")
        }
    }

    /**
     * Opens WhatsApp (via https://wa.me/<number>?text=) prefilled with [phone] and [message].
     */
    fun sendWhatsApp(
        context: Context,
        phone: String,
        message: String,
        onError: (String) -> Unit
    ) {
        if (phone.isBlank()) {
            onError("Phone number is missing")
            return
        }
        val formattedNumber = sanitizePhoneForWhatsApp(phone)
        if (formattedNumber.isBlank()) {
            onError("Invalid phone number format for WhatsApp")
            return
        }
        val encodedMessage = Uri.encode(message)
        val url = "https://wa.me/$formattedNumber?text=$encodedMessage"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            onError("WhatsApp is not installed or cannot be opened")
        } catch (e: Exception) {
            onError("Unable to open WhatsApp: ${e.localizedMessage}")
        }
    }

    /**
     * Sanitizes phone numbers for WhatsApp API.
     * Replaces leading local zero with default country code (+254 for Kenya if no country code provided),
     * and removes all non-numeric characters.
     */
    fun sanitizePhoneForWhatsApp(phone: String): String {
        var trimmed = phone.trim()
        if (trimmed.startsWith("+")) {
            return trimmed.replace(Regex("[^0-9]"), "")
        }
        if (trimmed.startsWith("0")) {
            trimmed = "254" + trimmed.substring(1)
        }
        return trimmed.replace(Regex("[^0-9]"), "")
    }
}
