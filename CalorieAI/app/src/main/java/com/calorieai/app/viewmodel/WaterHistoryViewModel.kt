package com.calorieai.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.WaterRecord
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WaterRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class WaterHistoryViewModel @Inject constructor(
    private val waterRecordRepository: WaterRecordRepository,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _waterRecords = MutableStateFlow<List<WaterRecord>>(emptyList())
    val waterRecords: StateFlow<List<WaterRecord>> = _waterRecords.asStateFlow()

    private val _todayAmount = MutableStateFlow(0)
    val todayAmount: StateFlow<Int> = _todayAmount.asStateFlow()

    private val _targetAmount = MutableStateFlow(2000) // 默认目标2000ml
    val targetAmount: StateFlow<Int> = _targetAmount.asStateFlow()

    private val _weeklyAverage = MutableStateFlow(0f)
    val weeklyAverage: StateFlow<Float> = _weeklyAverage.asStateFlow()

    init {
        loadWaterRecords()
        loadTargetAmount()
        loadTodayAmount()
        loadWeeklyAverage()
    }

    private fun loadWaterRecords() {
        viewModelScope.launch {
            waterRecordRepository.getAllRecords().collect { records ->
                _waterRecords.value = records
            }
        }
    }

    private fun loadTargetAmount() {
        viewModelScope.launch {
            val settings = userSettingsRepository.getSettings().firstOrNull()
            settings?.dailyWaterGoal?.let {
                _targetAmount.value = it
            }
        }
    }

    private fun loadTodayAmount() {
        viewModelScope.launch {
            val amount = waterRecordRepository.getTodayTotalAmount()
            _todayAmount.value = amount
        }
    }

    private fun loadWeeklyAverage() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val endOfToday = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_WEEK, -7)
            val startOfWeek = calendar.timeInMillis
            
            val records = waterRecordRepository.getRecordsBetweenSync(startOfWeek, endOfToday)
            val totalAmount = records.sumOf { it.amount }
            val days = 7
            _weeklyAverage.value = if (days > 0) totalAmount.toFloat() / days else 0f
        }
    }

    fun addWaterRecord(amount: Int, note: String?) {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            val recordDate = calendar.apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            val record = WaterRecord(
                amount = amount,
                recordDate = recordDate,
                note = note
            )
            waterRecordRepository.insert(record)
            
            // 刷新今日饮水量
            loadTodayAmount()
            loadWeeklyAverage()
        }
    }

    fun deleteWaterRecord(record: WaterRecord) {
        viewModelScope.launch {
            waterRecordRepository.delete(record)
            loadTodayAmount()
            loadWeeklyAverage()
        }
    }

    fun updateTargetAmount(amount: Int) {
        viewModelScope.launch {
            _targetAmount.value = amount
            val settings = userSettingsRepository.getSettings().firstOrNull()
            settings?.let {
                val updatedSettings = it.copy(dailyWaterGoal = amount)
                userSettingsRepository.saveSettings(updatedSettings)
            }
        }
    }

    // 获取指定日期的饮水总量
    fun getAmountForDate(date: Long): Flow<Int> = flow {
        val amount = waterRecordRepository.getTotalAmountByDate(date)
        emit(amount)
    }
}
