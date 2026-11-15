package com.example.thehive.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.thehive.data.local.entity.FavouriteEntity
import com.example.thehive.data.local.entity.ListingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun add(favourite: FavouriteEntity): Long

    @Query("DELETE FROM favourites WHERE userId = :userId AND listingId = :listingId")
    suspend fun remove(userId: Long, listingId: Long): Int

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE userId = :userId AND listingId = :listingId)")
    fun isFavourited(userId: Long, listingId: Long): Flow<Boolean>

    @Query(
        """
        SELECT l.* FROM listings l
        INNER JOIN favourites f ON f.listingId = l.id
        WHERE f.userId = :userId
        ORDER BY f.createdAt DESC
        """
    )
    fun getFavouriteListings(userId: Long): Flow<List<ListingEntity>>
}
