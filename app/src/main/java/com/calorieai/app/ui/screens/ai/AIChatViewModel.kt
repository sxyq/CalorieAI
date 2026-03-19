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
        if (_uiState.value.isSending) return
        if (message.isBlank()) return

        val userMessage = ChatMessage(
            content = message,
            isFromUser = true
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
                // 创建AI消息占位符
                val aiMessageId = UUID.randomUUID().toString()
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
                aiChatService.sendMessageStream(message).collect { char ->
                    fullResponse.append(char)
                    // 实时更新消息内容
                    val updatedMessages = _uiState.value.messages.map { msg ->
                        if (msg.id == aiMessageId) {
                            msg.copy(content = fullResponse.toString())
                        } else {
                            msg
                        }
                    }
                    _uiState.value = _uiState.value.copy(messages = updatedMessages)
                }

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
            } catch (e: Exception) {
                e.printStackTrace()
                
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
    val isSending: Boolean = false,  // 发送锁定状态
    val chatSessions: List<ChatSessionInfo> = emptyList(),
    val remainingCalls: Int = 10,
    val dailyLimit: Int = 10
)
