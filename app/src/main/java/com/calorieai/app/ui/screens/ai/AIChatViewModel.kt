package com.calorieai.app.ui.screens.ai

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ChatMessageData
import com.calorieai.app.data.model.ChatSessionSummary
import com.calorieai.app.data.repository.AIChatHistoryRepository
import com.calorieai.app.service.ai.AIChatService
import com.calorieai.app.service.ai.AIChatService.ConversationMessage
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
    private val aiChatHistoryRepository: AIChatHistoryRepository
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
            aiChatHistoryRepository.getAllHistory().collect { sessions ->
                SecureLogger.event(TAG, "chat_sessions_updated", "count" to sessions.size)
                _uiState.value = _uiState.value.copy(
                    chatSessions = sessions.map { it.toChatSessionInfo() }
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
            val sessionId = aiChatHistoryRepository.createNewSession("新对话")
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
            val (history, messages) = aiChatHistoryRepository.getHistoryBySessionId(sessionId)
            if (history != null) {
                SecureLogger.event(
                    TAG,
                    "load_session_success",
                    "sessionId" to sessionId,
                    "messageCount" to messages.size,
                    "title" to history.title
                )
                _uiState.value = _uiState.value.copy(
                    currentSessionId = sessionId,
                    currentSessionTitle = history.title,
                    messages = messages.map { it.toChatMessage() }
                )
            } else {
                SecureLogger.w(TAG, "load_session_not_found | sessionId=$sessionId")
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            SecureLogger.event(TAG, "delete_session_start", "sessionId" to sessionId)
            aiChatHistoryRepository.deleteSession(sessionId)
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
            aiChatHistoryRepository.togglePin(sessionId)
        }
    }

    private fun saveCurrentSession() {
        viewModelScope.launch {
            saveCurrentSessionInternal(_uiState.value)
        }
    }

    private suspend fun saveCurrentSessionInternal(state: AIChatUiState) {
        if (state.messages.isNotEmpty()) {
            val title = state.messages.firstOrNull { it.isFromUser }?.content?.take(30) ?: "新对话"
            val messages = state.messages.map { it.toChatMessageData() }
            SecureLogger.event(
                TAG,
                "save_session",
                "sessionId" to state.currentSessionId,
                "title" to title,
                "messageCount" to messages.size
            )
            aiChatHistoryRepository.saveChatSession(
                sessionId = state.currentSessionId,
                title = title,
                messages = messages
            )
        }
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
        val conversationHistory = (_uiState.value.messages + userMessage).map { chatMessage ->
            ConversationMessage(
                role = if (chatMessage.isFromUser) "user" else "assistant",
                content = chatMessage.content,
                timestamp = chatMessage.timestamp
            )
        }
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

                aiChatService.sendMessageStream(message, conversationHistory).collect { chunk ->
                    val piece = chunk.toString()
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
                
                val errorMsg = when {
                    e.message?.contains("未配置AI服务") == true -> "未配置AI服务，请先在设置中配置AI服务"
                    e.message?.contains("API调用次数已用完") == true -> e.message!!
                    e.message?.contains("API调用失败") == true -> e.message!!
                    e.message?.contains("API返回为空") == true -> "AI服务返回为空，请检查网络连接"
                    e.message?.contains("Unable to resolve host") == true -> "网络连接失败，请检查网络设置"
                    e.message != null -> "抱歉，我遇到了一些问题：${e.message}"
                    else -> "未知错误，请检查网络连接或AI配置。错误类型：${e.javaClass.simpleName}"
                }
                
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

    fun startCalorieAssessment() {
        val userMessage = "请帮我评估最近一周的热量消耗是否合理"
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
                val response = aiChatService.assessCalorieIntake()

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
                val errorMessage = ChatMessage(
                    content = "抱歉，评估失败：${e.message}",
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

    fun startMealPlanning() {
        val userMessage = "请帮我规划今天的健康菜谱"
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
                val response = aiChatService.planHealthyMeals()

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
                val errorMessage = ChatMessage(
                    content = "抱歉，规划失败：${e.message}",
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

    fun startWeeklyMealPlanning() {
        val userMessage = "请帮我制定未来7天菜谱周计划"
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
                val response = aiChatService.planWeeklyMeals()
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
                val errorMessage = ChatMessage(
                    content = "抱歉，周计划生成失败：${e.message}",
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

    fun startNextMealRecommendation() {
        val userMessage = "请给我下一餐智能推荐"
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
                val response = aiChatService.recommendNextMeal()
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
                val errorMessage = ChatMessage(
                    content = "抱歉，下一餐推荐失败：${e.message}",
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

    fun startHealthConsult() {
        val userMessage = "我想咨询一些营养健康问题"
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
                val response = aiChatService.healthConsult()

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
                val errorMessage = ChatMessage(
                    content = "抱歉，咨询失败：${e.message}",
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

    private fun ChatMessage.toChatMessageData(): ChatMessageData {
        return ChatMessageData(
            id = this.id,
            content = this.content,
            isFromUser = this.isFromUser,
            timestamp = this.timestamp,
            type = "TEXT"
        )
    }

    private fun ChatMessageData.toChatMessage(): ChatMessage {
        return ChatMessage(
            id = this.id,
            content = this.content,
            isFromUser = this.isFromUser,
            timestamp = this.timestamp
        )
    }

    private fun ChatSessionSummary.toChatSessionInfo(): ChatSessionInfo {
        return ChatSessionInfo(
            sessionId = this.sessionId,
            title = this.title,
            lastMessage = this.lastMessage,
            updatedAt = this.updatedAt,
            messageCount = this.messageCount,
            isPinned = this.isPinned
        )
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
