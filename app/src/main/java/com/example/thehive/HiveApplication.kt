package com.example.thehive

import android.app.Application
import com.example.thehive.util.SeedData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HiveApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)

        CoroutineScope(Dispatchers.IO).launch {
            SeedData.seedIfEmpty(container.userRepository, container.listingRepository)
        }
    }
}