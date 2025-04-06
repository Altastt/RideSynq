package com.example.ridesynq.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ridesynq.data.dao.CompanyDao
import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.dao.UserDao
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.entities.Post // Убедитесь, что Post используется или удалите
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.entities.UserTrip // Убедитесь, что UserTrip используется или удалите

@Database(
    entities = [
        Company::class,
        // Post::class, // Если Post не используется, уберите
        User::class,
        Trip::class, // Если Trip не используется, уберите
        // UserTrip::class // Если UserTrip не используется, уберите
    ],
    version = 2,
    exportSchema = false // Можно установить true и изучить генерируемую схему
)
@TypeConverters(RoomConverters::class) // Убедитесь, что RoomConverters существуют и нужны
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun companyDao(): CompanyDao
    // abstract fun tripDao(): TripDao // Если TripDao не используется, уберите

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
                    // Добавляем или подтверждаем наличие этой строки:
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Log.d("DB", "Database created (or recreated after destructive migration)")
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("DB", "Database opened")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}