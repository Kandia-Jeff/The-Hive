package com.example.thehive

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ListingDaoTest : DaoTestBase() {

    private val max = Double.MAX_VALUE

    @Test fun insertAndGetById_roundTripsAmenities() = runBlocking {
        val uid = db.userDao().insert(user())
        val id = db.listingDao().insert(listing(uid))
        assertEquals(listOf("WiFi", "Parking"), db.listingDao().getById(id).first()?.amenities)
    }

    @Test fun updateAndDelete() = runBlocking {
        val uid = db.userDao().insert(user())
        val id = db.listingDao().insert(listing(uid))
        val saved = db.listingDao().getById(id).first()!!
        db.listingDao().update(saved.copy(title = "Updated"))
        assertEquals("Updated", db.listingDao().getById(id).first()?.title)
        db.listingDao().delete(saved)
        assertNull(db.listingDao().getById(id).first())
    }

    @Test fun getAllAndGetByOwner() = runBlocking {
        val u1 = db.userDao().insert(user("a@test.com"))
        val u2 = db.userDao().insert(user("b@test.com"))
        db.listingDao().insert(listing(u1, title = "A1"))
        db.listingDao().insert(listing(u1, title = "A2"))
        db.listingDao().insert(listing(u2, title = "B1"))
        assertEquals(3, db.listingDao().getAll().first().size)
        assertEquals(2, db.listingDao().getByOwner(u1).first().size)
    }

    @Test fun search_byLocationRentAndBedrooms() = runBlocking {
        val uid = db.userDao().insert(user())
        db.listingDao().insert(listing(uid, "Cheap", "Kilimani", 20_000.0, 1))
        db.listingDao().insert(listing(uid, "Mid", "Westlands", 50_000.0, 2))
        db.listingDao().insert(listing(uid, "Pricey", "Karen", 150_000.0, 3))
        val dao = db.listingDao()
        assertEquals(1, dao.search("west", 0.0, max, null, false).first().size)
        assertEquals(2, dao.search(null, 10_000.0, 60_000.0, null, false).first().size)
        assertEquals(1, dao.search(null, 0.0, max, 3, false).first().size)
        assertEquals(3, dao.search(null, 0.0, max, null, false).first().size)
        assertEquals(0, dao.search("Karen", 0.0, 50_000.0, null, false).first().size)
    }

    @Test fun search_hidesTakenListingsByDefault() = runBlocking {
        val uid = db.userDao().insert(user())
        db.listingDao().insert(listing(uid, "Open", isAvailable = true))
        db.listingDao().insert(listing(uid, "Gone", isAvailable = false))
        assertEquals(1, db.listingDao().search(null, 0.0, max, null, false).first().size)
        assertEquals(2, db.listingDao().search(null, 0.0, max, null, true).first().size)
    }

    @Test fun setAvailability_updatesFlagAndTimestamp() = runBlocking {
        val uid = db.userDao().insert(user())
        val id = db.listingDao().insert(listing(uid, lastConfirmedAt = 1L))
        db.listingDao().setAvailability(id, false, 5_000L)
        val l = db.listingDao().getById(id).first()!!
        assertFalse(l.isAvailable)
        assertEquals(5_000L, l.lastConfirmedAt)
    }

    @Test fun getStaleListings_returnsOnlyOldAvailableListingsOfOwner() = runBlocking {
        val u1 = db.userDao().insert(user("a@test.com"))
        val u2 = db.userDao().insert(user("b@test.com"))
        val dao = db.listingDao()
        dao.insert(listing(u1, "Old", lastConfirmedAt = 100L))
        dao.insert(listing(u1, "Fresh", lastConfirmedAt = 10_000L))
        dao.insert(listing(u1, "OldButTaken", lastConfirmedAt = 100L, isAvailable = false))
        dao.insert(listing(u2, "OtherOwner", lastConfirmedAt = 100L))
        val stale = dao.getStaleListings(u1, olderThan = 5_000L)
        assertEquals(listOf("Old"), stale.map { it.title })
    }

    @Test fun confirmStillAvailable_removesListingFromStaleList() = runBlocking {
        val uid = db.userDao().insert(user())
        val id = db.listingDao().insert(listing(uid, lastConfirmedAt = 100L))
        db.listingDao().confirmStillAvailable(id, now = 9_000L)
        assertTrue(db.listingDao().getStaleListings(uid, 5_000L).isEmpty())
    }

    @Test fun deletingOwnerCascadesToListings() = runBlocking {
        val uid = db.userDao().insert(user())
        db.listingDao().insert(listing(uid))
        db.openHelper.writableDatabase.execSQL("DELETE FROM users WHERE id = $uid")
        assertTrue(db.listingDao().getAll().first().isEmpty())
    }

    @Test fun countDuplicates_isCaseInsensitive_andCanExcludeSelf() = runBlocking {
        val uid = db.userDao().insert(user())
        val id = db.listingDao().insert(listing(uid, "Nice Flat", "Kilimani"))
        assertEquals(1, db.listingDao().countDuplicates("nice flat", "KILIMANI", 0))
        assertEquals(0, db.listingDao().countDuplicates("nice flat", "KILIMANI", id))
    }
}
