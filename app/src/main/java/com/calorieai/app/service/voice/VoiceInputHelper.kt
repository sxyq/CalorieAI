package com.calorieai.app.service.voice

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import com.k2fsa.sherpa.onnx.FeatureConfig
import com.k2fsa.sherpa.onnx.OfflineModelConfig
import com.k2fsa.sherpa.onnx.OfflineRecognizer
import com.k2fsa.sherpa.onnx.OfflineRecognizerConfig
import com.k2fsa.sherpa.onnx.OfflineSenseVoiceModelConfig
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
import java.util.concurrent.atomic.AtomicBoolean
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
    private val stopRequested = AtomicBoolean(false)

    private var recognizer: OfflineRecognizer? = null
    private var audioRecord: AudioRecord? = null
    private var currentSessionJob: Job? = null
    @Volatile private var isListening = false

    fun startListening(
        context: Context,
        onResult: (String) -> Unit,
        onError: (String) -> Unit,
        onPartialResult: ((String) -> Unit)? = null,
        enableContinuous: Boolean = false
    ) {
        stopListening()
        if (!voiceModelManager.isModelInstalled()) {
            val message = "内置语音模型不可用，请重新安装应用"
            _voiceState.value = VoiceState.Error(message)
            onError(message)
            return
        }

        stopRequested.set(false)
        isListening = true
        _voiceState.value = VoiceState.Processing
        currentSessionJob = scope.launch {
            try {
                val localRecognizer = prepareRecognizer(context.applicationContext)
                if (!isListening || stopRequested.get()) return@launch

                val samples = withContext(Dispatchers.IO) { recordAudio() }
                if (samples.isEmpty()) {
                    finishError("说话时间太短", onError)
                    return@launch
                }

                _voiceState.value = VoiceState.Processing
                val result = withContext(Dispatchers.Default) {
                    decode(localRecognizer, samples)
                }
                val text = result.text.trim()
                if (!containsSpeechCharacters(text)) {
                    finishError("未能识别语音，请再试一次", onError)
                } else {
                    _voiceState.value = VoiceState.Success(text)
                    onResult(text)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "SenseVoice recognition failed", t)
                finishError("离线语音识别异常：${t.message ?: "未知错误"}", onError)
            } finally {
                releaseAudioRecord()
                isListening = false
                stopRequested.set(false)
                currentSessionJob = null
            }
        }
    }

    fun stopListening() {
        if (!isListening && currentSessionJob?.isActive != true) {
            _voiceState.value = VoiceState.Idle
            return
        }

        isListening = false
        stopRequested.set(true)
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }
    }

    fun cancel() = stopListening()

    fun destroy() {
        stopListening()
        currentSessionJob?.cancel()
        currentSessionJob = null
        releaseAudioRecord()
        try {
            recognizer?.release()
        } catch (_: Throwable) {
        }
        recognizer = null
        scope.cancel()
        _voiceState.value = VoiceState.Idle
    }

    fun isRecognitionAvailable(context: Context): Boolean {
        return androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    private suspend fun prepareRecognizer(context: Context): OfflineRecognizer = modelMutex.withLock {
        recognizer?.let { return it }

        withContext(Dispatchers.Default) {
            val modelConfig = OfflineModelConfig().apply {
                senseVoice = OfflineSenseVoiceModelConfig(
                    model = VoiceModelManager.MODEL_ASSET_PATH,
                    language = "",
                    useInverseTextNormalization = true
                )
                tokens = VoiceModelManager.TOKENS_ASSET_PATH
                numThreads = 2
                provider = "cpu"
                modelType = "sense_voice"
            }
            val config = OfflineRecognizerConfig().apply {
                featConfig = FeatureConfig(SAMPLE_RATE, FEATURE_DIM, 0f)
                this.modelConfig = modelConfig
                decodingMethod = "greedy_search"
            }
            OfflineRecognizer(context.assets, config).also { recognizer = it }
        }
    }

    private fun recordAudio(): FloatArray {
        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBufferSize <= 0) throw IllegalStateException("设备不支持录音")

        val record = try {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                (minBufferSize * 2).coerceAtLeast(SAMPLE_RATE / 2)
            )
        } catch (t: SecurityException) {
            throw IllegalStateException("录音权限被拒绝", t)
        }
        audioRecord = record
        val samples = FloatArray(MAX_RECORDING_SECONDS * SAMPLE_RATE)
        var sampleCount = 0
        val buffer = ShortArray(minBufferSize.coerceAtLeast(1024) / 2)

        try {
            record.startRecording()
            _voiceState.value = VoiceState.Listening
            while (isListening && !stopRequested.get() && sampleCount < samples.size) {
                val read = record.read(buffer, 0, buffer.size)
                if (read < 0) throw IllegalStateException("录音读取失败($read)")
                for (index in 0 until read) {
                    samples[sampleCount++] = buffer[index] / 32768.0f
                }
            }
        } finally {
            try {
                record.stop()
            } catch (_: Throwable) {
            }
            record.release()
            audioRecord = null
        }

        return samples.copyOf(sampleCount)
    }

    private fun decode(recognizer: OfflineRecognizer, samples: FloatArray) =
        recognizer.createStream().let { stream ->
            try {
                stream.acceptWaveform(samples, SAMPLE_RATE)
                recognizer.decode(stream)
                recognizer.getResult(stream)
            } finally {
                stream.release()
            }
        }

    private fun finishError(message: String, onError: (String) -> Unit) {
        _voiceState.value = VoiceState.Error(message)
        onError(message)
    }

    private fun containsSpeechCharacters(text: String): Boolean {
        return text.any { it.isLetterOrDigit() || it in '\u4E00'..'\u9FFF' }
    }

    private fun releaseAudioRecord() {
        try {
            audioRecord?.stop()
        } catch (_: Throwable) {
        }
        try {
            audioRecord?.release()
        } catch (_: Throwable) {
        }
        audioRecord = null
    }

    companion object {
        private const val TAG = "VoiceInputHelper"
        private const val SAMPLE_RATE = 16000
        private const val FEATURE_DIM = 80
        private const val MAX_RECORDING_SECONDS = 90
    }
}

sealed class VoiceState {
    data object Idle : VoiceState()
    data object Listening : VoiceState()
    data object Processing : VoiceState()
    data class Partial(val text: String) : VoiceState()
    data class Success(val text: String) : VoiceState()
    data class Error(val message: String) : VoiceState()
}
