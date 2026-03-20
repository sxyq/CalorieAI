package com.calorieai.app.service.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

data class WebDavConfig(
    val baseUrl: String,
    val directory: String,
    val fileName: String,
    val username: String,
    val password: String
)

@Singleton
class WebDavBackupService @Inject constructor() {
    private val client = OkHttpClient()
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    suspend fun uploadJson(config: WebDavConfig, json: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val url = buildUrl(config)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(config.username, config.password))
                .put(json.toRequestBody(jsonMediaType))
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("WebDAV上传失败: HTTP ${response.code}")
                }
            }
        }
    }

    suspend fun downloadJson(config: WebDavConfig): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            val url = buildUrl(config)
            val request = Request.Builder()
                .url(url)
                .header("Authorization", Credentials.basic(config.username, config.password))
                .get()
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    throw IllegalStateException("WebDAV下载失败: HTTP ${response.code}")
                }
                response.body?.string() ?: throw IllegalStateException("云备份文件内容为空")
            }
        }
    }

    private fun buildUrl(config: WebDavConfig): String {
        val base = config.baseUrl.trim().trimEnd('/')
        val dir = config.directory.trim().trim('/').takeIf { it.isNotBlank() }
        val file = config.fileName.trim().ifBlank { "calorieai_backup_latest.json" }
        return if (dir != null) "$base/$dir/$file" else "$base/$file"
    }
}
