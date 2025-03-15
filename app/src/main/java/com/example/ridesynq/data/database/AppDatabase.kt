package com.example.ridesynq.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.ridesynq.data.dao.CompanyDao
import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.dao.UserDao
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.entities.Post
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.entities.UserTrip

@Database(
    entities = [
        Company::class,
        Post::class,
        User::class,
        Trip::class,
        UserTrip::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(RoomConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun companyDao(): CompanyDao
    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}