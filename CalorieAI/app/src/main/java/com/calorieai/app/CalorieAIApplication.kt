package com.calorieai.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class CalorieAIApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // 初始化应用
    }
}
