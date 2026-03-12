package com.calorieai.app.ui.screens.add;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\rH\u0002J\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u000e\u001a\u00020\rJ\u000e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u0013J\u001a\u0010\u0014\u001a\u00020\u00102\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\r\u0012\u0004\u0012\u00020\u00100\u0016R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0017"}, d2 = {"Lcom/calorieai/app/ui/screens/add/AddFoodViewModel;", "Landroidx/lifecycle/ViewModel;", "foodRecordRepository", "Lcom/calorieai/app/data/repository/FoodRecordRepository;", "(Lcom/calorieai/app/data/repository/FoodRecordRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/calorieai/app/ui/screens/add/AddFoodUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "extractFoodName", "", "description", "onFoodDescriptionChange", "", "onMealTypeChange", "mealType", "Lcom/calorieai/app/data/model/MealType;", "saveFoodRecord", "onSuccess", "Lkotlin/Function1;", "app_release"})
public final class AddFoodViewModel extends androidx.lifecycle.ViewModel {
    private final com.calorieai.app.data.repository.FoodRecordRepository foodRecordRepository = null;
    private final kotlinx.coroutines.flow.MutableStateFlow<com.calorieai.app.ui.screens.add.AddFoodUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.ui.screens.add.AddFoodUiState> uiState = null;
    
    @javax.inject.Inject()
    public AddFoodViewModel(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.repository.FoodRecordRepository foodRecordRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.ui.screens.add.AddFoodUiState> getUiState() {
        return null;
    }
    
    public final void onFoodDescriptionChange(@org.jetbrains.annotations.NotNull()
    java.lang.String description) {
    }
    
    public final void onMealTypeChange(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.MealType mealType) {
    }
    
    public final void saveFoodRecord(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onSuccess) {
    }
    
    private final java.lang.String extractFoodName(java.lang.String description) {
        return null;
    }
}