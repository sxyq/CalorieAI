package com.calorieai.app.service.update

import android.net.Uri
import com.calorieai.app.BuildConfig

/** Shared validation for the manifest and APK endpoints used by the updater. */
internal object AppUpdateEndpoint {
    private const val CHECK_PATH = "/android/stable/latest.json"
    private val checkUri = parse(BuildConfig.UPDATE_CHECK_URL)
    private val downloadBaseUri = parseBase(BuildConfig.UPDATE_DOWNLOAD_BASE_URL)

    fun isValidCheckUrl(value: String): Boolean {
        val uri = parse(value) ?: return false
        return checkUri != null && sameAuthority(uri, checkUri) && uri.path == CHECK_PATH
    }

    fun isValidDownloadUrl(value: String, versionName: String): Boolean {
        val uri = parse(value) ?: return false
        val expectedPath = "/releases/$versionName/CalorieAI-v$versionName.apk"
        return downloadBaseUri != null && sameAuthority(uri, downloadBaseUri) &&
            uri.path == expectedPath
    }

    private fun parseBase(value: String): Uri? {
        val uri = parse(value) ?: return null
        return uri.takeIf { it.path.isNullOrEmpty() }
    }

    private fun parse(value: String): Uri? {
        val uri = runCatching { Uri.parse(value.trim()) }.getOrNull() ?: return null
        if (uri.scheme.isNullOrBlank() || uri.host.isNullOrBlank() || uri.authority.isNullOrBlank()) {
            return null
        }
        if (uri.userInfo != null || uri.query != null || uri.fragment != null) return null
        return uri
    }

    private fun sameAuthority(actual: Uri, expectedBase: Uri): Boolean {
        return actual.scheme.equals(expectedBase.scheme, ignoreCase = true) &&
            actual.host.equals(expectedBase.host, ignoreCase = true) &&
            actual.port == expectedBase.port &&
            actual.authority == expectedBase.authority
    }
}
