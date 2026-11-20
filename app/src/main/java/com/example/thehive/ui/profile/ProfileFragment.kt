package com.example.thehive.ui.profile

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.thehive.HiveApplication
import com.example.thehive.R
import com.example.thehive.data.local.entity.UserEntity
import com.example.thehive.databinding.FragmentProfileBinding
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.ProfileViewModel
import com.example.thehive.viewmodel.UiState
import com.example.thehive.viewmodel.ViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private val viewModel: ProfileViewModel by viewModels {
        val app = requireActivity().application as HiveApplication
        ViewModelFactory(
            app.container.userRepository,
            app.container.listingRepository,
            app.container.favouriteRepository,
            app.container.sessionManager
        )
    }

    private var currentUser: UserEntity? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentProfileBinding.bind(view)

        viewModel.loadProfile()

        binding.btnEditProfile.setOnClickListener {
            currentUser?.let { user ->
                showEditProfileDialog(user)
            } ?: showMessage("User profile not loaded")
        }

        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(R.id.action_profile_to_login)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.profileState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            // Loading state
                        }
                        is UiState.Success -> {
                            val user = state.data
                            currentUser = user
                            binding.tvProfileName.text = user.fullName
                            binding.tvProfileEmail.text = user.email
                            binding.tvProfilePhone.text = user.phone
                            binding.tvProfileRole.text = user.role.name.replace("_", " ")
                        }
                        is UiState.Error -> {
                            showMessage(state.message)
                        }
                    }
                }
            }
        }
    }

    private fun showEditProfileDialog(user: UserEntity) {
        val context = requireContext()
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        val etName = EditText(context).apply {
            hint = "Full Name"
            setText(user.fullName)
        }
        val etPhone = EditText(context).apply {
            hint = "Phone Number"
            setText(user.phone)
            inputType = InputType.TYPE_CLASS_PHONE
        }

        layout.addView(etName)
        layout.addView(etPhone)

        MaterialAlertDialogBuilder(context)
            .setTitle("Edit Profile")
            .setView(layout)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                val newPhone = etPhone.text.toString().trim()

                if (newName.isNotBlank() && newPhone.isNotBlank()) {
                    val updated = user.copy(fullName = newName, phone = newPhone)
                    viewModel.updateProfile(updated)
                    showMessage("Profile updated")
                } else {
                    showMessage("Fields cannot be empty")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
