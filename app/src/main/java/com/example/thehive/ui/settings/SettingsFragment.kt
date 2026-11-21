package com.example.thehive.ui.settings

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.thehive.HiveApplication
import com.example.thehive.R
import com.example.thehive.databinding.FragmentSettingsBinding
import com.example.thehive.ui.common.showMessage
import com.example.thehive.viewmodel.SettingsViewModel
import com.example.thehive.viewmodel.ViewModelFactory
import kotlinx.coroutines.launch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModels {
        val app = requireActivity().application as HiveApplication
        ViewModelFactory(
            app.container.userRepository,
            app.container.listingRepository,
            app.container.favouriteRepository,
            app.container.sessionManager
        )
    }

    private var binding: FragmentSettingsBinding? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            showMessage("Notification permission granted")
        } else {
            showMessage("Notification permission denied")
        }
        context?.let { viewModel.refreshPermissionStatus(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentSettingsBinding.bind(view)
        this.binding = binding

        val context = requireContext()
        viewModel.loadSettings(context)

        binding.btnGrantPermission.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                showMessage("Notifications are enabled for your OS version")
            }
        }

        binding.switchReminders.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setReminderEnabled(requireContext(), isChecked)
        }

        binding.rgFrequency.setOnCheckedChangeListener { _, checkedId ->
            val days = when (checkedId) {
                R.id.rb3Days -> 3
                R.id.rb7Days -> 7
                R.id.rb14Days -> 14
                R.id.rb30Days -> 30
                else -> 7
            }
            viewModel.setReminderFrequency(requireContext(), days)
        }

        binding.btnTestReminder.setOnClickListener {
            viewModel.triggerTestCheck(requireContext())
            showMessage("Triggered availability check worker")
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.switchReminders.isChecked = state.isReminderEnabled
                    binding.rgFrequency.isEnabled = state.isReminderEnabled
                    for (i in 0 until binding.rgFrequency.childCount) {
                        binding.rgFrequency.getChildAt(i).isEnabled = state.isReminderEnabled
                    }
                    binding.btnTestReminder.isEnabled = state.isReminderEnabled

                    when (state.reminderFrequencyDays) {
                        3 -> binding.rb3Days.isChecked = true
                        7 -> binding.rb7Days.isChecked = true
                        14 -> binding.rb14Days.isChecked = true
                        30 -> binding.rb30Days.isChecked = true
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        binding.cardNotificationPermission.isVisible = true
                        if (state.hasNotificationPermission) {
                            binding.tvPermissionStatus.text = "Notification permission is granted."
                            binding.btnGrantPermission.isVisible = false
                        } else {
                            binding.tvPermissionStatus.text = "Notification permission is required to receive reminders."
                            binding.btnGrantPermission.isVisible = true
                        }
                    } else {
                        binding.cardNotificationPermission.isVisible = false
                    }

                    binding.cardLandlordReminders.isVisible = state.isLandlord
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
