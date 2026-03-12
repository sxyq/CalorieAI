package com.calorieai.app.service.notification;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u0000 \n2\u00020\u0001:\u0001\nB\u0015\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\u0011\u0010\u0007\u001a\u00020\bH\u0096@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\t\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\u000b"}, d2 = {"Lcom/calorieai/app/service/notification/MealReminderWorker;", "Landroidx/work/CoroutineWorker;", "context", "Landroid/content/Context;", "params", "Landroidx/work/WorkerParameters;", "(Landroid/content/Context;Landroidx/work/WorkerParameters;)V", "doWork", "Landroidx/work/ListenableWorker$Result;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_release"})
public final class MealReminderWorker extends androidx.work.CoroutineWorker {
    @org.jetbrains.annotations.NotNull()
    public static final com.calorieai.app.service.notification.MealReminderWorker.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String KEY_MEAL_TYPE = "meal_type";
    private static final java.lang.String WORK_TAG_BREAKFAST = "breakfast_reminder";
    private static final java.lang.String WORK_TAG_LUNCH = "lunch_reminder";
    private static final java.lang.String WORK_TAG_DINNER = "dinner_reminder";
    
    public MealReminderWorker(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    androidx.work.WorkerParameters params) {
        super(null, null);
    }
    
    @org.jetbrains.annotations.Nullable()
    @java.lang.Override()
    public java.lang.Object doWork(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super androidx.work.ListenableWorker.Result> continuation) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000bJ\u001c\u0010\f\u001a\u000e\u0012\u0004\u0012\u00020\u000e\u0012\u0004\u0012\u00020\u000e0\r2\u0006\u0010\u000f\u001a\u00020\u0004H\u0002J&\u0010\u0010\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u00042\u0006\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\u0004J(\u0010\u0014\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u0004H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/calorieai/app/service/notification/MealReminderWorker$Companion;", "", "()V", "KEY_MEAL_TYPE", "", "WORK_TAG_BREAKFAST", "WORK_TAG_DINNER", "WORK_TAG_LUNCH", "cancelAllReminders", "", "context", "Landroid/content/Context;", "parseTime", "Lkotlin/Pair;", "", "time", "scheduleMealReminders", "breakfastTime", "lunchTime", "dinnerTime", "scheduleReminder", "mealType", "Lcom/calorieai/app/service/notification/MealType;", "tag", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        public final void scheduleMealReminders(@org.jetbrains.annotations.NotNull()
        android.content.Context context, @org.jetbrains.annotations.NotNull()
        java.lang.String breakfastTime, @org.jetbrains.annotations.NotNull()
        java.lang.String lunchTime, @org.jetbrains.annotations.NotNull()
        java.lang.String dinnerTime) {
        }
        
        public final void cancelAllReminders(@org.jetbrains.annotations.NotNull()
        android.content.Context context) {
        }
        
        private final void scheduleReminder(android.content.Context context, com.calorieai.app.service.notification.MealType mealType, java.lang.String time, java.lang.String tag) {
        }
        
        private final kotlin.Pair<java.lang.Integer, java.lang.Integer> parseTime(java.lang.String time) {
            return null;
        }
    }
}