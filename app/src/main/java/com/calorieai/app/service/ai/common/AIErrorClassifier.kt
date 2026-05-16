package com.calorieai.app.service.ai.common

import java.io.InterruptedIOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class AIErrorCategory {
    NETWORK,
    HTTP,
    PARSE,
    VALIDATION,
    UNKNOWN
}

data class AIErrorInfo(
    val category: AIErrorCategory,
    val retryEligible: Boolean,
    val userMessage: String,
    val detail: String
) {
    fun toLogMessage(): String {
        return "[category=${category.name}][retryEligible=$retryEligible] $detail"
    }
}

object AIErrorClassifier {
    fun classify(error: Throwable?): AIErrorInfo {
        val safeError = error ?: return AIErrorInfo(
            category = AIErrorCategory.UNKNOWN,
            retryEligible = false,
            userMessage = "AI 调用失败，请稍后重试",
            detail = "unknown error"
        )

        if (safeError is AIApiException) {
            val detail = safeError.responseBody?.take(240) ?: safeError.message.orEmpty()
            return when (safeError.category) {
                AIErrorCategory.NETWORK -> AIErrorInfo(
                    category = AIErrorCategory.NETWORK,
                    retryEligible = safeError.retryEligible,
                    userMessage = "网络或模型服务不可达，请先在 AI 配置页做连接测试",
                    detail = detail.ifBlank { "network unreachable" }
                )

                AIErrorCategory.HTTP -> AIErrorInfo(
                    category = if (safeError.httpCode == 0) AIErrorCategory.NETWORK else AIErrorCategory.HTTP,
                    retryEligible = safeError.retryEligible,
                    userMessage = if (safeError.httpCode == 0) {
                        "网络或模型服务不可达，请先在 AI 配置页做连接测试"
                    } else if (safeError.httpCode == 401 || safeError.httpCode == 403) {
                        "AI API 密钥无效或已失效，请在 AI 配置页更新密钥"
                    } else {
                        "AI 服务响应异常(HTTP ${safeError.httpCode})"
                    },
                    detail = detail.ifBlank {
                        if (safeError.httpCode == 0) "http 0" else "http ${safeError.httpCode}"
                    }
                )

                else -> AIErrorInfo(
                    category = safeError.category,
                    retryEligible = safeError.retryEligible,
                    userMessage = safeError.message ?: "AI 调用失败",
                    detail = detail.ifBlank { safeError.javaClass.simpleName }
                )
            }
        }

        val root = rootCause(safeError)
        val detail = root.message ?: safeError.message ?: safeError.javaClass.simpleName
        return when (root) {
            is UnknownHostException -> AIErrorInfo(
                category = AIErrorCategory.NETWORK,
                retryEligible = false,
                userMessage = "网络或模型服务不可达，请先在 AI 配置页做连接测试",
                detail = detail
            )

            is SocketTimeoutException, is ConnectException, is InterruptedIOException -> AIErrorInfo(
                category = AIErrorCategory.NETWORK,
                retryEligible = false,
                userMessage = "网络连接超时，请检查网络或模型服务后重试",
                detail = detail
            )

            else -> AIErrorInfo(
                category = AIErrorCategory.UNKNOWN,
                retryEligible = false,
                userMessage = safeError.message ?: "AI 调用失败，请稍后重试",
                detail = detail
            )
        }
    }

    private fun rootCause(error: Throwable): Throwable {
        var current = error
        while (current.cause != null && current.cause !== current) {
            current = current.cause!!
        }
        return current
    }
}
