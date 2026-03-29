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
        val displayRecords = groupRecordsForDisplay(records).take(100)

        return AIModelCallStatsUiState(
            isLoading = false,
            records = displayRecords,
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

    private fun groupRecordsForDisplay(records: List<APICallRecord>): List<APICallRecord> {
        if (records.isEmpty()) return emptyList()

        val normalRecords = mutableListOf<APICallRecord>()
        val batchGroups = linkedMapOf<String, MutableList<APICallRecord>>()

        records.forEach { record ->
            val key = deriveBatchKey(record)
            if (key == null) {
                normalRecords += record
            } else {
                batchGroups.getOrPut(key) { mutableListOf() }.add(record)
            }
        }

        val mergedBatchRecords = batchGroups.values.map { batch ->
            mergeBatch(batch)
        }

        return (normalRecords + mergedBatchRecords).sortedByDescending { it.timestamp }
    }

    private fun deriveBatchKey(record: APICallRecord): String? {
        val input = record.inputText
        val imageMarker = imageBatchRegex.find(input)?.groupValues?.getOrNull(1)
        if (!imageMarker.isNullOrBlank()) {
            return "img:$imageMarker"
        }
        val textMarker = textBatchRegex.find(input)?.groupValues?.getOrNull(1)
        if (!textMarker.isNullOrBlank()) {
            return "txt:$textMarker"
        }

        if (legacyImageAttemptRegex.containsMatchIn(input)) {
            val normalizedPrompt = sanitizeImagePrompt(input)
            val timeBucket = record.timestamp / 30_000L
            return "legacy:${record.configId}:${record.modelId}:${normalizedPrompt.hashCode()}:$timeBucket"
        }

        return null
    }

    private fun mergeBatch(batch: List<APICallRecord>): APICallRecord {
        val sorted = batch.sortedWith(
            compareBy<APICallRecord> { extractAttemptNumber(it.inputText) }
                .thenBy { it.timestamp }
        )
        val first = sorted.first()
        val isTextBatch = textBatchRegex.containsMatchIn(first.inputText)
        val title = if (isTextBatch) "文本导入并发任务" else "图片分析并发任务"

        val mergedPrompt = buildString {
            appendLine("【$title】共${sorted.size}次请求（已合并展示）")
            sorted.forEach { record ->
                val attempt = extractAttemptNumber(record.inputText)
                appendLine()
                appendLine("---- 尝试#$attempt Prompt ----")
                appendLine(sanitizePrompt(record.inputText))
            }
        }.trim()

        val mergedReply = buildString {
            sorted.forEach { record ->
                val attempt = extractAttemptNumber(record.inputText)
                appendLine("---- 尝试#$attempt 回复 ----")
                appendLine(record.outputText.ifBlank { "(无返回)" })
                appendLine()
            }
        }.trim()

        val mergedErrors = sorted
            .filter { !it.isSuccess && !it.errorMessage.isNullOrBlank() }
            .joinToString("\n") { record ->
                val attempt = extractAttemptNumber(record.inputText)
                "尝试#$attempt: ${record.errorMessage}"
            }
            .ifBlank { null }

        return APICallRecord(
            id = "merged-${first.id}",
            timestamp = sorted.maxOf { it.timestamp },
            configId = first.configId,
            configName = first.configName,
            modelId = first.modelId,
            inputText = mergedPrompt,
            outputText = mergedReply,
            promptTokens = sorted.sumOf { it.promptTokens },
            completionTokens = sorted.sumOf { it.completionTokens },
            totalTokens = sorted.sumOf { it.totalTokens },
            cost = sorted.sumOf { it.cost },
            duration = sorted.sumOf { it.duration },
            isSuccess = sorted.any { it.isSuccess },
            errorMessage = mergedErrors
        )
    }

    private fun sanitizePrompt(input: String): String {
        return input
            .replace(imageBatchRegex, "")
            .replace(textBatchRegex, "")
            .replace(imageAttemptRegex, "")
            .replace(legacyImageAttemptRegex, "")
            .trim()
    }

    private fun sanitizeImagePrompt(input: String): String {
        return input
            .replace(legacyImageAttemptRegex, "")
            .trim()
    }

    private fun extractAttemptNumber(input: String): Int {
        val markerAttempt = imageAttemptRegex.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull()
        if (markerAttempt != null) return markerAttempt
        return legacyImageAttemptRegex.find(input)?.groupValues?.getOrNull(1)?.toIntOrNull() ?: Int.MAX_VALUE
    }

    companion object {
        private val imageBatchRegex = Regex("\\[图片分析任务#([A-Za-z0-9-]+)]", RegexOption.IGNORE_CASE)
        private val textBatchRegex = Regex("\\[文本分析任务#([A-Za-z0-9-]+)]", RegexOption.IGNORE_CASE)
        private val imageAttemptRegex = Regex("\\[尝试#(\\d+)]")
        private val legacyImageAttemptRegex = Regex("^图片分析请求\\(尝试#(\\d+)\\):\\s*", RegexOption.IGNORE_CASE)
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
