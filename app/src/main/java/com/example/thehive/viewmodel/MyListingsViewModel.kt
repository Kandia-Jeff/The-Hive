package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.repository.ListingRepository
import com.example.thehive.util.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MyListingsViewModel(
    private val listingRepository: ListingRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _myListingsState =
        MutableStateFlow<UiState<List<ListingEntity>>>(UiState.Loading)

    val myListingsState: StateFlow<UiState<List<ListingEntity>>> =
        _myListingsState.asStateFlow()

    // Load listings belonging to the logged-in landlord
    fun loadMyListings() {
        viewModelScope.launch {
            _myListingsState.value = UiState.Loading

            val ownerId = sessionManager.getUserId()

            if (ownerId == SessionManager.NO_USER) {
                _myListingsState.value = UiState.Error("Please log in first")
                return@launch
            }

            try {
                listingRepository.getByOwner(ownerId).collect { listings ->
                    _myListingsState.value = UiState.Success(listings)
                }
            } catch (e: Exception) {
                _myListingsState.value = UiState.Error(
                    e.message ?: "Failed to load your listings"
                )
            }
        }
    }

    // Add a new listing
    fun addListing(listing: ListingEntity) {
        viewModelScope.launch {
            try {
                listingRepository.add(listing)
                loadMyListings()
            } catch (e: Exception) {
                _myListingsState.value = UiState.Error(
                    e.message ?: "Failed to add listing"
                )
            }
        }
    }

    // Update an existing listing
    fun updateListing(listing: ListingEntity) {
        viewModelScope.launch {
            try {
                val updated = listingRepository.update(listing)

                if (updated) {
                    loadMyListings()
                } else {
                    _myListingsState.value = UiState.Error("Listing update failed")
                }
            } catch (e: Exception) {
                _myListingsState.value = UiState.Error(
                    e.message ?: "Failed to update listing"
                )
            }
        }
    }

    // Delete a listing
    fun deleteListing(listing: ListingEntity) {
        viewModelScope.launch {
            try {
                val deleted = listingRepository.delete(listing)

                if (deleted) {
                    loadMyListings()
                } else {
                    _myListingsState.value = UiState.Error("Listing deletion failed")
                }
            } catch (e: Exception) {
                _myListingsState.value = UiState.Error(
                    e.message ?: "Failed to delete listing"
                )
            }
        }
    }
}