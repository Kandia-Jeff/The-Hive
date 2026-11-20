package com.example.thehive.ui.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.thehive.HiveApplication
import com.example.thehive.R
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.databinding.FragmentRegisterBinding
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.AuthViewModel
import com.example.thehive.viewmodel.UiState
import com.example.thehive.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class RegisterFragment : Fragment(R.layout.fragment_register) {

    private val viewModel: AuthViewModel by viewModels {
        val app = requireActivity().application as HiveApplication
        ViewModelFactory(
            app.container.userRepository,
            app.container.listingRepository,
            app.container.favouriteRepository,
            app.container.sessionManager
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentRegisterBinding.bind(view)

        binding.btnRegister.setOnClickListener {
            val fullName = binding.etFullName.text?.toString()?.trim() ?: ""
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val phone = binding.etPhone.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""

            var isValid = true

            if (fullName.isEmpty()) {
                binding.tilFullName.error = "Name cannot be empty"
                isValid = false
            } else {
                binding.tilFullName.error = null
            }

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.tilEmail.error = "Enter a valid email"
                isValid = false
            } else {
                binding.tilEmail.error = null
            }

            if (phone.isEmpty() || phone.length < 7) {
                binding.tilPhone.error = "Enter a valid phone number"
                isValid = false
            } else {
                binding.tilPhone.error = null
            }

            if (password.isEmpty() || password.length < 6) {
                binding.tilPassword.error = "Password must be at least 6 characters"
                isValid = false
            } else {
                binding.tilPassword.error = null
            }

            if (!isValid) return@setOnClickListener

            val role = if (binding.rbLandlord.isChecked) {
                UserRole.LANDLORD
            } else {
                UserRole.HOUSE_HUNTER
            }

            viewModel.register(
                fullName = fullName,
                email = email,
                phone = phone,
                password = password,
                role = role
            )
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().popBackStack()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.registerState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            showMessage("Account created successfully!")
                            findNavController().navigate(R.id.action_register_to_home)
                        }
                        is UiState.Error -> {
                            binding.progressBar.visibility = View.GONE
                            showMessage(state.message)
                        }
                    }
                }
            }
        }
    }
}
