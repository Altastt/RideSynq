package com.example.ridesynq.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.relations.TripWithUsers
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTripAndGetId(trip: Trip): Long

    @Transaction // Для загрузки Trip вместе с User (через UserTrip)
    @Query("SELECT * FROM trips WHERE company_id = :companyId ORDER BY datetime DESC")
    fun getTripsByCompany(companyId: Int): Flow<List<TripWithUsers>> // TripWithUsers - для примера, если такая связь нужна

    // Пример запроса для TripScreen: все активные поездки
    @Transaction
    @Query("SELECT * FROM trips WHERE status = 'pending' OR status = 'confirmed' ORDER BY datetime ASC")
    fun getAllActiveTripsWithUsers(): Flow<List<TripWithUsers>> // Или просто Flow<List<Trip>>

    @Transaction
    @Query("SELECT * FROM trips WHERE status = 'pending' OR status = 'confirmed' ORDER BY datetime ASC")
    fun getAllActiveAndFutureTripsWithUsers(): Flow<List<TripWithUsers>>

    // Запрос для поездок конкретного водителя
    @Transaction
    @Query("SELECT * FROM trips WHERE driver_id = :driverId ORDER BY datetime DESC")
    fun getTripsByDriver(driverId: Int): Flow<List<TripWithUsers>>

    // Запрос для получения поездок, в которых участвует пользователь (как пассажир)
    // Это более сложный запрос, т.к. нужно соединить с UserTrip
    @Transaction
    @Query("""
        SELECT trips.* FROM trips
        INNER JOIN user_trip ON trips.id = user_trip.trip_id
        WHERE user_trip.user_id = :userId AND user_trip.role = 'passenger'
        ORDER BY trips.datetime DESC
    """)
    fun getPassengerTripsForUser(userId: Int): Flow<List<TripWithUsers>> // Или Flow<List<Trip>>



    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Int): Trip?
    @Insert
    suspend fun insert(trip: Trip)

    @Delete
    suspend fun delete(trip: Trip)
}