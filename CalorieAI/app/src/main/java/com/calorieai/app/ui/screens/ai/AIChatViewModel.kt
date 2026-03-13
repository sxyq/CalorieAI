package com.calorieai.app.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ChatMessageData
import com.calorieai.app.data.model.ChatSessionSummary
import com.calorieai.app.data.repository.AIChatHistoryRepository
import com.calorieai.app.service.ai.AIChatService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class AIChatViewModel @Inject constructor(
    private val aiChatService: AIChatService,
    private val aiChatHistoryRepository: AIChatHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIChatUiState())
    val uiState: StateFlow<AIChatUiState> = _uiState.asStateFlow()

    init {
        loadChatSessions()
        startNewSession()
        updateRemainingCalls()
    }

    private fun loadChatSessions() {
        viewModelScope.launch {
            aiChatHistoryRepository.getAllHistory().collect { sessions ->
                _uiState.value = _uiState.value.copy(
                    chatSessions = sessions.map { it.toChatSessionInfo() }
                )
            }
        }
    }

    private fun updateRemainingCalls() {
        viewModelScope.launch {
            val remaining = aiChatService.getRemainingCalls()
            _uiState.value = _uiState.value.copy(remainingCalls = remaining)
        }
    }

    fun startNewSession() {
        // 先保存当前会话
        saveCurrentSession()
        
        // 创建新会话
        val sessionId = UUID.randomUUID().toString()
        _uiState.value = _uiState.value.copy(
            currentSessionId = sessionId,
            currentSessionTitle = "",
            messages = emptyList(),
            inputText = ""
        )
    }

    fun loadSession(sessionId: String) {
        // 保存当前会话
        saveCurrentSession()
        
        viewModelScope.launch {
            val (history, messages) = aiChatHistoryRepository.getHistoryBySessionId(sessionId)
            if (history != null) {
                _uiState.value = _uiState.value.copy(
                    currentSessionId = sessionId,
                    currentSessionTitle = history.title,
                    messages = messages.map { it.toChatMessage() }
                )
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            aiChatHistoryRepository.deleteSession(sessionId)
            if (sessionId == _uiState.value.currentSessionId) {
                startNewSession()
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
        val currentState = _uiState.value
        if (currentState.messages.isNotEmpty()) {
            viewModelScope.launch {
                val title = currentState.messages.firstOrNull { it.isFromUser }?.content?.take(30) ?: "新对话"
                val messages = currentState.messages.map { it.toChatMessageData() }
                aiChatHistoryRepository.saveChatSession(
                    sessionId = currentState.currentSessionId,
                    title = title,
                    messages = messages
                )
            }
        }
    }

    fun onInputChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun sendMessage() {
        val message = _uiState.value.inputText.trim()
        if (message.isBlank()) return

        val userMessage = ChatMessage(
            content = message,
            isFromUser = true
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            inputText = "",
            isLoading = true,
            isTyping = true
        )

        // 设置会话标题（如果是第一条消息）
        if (_uiState.value.currentSessionTitle.isBlank()) {
            _uiState.value = _uiState.value.copy(
                currentSessionTitle = message.take(30)
            )
        }

        viewModelScope.launch {
            try {
                val response = aiChatService.sendMessage(message)
                
                val aiMessage = ChatMessage(
                    content = response,
                    isFromUser = false
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + aiMessage,
                    isLoading = false,
                    isTyping = false
                )
                
                // 更新剩余调用次数
                updateRemainingCalls()
                
                // 自动保存会话
                saveCurrentSession()
            } catch (e: Exception) {
                val errorMessage = ChatMessage(
                    content = "抱歉，发生了错误：${e.message}",
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

    fun startCalorieAssessment() {
        _uiState.value = _uiState.value.copy(
            inputText = "请帮我评估今天的热量消耗是否合理"
        )
        sendMessage()
    }

    fun startMealPlanning() {
        _uiState.value = _uiState.value.copy(
            inputText = "请帮我规划今天的健康菜谱"
        )
        sendMessage()
    }

    fun startHealthConsult() {
        _uiState.value = _uiState.value.copy(
            inputText = "我想咨询一些营养健康问题"
        )
        sendMessage()
    }

    fun clearCurrentChat() {
        _uiState.value = _uiState.value.copy(
            messages = emptyList(),
            currentSessionTitle = ""
        )
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
    val chatSessions: List<ChatSessionInfo> = emptyList(),
    val remainingCalls: Int = 10,
    val dailyLimit: Int = 10
)
