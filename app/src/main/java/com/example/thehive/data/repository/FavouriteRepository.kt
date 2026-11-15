package com.example.thehive.data.repository

import com.example.thehive.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

interface FavouriteRepository {
    suspend fun add(userId: Long, listingId: Long)
    suspend fun remove(userId: Long, listingId: Long)
    /** Adds if missing, removes if present. */
    suspend fun toggle(userId: Long, listingId: Long, currentlyFavourited: Boolean)
    fun isFavourited(userId: Long, listingId: Long): Flow<Boolean>
    fun getFavourites(userId: Long): Flow<List<ListingEntity>>
}
