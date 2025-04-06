package com.example.ridesynq.viewmodel


import androidx.lifecycle.ViewModel
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.repositories.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers // Для переключения контекста, если репозиторий не делает это сам
import kotlinx.coroutines.withContext // Для переключения контекста

class CompanyViewModel(
    private val companyRepository: CompanyRepository
) : ViewModel() {
    val allCompanies: Flow<List<Company>> = companyRepository.allCompanies

    // Функция теперь suspend и возвращает Long? (ID или null при ошибке)
    suspend fun insertCompanyAndGetId(company: Company): Long? {
        return try {
            // Лучше выполнять операции с БД в IO диспатчере
            withContext(Dispatchers.IO) {
                companyRepository.insert(company)
            }
        } catch (e: Exception) {
            // Логируем ошибку или обрабатываем ее по-другому
            println("Error inserting company: ${e.message}")
            null // Возвращаем null в случае ошибки
        }
    }

}