package com.calorieai.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository
) : ViewModel() {

    // 当前选中的日期
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // UI状态
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        // 监听日期变化，加载对应日期的数据
        viewModelScope.launch {
            _selectedDate.collect { date ->
                loadDataForDate(date)
            }
        }
    }

    /**
     * 选择日期
     */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    /**
     * 加载指定日期的数据
     */
    private fun loadDataForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 将LocalDate转换为时间戳范围
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            
            combine(
                foodRecordRepository.getRecordsByDateRange(startOfDay, endOfDay),
                foodRecordRepository.getTotalCaloriesByDateRange(startOfDay, endOfDay)
            ) { records, totalCalories ->
                HomeUiState(
                    records = records,
                    totalCalories = totalCalories ?: 0,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    /**
     * 切换收藏状态
     */
    fun toggleStarred(record: FoodRecord) {
        viewModelScope.launch {
            foodRecordRepository.toggleStarred(record.id, !record.isStarred)
        }
    }

    /**
     * 删除记录
     */
    fun deleteRecord(record: FoodRecord) {
        viewModelScope.launch {
            foodRecordRepository.deleteRecord(record)
        }
    }

    /**
     * 刷新数据
     */
    fun refreshData() {
        loadDataForDate(_selectedDate.value)
    }
}

data class HomeUiState(
    val records: List<FoodRecord> = emptyList(),
    val totalCalories: Int = 0,
    val dailyGoal: Int = 2000,
    val isLoading: Boolean = true
)
