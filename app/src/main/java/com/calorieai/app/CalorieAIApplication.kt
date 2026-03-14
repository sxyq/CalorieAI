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
        // 延迟初始化非关键配置，避免阻塞启动
        initializeDefaultAIConfigLazy()
    }

    private fun initializeDefaultAIConfigLazy() {
        // 延迟2秒后初始化，让UI先渲染
        CoroutineScope(Dispatchers.IO).launch {
            kotlinx.coroutines.delay(2000)
            try {
                aiDefaultConfigInitializer.initializeDefaultConfig()
            } catch (e: Exception) {
                // 捕获所有异常，避免应用崩溃
                android.util.Log.e("CalorieAI", "初始化AI配置失败", e)
            }
        }
    }
}
