package com.calorieai.app.data.local;

import java.lang.System;

@androidx.room.TypeConverters(value = {com.calorieai.app.data.model.Converters.class})
@androidx.room.Database(entities = {com.calorieai.app.data.model.FoodRecord.class, com.calorieai.app.data.model.UserSettings.class}, version = 1, exportSchema = false)
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\'\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0003\u001a\u00020\u0004H&J\b\u0010\u0005\u001a\u00020\u0006H&\u00a8\u0006\u0007"}, d2 = {"Lcom/calorieai/app/data/local/AppDatabase;", "Landroidx/room/RoomDatabase;", "()V", "foodRecordDao", "Lcom/calorieai/app/data/local/FoodRecordDao;", "userSettingsDao", "Lcom/calorieai/app/data/local/UserSettingsDao;", "app_debug"})
public abstract class AppDatabase extends androidx.room.RoomDatabase {
    
    public AppDatabase() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.calorieai.app.data.local.FoodRecordDao foodRecordDao();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.calorieai.app.data.local.UserSettingsDao userSettingsDao();
}