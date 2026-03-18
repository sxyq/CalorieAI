package com.calorieai.app.service.tutorial

import android.content.Context
import android.content.SharedPreferences
import com.calorieai.app.data.model.TutorialConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 教程管理器
 * 管理引导教程的显示状态和进度
 */
@Singleton
class TutorialManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "tutorial_prefs",
        Context.MODE_PRIVATE
    )

    private val _isTutorialActive = MutableStateFlow(false)
    val isTutorialActive: StateFlow<Boolean> = _isTutorialActive.asStateFlow()

    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    /**
     * 检查是否需要显示教程
     */
    fun shouldShowTutorial(): Boolean {
        val completed = prefs.getBoolean(TutorialConfig.PREFS_TUTORIAL_COMPLETED, false)
        val version = prefs.getInt(TutorialConfig.PREFS_TUTORIAL_VERSION, 0)
        return !completed || version < TutorialConfig.CURRENT_TUTORIAL_VERSION
    }

    /**
     * 开始教程
     */
    fun startTutorial() {
        _currentStep.value = 0
        _isTutorialActive.value = true
    }

    /**
     * 下一步
     */
    fun nextStep() {
        val next = _currentStep.value + 1
        if (next < TutorialConfig.steps.size) {
            _currentStep.value = next
        } else {
            completeTutorial()
        }
    }

    /**
     * 上一步
     */
    fun previousStep() {
        val prev = _currentStep.value - 1
        if (prev >= 0) {
            _currentStep.value = prev
        }
    }

    fun skipTutorial() = finishTutorial(skipped = true)

    fun completeTutorial() = finishTutorial(skipped = false)

    private fun finishTutorial(skipped: Boolean) {
        _isTutorialActive.value = false
        prefs.edit().apply {
            putBoolean(TutorialConfig.PREFS_TUTORIAL_COMPLETED, true)
            putInt(TutorialConfig.PREFS_TUTORIAL_VERSION, TutorialConfig.CURRENT_TUTORIAL_VERSION)
            putBoolean("tutorial_skipped", skipped)
            apply()
        }
    }

    /**
     * 重置教程（用于测试）
     */
    fun resetTutorial() {
        prefs.edit().apply {
            remove(TutorialConfig.PREFS_TUTORIAL_COMPLETED)
            remove(TutorialConfig.PREFS_TUTORIAL_VERSION)
            remove("tutorial_skipped")
            apply()
        }
    }

    /**
     * 获取当前步骤信息
     */
    fun getCurrentStepInfo(): TutorialStepInfo {
        val step = TutorialConfig.steps.getOrNull(_currentStep.value)
        return TutorialStepInfo(
            step = step,
            stepNumber = _currentStep.value + 1,
            totalSteps = TutorialConfig.steps.size,
            isFirstStep = _currentStep.value == 0,
            isLastStep = _currentStep.value == TutorialConfig.steps.size - 1
        )
    }
}

data class TutorialStepInfo(
    val step: com.calorieai.app.data.model.TutorialStep?,
    val stepNumber: Int,
    val totalSteps: Int,
    val isFirstStep: Boolean,
    val isLastStep: Boolean
)
