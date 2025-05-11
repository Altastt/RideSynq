package com.example.ridesynq.data.repositories

import com.example.ridesynq.data.dao.TripDao
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.relations.TripWithUsers
import kotlinx.coroutines.flow.Flow

class TripRepository(private val tripDao: TripDao) {

    suspend fun insertTripAndGetId(trip: Trip): Long {
        return tripDao.insertTripAndGetId(trip)
    }

    fun getTripsByCompany(companyId: Int): Flow<List<TripWithUsers>> {
        return tripDao.getTripsByCompany(companyId)
    }

    fun getAllActiveTripsWithUsers(): Flow<List<TripWithUsers>> {
        return tripDao.getAllActiveTripsWithUsers()
    }

    fun getTripsByDriver(driverId: Int): Flow<List<TripWithUsers>> {
        return tripDao.getTripsByDriver(driverId)
    }

    fun getPassengerTripsForUser(userId: Int): Flow<List<TripWithUsers>> {
        return tripDao.getPassengerTripsForUser(userId)
    }

    suspend fun deleteTrip(trip: Trip) {
        tripDao.delete(trip)
    }

    suspend fun getTripById(tripId: Int): Trip? {
        return tripDao.getTripById(tripId)
    }
}