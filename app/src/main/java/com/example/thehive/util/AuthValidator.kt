package com.example.thehive.util

data class AuthInput(
    val fullName: String = "",
    val email: String,
    val phone: String = "",
    val password: String,
    val confirmPassword: String = ""
)

data class AuthValidationResult(val errors: Map<String, String> = emptyMap()) {
    val isValid get() = errors.isEmpty()
}

/**
 * Pure Kotlin validator for authentication (login and register) so it can be unit-tested on the JVM.
 */
object AuthValidator {
    private val PHONE = Regex("^(?:\\+?254|0)?[17]\\d{8}$")
    private val EMAIL = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validateLogin(email: String, password: String): AuthValidationResult {
        val errors = mutableMapOf<String, String>()
        if (email.isBlank() || !EMAIL.matches(email.trim())) {
            errors["email"] = "Invalid email address"
        }
        if (password.isEmpty()) {
            errors["password"] = "Password cannot be empty"
        }
        return AuthValidationResult(errors)
    }

    fun validateRegister(input: AuthInput): AuthValidationResult {
        val errors = mutableMapOf<String, String>()
        if (input.fullName.isBlank()) {
            errors["fullName"] = "Name cannot be empty"
        }
        if (input.email.isBlank() || !EMAIL.matches(input.email.trim())) {
            errors["email"] = "Invalid email address"
        }
        if (input.phone.isBlank() || !PHONE.matches(input.phone.trim().replace(" ", ""))) {
            errors["phone"] = "Invalid phone number"
        }
        if (input.password.length < 6) {
            errors["password"] = "Password must be at least 6 characters"
        }
        if (input.password != input.confirmPassword) {
            errors["confirmPassword"] = "Passwords do not match"
        }
        return AuthValidationResult(errors)
    }
}
