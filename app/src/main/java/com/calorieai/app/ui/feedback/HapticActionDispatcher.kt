package com.calorieai.app.ui.feedback

import androidx.compose.runtime.Stable
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.calorieai.app.data.model.UserSettings

@Stable
class HapticActionDispatcher(
    private val hapticFeedback: HapticFeedback,
    private val settings: UserSettings?
) {
    fun dispatch(action: AppHapticType) {
        if (!isHapticEnabled()) return
        val hapticType = when (action) {
            AppHapticType.CLICK -> HapticFeedbackType.TextHandleMove
            AppHapticType.LONG_PRESS -> HapticFeedbackType.LongPress
            AppHapticType.CONFIRM -> HapticFeedbackType.LongPress
        }
        runCatching {
            hapticFeedback.performHapticFeedback(hapticType)
        }
    }

    private fun isHapticEnabled(): Boolean {
        val current = settings ?: return true
        if (!current.enableVibration) return false
        return when (current.feedbackType.uppercase()) {
            "NONE", "SOUND" -> false
            else -> true
        }
    }
}
