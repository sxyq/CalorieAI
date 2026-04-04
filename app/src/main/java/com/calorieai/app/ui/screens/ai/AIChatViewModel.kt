package com.calorieai.app.ui.screens.ai

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.service.ai.AIChatService
import com.calorieai.app.utils.SecureLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiChatService: AIChatService,
    private val sendMessageUseCase: AIChatSendMessageUseCase,
    private val sessionUseCase: AIChatSessionUseCase,
    private val errorMapper: AIChatErrorMapper
) : ViewModel() {
    companion object {
        private const val TAG = "AIChatViewModel"
        // 保留流式打字观感，同时降低每字符重组造成的主线程压力
        private const val STREAM_UI_UPDATE_INTERVAL_MS = 48L
        private const val STREAM_UI_UPDATE_CHUNK_SIZE = 20
    }

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    init {
        SecureLogger.event(TAG, "init")
        loadChatSessions()
        startNewSession()
        updateRemainingCalls()
    }

    private fun loadChatSessions() {
        viewModelScope.launch {
            sessionUseCase.observeSessions().collect { sessions ->
                SecureLogger.event(TAG, "chat_sessions_updated", "count" to sessions.size)
                _uiState.value = _uiState.value.copy(
                    chatSessions = sessions
                )
            }
        }
    }

    private fun updateRemainingCalls() {
        viewModelScope.launch {
            val remaining = aiChatService.getRemainingCalls()
            SecureLogger.event(TAG, "remaining_calls_updated", "remaining" to remaining)
            _uiState.value = _uiState.value.copy(remainingCalls = remaining)
        }
    }

    fun startNewSession(skipSaveCurrent: Boolean = false) {
        viewModelScope.launch {
            val previousSessionId = _uiState.value.currentSessionId
            SecureLogger.event(
                TAG,
                "start_new_session",
                "skipSaveCurrent" to skipSaveCurrent,
                "previousSessionId" to previousSessionId
            )
            // 先保存当前会话（删除当前会话时需跳过，否则会被重新写回）
            if (!skipSaveCurrent) {
                saveCurrentSessionInternal(_uiState.value)
            }

            // 创建并持久化一个新会话，确保“+号新对话”有实际效果
            val sessionId = sessionUseCase.createNewSession("新对话")
            SecureLogger.event(TAG, "new_session_created", "sessionId" to sessionId)
            _uiState.value = _uiState.value.copy(
                currentSessionId = sessionId,
                currentSessionTitle = "新对话",
                messages = emptyList(),
                inputText = ""
            )
        }
    }

    fun loadSession(sessionId: String) {
        // 保存当前会话
        saveCurrentSession()
        
        viewModelScope.launch {
            SecureLogger.event(TAG, "load_session_start", "sessionId" to sessionId)
            val session = sessionUseCase.loadSession(sessionId)
            if (session != null) {
                SecureLogger.event(
                    TAG,
                    "load_session_success",
                    "sessionId" to sessionId,
                    "messageCount" to session.messages.size,
                    "title" to session.title
                )
                _uiState.value = _uiState.value.copy(
                    currentSessionId = sessionId,
                    currentSessionTitle = session.title,
                    messages = session.messages
                )
            } else {
                SecureLogger.w(TAG, "load_session_not_found | sessionId=$sessionId")
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            SecureLogger.event(TAG, "delete_session_start", "sessionId" to sessionId)
            sessionUseCase.deleteSession(sessionId)
            SecureLogger.event(TAG, "delete_session_done", "sessionId" to sessionId)
            if (sessionId == _uiState.value.currentSessionId) {
                startNewSession(skipSaveCurrent = true)
            }
        }
    }

    fun deleteCurrentSession() {
        val sessionId = _uiState.value.currentSessionId
        deleteSession(sessionId)
    }

    fun togglePinSession(sessionId: String) {
        viewModelScope.launch {
            sessionUseCase.togglePinSession(sessionId)
        }
    }

    private fun saveCurrentSession() {
        viewModelScope.launch {
            saveCurrentSessionInternal(_uiState.value)
        }
    }

    private suspend fun saveCurrentSessionInternal(state: AIChatUiState) {
        if (state.messages.isEmpty()) return
        SecureLogger.event(
            TAG,
            "save_session",
            "sessionId" to state.currentSessionId,
            "title" to (state.messages.firstOrNull { it.isFromUser }?.content?.take(30) ?: "新对话"),
            "messageCount" to state.messages.size
        )
        sessionUseCase.saveCurrentSession(state)
    }

    fun persistCurrentSession() {
        saveCurrentSession()
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val message = _uiState.value.inputText.trim()
        if (message.isBlank()) return
        sendMessage(message)
    }

    fun sendMessage(message: String) {
        // 并发控制：如果正在发送，则忽略新请求
        if (_uiState.value.isSending) {
            SecureLogger.w(TAG, "send_ignored_already_sending")
            return
        }
        if (message.isBlank()) {
            SecureLogger.w(TAG, "send_ignored_blank_message")
            return
        }

        val userMessage = ChatMessage(
            content = message,
            isFromUser = true
        )
        val conversationHistory = sendMessageUseCase.buildConversationHistory(
            existingMessages = _uiState.value.messages,
            userMessage = userMessage
        )
        val startAt = SystemClock.elapsedRealtime()
        val activeSessionId = _uiState.value.currentSessionId
        SecureLogger.event(
            TAG,
            "send_start",
            "sessionId" to activeSessionId,
            "inputLength" to message.length,
            "historyCount" to conversationHistory.size
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            inputText = "",
            isLoading = true,
            isTyping = true,
            isSending = true  // 锁定发送状态
        )

        // 设置会话标题（如果是第一条消息）
        if (_uiState.value.currentSessionTitle.isBlank()) {
            _uiState.value = _uiState.value.copy(
                currentSessionTitle = message.take(30)
            )
        }

        viewModelScope.launch {
            try {
                withContext(NonCancellable) {
                // 创建AI消息占位符
                val aiMessageId = UUID.randomUUID().toString()
                SecureLogger.event(
                    TAG,
                    "stream_placeholder_created",
                    "sessionId" to activeSessionId,
                    "messageId" to aiMessageId
                )
                val aiMessage = ChatMessage(
                    id = aiMessageId,
                    content = "",
                    isFromUser = false
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage
                )

                // 使用流式API接收响应
                val fullResponse = StringBuilder()
                var pendingLength = 0
                var lastUiUpdateAt = 0L

                sendMessageUseCase.streamAssistantReply(message, conversationHistory) { piece ->
                    fullResponse.append(piece)
                    pendingLength += piece.length

                    val now = SystemClock.elapsedRealtime()
                    val shouldUpdate = pendingLength >= STREAM_UI_UPDATE_CHUNK_SIZE ||
                        (now - lastUiUpdateAt) >= STREAM_UI_UPDATE_INTERVAL_MS ||
                        piece.contains('\n')

                    if (shouldUpdate) {
                        updateStreamingAssistantMessage(aiMessageId, fullResponse.toString())
                        pendingLength = 0
                        lastUiUpdateAt = now
                    }
                }
                if (pendingLength > 0) {
                    updateStreamingAssistantMessage(aiMessageId, fullResponse.toString())
                }
                val elapsed = SystemClock.elapsedRealtime() - startAt
                SecureLogger.event(
                    TAG,
                    "send_success",
                    "sessionId" to activeSessionId,
                    "messageId" to aiMessageId,
                    "responseLength" to fullResponse.length,
                    "elapsedMs" to elapsed
                )

                // 完成后更新状态
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isTyping = false,
                    isSending = false  // 解锁发送状态
                )

                // 更新剩余调用次数
                updateRemainingCalls()

                // 自动保存会话
                saveCurrentSession()
                }
            } catch (e: Exception) {
                val elapsed = SystemClock.elapsedRealtime() - startAt
                SecureLogger.e(
                    TAG,
                    "send_failed | sessionId=$activeSessionId | inputLength=${message.length} | elapsedMs=$elapsed | error=${e.message}",
                    e
                )

                val errorMsg = errorMapper.map(e)
                
                val errorMessage = ChatMessage(
                    content = errorMsg,
                    isFromUser = false
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false,
                    isTyping = false,
                    isSending = false  // 解锁发送状态
                )
            }
        }
    }
    fun startCalorieAssessment() = runQuickAction(
        userMessage = "\u8BF7\u5E2E\u6211\u8BC4\u4F30\u6700\u8FD1\u4E00\u5468\u7684\u70ED\u91CF\u6D88\u8017\u662F\u5426\u5408\u7406",
        actionName = "calorie_assessment"
    ) {
        assessCalorieIntake()
    }

    fun startMealPlanning() = runQuickAction(
        userMessage = "\u8BF7\u5E2E\u6211\u89C4\u5212\u4ECA\u5929\u7684\u5065\u5EB7\u83DC\u8C31",
        actionName = "meal_planning"
    ) {
        planHealthyMeals()
    }

    fun startWeeklyMealPlanning() = runQuickAction(
        userMessage = "\u8BF7\u5E2E\u6211\u5236\u5B9A\u672A\u67653\u5929\u83DC\u8C31\u5468\u8BA1\u5212",
        actionName = "weekly_meal_planning"
    ) {
        planWeeklyMeals()
    }

    fun startNextMealRecommendation() = runQuickAction(
        userMessage = "\u8BF7\u7ED9\u6211\u4E0B\u4E00\u9910\u667A\u80FD\u63A8\u8350",
        actionName = "next_meal_recommendation"
    ) {
        recommendNextMeal()
    }

    fun startHealthConsult() = runQuickAction(
        userMessage = "\u6211\u60F3\u54A8\u8BE2\u4E00\u4E9B\u8425\u517B\u5065\u5EB7\u95EE\u9898",
        actionName = "health_consult"
    ) {
        healthConsult()
    }

    private fun runQuickAction(
        userMessage: String,
        actionName: String,
        request: suspend AIChatService.() -> String
    ) {
        val displayMessage = ChatMessage(
            content = userMessage,
            isFromUser = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + displayMessage,
            inputText = "",
            isLoading = true,
            isTyping = true
        )

        if (_uiState.value.currentSessionTitle.isBlank()) {
            _uiState.value = _uiState.value.copy(
                currentSessionTitle = userMessage.take(30)
            )
        }

        viewModelScope.launch {
            try {
                val response = aiChatService.request()
                val aiMessage = ChatMessage(
                    content = response,
                    isFromUser = false
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false,
                    isTyping = false
                )

                updateRemainingCalls()
                saveCurrentSession()
            } catch (e: Exception) {
                SecureLogger.e(
                    TAG,
                    "quick_action_failed | action=$actionName | error=${e.message}",
                    e
                )

                val errorMessage = ChatMessage(
                    content = errorMapper.map(e),
                    isFromUser = false
                )
                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false,
                    isTyping = false
                )
            }
        }
    }

    fun clearCurrentChat() {
        SecureLogger.event(
            TAG,
            "clear_current_chat",
            "sessionId" to _uiState.value.currentSessionId,
            "messageCount" to _uiState.value.messages.size
        )
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            currentSessionTitle = ""
        )
    }

    private fun updateStreamingAssistantMessage(messageId: String, content: String) {
        val currentMessages = _uiState.value.messages
        val targetIndex = currentMessages.indexOfLast { it.id == messageId }
        if (targetIndex < 0) {
            SecureLogger.w(TAG, "stream_update_target_missing | messageId=$messageId")
            return
        }

        val updatedMessages = currentMessages.toMutableList()
        val old = updatedMessages[targetIndex]
        if (old.content == content) return

        updatedMessages[targetIndex] = old.copy(content = content)
        _uiState.value = _uiState.value.copy(messages = updatedMessages)
    }

}

data class AIChatUiState(
    val currentSessionId: String = "",
    val currentSessionTitle: String = "",
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false,
    val isTyping: Boolean = false,
    val isSending: Boolean = false,  // 发送锁定状态
    val chatSessions: List<ChatSessionInfo> = emptyList(),
    val remainingCalls: Int = 10,
    val dailyLimit: Int = 10
)

