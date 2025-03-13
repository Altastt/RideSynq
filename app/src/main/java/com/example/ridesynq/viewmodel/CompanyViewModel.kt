package com.example.ridesynq.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.ridesynq.data.database.AppDatabase
import com.example.ridesynq.data.entities.Company
import com.example.ridesynq.data.repositories.CompanyRepository
import kotlinx.coroutines.launch

class CompanyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: CompanyRepository
    val allCompanies: LiveData<List<Company>>

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CompanyRepository(db.companyDao())
        allCompanies = repository.allCompanies.asLiveData()
    }

    fun insert(company: Company) = viewModelScope.launch {
        repository.insert(company)
    }
}