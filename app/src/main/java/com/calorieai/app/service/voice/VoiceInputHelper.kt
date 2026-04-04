package com.calorieai.app.service.voice

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.RecognitionListener
import org.vosk.android.SpeechService
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceInputHelper @Inject constructor(
    private val voiceModelManager: VoiceModelManager
) {

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val modelMutex = Mutex()

    private var model: Model? = null
    private var speechService: SpeechService? = null
    private var recognizer: Recognizer? = null
    private var currentSessionJob: Job? = null
    private var isListening = false
    private var accumulatedText = StringBuilder()
    private var lastPartialText: String = ""

    fun startListening(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onPartialResult: ((String) -> Unit)? = null,
        enableContinuous: Boolean = false
    ) {
        stopListening()
        _voiceState.value = VoiceState.Processing
        accumulatedText.clear()
        lastPartialText = ""

        currentSessionJob = scope.launch {
            try {
                val localModel = prepareModel(context.applicationContext)
                startVoskListening(
                    model = localModel,
                    onResult = onResult,
                    onError = onError,
                    onPartialResult = onPartialResult,
                    enableContinuous = enableContinuous
                )
            } catch (t: Throwable) {
                Log.e(TAG, "startListening failed", t)
                _voiceState.value = VoiceState.Error("内置语音模型初始化失败: ${t.message ?: "未知错误"}")
                onError("内置语音模型初始化失败")
                isListening = false
            }
        }
    }

    fun stopListening() {
        isListening = false
        try {
            speechService?.stop()
        } catch (_: Throwable) {
        }
        releaseRecognizer()
        _voiceState.value = VoiceState.Idle
    }

    fun cancel() {
        stopListening()
    }

    fun destroy() {
        isListening = false
        currentSessionJob?.cancel()
        currentSessionJob = null

        try {
            speechService?.stop()
        } catch (_: Throwable) {
        }
        releaseRecognizer()

        try {
            model?.close()
        } catch (_: Throwable) {
        }
        model = null

        scope.cancel()
        _voiceState.value = VoiceState.Idle
    }

    fun isRecognitionAvailable(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private suspend fun prepareModel(context: Context): Model = modelMutex.withLock {
        model?.let { return it }

        val modelDir = voiceModelManager.getInstalledModelDir()
        if (modelDir == null || !isModelReady(modelDir)) {
            throw IllegalStateException("语音模型未安装，请前往 设置 > AI配置 下载语音模型")
        }

        return withContext(Dispatchers.IO) {
            Log.i(TAG, "loading downloaded voice model from ${modelDir.absolutePath}")
            Model(modelDir.absolutePath).also { loaded ->
                model = loaded
            }
        }
    }

    private fun isModelReady(modelDir: File): Boolean {
        if (!modelDir.exists() || !modelDir.isDirectory) return false
        val required = listOf("am", "conf", "graph")
        return required.all { File(modelDir, it).exists() }
    }

    private fun startVoskListening(
        model: Model,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onPartialResult: ((String) -> Unit)?,
        enableContinuous: Boolean
    ) {
        releaseRecognizer()
        recognizer = Recognizer(model, SAMPLE_RATE)
        speechService = SpeechService(recognizer, SAMPLE_RATE)
        isListening = true
        _voiceState.value = VoiceState.Listening

        speechService?.startListening(object : RecognitionListener {
            override fun onPartialResult(hypothesis: String?) {
                if (!isListening) return
                val text = parseHypothesisText(hypothesis, partial = true)
                if (text.isNotBlank() && text != lastPartialText) {
                    lastPartialText = text
                    _voiceState.value = VoiceState.Partial(text)
                    onPartialResult?.invoke(text)
                }
            }

            override fun onResult(hypothesis: String?) {
                if (!isListening) return
                appendRecognizedText(parseHypothesisText(hypothesis, partial = false))
            }

            override fun onFinalResult(hypothesis: String?) {
                if (!isListening) return
                appendRecognizedText(parseHypothesisText(hypothesis, partial = false))
                lastPartialText = ""

                val finalText = accumulatedText.toString().trim()
                if (finalText.isBlank()) {
                    _voiceState.value = VoiceState.Error("未能识别语音，请再试一次")
                    onError("未能识别语音，请再试一次")
                } else {
                    _voiceState.value = VoiceState.Success(finalText)
                    onResult(finalText)
                }

                if (!enableContinuous) {
                    stopListening()
                } else {
                    accumulatedText.clear()
                    lastPartialText = ""
                    _voiceState.value = VoiceState.Listening
                }
            }

            override fun onError(exception: Exception?) {
                if (!isListening) return
                val msg = exception?.message ?: "离线语音识别异常"
                Log.e(TAG, "vosk error: $msg", exception)
                _voiceState.value = VoiceState.Error(msg)
                onError(msg)
                stopListening()
            }

            override fun onTimeout() {
                if (!isListening) return
                val finalText = accumulatedText.toString().trim()
                if (finalText.isNotBlank()) {
                    _voiceState.value = VoiceState.Success(finalText)
                    onResult(finalText)
                } else {
                    _voiceState.value = VoiceState.Error("说话时间太短")
                    onError("说话时间太短")
                }
                stopListening()
            }
        })
    }

    private fun appendRecognizedText(text: String) {
        if (text.isBlank()) return
        val normalized = text.trim()
        if (accumulatedText.toString().endsWith(normalized)) {
            return
        }
        if (accumulatedText.isNotEmpty()) {
            accumulatedText.append(" ")
        }
        accumulatedText.append(normalized)
    }

    private fun parseHypothesisText(raw: String?, partial: Boolean): String {
        if (raw.isNullOrBlank()) return ""
        return try {
            val json = JsonParser.parseString(raw).asJsonObject
            when {
                partial -> json.get("partial")?.asString.orEmpty()
                else -> json.get("text")?.asString.orEmpty()
            }.trim()
        } catch (t: Throwable) {
            Log.w(TAG, "failed to parse hypothesis: $raw", t)
            ""
        }
    }

    private fun releaseRecognizer() {
        try {
            speechService?.stop()
        } catch (_: Throwable) {
        }
        speechService = null

        try {
            recognizer?.close()
        } catch (_: Throwable) {
        }
        recognizer = null
    }

    companion object {
        private const val TAG = "VoiceInputHelper"
        private const val SAMPLE_RATE = 16000f
    }
}

sealed class VoiceState {
    object Idle : VoiceState()
    object Listening : VoiceState()
    object Processing : VoiceState()
    data class Partial(val text: String) : VoiceState()
    data class Success(val text: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}
