package com.example.ridesynq.data.dao

import androidx.room.Dao
import androidx.room.Delete // Не используется в новой логике, но оставлю если нужен
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


    @Transaction
    @Query("SELECT * FROM trips WHERE status != 'finished' ORDER BY datetime ASC") // Убрал datetime >= :currentTimeMillis, т.к. ViewModel сделает это
    fun getAllActiveAndFutureTripsWithUsers(): Flow<List<TripWithUsers>>


    @Transaction
    @Query("SELECT * FROM trips WHERE driver_id = :driverId AND status != 'finished' ORDER BY datetime DESC")
    fun getTripsByDriver(driverId: Int): Flow<List<TripWithUsers>>

    @Transaction
    @Query("""
        SELECT trips.* FROM trips
        INNER JOIN user_trip ON trips.id = user_trip.trip_id
        WHERE user_trip.user_id = :userId AND user_trip.role = 'passenger' AND trips.status != 'finished'
        ORDER BY trips.datetime DESC
    """)
    fun getPassengerTripsForUser(userId: Int): Flow<List<TripWithUsers>>

    // Ваш метод getTripById возвращает Trip?, а не Flow<Trip?>. Это нормально для suspend функций.
    @Query("SELECT * FROM trips WHERE id = :tripId")
    suspend fun getTripById(tripId: Int): Trip? // Изменено на suspend и возвращает Trip?

    @Insert // Ваш существующий метод insert, если нужен отдельно от insertTripAndGetId
    suspend fun insert(trip: Trip)

    @Delete // Ваш существующий метод delete
    suspend fun delete(trip: Trip)

    @Transaction
    @Query("SELECT * FROM trips WHERE company_id = :companyId AND status != 'finished' ORDER BY datetime DESC") // Добавил AND status != 'finished' для консистентности
    fun getTripsByCompany(companyId: Int): Flow<List<TripWithUsers>>

    @Query("UPDATE trips SET status = :status WHERE id = :tripId")
    suspend fun updateTripStatus(tripId: Int, status: String)

    @Query("UPDATE trips SET seats_available = :seatsAvailable, status = :status, activated_at = :activatedAt WHERE id = :tripId")
    suspend fun updateTripSeatsAndStatus(tripId: Int, seatsAvailable: Int, status: String, activatedAt: Long?)

    @Query("UPDATE trips SET driver_id = :driverId, status = :status, activated_at = :activatedAt WHERE id = :tripId")
    suspend fun assignDriverToTrip(tripId: Int, driverId: Int, status: String, activatedAt: Long?)



}