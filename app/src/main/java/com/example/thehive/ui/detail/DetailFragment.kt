package com.example.thehive.ui.detail

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.thehive.HiveApplication
import com.example.thehive.R
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.databinding.FragmentDetailBinding
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.DetailViewModel
import com.example.thehive.viewmodel.UiState
import com.example.thehive.viewmodel.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class DetailFragment : Fragment(R.layout.fragment_detail) {

    private val viewModel: DetailViewModel by viewModels {
        val app = requireActivity().application as HiveApplication
        ViewModelFactory(
            app.container.userRepository,
            app.container.listingRepository,
            app.container.favouriteRepository,
            app.container.sessionManager
        )
    }

    private var currentListing: ListingEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentDetailBinding.bind(view)

        val listingId = arguments?.getLong("listingId") ?: -1L
        if (listingId != -1L) {
            viewModel.loadListing(listingId)
            viewModel.loadFavouriteStatus(listingId)
        }

        binding.ivFavourite.setOnClickListener {
            if (listingId != -1L) {
                viewModel.toggleFavourite(listingId)
                showMessage("Favourite updated")
            }
        }

        binding.btnContact.setOnClickListener {
            currentListing?.let { listing ->
                showContactSheet(listing)
            } ?: showMessage("Contact info not available")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.listingState.collect { state ->
                        when (state) {
                            is UiState.Loading -> {}
                            is UiState.Success -> {
                                val listing = state.data
                                if (listing != null) {
                                    currentListing = listing
                                    binding.tvTitle.text = listing.title
                                    binding.tvLocation.text = listing.location
                                    binding.tvRent.text = "Ksh %,.0f / mo".format(listing.rent)
                                    binding.tvBedrooms.text = "${listing.bedrooms} Bedrooms"
                                    binding.tvDescription.text = listing.description.ifBlank { "No description provided." }
                                    binding.chipBadge.text = if (listing.isAvailable) "Available" else "Taken"
                                }
                            }
                            is UiState.Error -> {
                                showMessage(state.message)
                            }
                        }
                    }
                }

                launch {
                    viewModel.favouriteState.collect { state ->
                        when (state) {
                            is UiState.Success -> {
                                val isFav = state.data
                                binding.ivFavourite.alpha = if (isFav) 1.0f else 0.5f
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    private fun showContactSheet(listing: ListingEntity) {
        val options = arrayOf("Call (${listing.contactPhone})", "Email (${listing.contactEmail})")
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Contact Landlord")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${listing.contactPhone}"))
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            showMessage("Unable to make phone call")
                        }
                    }
                    1 -> {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:${listing.contactEmail}"))
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about ${listing.title}")
                        try {
                            startActivity(intent)
                        } catch (e: Exception) {
                            showMessage("No email client found")
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
