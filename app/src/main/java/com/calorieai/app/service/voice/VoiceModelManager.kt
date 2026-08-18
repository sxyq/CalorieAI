package com.calorieai.app.service.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isModelInstalled(): Boolean {
        return runCatching {
            context.assets.open(MODEL_ASSET_PATH).use { }
            context.assets.open(TOKENS_ASSET_PATH).use { }
            true
        }.getOrDefault(false)
    }

    companion object {
        const val MODEL_ASSET_PATH = "sensevoice/model.int8.onnx"
        const val TOKENS_ASSET_PATH = "sensevoice/tokens.txt"
        const val MODEL_LABEL = "阿里 SenseVoiceSmall（内置离线模型）"
    }
}
