package com.calorieai.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.repository.UserSettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class BottomNavBehaviorViewModel @Inject constructor(
    userSettingsRepository: UserSettingsRepository
) : ViewModel() {

    val uiState: StateFlow<BottomNavBehaviorUiState> = userSettingsRepository.getSettings()
        .map { settings ->
            BottomNavBehaviorUiState(
                enableLongPressHomeToAdd = settings?.enableLongPressHomeToAdd ?: true,
                enableLongPressOverviewToStats = settings?.enableLongPressOverviewToStats ?: true,
                enableLongPressMyToProfileEdit = settings?.enableLongPressMyToProfileEdit ?: true
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BottomNavBehaviorUiState()
        )
}

data class BottomNavBehaviorUiState(
    val enableLongPressHomeToAdd: Boolean = true,
    val enableLongPressOverviewToStats: Boolean = true,
    val enableLongPressMyToProfileEdit: Boolean = true
)
