package com.calorieai.app.ui.feedback

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

enum class AppHapticType {
    CLICK,
    LONG_PRESS,
    CONFIRM
}

@Stable
class AppHapticController(
    private val hapticFeedback: HapticFeedback,
    private val settings: UserSettings?
) {
    fun click() = perform(AppHapticType.CLICK)

    fun longPress() = perform(AppHapticType.LONG_PRESS)

    fun confirm() = perform(AppHapticType.CONFIRM)

    fun perform(type: AppHapticType) {
        if (!isVibrationEnabled()) return
        val hapticType = when (type) {
            AppHapticType.CLICK -> HapticFeedbackType.TextHandleMove
            AppHapticType.LONG_PRESS -> HapticFeedbackType.LongPress
            AppHapticType.CONFIRM -> HapticFeedbackType.LongPress
        }
        runCatching {
            hapticFeedback.performHapticFeedback(hapticType)
        }
    }

    private fun isVibrationEnabled(): Boolean {
        val current = settings ?: return true
        if (!current.enableVibration) return false
        return when (current.feedbackType.uppercase()) {
            "NONE", "SOUND" -> false
            else -> true
        }
    }
}

@Composable
fun rememberAppHapticController(): AppHapticController {
    val context = LocalContext.current.applicationContext
    val haptic = LocalHapticFeedback.current
    val repository = remember(context) { appHapticEntryPoint(context).userSettingsRepository() }
    val settings by repository.getSettings().collectAsState(initial = null)
    return remember(haptic, settings) {
        AppHapticController(
            hapticFeedback = haptic,
            settings = settings
        )
    }
}

private fun appHapticEntryPoint(context: Context): AppHapticEntryPoint {
    return EntryPointAccessors.fromApplication(context, AppHapticEntryPoint::class.java)
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AppHapticEntryPoint {
    fun userSettingsRepository(): UserSettingsRepository
}
