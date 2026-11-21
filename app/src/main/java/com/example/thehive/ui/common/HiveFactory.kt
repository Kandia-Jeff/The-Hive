package com.example.thehive.ui.common

import androidx.fragment.app.Fragment
import com.example.thehive.HiveApplication
import com.example.thehive.viewmodel.ViewModelFactory

/**
 * Builds the shared ViewModelFactory exactly like HomeFragment does,
 * so Member 5's fragments don't repeat the same 6 lines.
 * Usage:  private val viewModel: MyListingsViewModel by viewModels { hiveViewModelFactory() }
 */
fun Fragment.hiveViewModelFactory(): ViewModelFactory {
    val app = requireActivity().application as HiveApplication
    return ViewModelFactory(
        app.container.userRepository,
        app.container.listingRepository,
        app.container.favouriteRepository,
        app.container.sessionManager
    )
}