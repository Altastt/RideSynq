package com.example.ridesynq.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.ridesynq.data.entities.UserTrip
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTripDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(userTrip: UserTrip)

    // В UserTripDao.kt
    @Query("SELECT * FROM user_trip WHERE user_id = :userId AND trip_id = :tripId")
    fun getUserTrip(userId: Int, tripId: Int): Flow<UserTrip?> // или suspend fun ... : UserTrip?

    @Query("UPDATE user_trip SET role = :newRole WHERE user_id = :userId AND trip_id = :tripId")
    suspend fun updateUserRoleInTrip(userId: Int, tripId: Int, newRole: String)
}