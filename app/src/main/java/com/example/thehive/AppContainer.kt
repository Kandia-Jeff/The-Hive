package com.example.thehive

import android.content.Context
import com.example.thehive.data.local.AppDatabase
import com.example.thehive.data.repository.FavouriteRepository
import com.example.thehive.data.repository.FavouriteRepositoryImpl
import com.example.thehive.data.repository.ListingRepository
import com.example.thehive.data.repository.ListingRepositoryImpl
import com.example.thehive.data.repository.UserRepository
import com.example.thehive.data.repository.UserRepositoryImpl
import com.example.thehive.util.SessionManager

class AppContainer(context: Context) {
    private val db = AppDatabase.getInstance(context)

    val userRepository: UserRepository = UserRepositoryImpl(db.userDao())
    val listingRepository: ListingRepository = ListingRepositoryImpl(db.listingDao())
    val favouriteRepository: FavouriteRepository = FavouriteRepositoryImpl(db.favouriteDao())
    val sessionManager = SessionManager(context)
}