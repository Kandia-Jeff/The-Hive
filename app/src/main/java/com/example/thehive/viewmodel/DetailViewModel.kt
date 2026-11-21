package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.local.entity.UserEntity
import com.example.thehive.data.repository.FavouriteRepository
import com.example.thehive.data.repository.ListingRepository
import com.example.thehive.data.repository.UserRepository
import com.example.thehive.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetailViewModel(
    private val listingRepository: ListingRepository,
    private val favouriteRepository: FavouriteRepository,
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    // Listing details
    private val _listingState =
        MutableStateFlow<UiState<ListingEntity?>>(UiState.Loading)

    val listingState: StateFlow<UiState<ListingEntity?>> =
        _listingState.asStateFlow()

    // Favourite status
    private val _favouriteState =
        MutableStateFlow<UiState<Boolean>>(UiState.Loading)

    val favouriteState: StateFlow<UiState<Boolean>> =
        _favouriteState.asStateFlow()

    // Current logged-in user
    private val _currentUserState = MutableStateFlow<UserEntity?>(null)
    val currentUserState: StateFlow<UserEntity?> = _currentUserState.asStateFlow()

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()
            if (userId != SessionManager.NO_USER) {
                _currentUserState.value = userRepository.findById(userId)
            }
        }
    }

    // Load a single listing
    fun loadListing(listingId: Long) {
        viewModelScope.launch {
            _listingState.value = UiState.Loading

            try {
                listingRepository.getById(listingId).collect { listing ->
                    _listingState.value = UiState.Success(listing)
                }
            } catch (e: Exception) {
                _listingState.value = UiState.Error(
                    e.message ?: "Failed to load listing"
                )
            }
        }
    }

    // Observe whether the listing is favourited
    fun loadFavouriteStatus(listingId: Long) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()

            if (userId == SessionManager.NO_USER) {
                _favouriteState.value = UiState.Error("Please log in first")
                return@launch
            }

            try {
                favouriteRepository.isFavourited(userId, listingId)
                    .collect { isFavourite ->
                        _favouriteState.value = UiState.Success(isFavourite)
                    }
            } catch (e: Exception) {
                _favouriteState.value = UiState.Error(
                    e.message ?: "Failed to check favourite status"
                )
            }
        }
    }

    // Add or remove a favourite
    fun toggleFavourite(listingId: Long) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()

            if (userId == SessionManager.NO_USER) {
                _favouriteState.value = UiState.Error("Please log in first")
                return@launch
            }

            val currentlyFavourited =
                (_favouriteState.value as? UiState.Success)?.data ?: false

            try {
                favouriteRepository.toggle(
                    userId = userId,
                    listingId = listingId,
                    currentlyFavourited = currentlyFavourited
                )
            } catch (e: Exception) {
                _favouriteState.value = UiState.Error(
                    e.message ?: "Failed to update favourite"
                )
            }
        }
    }
}
