package com.calorieai.app.service.update

import com.calorieai.app.BuildConfig
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class AppUpdateService @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    suspend fun fetchUpdateInfo(): AppUpdateInfo? {
        val endpoint = BuildConfig.UPDATE_CHECK_URL.trim()
        if (endpoint.isBlank()) return null

        val request = Request.Builder()
            .url(endpoint)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) return null
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return null

            val root = runCatching {
                JsonParser.parseString(body).asJsonObject
            }.getOrNull() ?: return null

            val latestCode = root.readInt("latestVersionCode")
                ?: root.readInt("latest_version_code")
                ?: root.readInt("versionCode")
                ?: root.readInt("version_code")
                ?: return null

            val latestName = root.readString("latestVersionName")
                ?: root.readString("latest_version_name")
                ?: root.readString("versionName")
                ?: root.readString("version_name")
                ?: latestCode.toString()

            val downloadUrl = root.readString("downloadUrl")
                ?: root.readString("download_url")
                ?: return null

            val changelog = root.readString("changelog")
                ?: root.readString("changeLog")
                ?: root.readString("releaseNotes")
                ?: "发现新版本，建议立即更新。"

            val forceUpdate = root.readBoolean("forceUpdate")
                ?: root.readBoolean("force_update")
                ?: false

            return AppUpdateInfo(
                latestVersionCode = latestCode,
                latestVersionName = latestName,
                downloadUrl = downloadUrl,
                changelog = changelog,
                forceUpdate = forceUpdate
            )
        }
    }

    private fun JsonObject.readString(key: String): String? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        return runCatching { element.asString }.getOrNull()?.trim()?.takeIf { it.isNotBlank() }
    }

    private fun JsonObject.readInt(key: String): Int? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        return runCatching { element.asInt }.getOrNull()
    }

    private fun JsonObject.readBoolean(key: String): Boolean? {
        val element = get(key) ?: return null
        if (element.isJsonNull) return null
        return runCatching { element.asBoolean }.getOrNull()
    }
}
