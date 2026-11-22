package com.example.thehive.util

/**
 * Builds prefilled enquiry text containing the listing title, location, and the sender's name.
 */
object EnquiryMessageBuilder {
    fun build(
        listingTitle: String,
        location: String,
        senderName: String? = null
    ): String {
        val greeting = if (!senderName.isNullOrBlank()) {
            "Hi, my name is ${senderName.trim()}."
        } else {
            "Hi,"
        }
        val cleanTitle = listingTitle.trim()
        val cleanLocation = location.trim()
        return "$greeting I am inquiring about your listing \"$cleanTitle\" in $cleanLocation. Is it still available?"
    }
}
