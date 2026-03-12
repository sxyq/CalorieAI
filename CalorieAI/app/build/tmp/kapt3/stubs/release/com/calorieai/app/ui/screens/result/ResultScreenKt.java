package com.calorieai.app.ui.screens.result;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 2, d1 = {"\u00002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\u001a6\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0012\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0003\u0012\u0004\u0012\u00020\u00010\u00052\u0006\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001a.\u0010\t\u001a\u00020\u00012\u0006\u0010\n\u001a\u00020\u000b2\u0012\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0007\u001a\u00020\bH\u0007\u001a(\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u00032\f\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\u00010\u00102\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0007\u00a8\u0006\u0013"}, d2 = {"NutritionInputField", "", "value", "", "onValueChange", "Lkotlin/Function1;", "label", "modifier", "Landroidx/compose/ui/Modifier;", "ResultContent", "record", "Lcom/calorieai/app/data/model/FoodRecord;", "onSave", "ResultScreen", "recordId", "onNavigateBack", "Lkotlin/Function0;", "viewModel", "Lcom/calorieai/app/ui/screens/result/ResultViewModel;", "app_release"})
public final class ResultScreenKt {
    
    @androidx.compose.runtime.Composable()
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    public static final void ResultScreen(@org.jetbrains.annotations.NotNull()
    java.lang.String recordId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onNavigateBack, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.ui.screens.result.ResultViewModel viewModel) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void ResultContent(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.model.FoodRecord record, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.calorieai.app.data.model.FoodRecord, kotlin.Unit> onSave, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    public static final void NutritionInputField(@org.jetbrains.annotations.NotNull()
    java.lang.String value, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onValueChange, @org.jetbrains.annotations.NotNull()
    java.lang.String label, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
}