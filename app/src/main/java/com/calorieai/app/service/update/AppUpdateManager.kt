package com.calorieai.app.service.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appUpdateService: AppUpdateService
) {
    private val prefs by lazy {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    suspend fun checkForUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        val remote = appUpdateService.fetchUpdateInfo() ?: return@withContext null
        if (remote.latestVersionCode <= currentVersionCode()) return@withContext null

        val ignoredVersion = prefs.getInt(KEY_IGNORED_VERSION_CODE, -1)
        if (!remote.forceUpdate && ignoredVersion == remote.latestVersionCode) {
            return@withContext null
        }
        remote
    }

    fun ignoreVersion(versionCode: Int) {
        prefs.edit().putInt(KEY_IGNORED_VERSION_CODE, versionCode).apply()
    }

    fun openDownloadPage(updateInfo: AppUpdateInfo): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateInfo.downloadUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (_: Throwable) {
            false
        }
    }

    private fun currentVersionCode(): Int {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo.longVersionCode.toInt()
        } else {
            @Suppress("DEPRECATION")
            packageInfo.versionCode
        }
    }

    companion object {
        private const val PREF_NAME = "app_update_prefs"
        private const val KEY_IGNORED_VERSION_CODE = "ignored_version_code"
    }
}
