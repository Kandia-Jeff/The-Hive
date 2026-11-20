package com.example.thehive

import com.example.thehive.util.AuthInput
import com.example.thehive.util.AuthValidator
import org.junit.Assert.*
import org.junit.Test

class AuthValidatorTest {
    private val validRegister = AuthInput(
        fullName = "John Doe",
        email = "john@example.com",
        phone = "0712345678",
        password = "password123",
        confirmPassword = "password123"
    )

    @Test
    fun validLogin_passes() {
        val result = AuthValidator.validateLogin("john@example.com", "password123")
        assertTrue(result.isValid)
    }

    @Test
    fun invalidLoginEmail_fails() {
        val result = AuthValidator.validateLogin("invalid-email", "password123")
        assertTrue("email" in result.errors)
    }

    @Test
    fun emptyLoginPassword_fails() {
        val result = AuthValidator.validateLogin("john@example.com", "")
        assertTrue("password" in result.errors)
    }

    @Test
    fun validRegister_passes() {
        val result = AuthValidator.validateRegister(validRegister)
        assertTrue(result.isValid)
    }

    @Test
    fun emptyName_fails() {
        val result = AuthValidator.validateRegister(validRegister.copy(fullName = ""))
        assertTrue("fullName" in result.errors)
    }

    @Test
    fun badEmail_fails() {
        val result = AuthValidator.validateRegister(validRegister.copy(email = "not-an-email"))
        assertTrue("email" in result.errors)
    }

    @Test
    fun badPhone_fails() {
        val result = AuthValidator.validateRegister(validRegister.copy(phone = "123"))
        assertTrue("phone" in result.errors)
    }

    @Test
    fun shortPassword_fails() {
        val result = AuthValidator.validateRegister(validRegister.copy(password = "123", confirmPassword = "123"))
        assertTrue("password" in result.errors)
    }

    @Test
    fun passwordsMismatch_fails() {
        val result = AuthValidator.validateRegister(validRegister.copy(confirmPassword = "different"))
        assertTrue("confirmPassword" in result.errors)
    }
}
