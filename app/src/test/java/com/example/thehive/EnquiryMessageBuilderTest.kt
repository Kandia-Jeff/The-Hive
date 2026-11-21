package com.example.thehive

import com.example.thehive.util.EnquiryMessageBuilder
import org.junit.Assert.assertTrue
import org.junit.Test

class EnquiryMessageBuilderTest {

    @Test
    fun build_withSenderName_includesSenderNameTitleAndLocation() {
        val result = EnquiryMessageBuilder.build(
            listingTitle = "Cozy Studio",
            location = "Kilimani",
            senderName = "Alice"
        )
        assertTrue(result.startsWith("Hi, my name is Alice."))
        assertTrue(result.contains("\"Cozy Studio\""))
        assertTrue(result.contains("in Kilimani."))
    }

    @Test
    fun build_withoutSenderName_usesDefaultGreeting() {
        val result = EnquiryMessageBuilder.build(
            listingTitle = "2 Bedroom Apartment",
            location = "Westlands",
            senderName = null
        )
        assertTrue(result.startsWith("Hi,"))
        assertTrue(result.contains("\"2 Bedroom Apartment\""))
        assertTrue(result.contains("in Westlands."))
    }

    @Test
    fun build_withEmptySenderName_usesDefaultGreeting() {
        val result = EnquiryMessageBuilder.build(
            listingTitle = "Luxury Villa",
            location = "Karen",
            senderName = "   "
        )
        assertTrue(result.startsWith("Hi,"))
    }

    @Test
    fun build_trimsWhitespaceInInputs() {
        val result = EnquiryMessageBuilder.build(
            listingTitle = "  Penthouse Suite  ",
            location = "  Lavington  ",
            senderName = "  Bob Smith  "
        )
        assertTrue(result.startsWith("Hi, my name is Bob Smith."))
        assertTrue(result.contains("\"Penthouse Suite\""))
        assertTrue(result.contains("in Lavington."))
    }
}
