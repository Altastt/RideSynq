package com.example.ridesynq.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.repositories.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers // Для переключения контекста, если репозиторий не делает это сам
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext // Для переключения контекста

class CompanyViewModel(
    private val companyRepository: CompanyRepository
) : ViewModel() {
    val allCompanies: Flow<List<Company>> = companyRepository.allCompanies
    // --- Состояние для текущей просматриваемой компании ---
    private val _selectedCompany = MutableStateFlow<Company?>(null)
    val selectedCompany: StateFlow<Company?> = _selectedCompany.asStateFlow()

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

    // --- Загрузка компании по ID ---
    fun loadCompanyById(companyId: Int) {
        if (companyId <= 0) {
            _selectedCompany.value = null
            return
        }
        viewModelScope.launch {
            // Очищаем перед загрузкой
            _selectedCompany.value = null
            // Загружаем в IO диспатчере
            val company = withContext(Dispatchers.IO) {
                companyRepository.getCompanyById(companyId)
            }
            // Обновляем StateFlow в основном потоке
            _selectedCompany.value = company
        }
    }
}