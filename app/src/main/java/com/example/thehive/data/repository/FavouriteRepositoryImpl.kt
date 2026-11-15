package com.example.thehive.data.repository

import com.example.thehive.data.local.dao.FavouriteDao
import com.example.thehive.data.local.entity.FavouriteEntity
import com.example.thehive.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

class FavouriteRepositoryImpl(private val dao: FavouriteDao) : FavouriteRepository {
    override suspend fun add(userId: Long, listingId: Long) {
        dao.add(FavouriteEntity(userId = userId, listingId = listingId))
    }
    override suspend fun remove(userId: Long, listingId: Long) {
        dao.remove(userId, listingId)
    }
    override suspend fun toggle(userId: Long, listingId: Long, currentlyFavourited: Boolean) {
        if (currentlyFavourited) remove(userId, listingId) else add(userId, listingId)
    }
    override fun isFavourited(userId: Long, listingId: Long): Flow<Boolean> =
        dao.isFavourited(userId, listingId)
    override fun getFavourites(userId: Long): Flow<List<ListingEntity>> =
        dao.getFavouriteListings(userId)
}
