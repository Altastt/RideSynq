package com.example.ridesynq.data.repositories

import com.example.ridesynq.data.dao.CompanyDao
import com.example.ridesynq.data.entities.Company
import kotlinx.coroutines.flow.Flow

class CompanyRepository(private val companyDao: CompanyDao) {
    val allCompanies: Flow<List<Company>> = companyDao.getAllCompanies()

    // Метод теперь возвращает Long (ID вставленной компании)
    suspend fun insert(company: Company): Long {
        return companyDao.insert(company) // Возвращаем результат insert из DAO
    }

    suspend fun getCompanyById(id: Int): Company? {
        return companyDao.getCompanyById(id)
    }
}