package com.example.thehive.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.thehive.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    @Insert
    suspend fun insert(listing: ListingEntity): Long

    @Update
    suspend fun update(listing: ListingEntity): Int

    @Delete
    suspend fun delete(listing: ListingEntity): Int

    @Query("SELECT * FROM listings WHERE id = :id")
    fun getById(id: Long): Flow<ListingEntity?>

    @Query("SELECT * FROM listings ORDER BY createdAt DESC")
    fun getAll(): Flow<List<ListingEntity>>

    @Query("SELECT * FROM listings WHERE ownerId = :ownerId ORDER BY createdAt DESC")
    fun getByOwner(ownerId: Long): Flow<List<ListingEntity>>

    /**
     * Pass null for location / bedrooms to ignore that filter.
     * Pass 0 and Double.MAX_VALUE for min/max rent to ignore rent.
     * includeTaken = false hides listings marked as taken (the Browse default).
     */
    @Query(
        """
        SELECT * FROM listings
        WHERE (:location IS NULL OR location LIKE '%' || :location || '%')
          AND rent BETWEEN :minRent AND :maxRent
          AND (:bedrooms IS NULL OR bedrooms = :bedrooms)
          AND (:includeTaken = 1 OR isAvailable = 1)
        ORDER BY createdAt DESC
        """
    )
    fun search(
        location: String?,
        minRent: Double,
        maxRent: Double,
        bedrooms: Int?,
        includeTaken: Boolean
    ): Flow<List<ListingEntity>>

    // ---- availability (My Listings switch + WorkManager reminder actions) ----
    @Query("UPDATE listings SET isAvailable = :available, lastConfirmedAt = :now WHERE id = :id")
    suspend fun setAvailability(id: Long, available: Boolean, now: Long): Int

    @Query("UPDATE listings SET isAvailable = 1, lastConfirmedAt = :now WHERE id = :id")
    suspend fun confirmStillAvailable(id: Long, now: Long): Int

    /** Available listings of this landlord not confirmed since [olderThan] (epoch millis). */
    @Query(
        """
        SELECT * FROM listings
        WHERE ownerId = :ownerId AND isAvailable = 1 AND lastConfirmedAt < :olderThan
        ORDER BY lastConfirmedAt ASC
        """
    )
    suspend fun getStaleListings(ownerId: Long, olderThan: Long): List<ListingEntity>

    // ---- duplicate check ----
    @Query(
        """
        SELECT COUNT(*) FROM listings
        WHERE LOWER(title) = LOWER(:title) AND LOWER(location) = LOWER(:location)
          AND id != :excludeId
        """
    )
    suspend fun countDuplicates(title: String, location: String, excludeId: Long): Int

    @Query("SELECT COUNT(*) FROM listings")
    suspend fun count(): Int
}
