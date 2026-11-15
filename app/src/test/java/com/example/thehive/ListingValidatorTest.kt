package com.example.thehive

import com.example.thehive.util.ListingInput
import com.example.thehive.util.ListingValidator
import org.junit.Assert.*
import org.junit.Test

class ListingValidatorTest {
    private val valid = ListingInput("Nice Flat", "Kilimani", 30_000.0, 2, "0712345678", "a@b.com")

    @Test fun validInput_passes() = assertTrue(ListingValidator.validate(valid).isValid)
    @Test fun emptyTitle_fails() = assertTrue("title" in ListingValidator.validate(valid.copy(title = " ")).errors)
    @Test fun zeroRent_fails() = assertTrue("rent" in ListingValidator.validate(valid.copy(rent = 0.0)).errors)
    @Test fun negativeBedrooms_fails() = assertTrue("bedrooms" in ListingValidator.validate(valid.copy(bedrooms = -1)).errors)
    @Test fun zeroBedrooms_isAllowed() = assertTrue(ListingValidator.validate(valid.copy(bedrooms = 0)).isValid)
    @Test fun badPhone_fails() = assertTrue("phone" in ListingValidator.validate(valid.copy(phone = "123")).errors)
    @Test fun intlPhone_passes() = assertTrue(ListingValidator.validate(valid.copy(phone = "+254712345678")).isValid)
    @Test fun badEmail_fails() = assertTrue("email" in ListingValidator.validate(valid.copy(email = "nope")).errors)
    @Test fun duplicate_fails() = assertFalse(ListingValidator.validate(valid, isDuplicate = true).isValid)
}
