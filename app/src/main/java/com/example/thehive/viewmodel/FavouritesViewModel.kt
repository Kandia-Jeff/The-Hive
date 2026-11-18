package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.repository.FavouriteRepository
import com.example.thehive.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FavouritesViewModel(
    private val favouriteRepository: FavouriteRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _favouritesState =
        MutableStateFlow<UiState<List<ListingEntity>>>(UiState.Loading)

    val favouritesState: StateFlow<UiState<List<ListingEntity>>> =
        _favouritesState.asStateFlow()

    // Load saved houses for the logged-in user
    fun loadFavourites() {
        viewModelScope.launch {
            _favouritesState.value = UiState.Loading

            val userId = sessionManager.getUserId()

            if (userId == SessionManager.NO_USER) {
                _favouritesState.value = UiState.Error("Please log in first")
                return@launch
            }

            try {
                favouriteRepository.getFavourites(userId).collect { listings ->
                    _favouritesState.value = UiState.Success(listings)
                }
            } catch (e: Exception) {
                _favouritesState.value = UiState.Error(
                    e.message ?: "Failed to load favourites"
                )
            }
        }
    }

    // Remove a house from favourites
    fun removeFavourite(listingId: Long) {
        viewModelScope.launch {
            val userId = sessionManager.getUserId()

            if (userId == SessionManager.NO_USER) {
                _favouritesState.value = UiState.Error("Please log in first")
                return@launch
            }

            try {
                favouriteRepository.remove(userId, listingId)
            } catch (e: Exception) {
                _favouritesState.value = UiState.Error(
                    e.message ?: "Failed to remove favourite"
                )
            }
        }
    }
}