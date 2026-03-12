package com.calorieai.app.data.local;

import java.lang.System;

@androidx.room.Dao()
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000b\n\u0002\b\u0002\bg\u0018\u00002\u00020\u0001J\u0019\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0007\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ\u0014\u0010\u000b\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\fH\'J\u001b\u0010\u000e\u001a\u0004\u0018\u00010\u00052\u0006\u0010\b\u001a\u00020\tH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ$\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\'J,\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\r0\f2\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\'J \u0010\u0016\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00170\f2\u0006\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u0011H\'J\u0019\u0010\u0018\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J\u0019\u0010\u0019\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0006J!\u0010\u001a\u001a\u00020\u00032\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u001b\u001a\u00020\u001cH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001d\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u001e"}, d2 = {"Lcom/calorieai/app/data/local/FoodRecordDao;", "", "deleteRecord", "", "record", "Lcom/calorieai/app/data/model/FoodRecord;", "(Lcom/calorieai/app/data/model/FoodRecord;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteRecordById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllRecords", "Lkotlinx/coroutines/flow/Flow;", "", "getRecordById", "getRecordsBetween", "startTime", "", "endTime", "getRecordsByMealType", "mealType", "Lcom/calorieai/app/data/model/MealType;", "getTotalCaloriesBetween", "", "insertRecord", "updateRecord", "updateStarredStatus", "isStarred", "", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_release"})
public abstract interface FoodRecordDao {
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.Query(value = "SELECT * FROM food_records ORDER BY recordTime DESC")
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.calorieai.app.data.model.FoodRecord>> getAllRecords();
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.Query(value = "SELECT * FROM food_records WHERE recordTime BETWEEN :startTime AND :endTime ORDER BY recordTime DESC")
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.calorieai.app.data.model.FoodRecord>> getRecordsBetween(long startTime, long endTime);
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.Query(value = "SELECT * FROM food_records WHERE mealType = :mealType AND recordTime BETWEEN :startTime AND :endTime ORDER BY recordTime DESC")
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.calorieai.app.data.model.FoodRecord>> getRecordsByMealType(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.MealType mealType, long startTime, long endTime);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Query(value = "SELECT * FROM food_records WHERE id = :id")
    public abstract java.lang.Object getRecordById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.calorieai.app.data.model.FoodRecord> continuation);
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.Query(value = "SELECT SUM(totalCalories) FROM food_records WHERE recordTime BETWEEN :startTime AND :endTime")
    public abstract kotlinx.coroutines.flow.Flow<java.lang.Integer> getTotalCaloriesBetween(long startTime, long endTime);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    public abstract java.lang.Object insertRecord(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Update()
    public abstract java.lang.Object updateRecord(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Delete()
    public abstract java.lang.Object deleteRecord(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Query(value = "DELETE FROM food_records WHERE id = :id")
    public abstract java.lang.Object deleteRecordById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
    
    @org.jetbrains.annotations.Nullable()
    @androidx.room.Query(value = "UPDATE food_records SET isStarred = :isStarred WHERE id = :id")
    public abstract java.lang.Object updateStarredStatus(@org.jetbrains.annotations.NotNull()
    java.lang.String id, boolean isStarred, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation);
}