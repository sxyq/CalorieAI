package com.calorieai.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.UserSettings
import com.calorieai.app.data.repository.UserSettingsRepository
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

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            userSettingsRepository.getSettings().collect { settings ->
                settings?.let {
                    _uiState.value = ProfileUiState(
                        avatarUrl = null, // TODO: 添加头像URL字段
                        userName = it.userName ?: "",
                        userId = it.userId ?: "",
                        gender = it.userGender ?: "MALE",
                        age = it.userAge,
                        height = it.userHeight,
                        weight = it.userWeight,
                        activityLevel = "SEDENTARY", // TODO: 添加活动水平字段
                        calorieGoal = it.dailyCalorieGoal
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

    fun saveProfile() {
        viewModelScope.launch {
            val currentState = _uiState.value
            val settings = UserSettings(
                id = 1,
                userName = currentState.userName,
                userId = currentState.userId,
                userGender = currentState.gender,
                userAge = currentState.age,
                userHeight = currentState.height,
                userWeight = currentState.weight,
                dailyCalorieGoal = currentState.calorieGoal
            )
            userSettingsRepository.saveSettings(settings)
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
    val calorieGoal: Int = 2000
)
