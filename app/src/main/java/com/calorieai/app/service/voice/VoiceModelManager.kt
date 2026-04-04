package com.calorieai.app.service.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    enum class VoiceModelPackage(
        val dirName: String,
        val zipName: String,
        val downloadUrl: String,
        val displayName: String,
        val sizeHint: String
    ) {
        LARGE_CN(
            dirName = "vosk-model-cn-0.22",
            zipName = "vosk-model-cn-0.22.zip",
            downloadUrl = "https://alphacephei.com/vosk/models/vosk-model-cn-0.22.zip",
            displayName = "高精度中文模型",
            sizeHint = "约 300-400MB"
        ),
        SMALL_CN(
            dirName = "vosk-model-small-cn-0.22",
            zipName = "vosk-model-small-cn-0.22.zip",
            downloadUrl = "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip",
            displayName = "轻量中文模型",
            sizeHint = "约几十MB"
        )
    }

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.MINUTES)
        .build()

    fun isModelInstalled(): Boolean {
        return getInstalledPackage() != null
    }

    fun getInstalledPackage(): VoiceModelPackage? {
        return VoiceModelPackage.entries.firstOrNull { pkg ->
            isModelReady(File(context.filesDir, pkg.dirName))
        }
    }

    fun getInstalledModelDir(): File? {
        val pkg = getInstalledPackage() ?: return null
        return File(context.filesDir, pkg.dirName)
    }

    suspend fun downloadAndInstallModel(pkg: VoiceModelPackage): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                val modelDir = File(context.filesDir, pkg.dirName)
                val tempZip = File(context.cacheDir, pkg.zipName)
                if (tempZip.exists()) {
                    tempZip.delete()
                }

                val request = Request.Builder()
                    .url(pkg.downloadUrl)
                    .get()
                    .build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        throw IllegalStateException("下载失败(${response.code})")
                    }
                    val body = response.body ?: throw IllegalStateException("下载内容为空")
                    FileOutputStream(tempZip).use { output ->
                        body.byteStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                }

            installFromZip(tempZip, modelDir)
            tempZip.delete()
            Unit
        }
    }
    }

    private fun installFromZip(zipFile: File, modelDir: File) {
        modelDir.deleteRecursively()
        modelDir.mkdirs()

        val extractRoot = modelDir.parentFile ?: modelDir
        val canonicalRoot = extractRoot.canonicalFile
        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry = zis.nextEntry
            while (entry != null) {
                val entryFile = resolveZipEntryTarget(
                    canonicalRoot = canonicalRoot,
                    entryName = entry.name
                )
                if (entry.isDirectory) {
                    entryFile.mkdirs()
                } else {
                    entryFile.parentFile?.mkdirs()
                    FileOutputStream(entryFile).use { out ->
                        zis.copyTo(out)
                    }
                }
                zis.closeEntry()
                entry = zis.nextEntry
            }
        }

        val expectedRoot = File(extractRoot, modelDir.name)
        if (!expectedRoot.exists()) {
            val candidates = extractRoot.listFiles()
                ?.filter { it.isDirectory && it.name.startsWith("vosk-model") }
                .orEmpty()
            val chosen = candidates.firstOrNull()
                ?: throw IllegalStateException("模型解压后目录不存在")

            if (modelDir.exists()) {
                modelDir.deleteRecursively()
            }
            if (!chosen.renameTo(modelDir)) {
                chosen.copyRecursively(modelDir, overwrite = true)
                chosen.deleteRecursively()
            }
        }

        if (!isModelReady(modelDir)) {
            throw IllegalStateException("模型安装不完整")
        }
    }

    private fun resolveZipEntryTarget(canonicalRoot: File, entryName: String): File {
        if (entryName.isBlank()) {
            throw IllegalStateException("模型压缩包包含非法空路径")
        }
        val target = File(canonicalRoot, entryName).canonicalFile
        val rootPath = canonicalRoot.path.trimEnd(File.separatorChar) + File.separator
        if (!target.path.startsWith(rootPath)) {
            throw IllegalStateException("检测到非法压缩路径: $entryName")
        }
        return target
    }

    private fun isModelReady(modelDir: File): Boolean {
        if (!modelDir.exists() || !modelDir.isDirectory) return false
        val required = listOf("am", "conf", "graph")
        return required.all { File(modelDir, it).exists() }
    }

    companion object {}
}
