package com.example.thehive.data.repository

import com.example.thehive.data.local.entity.UserEntity
import com.example.thehive.data.local.entity.UserRole

interface UserRepository {
    suspend fun register(
        fullName: String, email: String, phone: String, password: String,
        role: UserRole = UserRole.HOUSE_HUNTER
    ): Result<Long>
    suspend fun login(email: String, password: String): Result<UserEntity>
    suspend fun findById(id: Long): UserEntity?
    suspend fun findByEmail(email: String): UserEntity?
    suspend fun update(user: UserEntity): Boolean
}
