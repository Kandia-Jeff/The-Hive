package com.example.thehive

import com.example.thehive.util.PasswordHasher
import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    private val hasher = PasswordHasher()

    @Test fun verify_correctPassword() {
        val salt = hasher.generateSalt()
        assertTrue(hasher.verify("pw123", salt, hasher.hash("pw123", salt)))
    }

    @Test fun verify_wrongPassword() {
        val salt = hasher.generateSalt()
        assertFalse(hasher.verify("nope", salt, hasher.hash("pw123", salt)))
    }

    @Test fun differentSalts_giveDifferentHashes() {
        assertNotEquals(
            hasher.hash("pw123", hasher.generateSalt()),
            hasher.hash("pw123", hasher.generateSalt())
        )
    }
}
