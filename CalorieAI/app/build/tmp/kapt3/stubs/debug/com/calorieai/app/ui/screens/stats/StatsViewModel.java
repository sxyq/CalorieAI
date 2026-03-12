package com.calorieai.app.ui.screens.stats;

import java.lang.System;

@dagger.hilt.android.lifecycle.HiltViewModel()
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0016\u0010\f\u001a\u00020\u00072\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002J\u001c\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002J\u001c\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002J\u001c\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000e2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u000f0\u000eH\u0002J\b\u0010\u0013\u001a\u00020\u0014H\u0002R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0015"}, d2 = {"Lcom/calorieai/app/ui/screens/stats/StatsViewModel;", "Landroidx/lifecycle/ViewModel;", "foodRecordRepository", "Lcom/calorieai/app/data/repository/FoodRecordRepository;", "(Lcom/calorieai/app/data/repository/FoodRecordRepository;)V", "_uiState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Lcom/calorieai/app/ui/screens/stats/StatsUiState;", "uiState", "Lkotlinx/coroutines/flow/StateFlow;", "getUiState", "()Lkotlinx/coroutines/flow/StateFlow;", "calculateStats", "records", "", "Lcom/calorieai/app/data/model/FoodRecord;", "getMonthRecords", "getTodayRecords", "getWeekRecords", "loadStats", "", "app_debug"})
public final class StatsViewModel extends androidx.lifecycle.ViewModel {
    private final com.calorieai.app.data.repository.FoodRecordRepository foodRecordRepository = null;
    private final kotlinx.coroutines.flow.MutableStateFlow<com.calorieai.app.ui.screens.stats.StatsUiState> _uiState = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.ui.screens.stats.StatsUiState> uiState = null;
    
    @javax.inject.Inject()
    public StatsViewModel(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.repository.FoodRecordRepository foodRecordRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.calorieai.app.ui.screens.stats.StatsUiState> getUiState() {
        return null;
    }
    
    private final void loadStats() {
    }
    
    private final com.calorieai.app.ui.screens.stats.StatsUiState calculateStats(java.util.List<com.calorieai.app.data.model.FoodRecord> records) {
        return null;
    }
    
    private final java.util.List<com.calorieai.app.data.model.FoodRecord> getTodayRecords(java.util.List<com.calorieai.app.data.model.FoodRecord> records) {
        return null;
    }
    
    private final java.util.List<com.calorieai.app.data.model.FoodRecord> getWeekRecords(java.util.List<com.calorieai.app.data.model.FoodRecord> records) {
        return null;
    }
    
    private final java.util.List<com.calorieai.app.data.model.FoodRecord> getMonthRecords(java.util.List<com.calorieai.app.data.model.FoodRecord> records) {
        return null;
    }
}