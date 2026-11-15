package com.example.thehive.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class UserRole { LANDLORD, HOUSE_HUNTER }

@Entity(tableName = "users", indices = [Index(value = ["email"], unique = true)])
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: UserRole = UserRole.HOUSE_HUNTER,
    @ColumnInfo(name = "password_hash") val passwordHash: String,
    val salt: String,
    val createdAt: Long = System.currentTimeMillis()
)
