package com.example.thehive.data.repository

import com.example.thehive.data.local.dao.UserDao
import com.example.thehive.data.local.entity.UserEntity
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.util.PasswordHasher

class UserRepositoryImpl(
    private val dao: UserDao,
    private val hasher: PasswordHasher = PasswordHasher()
) : UserRepository {

    override suspend fun register(
        fullName: String, email: String, phone: String, password: String, role: UserRole
    ): Result<Long> {
        val cleanEmail = email.trim().lowercase()
        if (dao.findByEmail(cleanEmail) != null)
            return Result.failure(IllegalArgumentException("Email already registered"))
        val salt = hasher.generateSalt()
        val user = UserEntity(
            fullName = fullName.trim(),
            email = cleanEmail,
            phone = phone.trim(),
            role = role,
            passwordHash = hasher.hash(password, salt),
            salt = salt
        )
        return Result.success(dao.insert(user))
    }

    override suspend fun login(email: String, password: String): Result<UserEntity> {
        val user = dao.findByEmail(email.trim().lowercase())
            ?: return Result.failure(IllegalArgumentException("Invalid email or password"))
        return if (hasher.verify(password, user.salt, user.passwordHash))
            Result.success(user)
        else Result.failure(IllegalArgumentException("Invalid email or password"))
    }

    override suspend fun findById(id: Long) = dao.findById(id)
    override suspend fun findByEmail(email: String) = dao.findByEmail(email.trim().lowercase())
    override suspend fun update(user: UserEntity) = dao.update(user) > 0
}
