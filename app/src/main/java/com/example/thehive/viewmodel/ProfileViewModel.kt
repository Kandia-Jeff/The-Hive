package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.UserEntity
import com.example.thehive.data.repository.UserRepository
import com.example.thehive.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _profileState =
        MutableStateFlow<UiState<UserEntity>>(UiState.Loading)

    val profileState: StateFlow<UiState<UserEntity>> =
        _profileState.asStateFlow()

    // Load logged-in user's profile
    fun loadProfile() {
        viewModelScope.launch {
            _profileState.value = UiState.Loading

            val userId = sessionManager.getUserId()

            if (userId == SessionManager.NO_USER) {
                _profileState.value = UiState.Error("User is not logged in")
                return@launch
            }

            try {
                val user = userRepository.findById(userId)

                if (user != null) {
                    _profileState.value = UiState.Success(user)
                } else {
                    _profileState.value = UiState.Error("User profile not found")
                }
            } catch (e: Exception) {
                _profileState.value = UiState.Error(
                    e.message ?: "Failed to load profile"
                )
            }
        }
    }

    // Update profile details
    fun updateProfile(updatedUser: UserEntity) {
        viewModelScope.launch {
            _profileState.value = UiState.Loading

            try {
                val success = userRepository.update(updatedUser)

                if (success) {
                    _profileState.value = UiState.Success(updatedUser)
                } else {
                    _profileState.value = UiState.Error("Profile update failed")
                }
            } catch (e: Exception) {
                _profileState.value = UiState.Error(
                    e.message ?: "Failed to update profile"
                )
            }
        }
    }

    // Logout
    fun logout() {
        sessionManager.logout()
        _profileState.value = UiState.Loading
    }
}