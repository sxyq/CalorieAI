package com.calorieai.app.ui.screens.settings

import com.calorieai.app.service.voice.VoiceModelManager
import kotlin.math.abs

internal data class VoiceModelUiProgress(
    val stage: VoiceModelManager.OperationStage,
    val percent: Int,
    val message: String?
)

internal class VoiceModelStateMachine(
    private val minUpdateIntervalMs: Long = 120L,
    private val minPercentStep: Int = 2
) {
    private var lastStage: VoiceModelManager.OperationStage = VoiceModelManager.OperationStage.IDLE
    private var lastPercent: Int = -1
    private var lastMessage: String? = null
    private var lastDispatchAtMillis: Long = 0L

    fun reset() {
        lastStage = VoiceModelManager.OperationStage.IDLE
        lastPercent = -1
        lastMessage = null
        lastDispatchAtMillis = 0L
    }

    fun reduce(
        progress: VoiceModelManager.OperationProgress,
        nowElapsedMillis: Long
    ): VoiceModelUiProgress? {
        val forceDispatch = isTerminal(progress.stage) || progress.percent == 0 || progress.percent == 100
        val stageChanged = progress.stage != lastStage
        val percentDelta = abs(progress.percent - lastPercent)
        val messageChanged = progress.message != lastMessage
        val intervalPassed = nowElapsedMillis - lastDispatchAtMillis >= minUpdateIntervalMs

        if (!forceDispatch && !stageChanged && !intervalPassed && percentDelta < minPercentStep && !messageChanged) {
            return null
        }

        lastStage = progress.stage
        lastPercent = progress.percent
        lastMessage = progress.message
        lastDispatchAtMillis = nowElapsedMillis

        return VoiceModelUiProgress(
            stage = progress.stage,
            percent = progress.percent,
            message = progress.message
        )
    }

    private fun isTerminal(stage: VoiceModelManager.OperationStage): Boolean {
        return stage == VoiceModelManager.OperationStage.COMPLETED ||
            stage == VoiceModelManager.OperationStage.FAILED
    }
}
