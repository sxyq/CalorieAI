package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.APICallRecord
import com.calorieai.app.data.repository.APICallRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AIModelCallStatsViewModel @Inject constructor(
    private val apiCallRecordRepository: APICallRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AIModelCallStatsUiState())
    val uiState: StateFlow<AIModelCallStatsUiState> = _uiState.asStateFlow()

    init {
        observeRecords()
    }

    private fun observeRecords() {
        viewModelScope.launch {
            apiCallRecordRepository.getAllRecords().collect { records ->
                _uiState.update {
                    buildUiState(records = records)
                }
            }
        }
    }

    private fun buildUiState(records: List<APICallRecord>): AIModelCallStatsUiState {
        val totalCalls = records.size
        val successCalls = records.count { it.isSuccess }
        val failedCalls = totalCalls - successCalls
        val successRate = if (totalCalls == 0) 0f else successCalls.toFloat() / totalCalls.toFloat()

        val totalPromptChars = records.sumOf { it.inputText.length }
        val totalReplyChars = records.sumOf { it.outputText.length }
        val totalPromptTokens = records.sumOf { it.promptTokens }
        val totalCompletionTokens = records.sumOf { it.completionTokens }
        val totalCost = records.sumOf { it.cost }
        val avgDuration = if (records.isEmpty()) 0L else (records.sumOf { it.duration } / records.size)

        return AIModelCallStatsUiState(
            isLoading = false,
            records = records.take(100),
            totalCalls = totalCalls,
            successCalls = successCalls,
            failedCalls = failedCalls,
            successRate = successRate,
            totalPromptChars = totalPromptChars,
            totalReplyChars = totalReplyChars,
            totalPromptTokens = totalPromptTokens,
            totalCompletionTokens = totalCompletionTokens,
            totalCost = totalCost,
            avgDuration = avgDuration
        )
    }
}

data class AIModelCallStatsUiState(
    val isLoading: Boolean = true,
    val records: List<APICallRecord> = emptyList(),
    val totalCalls: Int = 0,
    val successCalls: Int = 0,
    val failedCalls: Int = 0,
    val successRate: Float = 0f,
    val totalPromptChars: Int = 0,
    val totalReplyChars: Int = 0,
    val totalPromptTokens: Int = 0,
    val totalCompletionTokens: Int = 0,
    val totalCost: Double = 0.0,
    val avgDuration: Long = 0L
)
