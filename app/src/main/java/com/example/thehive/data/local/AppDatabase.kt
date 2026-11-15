package com.example.thehive.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.thehive.data.local.dao.FavouriteDao
import com.example.thehive.data.local.dao.ListingDao
import com.example.thehive.data.local.dao.UserDao
import com.example.thehive.data.local.entity.FavouriteEntity
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.local.entity.UserEntity

@Database(
    entities = [UserEntity::class, ListingEntity::class, FavouriteEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun listingDao(): ListingDao
    abstract fun favouriteDao(): FavouriteDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "property_app.db"
                ).build().also { INSTANCE = it }
            }
    }
}
