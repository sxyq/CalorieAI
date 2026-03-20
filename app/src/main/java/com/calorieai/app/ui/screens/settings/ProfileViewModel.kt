package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
import com.calorieai.app.utils.MetabolicConstants
import com.calorieai.app.utils.SecureLogger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 个人信息页面ViewModel
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userSettingsRepository: UserSettingsRepository
) : ViewModel() {
    companion object {
        private const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                settings?.let {
                    SecureLogger.event(
                        TAG,
                        "profile_loaded",
                        "userName" to (it.userName ?: ""),
                        "hasAvatar" to !it.userAvatarUri.isNullOrBlank()
                    )
                    _uiState.value = ProfileUiState(
                        avatarUrl = it.userAvatarUri,
                        userName = it.userName ?: "",
                        userId = it.userId ?: "",
                        gender = it.userGender ?: "MALE",
                        age = it.userAge,
                        height = it.userHeight,
                        weight = it.userWeight,
                        activityLevel = it.activityLevel,
                        calorieGoal = it.dailyCalorieGoal,
                        dailyWaterGoal = it.dailyWaterGoal
                    )
                }
            }
        }
    }

    fun updateUserName(name: String) {
        _uiState.value = _uiState.value.copy(userName = name)
    }

    fun updateUserId(id: String) {
        _uiState.value = _uiState.value.copy(userId = id)
    }

    fun updateAvatarUrl(url: String?) {
        SecureLogger.event(
            TAG,
            "avatar_updated",
            "hasAvatar" to !url.isNullOrBlank(),
            "avatarLength" to (url?.length ?: 0)
        )
        _uiState.value = _uiState.value.copy(avatarUrl = url)
    }

    fun updateGender(gender: String) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateAge(age: Int?) {
        _uiState.value = _uiState.value.copy(age = age)
    }

    fun updateHeight(height: Float?) {
        _uiState.value = _uiState.value.copy(height = height)
    }

    fun updateWeight(weight: Float?) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun updateActivityLevel(level: String) {
        _uiState.value = _uiState.value.copy(activityLevel = level)
    }

    fun updateCalorieGoal(goal: Int) {
        _uiState.value = _uiState.value.copy(calorieGoal = goal)
    }

    fun updateDailyWaterGoal(goal: Int) {
        _uiState.value = _uiState.value.copy(dailyWaterGoal = goal.coerceIn(1200, 5000))
    }

    fun saveProfile() {
        viewModelScope.launch {
            val currentState = _uiState.value
            SecureLogger.event(
                TAG,
                "save_profile_start",
                "userName" to currentState.userName,
                "hasAvatar" to !currentState.avatarUrl.isNullOrBlank()
            )
            val existing = userSettingsRepository.getSettingsOnce()
            val bmr = MetabolicConstants.calculateBMR(
                gender = currentState.gender,
                weight = currentState.weight,
                height = currentState.height,
                age = currentState.age
            )
            val tdee = MetabolicConstants.calculateTDEE(
                bmr = bmr,
                activityLevel = currentState.activityLevel
            )
            val bmi = currentState.height
                ?.takeIf { it > 0f }
                ?.let { heightCm ->
                    currentState.weight?.let { weightKg ->
                        val heightM = heightCm / 100f
                        if (heightM > 0f) weightKg / (heightM * heightM) else null
                    }
                }
            val suggestedWaterGoal = MetabolicConstants.calculateDailyWaterGoal(
                weight = currentState.weight,
                activityLevel = currentState.activityLevel,
                age = currentState.age,
                gender = currentState.gender
            )
            val dailyWaterGoal = currentState.dailyWaterGoal.takeIf { it > 0 } ?: suggestedWaterGoal
            val settings = (existing ?: UserSettings()).copy(
                id = existing?.id ?: 1,
                userAvatarUri = currentState.avatarUrl,
                userName = currentState.userName,
                userId = currentState.userId,
                userGender = currentState.gender,
                userAge = currentState.age,
                userHeight = currentState.height,
                userWeight = currentState.weight,
                activityLevel = currentState.activityLevel,
                dailyCalorieGoal = currentState.calorieGoal,
                dailyWaterGoal = dailyWaterGoal,
                bmr = bmr,
                tdee = tdee,
                bmi = bmi
            )
            userSettingsRepository.saveSettings(settings)
            SecureLogger.event(
                TAG,
                "save_profile_done",
                "calorieGoal" to settings.dailyCalorieGoal,
                "dailyWaterGoal" to settings.dailyWaterGoal,
                "bmr" to settings.bmr,
                "tdee" to settings.tdee
            )
        }
    }
}

/**
 * 个人信息UI状态
 */
data class ProfileUiState(
    val avatarUrl: String? = null,
    val userName: String = "",
    val userId: String = "",
    val gender: String = "MALE",
    val age: Int? = null,
    val height: Float? = null,
    val weight: Float? = null,
    val activityLevel: String = "SEDENTARY",
    val calorieGoal: Int = 2000,
    val dailyWaterGoal: Int = 2000
)
