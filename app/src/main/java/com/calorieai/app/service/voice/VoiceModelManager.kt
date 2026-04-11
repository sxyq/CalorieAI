package com.calorieai.app.service.voice

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

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
            sizeHint = "约300-400MB"
        ),
        SMALL_CN(
            dirName = "vosk-model-small-cn-0.22",
            zipName = "vosk-model-small-cn-0.22.zip",
            downloadUrl = "https://alphacephei.com/vosk/models/vosk-model-small-cn-0.22.zip",
            displayName = "轻量中文模型",
            sizeHint = "约几十MB"
        )
    }

    enum class OperationStage {
        IDLE,
        DOWNLOADING,
        INSTALLING,
        FINALIZING,
        REMOVING,
        COMPLETED,
        FAILED
    }

    data class OperationProgress(
        val stage: OperationStage,
        val percent: Int,
        val message: String
    )

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.MINUTES)
        .build()

    fun isModelInstalled(): Boolean = getInstalledPackage() != null

    fun getInstalledPackage(): VoiceModelPackage? {
        return VoiceModelPackage.entries.firstOrNull { pkg ->
            isModelReady(File(context.filesDir, pkg.dirName))
        }
    }

    fun getInstalledModelDir(): File? {
        val pkg = getInstalledPackage() ?: return null
        return File(context.filesDir, pkg.dirName)
    }

    suspend fun downloadAndInstallModel(
        pkg: VoiceModelPackage,
        onProgress: (OperationProgress) -> Unit = {}
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                report(onProgress, OperationStage.DOWNLOADING, 0, "开始下载 ${pkg.displayName}")

                val modelDir = File(context.filesDir, pkg.dirName)
                val tempZip = File(context.cacheDir, pkg.zipName)
                if (tempZip.exists()) {
                    tempZip.delete()
                }

                downloadZip(pkg, tempZip, onProgress)

                report(onProgress, OperationStage.INSTALLING, 0, "开始安装 ${pkg.displayName}")
                installFromZip(tempZip, modelDir, onProgress)
                tempZip.delete()

                clearOtherPackages(pkg)

                report(onProgress, OperationStage.FINALIZING, 100, "安装完成，正在校验模型")
                if (!isModelReady(modelDir)) {
                    throw IllegalStateException("模型安装不完整")
                }

                report(onProgress, OperationStage.COMPLETED, 100, "模型已安装完成")
                Unit
            }.onFailure { error ->
                report(
                    onProgress,
                    OperationStage.FAILED,
                    0,
                    error.message ?: "语音模型安装失败"
                )
            }
        }
    }

    suspend fun uninstallModel(
        onProgress: (OperationProgress) -> Unit = {}
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            runCatching {
                report(onProgress, OperationStage.REMOVING, 0, "开始删除语音模型")

                val targets = VoiceModelPackage.entries
                    .map { File(context.filesDir, it.dirName) }
                    .filter { it.exists() }

                if (targets.isEmpty()) {
                    report(onProgress, OperationStage.COMPLETED, 100, "未发现已安装模型")
                    return@runCatching Unit
                }

                targets.forEachIndexed { index, file ->
                    file.deleteRecursively()
                    val percent = (((index + 1) * 100f) / targets.size).toInt().coerceIn(1, 100)
                    report(onProgress, OperationStage.REMOVING, percent, "正在删除 ${file.name}")
                }

                VoiceModelPackage.entries.forEach { pkg ->
                    File(context.cacheDir, pkg.zipName).takeIf { it.exists() }?.delete()
                }

                report(onProgress, OperationStage.COMPLETED, 100, "语音模型已删除")
            }.onFailure { error ->
                report(
                    onProgress,
                    OperationStage.FAILED,
                    0,
                    error.message ?: "语音模型删除失败"
                )
            }
        }
    }

    private fun downloadZip(
        pkg: VoiceModelPackage,
        tempZip: File,
        onProgress: (OperationProgress) -> Unit
    ) {
        val request = Request.Builder()
            .url(pkg.downloadUrl)
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("下载失败(${response.code})")
            }
            val body = response.body ?: throw IllegalStateException("下载内容为空")
            val totalBytes = body.contentLength().takeIf { it > 0L }

            body.byteStream().use { input ->
                FileOutputStream(tempZip).use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var downloaded = 0L
                    var lastPercent = -1
                    var read = input.read(buffer)
                    while (read >= 0) {
                        if (read > 0) {
                            output.write(buffer, 0, read)
                            downloaded += read
                            if (totalBytes != null) {
                                val percent = ((downloaded * 100f) / totalBytes)
                                    .toInt()
                                    .coerceIn(0, 100)
                                if (percent != lastPercent) {
                                    lastPercent = percent
                                    report(
                                        onProgress,
                                        OperationStage.DOWNLOADING,
                                        percent,
                                        "下载中 ${percent}%"
                                    )
                                }
                            }
                        }
                        read = input.read(buffer)
                    }
                }
            }

            if (totalBytes == null) {
                report(onProgress, OperationStage.DOWNLOADING, 100, "下载完成")
            }
        }
    }

    private fun installFromZip(
        zipFile: File,
        modelDir: File,
        onProgress: (OperationProgress) -> Unit
    ) {
        modelDir.deleteRecursively()
        modelDir.mkdirs()

        val extractRoot = modelDir.parentFile ?: modelDir
        val canonicalRoot = extractRoot.canonicalFile

        val totalUncompressedBytes = ZipFile(zipFile).use { archive ->
            archive.entries().asSequence()
                .filter { !it.isDirectory }
                .map { entry -> entry.size.coerceAtLeast(0L) }
                .sum()
                .coerceAtLeast(1L)
        }

        var extractedBytes = 0L
        var lastPercent = -1

        ZipInputStream(zipFile.inputStream()).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
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
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var read = zis.read(buffer)
                        while (read > 0) {
                            out.write(buffer, 0, read)
                            extractedBytes += read
                            val percent = ((extractedBytes * 100f) / totalUncompressedBytes)
                                .toInt()
                                .coerceIn(0, 100)
                            if (percent != lastPercent) {
                                lastPercent = percent
                                report(
                                    onProgress,
                                    OperationStage.INSTALLING,
                                    percent,
                                    "安装中 ${percent}%"
                                )
                            }
                            read = zis.read(buffer)
                        }
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

        report(onProgress, OperationStage.INSTALLING, 100, "安装完成")
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

    private fun clearOtherPackages(keptPackage: VoiceModelPackage) {
        VoiceModelPackage.entries
            .filter { it != keptPackage }
            .map { File(context.filesDir, it.dirName) }
            .filter { it.exists() }
            .forEach { it.deleteRecursively() }
    }

    private fun isModelReady(modelDir: File): Boolean {
        if (!modelDir.exists() || !modelDir.isDirectory) return false
        val required = listOf("am", "conf", "graph")
        return required.all { File(modelDir, it).exists() }
    }

    private fun report(
        onProgress: (OperationProgress) -> Unit,
        stage: OperationStage,
        percent: Int,
        message: String
    ) {
        onProgress(
            OperationProgress(
                stage = stage,
                percent = percent.coerceIn(0, 100),
                message = message
            )
        )
    }
}
