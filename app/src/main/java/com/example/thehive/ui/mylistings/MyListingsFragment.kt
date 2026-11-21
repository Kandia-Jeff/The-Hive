package com.example.thehive.ui.mylistings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thehive.R
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.databinding.FragmentMyListingsBinding
import com.example.thehive.ui.common.ScreenState
import com.example.thehive.ui.common.hiveViewModelFactory
import com.example.thehive.ui.common.setScreenState
import com.example.thehive.ui.common.showConfirmDialog
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.MyListingsViewModel
import com.example.thehive.viewmodel.UiState
import kotlinx.coroutines.launch

class MyListingsFragment : Fragment(R.layout.fragment_my_listings) {

    private val viewModel: MyListingsViewModel by viewModels { hiveViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMyListingsBinding.bind(view)

        val adapter = MyListingsAdapter(
            onEdit = { listing -> openForm(listing.id) },
            onDelete = { listing -> confirmDelete(listing) },
            onToggleAvailability = { listing -> toggleAvailability(listing) }
        )

        binding.rvMyListings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMyListings.adapter = adapter

        binding.fabAdd.setOnClickListener { openForm(-1L) }
        binding.errorView.retryButton.setOnClickListener { viewModel.loadMyListings() }

        viewModel.loadMyListings()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.myListingsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            // The ViewModel emits Loading after every edit/delete. If the list is
                            // already on screen, keep showing it instead of flashing a spinner.
                            if (adapter.currentList.isEmpty()) {
                                setScreenState(
                                    ScreenState.LOADING,
                                    binding.rvMyListings,
                                    binding.loadingView.root,
                                    binding.emptyView.root,
                                    binding.errorView.root
                                )
                            }
                        }
                        is UiState.Success -> {
                            val list = state.data
                            adapter.submitList(list)
                            binding.emptyView.emptyMessage.text = "You have no listings yet"
                            val screenState =
                                if (list.isEmpty()) ScreenState.EMPTY else ScreenState.CONTENT
                            setScreenState(
                                screenState,
                                binding.rvMyListings,
                                binding.loadingView.root,
                                binding.emptyView.root,
                                binding.errorView.root
                            )
                        }
                        is UiState.Error -> {
                            setScreenState(
                                ScreenState.ERROR,
                                binding.rvMyListings,
                                binding.loadingView.root,
                                binding.emptyView.root,
                                binding.errorView.root
                            )
                        }
                    }
                }
            }
        }
    }

    /** listingId = -1 means "create a new listing". */
    private fun openForm(listingId: Long) {
        val args = Bundle().apply { putLong("listingId", listingId) }
        findNavController().navigate(R.id.action_myListings_to_addEdit, args)
    }

    private fun confirmDelete(listing: ListingEntity) {
        requireContext().showConfirmDialog(
            title = "Delete listing?",
            message = "\"${listing.title}\" will be removed permanently, including from " +
                    "anyone's saved homes.",
            confirmText = "Delete"
        ) {
            viewModel.deleteListing(listing)
            showMessage("Listing deleted")
        }
    }

    private fun toggleAvailability(listing: ListingEntity) {
        val nowAvailable = !listing.isAvailable
        // Confirming availability also refreshes lastConfirmedAt for Member 6's reminder worker.
        viewModel.updateListing(
            listing.copy(
                isAvailable = nowAvailable,
                lastConfirmedAt = System.currentTimeMillis()
            )
        )
        showMessage(if (nowAvailable) "Marked as available" else "Marked as taken")
    }
}