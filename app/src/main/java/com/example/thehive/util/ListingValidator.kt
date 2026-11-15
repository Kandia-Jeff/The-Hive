package com.example.thehive.util

data class ListingInput(
    val title: String,
    val location: String,
    val rent: Double?,
    val bedrooms: Int?,
    val phone: String,
    val email: String
)

data class ValidationResult(val errors: Map<String, String> = emptyMap()) {
    val isValid get() = errors.isEmpty()
}

/**
 * Pure Kotlin (no Android classes) so it can be unit-tested on the JVM.
 * Get isDuplicate from ListingRepository.isDuplicate(title, location, excludeId).
 */
object ListingValidator {
    // Kenyan numbers: 0712345678, 712345678, 254712345678, +254712345678
    private val PHONE = Regex("^(?:\\+?254|0)?[17]\\d{8}$")
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validate(input: ListingInput, isDuplicate: Boolean = false): ValidationResult {
        val errors = mutableMapOf<String, String>()

        if (input.title.isBlank()) errors["title"] = "Title cannot be empty"
        if (input.location.isBlank()) errors["location"] = "Location cannot be empty"

        if (input.rent == null || input.rent <= 0.0) errors["rent"] = "Rent must be greater than 0"
        if (input.bedrooms == null || input.bedrooms < 0) errors["bedrooms"] = "Bedrooms must be 0 or more"

        if (!PHONE.matches(input.phone.trim().replace(" ", ""))) errors["phone"] = "Invalid phone number"
        if (!EMAIL.matches(input.email.trim())) errors["email"] = "Invalid email address"

        if (isDuplicate && "title" !in errors && "location" !in errors)
            errors["title"] = "A listing with this title and location already exists"

        return ValidationResult(errors)
    }
}
