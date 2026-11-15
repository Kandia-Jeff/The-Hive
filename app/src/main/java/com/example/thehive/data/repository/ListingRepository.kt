package com.example.thehive.data.repository

import com.example.thehive.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

interface ListingRepository {
    suspend fun add(listing: ListingEntity): Long
    suspend fun update(listing: ListingEntity): Boolean
    suspend fun delete(listing: ListingEntity): Boolean
    fun getById(id: Long): Flow<ListingEntity?>
    fun getAll(): Flow<List<ListingEntity>>
    fun getByOwner(ownerId: Long): Flow<List<ListingEntity>>

    /** Taken listings are hidden unless includeTaken = true. */
    fun search(
        location: String? = null,
        minRent: Double = 0.0,
        maxRent: Double = Double.MAX_VALUE,
        bedrooms: Int? = null,
        includeTaken: Boolean = false
    ): Flow<List<ListingEntity>>

    suspend fun setAvailability(id: Long, available: Boolean)
    suspend fun confirmStillAvailable(id: Long)
    /** For the WorkManager reminder: listings not confirmed in the last [days] days. */
    suspend fun getStaleListings(ownerId: Long, days: Int): List<ListingEntity>

    suspend fun isDuplicate(title: String, location: String, excludeId: Long = 0): Boolean
}
