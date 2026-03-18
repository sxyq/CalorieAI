package com.calorieai.app.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.calorieai.app.data.model.Gender
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.data.model.WeightLossStrategy
import com.calorieai.app.ui.screens.onboarding.OnboardingStepData
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_prefs")

/**
 * 引导流程数据存储
 * 用于临时存储引导过程中的数据
 */
@Singleton
class OnboardingDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val gson = Gson()
    
    companion object {
        // 引导完成标记
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        // 当前步骤
        private val CURRENT_STEP = intPreferencesKey("current_step")
        // 引导数据JSON
        private val ONBOARDING_DATA = stringPreferencesKey("onboarding_data")
    }
    
    /**
     * 检查引导是否已完成
     */
    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[ONBOARDING_COMPLETED] ?: false
        }
    
    /**
     * 获取当前步骤
     */
    val currentStep: Flow<Int> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[CURRENT_STEP] ?: 1
        }
    
    val onboardingData: Flow<OnboardingData?> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[ONBOARDING_DATA]?.let { json ->
                try {
                    gson.fromJson(json, OnboardingData::class.java)
                } catch (e: Exception) {
                    null
                }
            }
        }
    
    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }
    
    /**
     * 设置当前步骤
     */
    suspend fun setCurrentStep(step: Int) {
        context.onboardingDataStore.edit { preferences ->
            preferences[CURRENT_STEP] = step
        }
    }
    
    suspend fun saveOnboardingData(data: OnboardingData) {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_DATA] = gson.toJson(data)
        }
    }

    /**
     * 保存引导步骤数据（适配OnboardingStepData）
     */
    suspend fun saveOnboardingData(data: OnboardingStepData) {
        val onboardingData = OnboardingData(
            gender = data.gender?.name,
            birthDate = data.birthDate,
            weight = data.currentWeight,
            height = data.height,
            activityLevel = data.activityLevel?.name ?: "SEDENTARY",
            dailyCalorieGoal = data.dailyCalorieGoal,
            goalType = data.goalType?.name,
            targetWeight = data.targetWeight,
            weightLossStrategy = data.weightLossStrategy?.name,
            estimatedWeeksToGoal = data.estimatedWeeks,
            bmi = data.bmi
        )
        saveOnboardingData(onboardingData)
    }
    
    /**
     * 更新引导数据（部分更新）
     */
    suspend fun updateOnboardingData(update: (OnboardingData) -> OnboardingData) {
        context.onboardingDataStore.edit { preferences ->
            val currentData = preferences[ONBOARDING_DATA]?.let { json ->
                try {
                    gson.fromJson(json, OnboardingData::class.java)
                } catch (e: Exception) {
                    OnboardingData()
                }
            } ?: OnboardingData()
            
            val updatedData = update(currentData)
            preferences[ONBOARDING_DATA] = gson.toJson(updatedData)
        }
    }
    
    /**
     * 清除引导过程数据，但保留完成标记
     */
    suspend fun clearOnboardingData() {
        context.onboardingDataStore.edit { preferences ->
            val completed = preferences[ONBOARDING_COMPLETED]
            preferences.clear()
            // 恢复完成标记
            completed?.let {
                preferences[ONBOARDING_COMPLETED] = it
            }
        }
    }
    
    /**
     * 重置引导流程（用于重新引导）
     */
    suspend fun resetOnboarding() {
        context.onboardingDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = false
            preferences[CURRENT_STEP] = 1
            preferences[ONBOARDING_DATA] = gson.toJson(OnboardingData())
        }
    }
}

/**
 * 引导数据数据类
 * 存储引导流程中收集的所有用户信息
 */
data class OnboardingData(
    // 步骤1：基本信息
    @SerializedName("gender")
    val gender: String? = null,  // MALE, FEMALE, OTHER
    
    @SerializedName("birthDate")
    val birthDate: Long? = null,  // 时间戳
    
    // 步骤2：身体数据
    @SerializedName("weight")
    val weight: Float? = null,  // kg
    
    @SerializedName("height")
    val height: Float? = null,  // cm
    
    @SerializedName("weightUnit")
    val weightUnit: String = "kg",  // kg, lb
    
    @SerializedName("heightUnit")
    val heightUnit: String = "cm",  // cm, ft
    
    // 步骤3：生活习惯
    @SerializedName("exerciseHabits")
    val exerciseHabits: List<String> = emptyList(),  // 运动习惯列表
    
    @SerializedName("dailyCalorieGoal")
    val dailyCalorieGoal: Int? = null,  // 每日热量目标
    
    @SerializedName("activityLevel")
    val activityLevel: String = "SEDENTARY",  // 活动水平
    
    // 步骤4：目标设定
    @SerializedName("goalType")
    val goalType: String? = null,  // LOSE_WEIGHT, GAIN_MUSCLE, GAIN_WEIGHT, MAINTAIN
    
    // 步骤5：目标细化
    @SerializedName("targetWeight")
    val targetWeight: Float? = null,  // 目标体重(kg)
    
    @SerializedName("weightLossStrategy")
    val weightLossStrategy: String? = null,  // AGGRESSIVE, MODERATE, GENTLE
    
    @SerializedName("estimatedWeeksToGoal")
    val estimatedWeeksToGoal: Int? = null,  // 预计达成目标周数
    
    // 计算字段
    @SerializedName("bmr")
    val bmr: Int? = null,  // 基础代谢率
    
    @SerializedName("tdee")
    val tdee: Int? = null,  // 每日总能量消耗
    
    @SerializedName("bmi")
    val bmi: Float? = null  // BMI指数
) {
    /**
     * 计算年龄
     */
    fun calculateAge(): Int? {
        return birthDate?.let { birth ->
            val birthYear = java.time.Instant.ofEpochMilli(birth)
                .atZone(java.time.ZoneId.systemDefault())
                .year
            java.time.Year.now().value - birthYear
        }
    }
    
    /**
     * 计算BMR（基础代谢率）
     */
    fun calculateBMR(): Int? {
        val w = weight ?: return null
        val h = height ?: return null
        val age = calculateAge() ?: return null
        
        return when (gender?.uppercase()) {
            "MALE" -> (10 * w + 6.25f * h - 5 * age + 5).toInt()
            "FEMALE" -> (10 * w + 6.25f * h - 5 * age - 161).toInt()
            else -> (10 * w + 6.25f * h - 5 * age - 78).toInt()
        }
    }
    
    /**
     * 计算TDEE（每日总能量消耗）
     */
    fun calculateTDEE(): Int? {
        val bmrValue = calculateBMR() ?: return null
        val multiplier = when (activityLevel) {
            "SEDENTARY" -> 1.2f
            "LIGHT" -> 1.375f
            "MODERATE" -> 1.55f
            "ACTIVE" -> 1.725f
            "VERY_ACTIVE" -> 1.9f
            else -> 1.2f
        }
        return (bmrValue * multiplier).toInt()
    }
    
    /**
     * 计算BMI
     */
    fun calculateBMI(): Float? {
        val w = weight ?: return null
        val h = height ?: return null
        val heightInMeters = h / 100f
        return w / (heightInMeters * heightInMeters)
    }
    
    /**
     * 计算预计达成目标周数
     */
    fun calculateEstimatedWeeks(): Int? {
        val currentWeight = weight ?: return null
        val target = targetWeight ?: return null
        val strategy = weightLossStrategy?.let { WeightLossStrategy.fromString(it) } ?: return null
        
        val weightDiff = kotlin.math.abs(currentWeight - target)
        return if (strategy.weeklyChange > 0) {
            (weightDiff / strategy.weeklyChange).toInt().coerceAtLeast(1)
        } else null
    }
    
    /**
     * 获取性别枚举
     */
    fun getGenderEnum(): Gender? {
        return gender?.let { Gender.fromString(it) }
    }
    
    /**
     * 获取目标类型枚举
     */
    fun getGoalTypeEnum(): GoalType? {
        return goalType?.let { GoalType.fromString(it) }
    }
    
    /**
     * 获取减肥策略枚举
     */
    fun getWeightLossStrategyEnum(): WeightLossStrategy? {
        return weightLossStrategy?.let { WeightLossStrategy.fromString(it) }
    }
    
    /**
     * 验证数据完整性
     */
    fun isValidForStep(step: Int): Boolean {
        return when (step) {
            1 -> gender != null && birthDate != null
            2 -> weight != null && height != null
            3 -> dailyCalorieGoal != null
            4 -> goalType != null
            5 -> targetWeight != null && weightLossStrategy != null
            else -> true
        }
    }
    
    /**
     * 获取所有计算后的数据
     */
    fun withCalculatedValues(): OnboardingData {
        return copy(
            bmr = calculateBMR(),
            tdee = calculateTDEE(),
            bmi = calculateBMI(),
            estimatedWeeksToGoal = calculateEstimatedWeeks()
        )
    }
}
