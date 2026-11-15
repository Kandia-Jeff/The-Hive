package com.example.thehive.util

import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.data.repository.ListingRepository
import com.example.thehive.data.repository.UserRepository
import kotlinx.coroutines.flow.first

/** Call once at startup (e.g. HiveApplication, in a coroutine). Does nothing if data exists. */
object SeedData {
    const val DEMO_EMAIL = "landlord@demo.com"
    const val DEMO_PASSWORD = "Password123"

    suspend fun seedIfEmpty(users: UserRepository, listings: ListingRepository) {
        if (listings.getAll().first().isNotEmpty()) return

        val ownerId = users.findByEmail(DEMO_EMAIL)?.id
            ?: users.register("Demo Landlord", DEMO_EMAIL, "0712345678", DEMO_PASSWORD, UserRole.LANDLORD)
                .getOrThrow()

        sample(ownerId).forEach { listings.add(it) }
    }

    private fun home(
        ownerId: Long, title: String, location: String, rent: Double, bedrooms: Int,
        bathrooms: Int, description: String, amenities: List<String>, available: Boolean = true
    ) = ListingEntity(
        ownerId = ownerId, title = title, location = location, rent = rent,
        bedrooms = bedrooms, bathrooms = bathrooms, description = description,
        contactName = "Demo Landlord", contactPhone = "0712345678", contactEmail = DEMO_EMAIL,
        amenities = amenities, isAvailable = available
    )

    private fun sample(ownerId: Long) = listOf(
        home(ownerId, "Modern Studio", "Kilimani, Nairobi", 25_000.0, 0, 1,
            "Bright studio near Yaya Centre.", listOf("WiFi", "Parking", "Security")),
        home(ownerId, "Cozy 1BR Apartment", "Westlands, Nairobi", 45_000.0, 1, 1,
            "Walking distance to Sarit Centre.", listOf("Gym", "Backup Generator", "Balcony")),
        home(ownerId, "Spacious 2BR Flat", "Lavington, Nairobi", 70_000.0, 2, 2,
            "Quiet, family-friendly compound.", listOf("Parking", "Garden", "Borehole")),
        home(ownerId, "Student Bedsitter", "Madaraka, Nairobi", 12_000.0, 0, 1,
            "Close to Strathmore University.", listOf("WiFi", "Water 24/7")),
        home(ownerId, "Family 3BR Maisonette", "Karen, Nairobi", 120_000.0, 3, 3,
            "Gated community with a large garden.", listOf("Parking", "Garden", "Security", "DSQ")),
        // one taken listing so the "available / taken" badge can be demoed
        home(ownerId, "Executive 2BR Penthouse", "Kileleshwa, Nairobi", 95_000.0, 2, 2,
            "Rooftop views, already let.", listOf("Lift", "Gym"), available = false)
    )
}
