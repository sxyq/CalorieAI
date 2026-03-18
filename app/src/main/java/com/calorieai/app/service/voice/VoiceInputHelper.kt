package com.calorieai.app.service.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceInputHelper @Inject constructor() {

    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var accumulatedText = StringBuilder()
    
    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.Idle)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    /**
     * 开始语音识别
     * @param context 上下文
     * @param onResult 识别成功回调
     * @param onError 识别错误回调
     * @param onPartialResult 部分结果回调（实时显示）
     * @param enableContinuous 是否启用连续识别模式
     */
    fun startListening(
        context: Context, 
        onResult: (String) -> Unit, 
        onError: (String) -> Unit,
        onPartialResult: ((String) -> Unit)? = null,
        enableContinuous: Boolean = false
    ) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        }
        
        isListening = true
        accumulatedText.clear()
        _voiceState.value = VoiceState.Listening
        
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.CHINESE.toString())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            // 提高识别准确度
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 1500L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1000L)
        }
        
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = VoiceState.Listening
            }
            
            override fun onBeginningOfSpeech() {
                _voiceState.value = VoiceState.Listening
            }
            
            override fun onRmsChanged(rmsdB: Float) {
                // 可以在这里更新音量指示器
            }
            
            override fun onBufferReceived(buffer: ByteArray?) {}
            
            override fun onEndOfSpeech() {
                _voiceState.value = VoiceState.Processing
            }
            
            override fun onError(error: Int) {
                if (!isListening) return
                
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "音频录制失败，请检查麦克风"
                    SpeechRecognizer.ERROR_CLIENT -> "识别客户端错误"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "缺少录音权限"
                    SpeechRecognizer.ERROR_NETWORK -> "网络连接失败"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "网络请求超时"
                    SpeechRecognizer.ERROR_NO_MATCH -> "未能识别语音，请再说一次"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "识别器繁忙，请稍后再试"
                    SpeechRecognizer.ERROR_SERVER -> "识别服务器错误"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "说话时间太短"
                    else -> "识别失败，请重试"
                }
                
                // 如果是超时错误且已收集到部分文本，返回部分结果
                if ((error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT || error == SpeechRecognizer.ERROR_NO_MATCH) 
                    && accumulatedText.isNotEmpty()) {
                    val finalText = accumulatedText.toString().trim()
                    if (finalText.isNotEmpty()) {
                        _voiceState.value = VoiceState.Success(finalText)
                        onResult(finalText)
                        return
                    }
                }
                
                _voiceState.value = VoiceState.Error(errorMessage)
                onError(errorMessage)
            }
            
            override fun onResults(results: Bundle?) {
                if (!isListening) return
                
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull()?.trim() ?: ""
                
                if (text.isNotEmpty()) {
                    accumulatedText.append(text)
                    _voiceState.value = VoiceState.Success(accumulatedText.toString())
                    onResult(accumulatedText.toString())
                } else {
                    onError("未能识别语音内容")
                }
            }
            
            override fun onPartialResults(partialResults: Bundle?) {
                if (!isListening) return
                
                val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = matches?.firstOrNull() ?: ""
                
                if (text.isNotEmpty()) {
                    _voiceState.value = VoiceState.Partial(text)
                    onPartialResult?.invoke(text)
                }
            }
            
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
        
        try {
            speechRecognizer?.startListening(intent)
        } catch (e: Exception) {
            _voiceState.value = VoiceState.Error("启动识别失败: ${e.message}")
            onError("启动识别失败")
        }
    }
    
    /**
     * 停止语音识别
     */
    fun stopListening() {
        isListening = false
        try {
            speechRecognizer?.stopListening()
        } catch (e: Exception) {
            // 忽略停止时的错误
        }
        _voiceState.value = VoiceState.Idle
    }
    
    /**
     * 取消语音识别
     */
    fun cancel() {
        isListening = false
        try {
            speechRecognizer?.cancel()
        } catch (e: Exception) {
            // 忽略取消时的错误
        }
        _voiceState.value = VoiceState.Idle
    }
    
    /**
     * 释放资源
     */
    fun destroy() {
        isListening = false
        try {
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            // 忽略销毁时的错误
        }
        speechRecognizer = null
    }
    
    /**
     * 检查设备是否支持语音识别
     */
    fun isRecognitionAvailable(context: Context): Boolean {
        return SpeechRecognizer.isRecognitionAvailable(context)
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
