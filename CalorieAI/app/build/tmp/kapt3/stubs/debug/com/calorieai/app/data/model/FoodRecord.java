package com.calorieai.app.data.model;

import java.lang.System;

@androidx.room.TypeConverters(value = {com.calorieai.app.data.model.Converters.class})
@androidx.room.Entity(tableName = "food_records")
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b-\b\u0087\b\u0018\u00002\u00020\u0001B\u0099\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\t\u0012\u0006\u0010\u000b\u001a\u00020\t\u0012\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r\u0012\u0006\u0010\u000f\u001a\u00020\u0010\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0016\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0018\u0012\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\u0002\u0010\u001aJ\t\u00101\u001a\u00020\u0003H\u00c6\u0003J\t\u00102\u001a\u00020\u0012H\u00c6\u0003J\u000b\u00103\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\u000b\u00104\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u00105\u001a\u00020\u0016H\u00c6\u0003J\t\u00106\u001a\u00020\u0018H\u00c6\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003J\t\u00108\u001a\u00020\u0003H\u00c6\u0003J\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\t\u0010:\u001a\u00020\u0007H\u00c6\u0003J\t\u0010;\u001a\u00020\tH\u00c6\u0003J\t\u0010<\u001a\u00020\tH\u00c6\u0003J\t\u0010=\u001a\u00020\tH\u00c6\u0003J\u000f\u0010>\u001a\b\u0012\u0004\u0012\u00020\u000e0\rH\u00c6\u0003J\t\u0010?\u001a\u00020\u0010H\u00c6\u0003J\u00ab\u0001\u0010@\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\t2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r2\b\b\u0002\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u00122\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00032\b\b\u0002\u0010\u0015\u001a\u00020\u00162\b\b\u0002\u0010\u0017\u001a\u00020\u00182\n\b\u0002\u0010\u0019\u001a\u0004\u0018\u00010\u0003H\u00c6\u0001J\u0013\u0010A\u001a\u00020\u00162\b\u0010B\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010C\u001a\u00020\u0007H\u00d6\u0001J\t\u0010D\u001a\u00020\u0003H\u00d6\u0001R\u0011\u0010\n\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0011\u0010\u0017\u001a\u00020\u0018\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001d\u0010\u001eR\u0011\u0010\u000b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u001cR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010!R\u0013\u0010\u0014\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010!R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010!R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010!R\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u000e0\r\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010&R\u0011\u0010\u0015\u001a\u00020\u0016\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\'R\u0011\u0010\u000f\u001a\u00020\u0010\u00a2\u0006\b\n\u0000\u001a\u0004\b(\u0010)R\u0013\u0010\u0019\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010!R\u0011\u0010\b\u001a\u00020\t\u00a2\u0006\b\n\u0000\u001a\u0004\b+\u0010\u001cR\u0011\u0010\u0011\u001a\u00020\u0012\u00a2\u0006\b\n\u0000\u001a\u0004\b,\u0010-R\u0011\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010/R\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b0\u0010!\u00a8\u0006E"}, d2 = {"Lcom/calorieai/app/data/model/FoodRecord;", "", "id", "", "foodName", "userInput", "totalCalories", "", "protein", "", "carbs", "fat", "ingredients", "", "Lcom/calorieai/app/data/model/Ingredient;", "mealType", "Lcom/calorieai/app/data/model/MealType;", "recordTime", "", "iconUrl", "iconLocalPath", "isStarred", "", "confidence", "Lcom/calorieai/app/data/model/ConfidenceLevel;", "notes", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IFFFLjava/util/List;Lcom/calorieai/app/data/model/MealType;JLjava/lang/String;Ljava/lang/String;ZLcom/calorieai/app/data/model/ConfidenceLevel;Ljava/lang/String;)V", "getCarbs", "()F", "getConfidence", "()Lcom/calorieai/app/data/model/ConfidenceLevel;", "getFat", "getFoodName", "()Ljava/lang/String;", "getIconLocalPath", "getIconUrl", "getId", "getIngredients", "()Ljava/util/List;", "()Z", "getMealType", "()Lcom/calorieai/app/data/model/MealType;", "getNotes", "getProtein", "getRecordTime", "()J", "getTotalCalories", "()I", "getUserInput", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "equals", "other", "hashCode", "toString", "app_debug"})
public final class FoodRecord {
    @org.jetbrains.annotations.NotNull()
    @androidx.room.PrimaryKey()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String foodName = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String userInput = null;
    private final int totalCalories = 0;
    private final float protein = 0.0F;
    private final float carbs = 0.0F;
    private final float fat = 0.0F;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.calorieai.app.data.model.Ingredient> ingredients = null;
    @org.jetbrains.annotations.NotNull()
    private final com.calorieai.app.data.model.MealType mealType = null;
    private final long recordTime = 0L;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String iconUrl = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String iconLocalPath = null;
    private final boolean isStarred = false;
    @org.jetbrains.annotations.NotNull()
    private final com.calorieai.app.data.model.ConfidenceLevel confidence = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String notes = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.calorieai.app.data.model.FoodRecord copy(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String foodName, @org.jetbrains.annotations.NotNull()
    java.lang.String userInput, int totalCalories, float protein, float carbs, float fat, @org.jetbrains.annotations.NotNull()
    java.util.List<com.calorieai.app.data.model.Ingredient> ingredients, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.MealType mealType, long recordTime, @org.jetbrains.annotations.Nullable()
    java.lang.String iconUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String iconLocalPath, boolean isStarred, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.ConfidenceLevel confidence, @org.jetbrains.annotations.Nullable()
    java.lang.String notes) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @org.jetbrains.annotations.NotNull()
    @java.lang.Override()
    public java.lang.String toString() {
        return null;
    }
    
    public FoodRecord(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.lang.String foodName, @org.jetbrains.annotations.NotNull()
    java.lang.String userInput, int totalCalories, float protein, float carbs, float fat, @org.jetbrains.annotations.NotNull()
    java.util.List<com.calorieai.app.data.model.Ingredient> ingredients, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.MealType mealType, long recordTime, @org.jetbrains.annotations.Nullable()
    java.lang.String iconUrl, @org.jetbrains.annotations.Nullable()
    java.lang.String iconLocalPath, boolean isStarred, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.ConfidenceLevel confidence, @org.jetbrains.annotations.Nullable()
    java.lang.String notes) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component1() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component2() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getFoodName() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getUserInput() {
        return null;
    }
    
    public final int component4() {
        return 0;
    }
    
    public final int getTotalCalories() {
        return 0;
    }
    
    public final float component5() {
        return 0.0F;
    }
    
    public final float getProtein() {
        return 0.0F;
    }
    
    public final float component6() {
        return 0.0F;
    }
    
    public final float getCarbs() {
        return 0.0F;
    }
    
    public final float component7() {
        return 0.0F;
    }
    
    public final float getFat() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.calorieai.app.data.model.Ingredient> component8() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.calorieai.app.data.model.Ingredient> getIngredients() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.calorieai.app.data.model.MealType component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.calorieai.app.data.model.MealType getMealType() {
        return null;
    }
    
    public final long component10() {
        return 0L;
    }
    
    public final long getRecordTime() {
        return 0L;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getIconUrl() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component12() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getIconLocalPath() {
        return null;
    }
    
    public final boolean component13() {
        return false;
    }
    
    public final boolean isStarred() {
        return false;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.calorieai.app.data.model.ConfidenceLevel component14() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.calorieai.app.data.model.ConfidenceLevel getConfidence() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getNotes() {
        return null;
    }
}