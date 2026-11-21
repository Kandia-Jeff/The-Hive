package com.example.thehive

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.thehive.data.local.entity.UserRole
import com.example.thehive.databinding.ActivityMainBinding
import com.example.thehive.util.SessionManager
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Keep content clear of the status bar and navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val app = application as HiveApplication
        val sessionManager = app.container.sessionManager
        val userRepository = app.container.userRepository

        // Find the navigation host and connect the bottom bar to it
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment
        val navController = navHost.navController
        binding.bottomNav.setupWithNavController(navController)

        // If user is already logged in, start on homeFragment instead of loginFragment
        if (sessionManager.isLoggedIn()) {
            navController.navigate(R.id.action_login_to_home)
        }

        // Hide the bottom bar on the login and register screens, and update role-based tabs
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNav.isVisible =
                destination.id != R.id.loginFragment &&
                        destination.id != R.id.registerFragment

            if (destination.id == R.id.loginFragment && sessionManager.isLoggedIn()) {
                navController.navigate(R.id.action_login_to_home)
            }

            // Hide the My Listings tab when the user is a house hunter
            lifecycleScope.launch {
                val userId = sessionManager.getUserId()
                if (userId != SessionManager.NO_USER) {
                    val user = userRepository.findById(userId)
                    val isLandlord = user?.role == UserRole.LANDLORD
                    binding.bottomNav.menu.findItem(R.id.myListingsFragment)?.isVisible = isLandlord
                } else {
                    binding.bottomNav.menu.findItem(R.id.myListingsFragment)?.isVisible = false
                }
            }
        }
    }
}
