package com.calorieai.app.service.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Singleton
class AppUpdateManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val appUpdateService: AppUpdateService
) {
    private val workManager by lazy { WorkManager.getInstance(context) }

    /**
     * Returns a previously downloaded, verified APK for the requested version.
     * This lets the update dialog recover after an OEM permission/settings activity
     * recreates MainActivity.
     */
    fun findDownloadedApk(versionCode: Int): String? {
        if (versionCode <= 0) return null
        val file = File(context.filesDir, "updates/$versionCode/CalorieAI-$versionCode.apk")
        return file.takeIf { it.isFile && it.length() > 0L }?.absolutePath
    }

    suspend fun checkForUpdate(): AppUpdateInfo? = withContext(Dispatchers.IO) {
        runCatching {
            val remote = appUpdateService.fetchUpdateInfo() ?: return@runCatching null
            val currentCode = currentVersionCode()
            if (remote.latestVersionCode <= currentCode) return@runCatching null
            remote.copy(
                isMandatory = remote.forceUpdate || currentCode < remote.minSupportedVersionCode
            )
        }.getOrNull()
    }

    fun startDownload(updateInfo: AppUpdateInfo): UUID {
        val request = OneTimeWorkRequestBuilder<AppUpdateDownloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                workDataOf(
                    AppUpdateDownloadWorker.KEY_URL to updateInfo.downloadUrl,
                    AppUpdateDownloadWorker.KEY_VERSION_CODE to updateInfo.latestVersionCode,
                    AppUpdateDownloadWorker.KEY_EXPECTED_SIZE to updateInfo.apkSize,
                    AppUpdateDownloadWorker.KEY_EXPECTED_SHA256 to updateInfo.apkSha256
                )
            )
            .addTag(AppUpdateDownloadWorker.TAG)
            .build()

        workManager.enqueueUniqueWork(
            AppUpdateDownloadWorker.uniqueWorkName(updateInfo.latestVersionCode),
            ExistingWorkPolicy.REPLACE,
            request
        )
        return request.id
    }

    fun observeDownload(workId: UUID): Flow<AppUpdateDownloadState> {
        return workManager.getWorkInfoByIdFlow(workId).map { workInfo ->
            workInfo.toDownloadState()
        }
    }

    suspend fun installDownloadedApk(apkPath: String): InstallResult = withContext(Dispatchers.IO) {
        val file = runCatching { File(apkPath).canonicalFile }.getOrNull()
            ?: return@withContext InstallResult.Failed("安装文件路径无效。")
        val updatesRoot = runCatching {
            File(context.filesDir, "updates").canonicalFile
        }.getOrNull() ?: return@withContext InstallResult.Failed("更新目录不可用。")
        if (!file.path.startsWith(updatesRoot.path + File.separator) || !file.isFile) {
            return@withContext InstallResult.Failed("安装文件位置无效，请重试。")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            return@withContext try {
                withContext(Dispatchers.Main) {
                    context.startActivity(
                        Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}")
                        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
                InstallResult.PermissionRequired
            } catch (_: Throwable) {
                InstallResult.Failed("请在系统设置中允许本应用安装未知来源应用。")
            }
        }

        val installer = context.packageManager.packageInstaller
        var sessionId = -1
        var commitRequested = false
        return@withContext try {
            val params = android.content.pm.PackageInstaller.SessionParams(
                android.content.pm.PackageInstaller.SessionParams.MODE_FULL_INSTALL
            ).apply {
                setAppPackageName(context.packageName)
                setSize(file.length())
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    setRequireUserAction(
                        android.content.pm.PackageInstaller.SessionParams.USER_ACTION_REQUIRED
                    )
                }
            }
            sessionId = installer.createSession(params)
            installer.openSession(sessionId).use { session ->
                file.inputStream().use { input ->
                    session.openWrite("base.apk", 0L, file.length()).use { output ->
                        input.copyTo(output)
                        session.fsync(output)
                    }
                }
                val callbackIntent = Intent(context, AppUpdateInstallActivity::class.java)
                    .setAction(AppUpdateInstallActivity.ACTION_INSTALL_RESULT)
                    .putExtra(AppUpdateInstallActivity.EXTRA_SESSION_ID, sessionId)
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    sessionId,
                    callbackIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            // PackageInstaller fills status extras into the callback on Android 12+.
                            PendingIntent.FLAG_MUTABLE
                        } else {
                            0
                        }
                )
                session.commit(pendingIntent.intentSender)
            }
            commitRequested = true
            InstallResult.Started
        } catch (error: Throwable) {
            InstallResult.Failed(error.message ?: "安装启动失败，请重试。")
        } finally {
            if (!commitRequested && sessionId >= 0) {
                runCatching { installer.abandonSession(sessionId) }
            }
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

    private fun WorkInfo?.toDownloadState(): AppUpdateDownloadState {
        if (this == null) return AppUpdateDownloadState.Idle
        val progressData = progress
        return when (state) {
            WorkInfo.State.ENQUEUED, WorkInfo.State.BLOCKED -> AppUpdateDownloadState.Queued
            WorkInfo.State.RUNNING -> AppUpdateDownloadState.Downloading(
                percent = progressData.getInt(AppUpdateDownloadWorker.KEY_PROGRESS, 0)
                    .coerceIn(0, 100),
                downloadedBytes = progressData.getLong(
                    AppUpdateDownloadWorker.KEY_DOWNLOADED_BYTES,
                    0L
                ),
                totalBytes = progressData.getLong(AppUpdateDownloadWorker.KEY_TOTAL_BYTES, 0L)
            )

            WorkInfo.State.SUCCEEDED -> AppUpdateDownloadState.Ready(
                progressData.getString(AppUpdateDownloadWorker.KEY_APK_PATH)
                    ?: outputData.getString(AppUpdateDownloadWorker.KEY_APK_PATH).orEmpty()
            )

            WorkInfo.State.FAILED -> AppUpdateDownloadState.Failed(
                outputData.getString(AppUpdateDownloadWorker.KEY_ERROR)
                    ?: "下载或校验失败，请重试。"
            )

            WorkInfo.State.CANCELLED -> AppUpdateDownloadState.Failed("下载已取消，请重试。")
        }
    }

    sealed interface InstallResult {
        data object Started : InstallResult
        data object PermissionRequired : InstallResult
        data class Failed(val message: String) : InstallResult
    }
}

sealed interface AppUpdateDownloadState {
    data object Idle : AppUpdateDownloadState
    data object Queued : AppUpdateDownloadState
    data class Downloading(
        val percent: Int,
        val downloadedBytes: Long,
        val totalBytes: Long
    ) : AppUpdateDownloadState

    data class Ready(val apkPath: String) : AppUpdateDownloadState
    data class Failed(val message: String) : AppUpdateDownloadState
}
