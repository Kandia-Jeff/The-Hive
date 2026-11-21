package com.example.thehive.ui.favourites

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
import com.example.thehive.databinding.FragmentFavouritesBinding
import com.example.thehive.ui.common.ScreenState
import com.example.thehive.ui.common.hiveViewModelFactory
import com.example.thehive.ui.common.setScreenState
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.FavouritesViewModel
import com.example.thehive.viewmodel.UiState
import kotlinx.coroutines.launch

class FavouritesFragment : Fragment(R.layout.fragment_favourites) {

    private val viewModel: FavouritesViewModel by viewModels { hiveViewModelFactory() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentFavouritesBinding.bind(view)

        val adapter = FavouritesAdapter(
            onListingClick = { listing ->
                val args = Bundle().apply { putLong("listingId", listing.id) }
                findNavController().navigate(R.id.action_favourites_to_detail, args)
            },
            onRemoveClick = { listing ->
                viewModel.removeFavourite(listing.id)
                showMessage("Removed from favourites")
            }
        )

        binding.rvFavourites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavourites.adapter = adapter

        binding.errorView.retryButton.setOnClickListener { viewModel.loadFavourites() }

        viewModel.loadFavourites()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.favouritesState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            setScreenState(
                                ScreenState.LOADING,
                                binding.rvFavourites,
                                binding.loadingView.root,
                                binding.emptyView.root,
                                binding.errorView.root
                            )
                        }
                        is UiState.Success -> {
                            val list = state.data
                            adapter.submitList(list)
                            binding.emptyView.emptyMessage.text = "No saved homes yet"
                            val screenState =
                                if (list.isEmpty()) ScreenState.EMPTY else ScreenState.CONTENT
                            setScreenState(
                                screenState,
                                binding.rvFavourites,
                                binding.loadingView.root,
                                binding.emptyView.root,
                                binding.errorView.root
                            )
                        }
                        is UiState.Error -> {
                            setScreenState(
                                ScreenState.ERROR,
                                binding.rvFavourites,
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
}