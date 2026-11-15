package com.example.thehive.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "listings",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["ownerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerId"), Index("location")]
)
data class ListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val title: String,
    val description: String = "",
    val location: String,
    val rent: Double,
    val bedrooms: Int,
    val bathrooms: Int = 1,
    val contactName: String = "",
    val contactPhone: String,
    val contactEmail: String,
    val amenities: List<String> = emptyList(),   // handled by Converters
    val imageUri: String? = null,
    val isAvailable: Boolean = true,              // false = "Taken"
    val lastConfirmedAt: Long = System.currentTimeMillis(), // used by the WorkManager reminder
    val createdAt: Long = System.currentTimeMillis()
)
