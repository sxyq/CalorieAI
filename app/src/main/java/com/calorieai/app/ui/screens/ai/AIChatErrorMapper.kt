package com.calorieai.app.ui.screens.ai

import com.calorieai.app.service.ai.common.AIErrorCategory
import com.calorieai.app.service.ai.common.AIErrorClassifier
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AIChatErrorMapper @Inject constructor() {
    fun map(error: Throwable): String {
        val classified = AIErrorClassifier.classify(error)
        val message = error.message.orEmpty()

        if (message.contains("未配置AI服务")) {
            return "未配置AI服务，请先在设置中配置AI服务。"
        }
        if (message.contains("API调用次数已用完")) {
            return message
        }

        return when (classified.category) {
            AIErrorCategory.NETWORK -> classified.userMessage
            AIErrorCategory.HTTP -> {
                val detail = classified.detail.take(120).trim()
                if (detail.isBlank()) {
                    classified.userMessage
                } else {
                    "${classified.userMessage}: $detail"
                }
            }
            AIErrorCategory.PARSE,
            AIErrorCategory.VALIDATION -> "AI返回结果不稳定，请重试或调整描述后再试。"
            AIErrorCategory.UNKNOWN -> {
                if (message.isNotBlank()) {
                    "抱歉，发生错误：$message"
                } else {
                    classified.userMessage
                }
            }
        }
    }
}
