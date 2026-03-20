package com.calorieai.app.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.repository.ExerciseRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExerciseRecordViewModel @Inject constructor(
    private val exerciseRecordRepository: ExerciseRecordRepository
) : ViewModel() {
    private var recordsJob: Job? = null

    private val _uiState = MutableStateFlow(ExerciseRecordUiState())
    val uiState: StateFlow<ExerciseRecordUiState> = _uiState.asStateFlow()

    init {
        loadRecordsForSelectedDate()
    }

    private fun getDayRange(timestamp: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis
        return startOfDay to endOfDay
    }

    private fun loadRecordsForSelectedDate() {
        recordsJob?.cancel()
        recordsJob = viewModelScope.launch {
            val (startOfDay, endOfDay) = getDayRange(_uiState.value.selectedDateMillis)

            exerciseRecordRepository.getRecordsBetween(startOfDay, endOfDay)
                .collect { records ->
                    val totalCalories = records.sumOf { it.caloriesBurned }
                    val items = records.map { record ->
                        ExerciseRecordItem(
                            id = record.id.toString(),
                            exerciseTypeName = getExerciseTypeDisplayName(record.exerciseType),
                            duration = record.durationMinutes,
                            calories = record.caloriesBurned,
                            note = record.notes ?: "",
                            timestamp = record.recordTime
                        )
                    }
                    _uiState.value = _uiState.value.copy(
                        todayRecords = items,
                        todayCalories = totalCalories,
                        todayExerciseCount = records.size
                    )
                }
        }
    }

    fun setSelectedDateFromString(dateStr: String) {
        try {
            val parts = dateStr.split("-")
            if (parts.size == 3) {
                val year = parts[0].toInt()
                val month = parts[1].toInt() - 1
                val day = parts[2].toInt()
                val calendar = Calendar.getInstance().apply {
                    set(year, month, day, 12, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                _uiState.value = _uiState.value.copy(selectedDateMillis = calendar.timeInMillis)
                loadRecordsForSelectedDate()
            }
        } catch (_: Exception) {
        }
    }

    private fun getExerciseTypeDisplayName(type: ExerciseType): String {
        return when (type) {
            ExerciseType.RUNNING -> "跑步"
            ExerciseType.WALKING -> "快走"
            ExerciseType.CYCLING -> "骑行"
            ExerciseType.SWIMMING -> "游泳"
            ExerciseType.YOGA -> "瑜伽"
            ExerciseType.WEIGHT_TRAINING -> "力量训练"
            ExerciseType.HIIT -> "HIIT"
            ExerciseType.DANCING -> "跳舞"
            ExerciseType.HIKING -> "徒步"
            ExerciseType.SKIPPING -> "跳绳"
            ExerciseType.PILATES -> "普拉提"
            ExerciseType.ELLIPTICAL -> "椭圆机"
            ExerciseType.ROWING -> "划船"
            ExerciseType.BOXING -> "拳击"
            ExerciseType.SKATING -> "滑冰"
            ExerciseType.SKIING -> "滑雪"
            ExerciseType.BASKETBALL -> "篮球"
            ExerciseType.FOOTBALL -> "足球"
            ExerciseType.BADMINTON -> "羽毛球"
            ExerciseType.TENNIS -> "网球"
            ExerciseType.TABLE_TENNIS -> "乒乓球"
            ExerciseType.GOLF -> "高尔夫"
            ExerciseType.VOLLEYBALL -> "排球"
            ExerciseType.BASEBALL -> "棒球"
            ExerciseType.CLIMBING -> "攀岩"
            ExerciseType.SURFING -> "冲浪"
            ExerciseType.SKATEBOARDING -> "滑板"
            ExerciseType.OTHER -> "其他"
        }
    }

    fun selectExerciseType(type: ExerciseType) {
        _uiState.value = _uiState.value.copy(selectedExerciseType = type)
    }

    fun updateDuration(duration: String) {
        _uiState.value = _uiState.value.copy(durationInput = duration)
    }

    fun updateCalories(calories: String) {
        _uiState.value = _uiState.value.copy(caloriesInput = calories)
    }

    fun updateNote(note: String) {
        _uiState.value = _uiState.value.copy(noteInput = note)
    }

    fun showHistoryDialog() {
        _uiState.value = _uiState.value.copy(showHistoryDialog = true)
    }

    fun hideHistoryDialog() {
        _uiState.value = _uiState.value.copy(showHistoryDialog = false)
    }

    fun saveExerciseRecord() {
        val duration = _uiState.value.durationInput.toIntOrNull() ?: return
        val calories = if (_uiState.value.caloriesInput.isNotBlank()) {
            _uiState.value.caloriesInput.toIntOrNull()
        } else {
            _uiState.value.selectedExerciseType.caloriesPerMinute * duration
        } ?: return

        viewModelScope.launch {
            val record = ExerciseRecord(
                exerciseType = _uiState.value.selectedExerciseType,
                durationMinutes = duration,
                caloriesBurned = calories,
                notes = _uiState.value.noteInput.takeIf { it.isNotBlank() },
                recordTime = _uiState.value.selectedDateMillis
            )
            exerciseRecordRepository.addRecord(record)

            // 重置输入
            _uiState.value = _uiState.value.copy(
                durationInput = "",
                caloriesInput = "",
                noteInput = ""
            )
        }
    }

    fun deleteRecord(recordId: String) {
        viewModelScope.launch {
            exerciseRecordRepository.deleteRecordById(recordId)
        }
    }
}

data class ExerciseRecordUiState(
    val selectedExerciseType: ExerciseType = ExerciseType.RUNNING,
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val durationInput: String = "",
    val caloriesInput: String = "",
    val noteInput: String = "",
    val todayRecords: List<ExerciseRecordItem> = emptyList(),
    val todayCalories: Int = 0,
    val todayExerciseCount: Int = 0,
    val showHistoryDialog: Boolean = false
)
