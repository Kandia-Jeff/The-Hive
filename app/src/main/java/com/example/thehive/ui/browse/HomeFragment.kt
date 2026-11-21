package com.example.thehive.ui.browse

import android.os.Bundle
import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.thehive.HiveApplication
import com.example.thehive.R
import com.example.thehive.databinding.DialogFilterBinding
import com.example.thehive.databinding.FragmentHomeBinding
import com.example.thehive.ui.common.ScreenState
import com.example.thehive.ui.common.setScreenState
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.BrowseViewModel
import com.example.thehive.viewmodel.UiState
import com.example.thehive.viewmodel.ViewModelFactory
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

class HomeFragment : Fragment(R.layout.fragment_home) {

    private val viewModel: BrowseViewModel by viewModels {
        val app = requireActivity().application as HiveApplication
        ViewModelFactory(
            app.container.userRepository,
            app.container.listingRepository,
            app.container.favouriteRepository,
            app.container.sessionManager
        )
    }

    private var currentQuery: String? = null
    private var currentMinRent: Double = 0.0
    private var currentMaxRent: Double = Double.MAX_VALUE
    private var currentBedrooms: Int? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)

        val adapter = ListingsAdapter(
            onListingClick = { listing ->
                val bundle = Bundle().apply { putLong("listingId", listing.id) }
                findNavController().navigate(R.id.action_home_to_detail, bundle)
            },
            onFavouriteClick = { listing ->
                showMessage("Favourite toggled for ${listing.title}")
            }
        )

        binding.rvListings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvListings.adapter = adapter

        binding.etSearch.doAfterTextChanged { text ->
            currentQuery = text?.toString()?.takeIf { it.isNotBlank() }
            applyFilters()
        }

        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }

        binding.errorView.retryButton.setOnClickListener {
            viewModel.loadListings()
        }

        viewModel.loadListings()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.listingsState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            setScreenState(
                                ScreenState.LOADING,
                                binding.rvListings,
                                binding.loadingView.root,
                                binding.emptyView.root,
                                binding.errorView.root
                            )
                        }
                        is UiState.Success -> {
                            val list = state.data
                            adapter.submitList(list)
                            val screenState = if (list.isEmpty()) ScreenState.EMPTY else ScreenState.CONTENT
                            setScreenState(
                                screenState,
                                binding.rvListings,
                                binding.loadingView.root,
                                binding.emptyView.root,
                                binding.errorView.root
                            )
                            binding.emptyView.emptyMessage.text = "No homes match your filters"
                        }
                        is UiState.Error -> {
                            setScreenState(
                                ScreenState.ERROR,
                                binding.rvListings,
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

    private fun applyFilters() {
        viewModel.searchListings(
            location = currentQuery,
            minRent = currentMinRent,
            maxRent = currentMaxRent,
            bedrooms = currentBedrooms,
            includeTaken = false
        )
    }

    private fun showFilterDialog() {
        val dialog = BottomSheetDialog(requireContext())
        val dialogBinding = DialogFilterBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialogBinding.etMinRent.setText(if (currentMinRent > 0.0) currentMinRent.toString() else "")
        dialogBinding.etMaxRent.setText(if (currentMaxRent < Double.MAX_VALUE) currentMaxRent.toString() else "")
        dialogBinding.etBedrooms.setText(currentBedrooms?.toString() ?: "")

        dialogBinding.btnClear.setOnClickListener {
            currentMinRent = 0.0
            currentMaxRent = Double.MAX_VALUE
            currentBedrooms = null
            applyFilters()
            dialog.dismiss()
        }

        dialogBinding.btnApply.setOnClickListener {
            currentMinRent = dialogBinding.etMinRent.text?.toString()?.toDoubleOrNull() ?: 0.0
            currentMaxRent = dialogBinding.etMaxRent.text?.toString()?.toDoubleOrNull()?.takeIf { it > 0 } ?: Double.MAX_VALUE
            currentBedrooms = dialogBinding.etBedrooms.text?.toString()?.toIntOrNull()
            applyFilters()
            dialog.dismiss()
        }

        dialog.show()
    }
}
