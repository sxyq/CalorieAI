package com.calorieai.app.data.model;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0006H\u0007J\u0016\u0010\u0007\u001a\u00020\u00042\f\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\t0\bH\u0007J\u0010\u0010\n\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u000bH\u0007J\u0010\u0010\f\u001a\u00020\u00062\u0006\u0010\u0005\u001a\u00020\u0004H\u0007J\u0016\u0010\r\u001a\b\u0012\u0004\u0012\u00020\t0\b2\u0006\u0010\u0005\u001a\u00020\u0004H\u0007J\u0010\u0010\u000e\u001a\u00020\u000b2\u0006\u0010\u0005\u001a\u00020\u0004H\u0007\u00a8\u0006\u000f"}, d2 = {"Lcom/calorieai/app/data/model/Converters;", "", "()V", "fromConfidenceLevel", "", "value", "Lcom/calorieai/app/data/model/ConfidenceLevel;", "fromIngredientsList", "", "Lcom/calorieai/app/data/model/Ingredient;", "fromMealType", "Lcom/calorieai/app/data/model/MealType;", "toConfidenceLevel", "toIngredientsList", "toMealType", "app_debug"})
public final class Converters {
    
    public Converters() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromIngredientsList(@org.jetbrains.annotations.NotNull()
    java.util.List<com.calorieai.app.data.model.Ingredient> value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.util.List<com.calorieai.app.data.model.Ingredient> toIngredientsList(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromMealType(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.MealType value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final com.calorieai.app.data.model.MealType toMealType(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final java.lang.String fromConfidenceLevel(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.ConfidenceLevel value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    @androidx.room.TypeConverter()
    public final com.calorieai.app.data.model.ConfidenceLevel toConfidenceLevel(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
}