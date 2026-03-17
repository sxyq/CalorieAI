package com.calorieai.app.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.local.OnboardingDataStore
import com.calorieai.app.data.model.ActivityLevel
import com.calorieai.app.data.model.Gender
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WeightLossStrategy
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.pow

/**
 * 引导流程ViewModel
 * 管理6步引导流程的状态和数据
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val onboardingDataStore: OnboardingDataStore,
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    // 当前步骤 (1-6)
    private val _currentStep = MutableStateFlow(1)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // 引导数据
    private val _onboardingData = MutableStateFlow(OnboardingStepData())
    val onboardingData: StateFlow<OnboardingStepData> = _onboardingData.asStateFlow()

    // 是否正在保存
    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    // 是否已完成
    private val _isCompleted = MutableStateFlow(false)
    val isCompleted: StateFlow<Boolean> = _isCompleted.asStateFlow()

    init {
        viewModelScope.launch {
            // 恢复之前的进度
            val savedStep = onboardingDataStore.getCurrentStep().first()
            val savedData = onboardingDataStore.getOnboardingData().first()

            _currentStep.value = savedStep
            savedData?.let { data ->
                _onboardingData.value = OnboardingStepData(
                    gender = data.gender?.let { Gender.fromString(it) },
                    birthDate = data.birthDate,
                    height = data.height ?: 0f,
                    currentWeight = data.weight ?: 0f,
                    bmi = data.bmi,
                    activityLevel = ActivityLevel.fromString(data.activityLevel),
                    dailyCalorieGoal = data.dailyCalorieGoal ?: 2000,
                    goalType = data.goalType?.let { GoalType.fromString(it) },
                    targetWeight = data.targetWeight,
                    weightLossStrategy = data.weightLossStrategy?.let { WeightLossStrategy.fromString(it) },
                    estimatedWeeks = data.estimatedWeeksToGoal
                )
            }
        }
    }

    /**
     * 跳转到指定步骤
     */
    fun navigateToStep(step: Int) {
        if (step in 1..6) {
            _currentStep.value = step
            viewModelScope.launch {
                onboardingDataStore.saveCurrentStep(step)
            }
        }
    }

    /**
     * 下一步
     */
    fun nextStep() {
        if (_currentStep.value < 6) {
            navigateToStep(_currentStep.value + 1)
        }
    }

    /**
     * 上一步
     */
    fun previousStep() {
        if (_currentStep.value > 1) {
            navigateToStep(_currentStep.value - 1)
        }
    }

    /**
     * 保存步骤1数据：基本信息
     */
    fun saveStep1Data(gender: Gender, birthDate: Long) {
        _onboardingData.value = _onboardingData.value.copy(
            gender = gender,
            birthDate = birthDate
        )
        saveData()
    }

    /**
     * 保存步骤2数据：身体数据
     */
    fun saveStep2Data(height: Float, weight: Float) {
        val bmi = calculateBMI(height, weight)
        _onboardingData.value = _onboardingData.value.copy(
            height = height,
            currentWeight = weight,
            bmi = bmi
        )
        saveData()
    }

    /**
     * 保存步骤3数据：生活习惯
     */
    fun saveStep3Data(activityLevel: ActivityLevel, dailyCalorieGoal: Int) {
        _onboardingData.value = _onboardingData.value.copy(
            activityLevel = activityLevel,
            dailyCalorieGoal = dailyCalorieGoal
        )
        saveData()
    }

    /**
     * 保存步骤4数据：目标设定
     */
    fun saveStep4Data(goalType: GoalType) {
        _onboardingData.value = _onboardingData.value.copy(
            goalType = goalType
        )
        saveData()
    }

    /**
     * 保存步骤5数据：目标细化
     */
    fun saveStep5Data(targetWeight: Float, strategy: WeightLossStrategy, estimatedWeeks: Int) {
        _onboardingData.value = _onboardingData.value.copy(
            targetWeight = targetWeight,
            weightLossStrategy = strategy,
            estimatedWeeks = estimatedWeeks
        )
        saveData()
    }

    /**
     * 完成引导流程
     */
    fun completeOnboarding() {
        viewModelScope.launch {
            _isSaving.value = true
            
            try {
                val data = _onboardingData.value
                
                // 计算BMR和TDEE
                val bmr = calculateBMR(data)
                val tdee = calculateTDEE(bmr, data.activityLevel)
                
                // 创建用户设置
                val userSettings = UserSettings(
                    id = 1,
                    dailyCalorieGoal = data.dailyCalorieGoal,
                    userGender = data.gender?.name,
                    userAge = calculateAge(data.birthDate),
                    userHeight = data.height,
                    userWeight = data.currentWeight,
                    activityLevel = data.activityLevel?.name ?: "SEDENTARY",
                    // 引导相关
                    onboardingCompleted = true,
                    onboardingCurrentStep = 6,
                    // 目标相关
                    goalType = data.goalType?.name,
                    targetWeight = data.targetWeight,
                    weightLossStrategy = data.weightLossStrategy?.name,
                    estimatedWeeksToGoal = data.estimatedWeeks,
                    weeklyWeightChangeGoal = data.weightLossStrategy?.weeklyChange,
                    // 身体档案
                    birthDate = data.birthDate,
                    bmr = bmr,
                    tdee = tdee,
                    bmi = data.bmi
                )
                
                // 保存到数据库
                userSettingsRepository.saveSettings(userSettings)
                
                // 标记引导完成
                onboardingDataStore.setOnboardingCompleted(true)
                onboardingDataStore.clearOnboardingData()
                
                _isCompleted.value = true
            } catch (e: Exception) {
                // 处理错误
                e.printStackTrace()
            } finally {
                _isSaving.value = false
            }
        }
    }

    /**
     * 保存当前数据到DataStore
     */
    private fun saveData() {
        viewModelScope.launch {
            onboardingDataStore.saveOnboardingData(_onboardingData.value)
        }
    }

    /**
     * 计算BMI
     */
    private fun calculateBMI(heightCm: Float, weightKg: Float): Float {
        val heightM = heightCm / 100
        return weightKg / heightM.pow(2)
    }

    /**
     * 计算BMR（基础代谢率）- Mifflin-St Jeor公式
     */
    private fun calculateBMR(data: OnboardingStepData): Int {
        val age = calculateAge(data.birthDate) ?: 30
        val weight = data.currentWeight
        val height = data.height
        
        return when (data.gender) {
            Gender.MALE -> (10 * weight + 6.25 * height - 5 * age + 5).toInt()
            Gender.FEMALE -> (10 * weight + 6.25 * height - 5 * age - 161).toInt()
            else -> (10 * weight + 6.25 * height - 5 * age - 78).toInt()
        }
    }

    /**
     * 计算TDEE（每日总能量消耗）
     */
    private fun calculateTDEE(bmr: Int, activityLevel: ActivityLevel?): Int {
        val multiplier = activityLevel?.multiplier ?: 1.2f
        return (bmr * multiplier).toInt()
    }

    /**
     * 计算年龄
     */
    private fun calculateAge(birthDate: Long?): Int? {
        if (birthDate == null) return null
        
        val birth = java.util.Calendar.getInstance().apply { timeInMillis = birthDate }
        val now = java.util.Calendar.getInstance()
        
        var age = now.get(java.util.Calendar.YEAR) - birth.get(java.util.Calendar.YEAR)
        if (now.get(java.util.Calendar.DAY_OF_YEAR) < birth.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--
        }
        return age
    }
}

/**
 * 引导步骤数据
 */
data class OnboardingStepData(
    // 步骤1：基本信息
    val gender: Gender? = null,
    val birthDate: Long? = null,
    
    // 步骤2：身体数据
    val height: Float = 0f,
    val currentWeight: Float = 0f,
    val bmi: Float? = null,
    
    // 步骤3：生活习惯
    val activityLevel: ActivityLevel? = null,
    val dailyCalorieGoal: Int = 2000,
    
    // 步骤4：目标设定
    val goalType: GoalType? = null,
    
    // 步骤5：目标细化
    val targetWeight: Float? = null,
    val weightLossStrategy: WeightLossStrategy? = null,
    val estimatedWeeks: Int? = null
)
