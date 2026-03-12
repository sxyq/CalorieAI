package com.calorieai.app.ui.screens.home;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 2, d1 = {"\u0000B\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\u001a-\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u0006H\u0007\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0007\u0010\b\u001a\b\u0010\t\u001a\u00020\u0001H\u0007\u001a:\u0010\n\u001a\u00020\u00012\u0006\u0010\u000b\u001a\u00020\f2\f\u0010\r\u001a\b\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00010\u000eH\u0007\u001aP\u0010\u0011\u001a\u00020\u00012\f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00010\u000e2\u0012\u0010\u0015\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00162\b\b\u0002\u0010\u0017\u001a\u00020\u0018H\u0007\u001a\u0018\u0010\u0019\u001a\u00020\u00012\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001bH\u0007\u001a\u000e\u0010\u001d\u001a\u00020\u00032\u0006\u0010\u001e\u001a\u00020\u001f\u0082\u0002\u000b\n\u0002\b\u0019\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006 "}, d2 = {"CalorieInfo", "", "value", "", "label", "color", "Landroidx/compose/ui/graphics/Color;", "CalorieInfo-mxwnekA", "(Ljava/lang/String;Ljava/lang/String;J)V", "EmptyState", "FoodRecordItem", "record", "Lcom/calorieai/app/data/model/FoodRecord;", "onClick", "Lkotlin/Function0;", "onStarClick", "onDeleteClick", "HomeScreen", "onNavigateToAdd", "onNavigateToStats", "onNavigateToSettings", "onNavigateToResult", "Lkotlin/Function1;", "viewModel", "Lcom/calorieai/app/ui/screens/home/HomeViewModel;", "TodayOverviewCard", "totalCalories", "", "dailyGoal", "getMealTypeName", "mealType", "Lcom/calorieai/app/data/model/MealType;", "app_release"})
public final class HomeScreenKt {
    
    @androidx.compose.runtime.Composable()
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    public static final void HomeScreen(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToAdd, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToStats, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateToSettings, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onNavigateToResult, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.ui.screens.home.HomeViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void TodayOverviewCard(int totalCalories, int dailyGoal) {
    }
    
    @androidx.compose.runtime.Composable()
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    public static final void FoodRecordItem(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onStarClick, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onDeleteClick) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void EmptyState() {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String getMealTypeName(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.MealType mealType) {
        return null;
    }
}