// data/repositories/UserRepository.kt
package com.example.stroymaterials.data.repositories

import com.example.stroymaterials.data.database.daos.UserDao
import com.example.stroymaterials.data.database.entities.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    suspend fun authenticateUser(username: String, password: String): UserEntity? {
        return userDao.authenticateUser(username, password)
    }

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun getUserById(id: Long): UserEntity? {
        return userDao.getUserById(id)
    }

    fun getAllUsers(): Flow<List<UserEntity>> {
        return userDao.getAllUsers()
    }

    suspend fun insertUser(user: UserEntity): Long {
        return userDao.insert(user)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.update(user)
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.delete(user)
    }

    suspend fun getUsersCount(): Int {
        return userDao.getUsersCount()
    }
}

