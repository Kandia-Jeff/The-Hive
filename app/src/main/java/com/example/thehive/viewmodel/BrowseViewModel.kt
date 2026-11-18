package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.repository.ListingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class BrowseViewModel(
    private val listingRepository: ListingRepository
) : ViewModel() {

    private val _listingsState =
        MutableStateFlow<UiState<List<ListingEntity>>>(UiState.Loading)

    val listingsState: StateFlow<UiState<List<ListingEntity>>> =
        _listingsState.asStateFlow()

    // Load all available listings
    fun loadListings() {
        viewModelScope.launch {
            _listingsState.value = UiState.Loading

            try {
                listingRepository.getAll().collect { listings ->
                    _listingsState.value = UiState.Success(listings)
                }
            } catch (e: Exception) {
                _listingsState.value = UiState.Error(
                    e.message ?: "Failed to load listings"
                )
            }
        }
    }

    // Search and filter listings
    fun searchListings(
        location: String? = null,
        minRent: Double = 0.0,
        maxRent: Double = Double.MAX_VALUE,
        bedrooms: Int? = null,
        includeTaken: Boolean = false
    ) {
        viewModelScope.launch {
            _listingsState.value = UiState.Loading

            try {
                listingRepository.search(
                    location = location,
                    minRent = minRent,
                    maxRent = maxRent,
                    bedrooms = bedrooms,
                    includeTaken = includeTaken
                ).collect { listings ->
                    _listingsState.value = UiState.Success(listings)
                }
            } catch (e: Exception) {
                _listingsState.value = UiState.Error(
                    e.message ?: "Search failed"
                )
            }
        }
    }

    // Clear filters and reload all listings
    fun clearFilters() {
        loadListings()
    }
}