package com.example.ridesynq.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.relations.TripWithUsers
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {
    @Insert
    suspend fun insert(trip: Trip)

    @Transaction
    @Query("SELECT * FROM trips WHERE company_id = :companyId")
    fun getTripsByCompany(companyId: Int): Flow<List<TripWithUsers>>

    @Delete
    suspend fun delete(trip: Trip)
}