package com.calorieai.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.ui.screens.settings.calculateBMR
import com.calorieai.app.ui.screens.settings.calculateTDEE
import com.calorieai.app.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val foodRecordRepository: FoodRecordRepository,
    private val userSettingsRepository: UserSettingsRepository,
    private val exerciseRecordRepository: ExerciseRecordRepository,
    private val weightRecordRepository: WeightRecordRepository
) : ViewModel() {

    // 当前选中的日期
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    // UI状态
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var dateDataJob: Job? = null

    init {
        // 监听日期变化，加载对应日期的数据
        viewModelScope.launch {
            _selectedDate.collectLatest { date ->
                loadDataForDate(date)
            }
        }
        
        // 加载用户设置和最新体重（BMR等）
        viewModelScope.launch {
            combine(
                userSettingsRepository.getSettings(),
                weightRecordRepository.getLatestRecord()
            ) { settings, latestWeightRecord ->
                settings?.let {
                    // 优先使用最新体重记录，如果没有则使用用户设置中的体重
                    val currentWeight = latestWeightRecord?.weight ?: it.userWeight
                    val bmr = calculateBMR(
                        gender = it.userGender ?: "MALE",
                        weight = currentWeight,
                        height = it.userHeight,
                        age = it.userAge
                    )
                    val tdee = calculateTDEE(bmr, it.activityLevel)
                    _uiState.value = _uiState.value.copy(
                        dailyGoal = it.dailyCalorieGoal,
                        bmr = bmr,
                        tdee = tdee,
                        currentWeight = currentWeight,
                        showAIWidget = it.showAIWidget,
                        enableQuickAdd = it.enableQuickAdd
                    )
                }
            }.collect()
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
        dateDataJob?.cancel()
        dateDataJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 使用 DateUtils 获取日期范围
            val (startOfDay, endOfDay) = DateUtils.getDayRange(date)
            val calendarRange = getCalendarRange(date)
            
            // 加载当前日期数据
            combine(
                foodRecordRepository.getRecordsByDateRange(startOfDay, endOfDay),
                foodRecordRepository.getTotalCaloriesByDateRange(startOfDay, endOfDay),
                exerciseRecordRepository.getRecordsBetween(startOfDay, endOfDay),
                foodRecordRepository.getRecordsByDateRange(calendarRange.startMillis, calendarRange.endMillis)
            ) { records, totalCalories, exerciseRecords, heatmapRecords ->
                Quadruple(records, totalCalories, exerciseRecords, heatmapRecords)
            }.collectLatest { (records, totalCalories, exerciseRecords, heatmapRecords) ->
                val calendarData = withContext(Dispatchers.Default) {
                    buildCalendarData(heatmapRecords)
                }

                // 计算运动消耗和时长
                val totalExerciseCalories = exerciseRecords.sumOf { it.caloriesBurned }
                val totalExerciseMinutes = exerciseRecords.sumOf { it.durationMinutes }

                _uiState.value = _uiState.value.copy(
                    records = records,
                    exerciseRecords = exerciseRecords,
                    totalCalories = totalCalories ?: 0,
                    calorieData = calendarData,
                    exerciseCalories = totalExerciseCalories,
                    totalExerciseMinutes = totalExerciseMinutes,
                    isLoading = false
                )
            }
        }
    }
    
    /**
     * 加载日历数据（最近30天）
     */
    private fun buildCalendarData(records: List<FoodRecord>): Map<LocalDate, Int> {
        return records
            .groupBy {
                java.time.Instant.ofEpochMilli(it.recordTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .mapValues { (_, dayRecords) -> 
                dayRecords.sumOf { it.totalCalories }
            }
    }

    private fun getCalendarRange(endDate: LocalDate): CalendarRange {
        val zoneId = ZoneId.systemDefault()
        val start = endDate.minusDays(29)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli()
        val end = endDate.plusDays(1)
            .atStartOfDay(zoneId)
            .toInstant()
            .toEpochMilli() - 1
        return CalendarRange(startMillis = start, endMillis = end)
    }

    private data class CalendarRange(
        val startMillis: Long,
        val endMillis: Long
    )

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
    
    /**
     * 添加运动消耗
     * @param exerciseType 运动类型
     * @param calories 消耗热量
     * @param notes 备注（自定义运动时格式为 "CUSTOM:{name}:{caloriesPerMinute}"）
     * @param durationMinutes 运动时长（分钟）
     */
    fun addExercise(
        exerciseType: ExerciseType,
        calories: Int,
        notes: String? = null,
        durationMinutes: Int = 30
    ) {
        viewModelScope.launch {
            // 创建运动记录
            val record = ExerciseRecord(
                exerciseType = exerciseType,
                durationMinutes = durationMinutes,
                caloriesBurned = calories,
                notes = notes,
                recordTime = System.currentTimeMillis()
            )

            // 保存到数据库
            exerciseRecordRepository.addRecord(record)

            // 刷新数据以更新运动记录列表
            refreshData()
        }
    }
    
    /**
     * 删除运动记录
     */
    fun deleteExerciseRecord(record: ExerciseRecord) {
        viewModelScope.launch {
            exerciseRecordRepository.deleteRecord(record)
            refreshData()
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

data class HomeUiState(
    val records: List<FoodRecord> = emptyList(),
    val exerciseRecords: List<ExerciseRecord> = emptyList(),
    val totalCalories: Int = 0,
    val dailyGoal: Int = 2000,
    val bmr: Int = 0,
    val exerciseCalories: Int = 0,
    val totalExerciseMinutes: Int = 0,
    val tdee: Int = 0,
    val currentWeight: Float? = null,
    val enableQuickAdd: Boolean = false,
    val calorieData: Map<LocalDate, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val showAIWidget: Boolean = true
)
