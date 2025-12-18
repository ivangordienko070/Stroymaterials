// presentation/viewmodels/AuthViewModel.kt
package com.example.stroymaterials.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.stroymaterials.data.database.entities.UserEntity
import com.example.stroymaterials.data.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(private val userRepository: UserRepository) : ViewModel() {
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    val isAuthenticated: Boolean
        get() = _currentUser.value != null

    val isAdmin: Boolean
        get() = _currentUser.value?.role == "admin"

    val isGuest: Boolean
        get() = _currentUser.value?.role == "guest"

    fun login(username: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val user = userRepository.authenticateUser(username, password)
                if (user != null) {
                    _currentUser.value = user
                    onResult(true, null)
                } else {
                    onResult(false, "Неверное имя пользователя или пароль")
                }
            } catch (e: Exception) {
                onResult(false, "Ошибка входа: ${e.message}")
            }
        }
    }

    fun logout() {
        _currentUser.value = null
    }

    fun setCurrentUser(user: UserEntity?) {
        _currentUser.value = user
    }
}

