package com.calorieai.app.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class FeatureVisibilityViewModel @Inject constructor(
    featureGate: FeatureGate
) : ViewModel() {

    val uiState: StateFlow<FeatureVisibilityUiState> = featureGate.observeWaterFeaturesEnabled()
        .map { settings ->
            FeatureVisibilityUiState(
                showWaterFeatures = settings
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FeatureVisibilityUiState()
        )
}

data class FeatureVisibilityUiState(
    val showWaterFeatures: Boolean = true
)
