package com.example.thehive

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.thehive.data.local.entity.FavouriteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteDaoTest : DaoTestBase() {

    @Test fun addCheckAndRemove() = runBlocking {
        val uid = db.userDao().insert(user())
        val lid = db.listingDao().insert(listing(uid))
        val dao = db.favouriteDao()

        assertFalse(dao.isFavourited(uid, lid).first())
        dao.add(FavouriteEntity(userId = uid, listingId = lid))
        assertTrue(dao.isFavourited(uid, lid).first())
        dao.remove(uid, lid)
        assertFalse(dao.isFavourited(uid, lid).first())
    }

    @Test fun addingTwiceDoesNotDuplicate() = runBlocking {
        val uid = db.userDao().insert(user())
        val lid = db.listingDao().insert(listing(uid))
        val dao = db.favouriteDao()
        dao.add(FavouriteEntity(userId = uid, listingId = lid))
        dao.add(FavouriteEntity(userId = uid, listingId = lid))
        assertEquals(1, dao.getFavouriteListings(uid).first().size)
    }

    @Test fun favouritesAreJoinedWithListings() = runBlocking {
        val uid = db.userDao().insert(user())
        val l1 = db.listingDao().insert(listing(uid, title = "One"))
        db.listingDao().insert(listing(uid, title = "Two"))
        db.favouriteDao().add(FavouriteEntity(userId = uid, listingId = l1))
        val favs = db.favouriteDao().getFavouriteListings(uid).first()
        assertEquals(listOf("One"), favs.map { it.title })
    }

    @Test fun deletingListingCascadesToFavourites() = runBlocking {
        val uid = db.userDao().insert(user())
        val lid = db.listingDao().insert(listing(uid))
        db.favouriteDao().add(FavouriteEntity(userId = uid, listingId = lid))
        db.listingDao().delete(db.listingDao().getById(lid).first()!!)
        assertTrue(db.favouriteDao().getFavouriteListings(uid).first().isEmpty())
    }
}
