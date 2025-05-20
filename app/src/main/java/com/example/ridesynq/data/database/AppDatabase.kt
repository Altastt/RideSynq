package com.example.ridesynq.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.ridesynq.data.dao.CompanyDao
import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.dao.UserDao
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.dao.UserTripDao
import com.example.ridesynq.data.entities.Post // Убедитесь, что Post используется или удалите
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.entities.UserTrip // Убедитесь, что UserTrip используется или удалите

@Database(
    entities = [
        Company::class,
        User::class,
        Trip::class,
        UserTrip::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(RoomConverters::class) // Убедитесь, что RoomConverters существуют и нужны
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun companyDao(): CompanyDao
    abstract fun tripDao(): TripDao
    abstract fun userTripDao(): UserTripDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                Log.d("DB_MIGRATION", "Starting MIGRATION_5_6")
                // Шаг 1: Создаем новую таблицу
                db.execSQL("""
                CREATE TABLE trips_new (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    company_id INTEGER NOT NULL,
                    datetime INTEGER NOT NULL,
                    start_latitude REAL NOT NULL,
                    start_longitude REAL NOT NULL,
                    end_latitude REAL NOT NULL,
                    end_longitude REAL NOT NULL,
                    is_to_work INTEGER NOT NULL,
                    driver_id INTEGER,
                    seats_available INTEGER,
                    price_per_seat REAL,
                    status TEXT NOT NULL DEFAULT 'pending',
                    activated_at INTEGER DEFAULT NULL,
                    FOREIGN KEY(company_id) REFERENCES companies(id) ON DELETE CASCADE,
                    FOREIGN KEY(driver_id) REFERENCES users(id) ON DELETE SET NULL
                )
            """)
                Log.d("DB_MIGRATION", "MIGRATION_5_6: trips_new created")

                // Шаг 2: Копируем данные
                db.execSQL("""
                INSERT INTO trips_new (id, company_id, datetime, start_latitude, start_longitude, end_latitude, end_longitude, is_to_work, driver_id, seats_available, price_per_seat, status)
                SELECT id, company_id, datetime, start_latitude, start_longitude, end_latitude, end_longitude, is_to_work, driver_id, seats_available, price_per_seat, status
                FROM trips
            """)
                Log.d("DB_MIGRATION", "MIGRATION_5_6: Data copied to trips_new")

                // Шаг 3: Удаляем старую таблицу
                db.execSQL("DROP TABLE trips")
                Log.d("DB_MIGRATION", "MIGRATION_5_6: Old trips table dropped")

                // Шаг 4: Переименовываем
                db.execSQL("ALTER TABLE trips_new RENAME TO trips")
                Log.d("DB_MIGRATION", "MIGRATION_5_6: trips_new renamed to trips")

                // Шаг 5: Воссоздаем индексы
                db.execSQL("CREATE INDEX IF NOT EXISTS index_trips_company_id ON trips(company_id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_trips_driver_id ON trips(driver_id)")
                Log.d("DB_MIGRATION", "MIGRATION_5_6: Indexes recreated. Migration finished.")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addMigrations(MIGRATION_5_6)
                    .fallbackToDestructiveMigration() // Оставляем на всякий случай
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Этот метод вызывается, если БД создается с нуля (например, после удаления приложения
                            // или если нет существующей БД для миграции)
                            Log.d("DB_LIFECYCLE", "onCreate CALLED. Room will create tables based on Entities.")
                            // Room автоматически создаст таблицы на основе аннотаций в ваших Entity,
                            // включая все ForeignKey, если они правильно определены.
                        }
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            Log.d("DB_LIFECYCLE", "onOpen CALLED. DB Version: ${db.version}")
                        }
                        override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                            super.onDestructiveMigration(db)
                            Log.w("DB_LIFECYCLE", "onDestructiveMigration CALLED.")
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}