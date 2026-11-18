package com.example.thehive.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.thehive.data.repository.UserRepository
import com.example.thehive.data.repository.ListingRepository
import com.example.thehive.data.repository.FavouriteRepository
import com.example.thehive.util.SessionManager

class ViewModelFactory(
    private val userRepository: UserRepository,
    private val listingRepository: ListingRepository,
    private val favouriteRepository: FavouriteRepository,
    private val sessionManager: SessionManager
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(userRepository, sessionManager) as T

            modelClass.isAssignableFrom(BrowseViewModel::class.java) ->
                BrowseViewModel(listingRepository) as T

            modelClass.isAssignableFrom(DetailViewModel::class.java) ->
                DetailViewModel(
                    listingRepository,
                    favouriteRepository,
                    sessionManager
                ) as T

            modelClass.isAssignableFrom(ProfileViewModel::class.java) ->
                ProfileViewModel(userRepository, sessionManager) as T

            modelClass.isAssignableFrom(MyListingsViewModel::class.java) ->
                MyListingsViewModel(listingRepository, sessionManager) as T

            modelClass.isAssignableFrom(ListingFormViewModel::class.java) ->
                ListingFormViewModel(listingRepository) as T

            modelClass.isAssignableFrom(FavouritesViewModel::class.java) ->
                FavouritesViewModel(favouriteRepository, sessionManager) as T

            else -> throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
}