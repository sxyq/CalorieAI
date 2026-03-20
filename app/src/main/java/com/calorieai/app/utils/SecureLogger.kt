package com.calorieai.app.utils

import android.util.Log
import com.calorieai.app.BuildConfig

/**
 * 安全日志工具类
 * 生产环境自动过滤敏感信息
 */
object SecureLogger {
    private const val MAX_LOG_LENGTH = 4000
    private const val MAX_ERROR_BODY_LENGTH = 200
    
    // 敏感字段关键词
    private val SENSITIVE_KEYWORDS = listOf(
        "apiKey", "api_key", "key", "token", "password", "secret",
        "authorization", "bearer", "credential"
    )
    
    /**
     * 调试日志
     */
    fun d(tag: String, message: String) = log(Log.DEBUG, tag, message)
    
    /**
     * 信息日志
     */
    fun i(tag: String, message: String) = log(Log.INFO, tag, message)
    
    /**
     * 警告日志
     */
    fun w(tag: String, message: String) = log(Log.WARN, tag, message)
    
    /**
     * 错误日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) =
        log(Log.ERROR, tag, message, throwable)

    /**
     * 结构化事件日志，便于故障精确定位
     */
    fun event(tag: String, event: String, vararg fields: Pair<String, Any?>) {
        val payload = buildString {
            append(event)
            if (fields.isNotEmpty()) {
                append(" | ")
                append(
                    fields.joinToString(" | ") { (key, value) ->
                        "$key=${value ?: "null"}"
                    }
                )
            }
        }
        i(tag, payload)
    }
    
    /**
     * API错误日志 - 安全处理响应体
     */
    fun logApiError(tag: String, code: Int, errorBody: String?) {
        if (BuildConfig.DEBUG) {
            // 调试模式显示完整错误
            e(tag, "API错误($code): ${errorBody?.take(MAX_ERROR_BODY_LENGTH)}")
        } else {
            // 生产环境只显示状态码
            e(tag, "API请求失败，错误码: $code")
        }
    }
    
    /**
     * 清理消息中的敏感信息
     */
    private fun sanitizeMessage(message: String): String {
        var sanitized = message
        
        // 移除敏感字段值
        SENSITIVE_KEYWORDS.forEach { keyword ->
            // 匹配 "key": "value" 或 key=value 格式
            val patterns = listOf(
                Regex("(\"?$keyword\"?\\s*[:=]\\s*\")[^\"]*(\")", RegexOption.IGNORE_CASE),
                Regex("($keyword\\s*=\\s*)[^\\s&]+", RegexOption.IGNORE_CASE)
            )
            patterns.forEach { pattern ->
                sanitized = sanitized.replace(pattern) { matchResult ->
                    matchResult.value.replace(Regex(":[^\"]*(?=\")|=[^\\s&]+"), ": ***REDACTED***")
                }
            }
        }
        
        return sanitized
    }

    private fun log(priority: Int, tag: String, message: String, throwable: Throwable? = null) {
        val sanitized = sanitizeMessage(message)
        val normalized = if (BuildConfig.DEBUG) sanitized else sanitized.take(MAX_LOG_LENGTH)
        val chunks = normalized.chunked(MAX_LOG_LENGTH)

        if (chunks.isEmpty()) {
            logInternal(priority, tag, "", throwable)
            return
        }

        chunks.forEachIndexed { index, chunk ->
            val withIndex = if (chunks.size > 1) {
                "(${index + 1}/${chunks.size}) $chunk"
            } else {
                chunk
            }
            val shouldAttachThrowable = throwable != null && index == chunks.lastIndex
            logInternal(priority, tag, withIndex, throwable.takeIf { shouldAttachThrowable })
        }
    }

    private fun logInternal(priority: Int, tag: String, message: String, throwable: Throwable?) {
        when (priority) {
            Log.ERROR -> Log.e(tag, message, throwable)
            Log.WARN -> Log.w(tag, message, throwable)
            Log.INFO -> Log.i(tag, message, throwable)
            Log.DEBUG -> {
                if (BuildConfig.DEBUG) {
                    Log.d(tag, message, throwable)
                }
            }
            else -> {
                if (BuildConfig.DEBUG) {
                    Log.v(tag, message, throwable)
                }
            }
        }
    }
}
