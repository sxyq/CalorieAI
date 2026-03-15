package com.calorieai.app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.ExerciseRecord
import com.calorieai.app.data.model.ExerciseType
import com.calorieai.app.data.model.FoodRecord
import com.calorieai.app.data.model.MealType
import com.calorieai.app.data.repository.ExerciseRecordRepository
import com.calorieai.app.data.repository.FoodRecordRepository
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.data.repository.WeightRecordRepository
import com.calorieai.app.ui.screens.settings.calculateBMR
import com.calorieai.app.ui.screens.settings.calculateTDEE
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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

    init {
        // 监听日期变化，加载对应日期的数据
        viewModelScope.launch {
            _selectedDate.collect { date ->
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
                        showAIWidget = it.showAIWidget
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
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // 将LocalDate转换为时间戳范围
            val startOfDay = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endOfDay = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1
            
            // 加载当前日期数据
            combine(
                foodRecordRepository.getRecordsByDateRange(startOfDay, endOfDay),
                foodRecordRepository.getTotalCaloriesByDateRange(startOfDay, endOfDay),
                exerciseRecordRepository.getRecordsBetween(startOfDay, endOfDay)
            ) { records, totalCalories, exerciseRecords ->
                // 加载最近30天的热量数据用于日历显示
                val calendarData = loadCalendarData()
                
                // 计算运动消耗和时长
                val totalExerciseCalories = exerciseRecords.sumOf { it.caloriesBurned }
                val totalExerciseMinutes = exerciseRecords.sumOf { it.durationMinutes }
                
                HomeUiState(
                    records = records,
                    exerciseRecords = exerciseRecords,
                    totalCalories = totalCalories ?: 0,
                    calorieData = calendarData,
                    dailyGoal = _uiState.value.dailyGoal,
                    bmr = _uiState.value.bmr,
                    exerciseCalories = totalExerciseCalories,
                    totalExerciseMinutes = totalExerciseMinutes,
                    tdee = _uiState.value.tdee,
                    currentWeight = _uiState.value.currentWeight,
                    showAIWidget = _uiState.value.showAIWidget,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
    
    /**
     * 加载日历数据（最近30天）
     */
    private suspend fun loadCalendarData(): Map<LocalDate, Int> {
        val today = LocalDate.now()
        val startDate = today.minusDays(30)
        val startOfRange = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfRange = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        val records = foodRecordRepository.getAllRecordsOnce()
        return records
            .filter { it.recordTime in startOfRange..endOfRange }
            .groupBy { 
                java.time.Instant.ofEpochMilli(it.recordTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
            .mapValues { (_, dayRecords) -> 
                dayRecords.sumOf { it.totalCalories }
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
    val calorieData: Map<LocalDate, Int> = emptyMap(),
    val isLoading: Boolean = true,
    val showAIWidget: Boolean = true
)
