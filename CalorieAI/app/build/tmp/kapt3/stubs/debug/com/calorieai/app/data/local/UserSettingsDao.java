package com.calorieai.app.data.local;

import java.lang.System;

@androidx.room.Dao()
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000.\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\bg\u0018\u00002\u00020\u0001J\u0010\u0010\u0002\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00040\u0003H\'J\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0004H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ\u0019\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eJ\u0019\u0010\u000f\u001a\u00020\b2\u0006\u0010\u0010\u001a\u00020\u0011H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0012J\u0019\u0010\u0013\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u0004H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\n\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u0014"}, d2 = {"Lcom/calorieai/app/data/local/UserSettingsDao;", "", "getSettings", "Lkotlinx/coroutines/flow/Flow;", "Lcom/calorieai/app/data/model/UserSettings;", "getSettingsSync", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "insertSettings", "", "settings", "(Lcom/calorieai/app/data/model/UserSettings;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateDailyGoal", "goal", "", "(ILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateNotificationEnabled", "enabled", "", "(ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateSettings", "app_debug"})
public abstract interface UserSettingsDao {
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.Query(value = "SELECT * FROM user_settings WHERE id = 1")
    public abstract kotlinx.coroutines.flow.Flow<com.calorieai.app.data.model.UserSettings> getSettings();
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Query(value = "SELECT * FROM user_settings WHERE id = 1")
    public abstract java.lang.Object getSettingsSync(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.calorieai.app.data.model.UserSettings> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    public abstract java.lang.Object insertSettings(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.UserSettings settings, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Update()
    public abstract java.lang.Object updateSettings(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.UserSettings settings, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Query(value = "UPDATE user_settings SET dailyCalorieGoal = :goal WHERE id = 1")
    public abstract java.lang.Object updateDailyGoal(int goal, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Query(value = "UPDATE user_settings SET isNotificationEnabled = :enabled WHERE id = 1")
    public abstract java.lang.Object updateNotificationEnabled(boolean enabled, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
}