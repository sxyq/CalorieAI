package com.calorieai.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.calorieai.app.data.local.UserSettingsDao
import com.calorieai.app.data.model.GoalType
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.model.WeightLossStrategy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserSettingsRepository @Inject constructor(
    private val userSettingsDao: UserSettingsDao,
    @ApplicationContext private val context: Context
) {
    private val encryptedPrefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            EncryptedSharedPreferences.create(
                context,
                "encrypted_app_settings",
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            context.getSharedPreferences("app_settings_fallback", Context.MODE_PRIVATE)
        }
    }

    fun getSettings(): Flow<UserSettings?> = userSettingsDao.getSettings()

    suspend fun saveSettings(settings: UserSettings) {
        userSettingsDao.insertOrUpdate(settings)
        syncToEncryptedPreferences(settings)
    }

    suspend fun getSettingsOnce(): UserSettings? {
        return userSettingsDao.getSettingsOnce()
    }

    /**
     * 更新引导完成状态
     */
    suspend fun updateOnboardingCompleted(completed: Boolean) {
        val settings = getSettingsOnce() ?: UserSettings()
        saveSettings(settings.copy(onboardingCompleted = completed))
    }

    /**
     * 更新当前引导步骤
     */
    suspend fun updateOnboardingStep(step: Int) {
        val settings = getSettingsOnce() ?: UserSettings()
        saveSettings(settings.copy(onboardingCurrentStep = step))
    }

    /**
     * 更新引导数据JSON
     */
    suspend fun updateOnboardingData(dataJson: String?) {
        val settings = getSettingsOnce() ?: UserSettings()
        saveSettings(settings.copy(onboardingDataJson = dataJson))
    }

    /**
     * 更新用户目标
     */
    suspend fun updateGoal(
        goalType: GoalType?,
        targetWeight: Float?,
        strategy: WeightLossStrategy?,
        estimatedWeeks: Int? = null
    ) {
        val settings = getSettingsOnce() ?: UserSettings()
        val weeklyChange = strategy?.weeklyChange
        saveSettings(settings.copy(
            goalType = goalType?.name,
            targetWeight = targetWeight,
            weightLossStrategy = strategy?.name,
            estimatedWeeksToGoal = estimatedWeeks,
            weeklyWeightChangeGoal = weeklyChange
        ))
    }

    /**
     * 更新用户身体档案
     */
    suspend fun updateBodyProfile(
        birthDate: Long? = null,
        height: Float? = null,
        weight: Float? = null,
        bmr: Int? = null,
        tdee: Int? = null,
        bmi: Float? = null
    ) {
        val settings = getSettingsOnce() ?: UserSettings()
        saveSettings(settings.copy(
            birthDate = birthDate ?: settings.birthDate,
            userHeight = height ?: settings.userHeight,
            userWeight = weight ?: settings.userWeight,
            bmr = bmr ?: settings.bmr,
            tdee = tdee ?: settings.tdee,
            bmi = bmi ?: settings.bmi
        ))
    }

    /**
     * 更新运动习惯
     */
    suspend fun updateExerciseHabits(habitsJson: String?) {
        val settings = getSettingsOnce() ?: UserSettings()
        saveSettings(settings.copy(exerciseHabitsJson = habitsJson))
    }

    /**
     * 检查是否需要显示引导流程
     */
    suspend fun shouldShowOnboarding(): Boolean {
        val settings = getSettingsOnce()
        return settings?.onboardingCompleted != true
    }

    /**
     * 获取用户目标信息（用于AI助手提示词）
     */
    suspend fun getGoalInfoForAI(): String {
        val settings = getSettingsOnce() ?: return "用户尚未设置目标"
        
        val goalType = settings.goalType?.let { GoalType.fromString(it) }
        val strategy = settings.weightLossStrategy?.let { WeightLossStrategy.fromString(it) }
        
        return buildString {
            appendLine("用户目标信息：")
            goalType?.let { appendLine("- 目标类型：${it.displayName} (${it.description})") }
            settings.targetWeight?.let { appendLine("- 目标体重：${it}kg") }
            settings.userWeight?.let { appendLine("- 当前体重：${it}kg") }
            strategy?.let { 
                appendLine("- 减肥策略：${it.displayName} (${it.description})")
                appendLine("- 每周目标变化：${it.weeklyChange}kg")
            }
            settings.estimatedWeeksToGoal?.let { appendLine("- 预计达成时间：${it}周") }
            settings.dailyCalorieGoal?.let { appendLine("- 每日热量目标：${it}kcal") }
        }
    }

    /**
     * 计算并更新BMR和TDEE
     */
    suspend fun calculateAndUpdateMetabolicRates() {
        val settings = getSettingsOnce() ?: return
        
        val weight = settings.userWeight ?: return
        val height = settings.userHeight ?: return
        val age = settings.userAge ?: return
        val gender = settings.userGender ?: return
        val activityLevel = settings.activityLevel
        
        // 计算BMR（基础代谢率）- 使用Mifflin-St Jeor公式
        val bmr = when (gender.uppercase()) {
            "MALE" -> (10 * weight + 6.25f * height - 5 * age + 5).toInt()
            "FEMALE" -> (10 * weight + 6.25f * height - 5 * age - 161).toInt()
            else -> (10 * weight + 6.25f * height - 5 * age - 78).toInt() // 其他取中间值
        }
        
        // 计算TDEE（每日总能量消耗）
        val activityMultiplier = when (activityLevel) {
            "SEDENTARY" -> 1.2f
            "LIGHT" -> 1.375f
            "MODERATE" -> 1.55f
            "ACTIVE" -> 1.725f
            "VERY_ACTIVE" -> 1.9f
            else -> 1.2f
        }
        val tdee = (bmr * activityMultiplier).toInt()
        
        // 计算BMI
        val heightInMeters = height / 100f
        val bmi = weight / (heightInMeters * heightInMeters)
        
        saveSettings(settings.copy(
            bmr = bmr,
            tdee = tdee,
            bmi = bmi
        ))
    }

    /**
     * 同步设置到加密SharedPreferences，用于启动时快速读取
     */
    private fun syncToEncryptedPreferences(settings: UserSettings) {
        encryptedPrefs.edit()
            .putString("theme_mode", settings.themeMode)
            .putBoolean("show_ai_widget", settings.showAIWidget)
            .putBoolean("onboarding_completed", settings.onboardingCompleted)
            .putInt("onboarding_step", settings.onboardingCurrentStep)
            .apply()
    }
}
