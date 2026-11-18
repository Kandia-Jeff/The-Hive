package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.repository.ListingRepository
import com.example.thehive.util.ListingInput
import com.example.thehive.util.ListingValidator
import com.example.thehive.util.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ListingFormViewModel(
    private val listingRepository: ListingRepository
) : ViewModel() {

    // Saving listing state
    private val _formState =
        MutableStateFlow<UiState<ListingEntity>>(UiState.Loading)

    val formState: StateFlow<UiState<ListingEntity>> =
        _formState.asStateFlow()

    // Validation errors
    private val _validationState =
        MutableStateFlow(ValidationResult())

    val validationState: StateFlow<ValidationResult> =
        _validationState.asStateFlow()

    // Validate and save a listing
    fun saveListing(listing: ListingEntity) {
        viewModelScope.launch {
            _formState.value = UiState.Loading

            try {
                // Check if listing already exists
                val duplicate = listingRepository.isDuplicate(
                    title = listing.title,
                    location = listing.location,
                    excludeId = listing.id
                )

                // Convert listing into input for validation
                val input = ListingInput(
                    title = listing.title,
                    location = listing.location,
                    rent = listing.rent,
                    bedrooms = listing.bedrooms,
                    phone = listing.contactPhone,
                    email = listing.contactEmail
                )

                // Validate listing details
                val validation = ListingValidator.validate(
                    input = input,
                    isDuplicate = duplicate
                )

                _validationState.value = validation

                if (!validation.isValid) {
                    _formState.value = UiState.Error(
                        validation.errors.values.joinToString("\n")
                    )
                    return@launch
                }

                // Add new listing or update existing one
                if (listing.id == 0L) {
                    val newId = listingRepository.add(listing)
                    _formState.value = UiState.Success(
                        listing.copy(id = newId)
                    )
                } else {
                    val updated = listingRepository.update(listing)

                    if (updated) {
                        _formState.value = UiState.Success(listing)
                    } else {
                        _formState.value = UiState.Error(
                            "Failed to update listing"
                        )
                    }
                }

            } catch (e: Exception) {
                _formState.value = UiState.Error(
                    e.message ?: "Failed to save listing"
                )
            }
        }
    }

    // Clear validation errors
    fun clearValidation() {
        _validationState.value = ValidationResult()
    }
}