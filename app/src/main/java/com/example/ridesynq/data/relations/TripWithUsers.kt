package com.example.ridesynq.data.relations

import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Junction
import androidx.room.Relation
import com.example.ridesynq.data.entities.Trip
import com.example.ridesynq.data.entities.User
import com.example.ridesynq.data.entities.UserTrip


data class TripWithUsers(
    @Embedded
    var trip: Trip,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = UserTrip::class,
            parentColumn = "trip_id",
            entityColumn = "user_id"
        )
    )
    var users: List<User> // Сделаем var на случай, если захотите изменять из ViewModel
) {
    // Эти поля будут полностью игнорироваться Room при чтении/записи из/в БД.
    // Они не будут частью конструктора, который использует Room.
    // Инициализируем их значениями по умолчанию здесь.
    @Ignore
    var canCurrentUserJoin: Boolean = false
    @Ignore
    var canCurrentUserTakeRide: Boolean = false
    @Ignore
    var isCurrentUserParticipant: Boolean = false

    // Опционально: Вторичный конструктор для вашего удобства, если вы создаете
    // экземпляры TripWithUsers вручную и хотите сразу задать эти флаги.
    @Ignore
    constructor(
        trip: Trip,
        users: List<User>,
        canJoin: Boolean,
        canTake: Boolean,
        isParticipant: Boolean
    ) : this(trip, users) {
        this.canCurrentUserJoin = canJoin
        this.canCurrentUserTakeRide = canTake
        this.isCurrentUserParticipant = isParticipant
    }
}