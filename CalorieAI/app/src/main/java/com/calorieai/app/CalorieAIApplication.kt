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
    
    override fun onCreate() {
        super.onCreate()
        // 初始化应用
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
