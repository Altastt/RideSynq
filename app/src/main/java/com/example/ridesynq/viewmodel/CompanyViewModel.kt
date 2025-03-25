package com.example.ridesynq.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.repositories.CompanyRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class CompanyViewModel(
    private val companyRepository: CompanyRepository
) : ViewModel() {
    val allCompanies: Flow<List<Company>> = companyRepository.allCompanies

    fun insertCompany(company: Company) = viewModelScope.launch {
        companyRepository.insert(company)
    }
}