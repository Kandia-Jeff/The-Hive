package com.example.thehive

import com.example.thehive.util.ContactHelper
import org.junit.Assert.assertEquals
import org.junit.Test

class ContactHelperTest {

    @Test
    fun sanitizePhone_localKenyaFormat_convertsToInternational() {
        val result = ContactHelper.sanitizePhoneForWhatsApp("0712345678")
        assertEquals("254712345678", result)
    }

    @Test
    fun sanitizePhone_internationalWithPlus_removesPlusAndSpaces() {
        val result = ContactHelper.sanitizePhoneForWhatsApp("+254 712 345 678")
        assertEquals("254712345678", result)
    }

    @Test
    fun sanitizePhone_alreadyFormatted_stripsSpecialChars() {
        val result = ContactHelper.sanitizePhoneForWhatsApp("254-712-345-678")
        assertEquals("254712345678", result)
    }

    @Test
    fun sanitizePhone_withParenthesesAndDashes_stripsNonDigits() {
        val result = ContactHelper.sanitizePhoneForWhatsApp("+254 (0) 712-345-678")
        assertEquals("2540712345678", result)
    }
}
