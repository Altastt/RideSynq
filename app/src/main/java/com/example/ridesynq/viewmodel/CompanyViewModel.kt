package com.example.ridesynq.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.repositories.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CompanyViewModel(
    private val companyRepository: CompanyRepository
) : ViewModel() {
    val allCompanies: Flow<List<Company>> = companyRepository.allCompanies

    private val _selectedCompany = MutableStateFlow<Company?>(null)
    val selectedCompany: StateFlow<Company?> = _selectedCompany.asStateFlow()

    private val _mapTargetCoordinates = MutableStateFlow<Pair<Double, Double>?>(null)
    val mapTargetCoordinates: StateFlow<Pair<Double, Double>?> = _mapTargetCoordinates.asStateFlow()

    fun setMapTarget(latitude: Double, longitude: Double) {
        _mapTargetCoordinates.value = Pair(latitude, longitude)
    }

    fun consumeMapTarget() {
        _mapTargetCoordinates.value = null
    }
    // ------------------------------------

    suspend fun insertCompanyAndGetId(company: Company): Long? {
        return try {
            withContext(Dispatchers.IO) {
                companyRepository.insert(company)
            }
        } catch (e: Exception) {
            println("Error inserting company: ${e.message}")
            null
        }
    }

    fun loadCompanyById(companyId: Int) {
        if (companyId <= 0) {
            _selectedCompany.value = null
            return
        }
        viewModelScope.launch {
            _selectedCompany.value = null
            val company = withContext(Dispatchers.IO) {
                companyRepository.getCompanyById(companyId)
            }
            _selectedCompany.value = company
        }
    }
}