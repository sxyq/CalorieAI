package com.calorieai.app

import android.app.Application
import com.calorieai.app.service.ai.AIDefaultConfigInitializer
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class CalorieAIApplication : Application(), Configuration.Provider {
    
    @Inject
    lateinit var aiDefaultConfigInitializer: AIDefaultConfigInitializer

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    
    companion object {
        @Volatile
        var isOnboardingCompleted: Boolean? = null
            internal set
    }
    
    override fun onCreate() {
        super.onCreate()
        initializeDefaultAIConfig()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    
    private fun initializeDefaultAIConfig() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 冷启动阶段优先保证首屏流畅，默认AI配置延迟初始化
                delay(1500)
                aiDefaultConfigInitializer.initializeDefaultConfig()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
