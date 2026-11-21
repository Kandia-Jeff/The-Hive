package com.example.thehive.ui.mylistings

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.thehive.HiveApplication
import com.example.thehive.R
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.data.local.entity.UserEntity
import com.example.thehive.databinding.FragmentAddEditListingBinding
import com.example.thehive.ui.common.hiveViewModelFactory
import com.example.thehive.ui.common.showConfirmDialog
import com.example.thehive.ui.common.showMessage
import com.example.thehive.util.SessionManager
import com.example.thehive.viewmodel.ListingFormViewModel
import com.example.thehive.viewmodel.MyListingsViewModel
import com.example.thehive.viewmodel.ProfileViewModel
import com.example.thehive.viewmodel.UiState
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

class AddEditListingFragment : Fragment(R.layout.fragment_add_edit_listing) {

    private val formViewModel: ListingFormViewModel by viewModels { hiveViewModelFactory() }
    private val profileViewModel: ProfileViewModel by viewModels { hiveViewModelFactory() }

    // Only touched in edit mode (lazy), used to find the listing being edited.
    private val myListingsViewModel: MyListingsViewModel by viewModels { hiveViewModelFactory() }

    private var _binding: FragmentAddEditListingBinding? = null
    private val binding get() = _binding!!

    private var listingId: Long = -1L
    private val isEditMode get() = listingId != -1L

    private var existing: ListingEntity? = null   // the listing being edited
    private var prefilled = false                 // contact details copied from profile (create mode)
    private var saveRequested = false             // true between tapping Save and getting a result
    private var initialForm = FormSnapshot()      // used to detect unsaved changes

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentAddEditListingBinding.bind(view)

        listingId = arguments?.getLong("listingId", -1L) ?: -1L

        setUpModeUi()
        clearErrorsWhileTyping()
        setUpBackHandling()

        binding.btnSave.setOnClickListener { onSaveClicked() }

        // Load what the form needs
        val loaded = existing
        if (isEditMode) {
            if (loaded != null) fillFrom(loaded) else myListingsViewModel.loadMyListings()
        } else {
            populateAmenities(emptySet())
            if (!prefilled) profileViewModel.loadProfile()
        }
        initialForm = readForm()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { formViewModel.validationState.collect { showFieldErrors(it.errors) } }
                launch { formViewModel.formState.collect { handleSaveState(it) } }
                if (isEditMode) {
                    launch { myListingsViewModel.myListingsState.collect { handleListingLoaded(it) } }
                } else {
                    launch { profileViewModel.profileState.collect { handleProfile(it) } }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // ---------------------------------------------------------------- setup

    private fun setUpModeUi() {
        binding.tvFormTitle.text = if (isEditMode) "Edit listing" else "Add listing"
        binding.btnSave.text = if (isEditMode) "Save changes" else "Add listing"
        // In edit mode, Save stays disabled until the listing has been loaded.
        binding.btnSave.isEnabled = !isEditMode || existing != null
    }

    private fun clearErrorsWhileTyping() {
        binding.etTitle.doAfterTextChanged { binding.tilTitle.error = null }
        binding.etLocation.doAfterTextChanged { binding.tilLocation.error = null }
        binding.etRent.doAfterTextChanged { binding.tilRent.error = null }
        binding.etBedrooms.doAfterTextChanged { binding.tilBedrooms.error = null }
        binding.etPhone.doAfterTextChanged { binding.tilPhone.error = null }
        binding.etEmail.doAfterTextChanged { binding.tilEmail.error = null }
    }

    /** Back button: ask before throwing away typed changes. */
    private fun setUpBackHandling() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!saveRequested && readForm() != initialForm) {
                        requireContext().showConfirmDialog(
                            title = "Discard changes?",
                            message = "You have unsaved changes. If you leave now, they will be lost.",
                            confirmText = "Discard"
                        ) { findNavController().popBackStack() }
                    } else {
                        isEnabled = false
                        requireActivity().onBackPressedDispatcher.onBackPressed()
                    }
                }
            }
        )
    }

    // ---------------------------------------------------------------- saving

    private fun onSaveClicked() {
        val ownerId = (requireActivity().application as HiveApplication)
            .container.sessionManager.getUserId()
        if (ownerId == SessionManager.NO_USER) {
            showMessage("Please log in first")
            return
        }

        val form = readForm()

        // Bad numbers become values the validator rejects, so it can show its own message.
        val rent = form.rent.toDoubleOrNull() ?: 0.0
        val bedrooms = form.bedrooms.toIntOrNull() ?: -1

        val current = existing
        val listing = current?.copy(
            title = form.title,
            location = form.location,
            rent = rent,
            bedrooms = bedrooms,
            description = form.description,
            contactName = form.contactName,
            contactPhone = form.phone,
            contactEmail = form.email,
            amenities = form.amenities.toList(),
            lastConfirmedAt = System.currentTimeMillis()
        ) ?: ListingEntity(
            ownerId = ownerId,
            title = form.title,
            description = form.description,
            location = form.location,
            rent = rent,
            bedrooms = bedrooms,
            contactName = form.contactName,
            contactPhone = form.phone,
            contactEmail = form.email,
            amenities = form.amenities.toList()
        )

        saveRequested = true
        setSaving(true)
        formViewModel.saveListing(listing)
    }

    private fun handleSaveState(state: UiState<ListingEntity>) {
        // formState starts as Loading, so ignore everything until Save has been tapped.
        if (!saveRequested) return
        when (state) {
            is UiState.Loading -> setSaving(true)
            is UiState.Success -> {
                saveRequested = false
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) "Listing updated" else "Listing added",
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().popBackStack() // back to My Listings
            }
            is UiState.Error -> {
                saveRequested = false
                setSaving(false)
                val hasFieldErrors = formViewModel.validationState.value.errors.isNotEmpty()
                showMessage(
                    if (hasFieldErrors) "Please fix the highlighted fields" else state.message
                )
            }
        }
    }

    private fun setSaving(saving: Boolean) {
        binding.btnSave.isEnabled = !saving && (!isEditMode || existing != null)
        binding.btnSave.text = if (saving) "" else if (isEditMode) "Save changes" else "Add listing"
        binding.progressSaving.visibility = if (saving) View.VISIBLE else View.GONE
    }

    private fun showFieldErrors(errors: Map<String, String>) {
        binding.tilTitle.error = errors["title"]
        binding.tilLocation.error = errors["location"]
        binding.tilRent.error = errors["rent"]
        binding.tilBedrooms.error = errors["bedrooms"]
        binding.tilPhone.error = errors["phone"]
        binding.tilEmail.error = errors["email"]
    }

    // ---------------------------------------------------------------- loading / prefill

    /** Edit mode: pick the listing out of the landlord's own listings. */
    private fun handleListingLoaded(state: UiState<List<ListingEntity>>) {
        if (existing != null) return
        when (state) {
            is UiState.Success -> {
                val found = state.data.firstOrNull { it.id == listingId }
                if (found != null) {
                    existing = found
                    fillFrom(found)
                    initialForm = readForm()
                    binding.btnSave.isEnabled = true
                } else {
                    showMessage("Listing not found")
                    findNavController().popBackStack()
                }
            }
            is UiState.Error -> showMessage(state.message)
            is UiState.Loading -> Unit
        }
    }

    /** Create mode: prefill contact details from the logged-in user's profile. */
    private fun handleProfile(state: UiState<UserEntity>) {
        if (prefilled || state !is UiState.Success) return
        prefilled = true
        val user = state.data
        if (binding.etContactName.text.isNullOrBlank()) binding.etContactName.setText(user.fullName)
        if (binding.etPhone.text.isNullOrBlank()) binding.etPhone.setText(user.phone)
        if (binding.etEmail.text.isNullOrBlank()) binding.etEmail.setText(user.email)
        initialForm = readForm()
    }

    private fun fillFrom(listing: ListingEntity) {
        binding.etTitle.setText(listing.title)
        binding.etLocation.setText(listing.location)
        binding.etRent.setText(rentToText(listing.rent))
        binding.etBedrooms.setText(listing.bedrooms.toString())
        binding.etDescription.setText(listing.description)
        binding.etContactName.setText(listing.contactName)
        binding.etPhone.setText(listing.contactPhone)
        binding.etEmail.setText(listing.contactEmail)
        populateAmenities(listing.amenities.toSet())
    }

    // ---------------------------------------------------------------- amenities

    private fun populateAmenities(selected: Set<String>) {
        val group = binding.chipGroupAmenities
        group.removeAllViews()
        // Keep any amenity already on the listing even if it is not in the standard list.
        val options = AMENITY_OPTIONS + (selected - AMENITY_OPTIONS.toSet()).sorted()
        options.forEach { name ->
            val chip = Chip(requireContext()).apply {
                text = name
                isCheckable = true
                isChecked = name in selected
            }
            group.addView(chip)
        }
    }

    private fun selectedAmenities(): Set<String> {
        val group = binding.chipGroupAmenities
        val selected = mutableSetOf<String>()
        for (i in 0 until group.childCount) {
            val chip = group.getChildAt(i) as? Chip ?: continue
            if (chip.isChecked) selected.add(chip.text.toString())
        }
        return selected
    }

    // ---------------------------------------------------------------- helpers

    private fun readForm() = FormSnapshot(
        title = binding.etTitle.text.toString().trim(),
        location = binding.etLocation.text.toString().trim(),
        rent = binding.etRent.text.toString().trim(),
        bedrooms = binding.etBedrooms.text.toString().trim(),
        amenities = selectedAmenities(),
        description = binding.etDescription.text.toString().trim(),
        contactName = binding.etContactName.text.toString().trim(),
        phone = binding.etPhone.text.toString().trim(),
        email = binding.etEmail.text.toString().trim()
    )

    /** 25000.0 -> "25000", 25000.5 -> "25000.5" */
    private fun rentToText(rent: Double): String =
        if (rent % 1.0 == 0.0) rent.toLong().toString() else rent.toString()

    private data class FormSnapshot(
        val title: String = "",
        val location: String = "",
        val rent: String = "",
        val bedrooms: String = "",
        val amenities: Set<String> = emptySet(),
        val description: String = "",
        val contactName: String = "",
        val phone: String = "",
        val email: String = ""
    )

    companion object {
        private val AMENITY_OPTIONS = listOf(
            "Wi-Fi", "Water 24/7", "Parking", "Security", "CCTV",
            "Balcony", "Furnished", "Backup generator", "Lift", "Gym"
        )
    }
}