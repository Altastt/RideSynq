package com.example.ridesynq.viewmodel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.ridesynq.data.repositories.CompanyRepository

class CompanyVMFactory(
    private val companyRepository: CompanyRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CompanyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CompanyViewModel(companyRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}