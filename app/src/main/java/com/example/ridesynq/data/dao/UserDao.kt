package com.example.ridesynq.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.ridesynq.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert
    suspend fun insert(user: User)

    @Query("SELECT * FROM users WHERE company_id = :companyId")
    fun getUsersByCompany(companyId: Int): Flow<List<User>>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Update
    suspend fun update(user: User)

    // Проверка существования пользователя
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE login = :login)")
    suspend fun isLoginExists(login: String): Boolean

    // Получение пользователя по логину и паролю
    @Query("SELECT * FROM users WHERE login = :login AND password = :password")
    suspend fun getUserByCredentials(login: String, password: String): User?
}