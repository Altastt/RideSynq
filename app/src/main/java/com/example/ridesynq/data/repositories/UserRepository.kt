package com.example.ridesynq.data.repositories

import com.example.ridesynq.data.dao.CompanyDao
import com.example.ridesynq.data.dao.UserDao
import com.example.ridesynq.data.entities.User
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val companyDao: CompanyDao
) {
    suspend fun createUser(user: User) {
        val company = companyDao.getCompanyById(user.company_id)
        if (company != null) {
            userDao.insert(user)
        } else {
            throw Exception("Company not found")
        }
    }

    fun getUsersByCompany(companyId: Int): Flow<List<User>> {
        return userDao.getUsersByCompany(companyId)
    }

    suspend fun isLoginUnique(login: String) =
        !userDao.isLoginExists(login)

    suspend fun validateCredentials(login: String, password: String) =
        userDao.getUserByCredentials(login, password)
}