package com.example.thehive

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.data.repository.FavouriteRepositoryImpl
import com.example.thehive.data.repository.ListingRepositoryImpl
import com.example.thehive.data.repository.UserRepositoryImpl
import com.example.thehive.util.SeedData
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RepositoryTest : DaoTestBase() {

    private val users get() = UserRepositoryImpl(db.userDao())
    private val listings get() = ListingRepositoryImpl(db.listingDao())
    private val favourites get() = FavouriteRepositoryImpl(db.favouriteDao())

    @Test fun register_thenLogin_succeeds_andKeepsRole() = runBlocking {
        users.register("Jane", "Jane@Test.com", "0712345678", "secret123", UserRole.LANDLORD)
        val result = users.login("jane@test.com", "secret123")
        assertTrue(result.isSuccess)
        assertEquals(UserRole.LANDLORD, result.getOrThrow().role)
    }

    @Test fun login_withWrongPassword_fails() = runBlocking {
        users.register("Jane", "jane@test.com", "0712345678", "secret123")
        assertTrue(users.login("jane@test.com", "wrong").isFailure)
    }

    @Test fun login_unknownEmail_fails() = runBlocking {
        assertTrue(users.login("ghost@test.com", "pw").isFailure)
    }

    @Test fun register_duplicateEmail_fails() = runBlocking {
        users.register("Jane", "jane@test.com", "0712345678", "secret123")
        assertTrue(users.register("Jane2", "jane@test.com", "0722222222", "pw").isFailure)
    }

    @Test fun password_isNotStoredInPlainText() = runBlocking {
        users.register("Jane", "jane@test.com", "0712345678", "secret123")
        assertNotEquals("secret123", users.findByEmail("jane@test.com")?.passwordHash)
    }

    @Test fun listingRepository_addSearchAndDuplicateCheck() = runBlocking {
        val uid = users.register("J", "j@test.com", "0712345678", "pw").getOrThrow()
        listings.add(listing(uid, "Sunny Flat", "Kilimani", 30_000.0, 2))
        assertTrue(listings.isDuplicate("sunny flat", "kilimani"))
        assertFalse(listings.isDuplicate("Other", "kilimani"))
        assertEquals(1, listings.search(location = "kili", bedrooms = 2).first().size)
        assertEquals(1, listings.search(location = "   ").first().size) // blank = no filter
    }

    @Test fun listingRepository_availabilityAndStaleReminder() = runBlocking {
        val uid = users.register("J", "j@test.com", "0712345678", "pw").getOrThrow()
        val oldTime = System.currentTimeMillis() - 40L * 24 * 60 * 60 * 1000
        val id = listings.add(listing(uid, lastConfirmedAt = oldTime))

        assertEquals(1, listings.getStaleListings(uid, days = 30).size)
        listings.confirmStillAvailable(id)
        assertTrue(listings.getStaleListings(uid, days = 30).isEmpty())

        listings.setAvailability(id, false)
        assertTrue(listings.search().first().isEmpty())                 // hidden by default
        assertEquals(1, listings.search(includeTaken = true).first().size)
    }

    @Test fun favouriteRepository_toggle() = runBlocking {
        val uid = users.register("J", "j@test.com", "0712345678", "pw").getOrThrow()
        val lid = listings.add(listing(uid))
        favourites.toggle(uid, lid, currentlyFavourited = false)
        assertTrue(favourites.isFavourited(uid, lid).first())
        favourites.toggle(uid, lid, currentlyFavourited = true)
        assertFalse(favourites.isFavourited(uid, lid).first())
    }

    @Test fun seedData_runsOnlyOnce_andCanLogInAsDemoLandlord() = runBlocking {
        SeedData.seedIfEmpty(users, listings)
        val first = listings.getAll().first().size
        assertTrue(first > 0)
        SeedData.seedIfEmpty(users, listings)
        assertEquals(first, listings.getAll().first().size)
        assertTrue(users.login(SeedData.DEMO_EMAIL, SeedData.DEMO_PASSWORD).isSuccess)
    }
}
