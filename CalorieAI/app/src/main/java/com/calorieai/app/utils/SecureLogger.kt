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
    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, sanitizeMessage(message))
        }
    }
    
    /**
     * 信息日志
     */
    fun i(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.i(tag, sanitizeMessage(message))
        }
    }
    
    /**
     * 警告日志
     */
    fun w(tag: String, message: String) {
        Log.w(tag, sanitizeMessage(message))
    }
    
    /**
     * 错误日志
     */
    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, sanitizeMessage(message), throwable)
        } else {
            // 生产环境只记录简短错误信息
            Log.e(tag, message.take(100), throwable)
        }
    }
    
    /**
     * API错误日志 - 安全处理响应体
     */
    fun logApiError(tag: String, code: Int, errorBody: String?) {
        if (BuildConfig.DEBUG) {
            // 调试模式显示完整错误
            Log.e(tag, "API错误($code): ${errorBody?.take(MAX_ERROR_BODY_LENGTH)}")
        } else {
            // 生产环境只显示状态码
            Log.e(tag, "API请求失败，错误码: $code")
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
    
    /**
     * 截断长消息
     */
    private fun truncateMessage(message: String): String {
        return if (message.length > MAX_LOG_LENGTH) {
            message.take(MAX_LOG_LENGTH) + "...[截断]"
        } else {
            message
        }
    }
}
