package com.example.thehive.data.repository

import com.example.thehive.data.local.dao.ListingDao
import com.example.thehive.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit

class ListingRepositoryImpl(private val dao: ListingDao) : ListingRepository {
    override suspend fun add(listing: ListingEntity) = dao.insert(listing)
    override suspend fun update(listing: ListingEntity) = dao.update(listing) > 0
    override suspend fun delete(listing: ListingEntity) = dao.delete(listing) > 0
    override fun getById(id: Long): Flow<ListingEntity?> = dao.getById(id)
    override fun getAll(): Flow<List<ListingEntity>> = dao.getAll()
    override fun getByOwner(ownerId: Long): Flow<List<ListingEntity>> = dao.getByOwner(ownerId)

    override fun search(
        location: String?, minRent: Double, maxRent: Double, bedrooms: Int?, includeTaken: Boolean
    ): Flow<List<ListingEntity>> =
        dao.search(location?.trim()?.takeIf { it.isNotEmpty() }, minRent, maxRent, bedrooms, includeTaken)

    override suspend fun setAvailability(id: Long, available: Boolean) {
        dao.setAvailability(id, available, System.currentTimeMillis())
    }

    override suspend fun confirmStillAvailable(id: Long) {
        dao.confirmStillAvailable(id, System.currentTimeMillis())
    }

    override suspend fun getStaleListings(ownerId: Long, days: Int): List<ListingEntity> {
        val cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(days.toLong())
        return dao.getStaleListings(ownerId, cutoff)
    }

    override suspend fun isDuplicate(title: String, location: String, excludeId: Long) =
        dao.countDuplicates(title.trim(), location.trim(), excludeId) > 0
}
