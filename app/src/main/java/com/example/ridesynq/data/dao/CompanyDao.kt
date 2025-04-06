package com.example.ridesynq.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy // Добавляем импорт
import androidx.room.Query
import androidx.room.Update
import com.example.ridesynq.data.entities.Company
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    // Указываем стратегию конфликта и возвращаем Long (rowId)
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(company: Company): Long // Теперь возвращает Long

    // Остальные методы без изменений
    @Query("SELECT * FROM companies")
    fun getAllCompanies(): Flow<List<Company>>

    @Query("SELECT * FROM companies WHERE id = :id")
    suspend fun getCompanyById(id: Int): Company?

    @Update
    suspend fun update(company: Company)

    @Delete
    suspend fun delete(company: Company)
}