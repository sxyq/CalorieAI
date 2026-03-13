package com.calorieai.app.ui.screens.exercise

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.repository.ExerciseRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExerciseRecordViewModel @Inject constructor(
    private val exerciseRecordRepository: ExerciseRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExerciseRecordUiState())
    val uiState: StateFlow<ExerciseRecordUiState> = _uiState.asStateFlow()

    init {
        loadTodayRecords()
    }

    private fun loadTodayRecords() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val endOfDay = calendar.timeInMillis

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
                recordTime = System.currentTimeMillis()
            )
            exerciseRecordRepository.insertRecord(record)

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
    val durationInput: String = "",
    val caloriesInput: String = "",
    val noteInput: String = "",
    val todayRecords: List<ExerciseRecordItem> = emptyList(),
    val todayCalories: Int = 0,
    val todayExerciseCount: Int = 0,
    val showHistoryDialog: Boolean = false
)
