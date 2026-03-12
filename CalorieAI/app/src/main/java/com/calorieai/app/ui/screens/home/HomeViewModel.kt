package com.calorieai.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.FoodRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadTodayData()
    }

    private fun loadTodayData() {
        viewModelScope.launch {
            combine(
                foodRecordRepository.getTodayRecords(),
                foodRecordRepository.getTodayTotalCalories()
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

    fun toggleStarred(record: FoodRecord) {
        viewModelScope.launch {
            foodRecordRepository.toggleStarred(record.id, record.isStarred)
        }
    }

    fun deleteRecord(record: FoodRecord) {
        viewModelScope.launch {
            foodRecordRepository.deleteRecord(record)
        }
    }

    fun refreshData() {
        // 数据流会自动刷新，这里可以添加额外的刷新逻辑
        // 比如重新加载数据或触发UI更新
        _uiState.value = _uiState.value.copy()
    }
}

data class HomeUiState(
    val records: List<FoodRecord> = emptyList(),
    val totalCalories: Int = 0,
    val dailyGoal: Int = 2000,
    val isLoading: Boolean = true
)
