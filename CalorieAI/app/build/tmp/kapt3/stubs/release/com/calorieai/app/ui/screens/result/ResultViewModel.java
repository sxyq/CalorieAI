package com.calorieai.app.ui.screens.result;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fJ\u000e\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0012R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0013"}, d2 = {"Lcom/calorieai/app/ui/screens/result/ResultViewModel;", "Landroidx/lifecycle/ViewModel;", "foodRecordRepository", "Lcom/calorieai/app/data/repository/FoodRecordRepository;", "(Lcom/calorieai/app/data/repository/FoodRecordRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/calorieai/app/ui/screens/result/ResultUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "loadRecord", "", "recordId", "", "updateRecord", "updatedRecord", "Lcom/calorieai/app/data/model/FoodRecord;", "app_release"})
public final class ResultViewModel extends androidx.lifecycle.ViewModel {
    private final com.calorieai.app.data.repository.FoodRecordRepository foodRecordRepository = null;
    private final kotlinx.coroutines.flow.MutableStateFlow<com.calorieai.app.ui.screens.result.ResultUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.ui.screens.result.ResultUiState> uiState = null;
    
    @javax.inject.Inject()
    public ResultViewModel(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.repository.FoodRecordRepository foodRecordRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.ui.screens.result.ResultUiState> getUiState() {
        return null;
    }
    
    public final void loadRecord(@org.jetbrains.annotations.NotNull()
    java.lang.String recordId) {
    }
    
    public final void updateRecord(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord updatedRecord) {
    }
}