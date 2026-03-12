package com.calorieai.app.service.notification;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0004\b\u0007\u0018\u0000 \u00112\u00020\u0001:\u0001\u0011B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\b\u0010\t\u001a\u00020\u0006H\u0002J\b\u0010\n\u001a\u00020\u000bH\u0002J\u0016\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u000eJ\u000e\u0010\u0010\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0012"}, d2 = {"Lcom/calorieai/app/service/notification/NotificationHelper;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "cancelMealReminderNotification", "", "mealType", "Lcom/calorieai/app/service/notification/MealType;", "createNotificationChannels", "isOppoDevice", "", "showGeneralNotification", "title", "", "message", "showMealReminderNotification", "Companion", "app_debug"})
@javax.inject.Singleton()
public final class NotificationHelper {
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.calorieai.app.service.notification.NotificationHelper.Companion Companion = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID_MEAL_REMINDER = "meal_reminder_channel";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_ID_GENERAL = "general_channel";
    public static final int NOTIFICATION_ID_BREAKFAST = 1001;
    public static final int NOTIFICATION_ID_LUNCH = 1002;
    public static final int NOTIFICATION_ID_DINNER = 1003;
    
    @javax.inject.Inject()
    public NotificationHelper(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    private final void createNotificationChannels() {
    }
    
    public final void showMealReminderNotification(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.service.notification.MealType mealType) {
    }
    
    public final void cancelMealReminderNotification(@org.jetbrains.annotations.NotNull()
    com.calorieai.app.service.notification.MealType mealType) {
    }
    
    public final void showGeneralNotification(@org.jetbrains.annotations.NotNull()
    java.lang.String title, @org.jetbrains.annotations.NotNull()
    java.lang.String message) {
    }
    
    private final boolean isOppoDevice() {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0007X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\n"}, d2 = {"Lcom/calorieai/app/service/notification/NotificationHelper$Companion;", "", "()V", "CHANNEL_ID_GENERAL", "", "CHANNEL_ID_MEAL_REMINDER", "NOTIFICATION_ID_BREAKFAST", "", "NOTIFICATION_ID_DINNER", "NOTIFICATION_ID_LUNCH", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}