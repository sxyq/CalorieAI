package com.calorieai.app.data.repository;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\n\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tJ\u0019\u0010\u000b\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\rH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eJ\u0012\u0010\u000f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00110\u0010J\u001b\u0010\u0012\u001a\u0004\u0018\u00010\b2\u0006\u0010\f\u001a\u00020\rH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000eJ\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00110\u00102\u0006\u0010\u0014\u001a\u00020\u0015J\u0014\u0010\u0016\u001a\u000e\u0012\u0004\u0012\u00020\u0018\u0012\u0004\u0012\u00020\u00180\u0017H\u0002J\u0012\u0010\u0019\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00110\u0010J\u000e\u0010\u001a\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u001b0\u0010J!\u0010\u001c\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u001d\u001a\u00020\u001eH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001fJ\u0019\u0010 \u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006!"}, d2 = {"Lcom/calorieai/app/data/repository/FoodRecordRepository;", "", "foodRecordDao", "Lcom/calorieai/app/data/local/FoodRecordDao;", "(Lcom/calorieai/app/data/local/FoodRecordDao;)V", "addRecord", "", "record", "Lcom/calorieai/app/data/model/FoodRecord;", "(Lcom/calorieai/app/data/model/FoodRecord;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteRecord", "deleteRecordById", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllRecords", "Lkotlinx/coroutines/flow/Flow;", "", "getRecordById", "getRecordsByMealType", "mealType", "Lcom/calorieai/app/data/model/MealType;", "getTodayRange", "Lkotlin/Pair;", "", "getTodayRecords", "getTodayTotalCalories", "", "toggleStarred", "currentStatus", "", "(Ljava/lang/String;ZLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "updateRecord", "app_debug"})
@javax.inject.Singleton()
public final class FoodRecordRepository {
    private final com.calorieai.app.data.local.FoodRecordDao foodRecordDao = null;
    
    @javax.inject.Inject()
    public FoodRecordRepository(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.local.FoodRecordDao foodRecordDao) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.calorieai.app.data.model.FoodRecord>> getAllRecords() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.calorieai.app.data.model.FoodRecord>> getTodayRecords() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.util.List<com.calorieai.app.data.model.FoodRecord>> getRecordsByMealType(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.MealType mealType) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.Flow<java.lang.Integer> getTodayTotalCalories() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object getRecordById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.calorieai.app.data.model.FoodRecord> continuation) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object addRecord(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object updateRecord(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteRecord(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object deleteRecordById(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Object toggleStarred(@org.jetbrains.annotations.NotNull()
    java.lang.String id, boolean currentStatus, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> continuation) {
        return null;
    }
    
    private final kotlin.Pair<java.lang.Long, java.lang.Long> getTodayRange() {
        return null;
    }
}