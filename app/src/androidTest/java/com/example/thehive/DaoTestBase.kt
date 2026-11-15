package com.example.thehive

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.thehive.data.local.AppDatabase
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.local.entity.UserEntity
import org.junit.After
import org.junit.Before

abstract class DaoTestBase {
    protected lateinit var db: AppDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun closeDb() = db.close()

    protected fun user(email: String = "a@test.com") = UserEntity(
        fullName = "Test User", email = email, phone = "0712345678",
        passwordHash = "hash", salt = "salt"
    )

    protected fun listing(
        ownerId: Long, title: String = "Nice Flat", location: String = "Kilimani",
        rent: Double = 30_000.0, bedrooms: Int = 1, isAvailable: Boolean = true,
        lastConfirmedAt: Long = System.currentTimeMillis()
    ) = ListingEntity(
        ownerId = ownerId, title = title, location = location, rent = rent, bedrooms = bedrooms,
        isAvailable = isAvailable, lastConfirmedAt = lastConfirmedAt, contactName = "Owner",
        contactPhone = "0712345678", contactEmail = "a@test.com",
        amenities = listOf("WiFi", "Parking")
    )
}
