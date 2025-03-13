package com.example.ridesynq.data.repositories

import com.example.ridesynq.data.dao.CompanyDao
import com.example.ridesynq.data.entities.Company
import kotlinx.coroutines.flow.Flow

class CompanyRepository(private val companyDao: CompanyDao) {
    val allCompanies: Flow<List<Company>> = companyDao.getAllCompanies()

    suspend fun insert(company: Company) {
        companyDao.insert(company)
    }

    suspend fun getCompanyById(id: Int): Company? {
        return companyDao.getCompanyById(id)
    }
}