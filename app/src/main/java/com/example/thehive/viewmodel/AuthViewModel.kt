package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.UserEntity
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.data.repository.UserRepository
import com.example.thehive.util.SessionManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Login state
    private val _loginState = MutableStateFlow<UiState<UserEntity>>(UiState.Loading)
    val loginState: StateFlow<UiState<UserEntity>> = _loginState.asStateFlow()

    // Registration state
    private val _registerState = MutableStateFlow<UiState<Long>>(UiState.Loading)
    val registerState: StateFlow<UiState<Long>> = _registerState.asStateFlow()

    // Login
    fun login(email: String, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading

            val result = userRepository.login(email, password)

            result.onSuccess { user ->
                sessionManager.saveUserId(user.id)
                _loginState.value = UiState.Success(user)
            }.onFailure { error ->
                _loginState.value = UiState.Error(
                    error.message ?: "Login failed"
                )
            }
        }
    }

    // Register
    fun register(
        fullName: String,
        email: String,
        phone: String,
        password: String,
        role: UserRole = UserRole.HOUSE_HUNTER
    ) {
        viewModelScope.launch {
            _registerState.value = UiState.Loading

            val result = userRepository.register(
                fullName = fullName,
                email = email,
                phone = phone,
                password = password,
                role = role
            )

            result.onSuccess { userId ->
                sessionManager.saveUserId(userId)
                _registerState.value = UiState.Success(userId)
            }.onFailure { error ->
                _registerState.value = UiState.Error(
                    error.message ?: "Registration failed"
                )
            }
        }
    }

    // Logout
    fun logout() {
        sessionManager.logout()
        _loginState.value = UiState.Loading
        _registerState.value = UiState.Loading
    }
}