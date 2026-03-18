package com.calorieai.app

import android.app.Application
import com.calorieai.app.service.ai.AIDefaultConfigInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CalorieAIApplication : Application() {
    
    @Inject
    lateinit var aiDefaultConfigInitializer: AIDefaultConfigInitializer
    
    companion object {
        @Volatile
        var isOnboardingCompleted: Boolean? = null
            internal set
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeDefaultAIConfig()
    }
    
    private fun initializeDefaultAIConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                aiDefaultConfigInitializer.initializeDefaultConfig()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
