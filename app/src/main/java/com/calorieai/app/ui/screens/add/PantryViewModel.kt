package com.calorieai.app.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.calorieai.app.data.model.PantryIngredient
import com.calorieai.app.domain.recipe.PantryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PantryViewModel @Inject constructor(
    private val pantryUseCase: PantryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PantryUiState())
    val uiState = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RecipeUiEvent>()
    val events: SharedFlow<RecipeUiEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            pantryUseCase.observePantry().collectLatest { items ->
                _uiState.update { it.copy(pantryIngredients = items) }
            }
        }
    }

    fun dispatch(action: RecipeAction.Pantry) {
        when (action) {
            is RecipeAction.Pantry.AddIngredient -> addIngredient(action)
            is RecipeAction.Pantry.DeleteIngredient -> deleteIngredient(action.item)
        }
    }

    private fun addIngredient(action: RecipeAction.Pantry.AddIngredient) {
        viewModelScope.launch {
            pantryUseCase.addIngredient(
                name = action.name,
                quantity = action.quantity,
                unit = action.unit,
                daysToExpire = action.daysToExpire,
                notes = action.notes
            ).onSuccess {
                _events.emit(RecipeUiEvent.Snackbar("已添加食材：${it.name}"))
            }.onFailure {
                _events.emit(RecipeUiEvent.Snackbar(it.message ?: "添加失败"))
            }
        }
    }

    private fun deleteIngredient(item: PantryIngredient) {
        viewModelScope.launch {
            pantryUseCase.removeIngredient(item)
                .onSuccess {
                    _events.emit(RecipeUiEvent.Snackbar("已删除：${item.name}"))
                }
                .onFailure {
                    _events.emit(RecipeUiEvent.Snackbar(it.message ?: "删除失败"))
                }
        }
    }
}

data class PantryUiState(
    val pantryIngredients: List<PantryIngredient> = emptyList()
)
