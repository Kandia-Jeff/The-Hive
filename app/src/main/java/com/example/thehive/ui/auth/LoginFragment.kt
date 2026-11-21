package com.example.thehive.ui.auth

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.thehive.HiveApplication
import com.example.thehive.R
import com.example.thehive.databinding.FragmentLoginBinding
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.AuthViewModel
import com.example.thehive.viewmodel.UiState
import com.example.thehive.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class LoginFragment : Fragment(R.layout.fragment_login) {

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
        val binding = FragmentLoginBinding.bind(view)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text?.toString()?.trim() ?: ""
            val password = binding.etPassword.text?.toString() ?: ""

            if (email.isEmpty()) {
                binding.tilEmail.error = "Email cannot be empty"
                return@setOnClickListener
            } else {
                binding.tilEmail.error = null
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Password cannot be empty"
                return@setOnClickListener
            } else {
                binding.tilPassword.error = null
            }

            viewModel.login(email, password)
        }

        binding.tvRegisterLink.setOnClickListener {
            findNavController().navigate(R.id.action_login_to_register)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.loginState.collect { state ->
                    when (state) {
                        is UiState.Loading -> {
                            binding.progressBar.visibility = View.GONE
                        }
                        is UiState.Success -> {
                            binding.progressBar.visibility = View.GONE
                            showMessage("Welcome back, ${state.data.fullName}!")
                            findNavController().navigate(R.id.action_login_to_home)
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
