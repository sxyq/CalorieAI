package com.calorieai.app.data.model;

import java.lang.System;

@androidx.room.Entity(tableName = "user_settings")
@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b6\b\u0087\b\u0018\u00002\u00020\u0001B\u00c5\u0001\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0003\u0012\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\n\u0012\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u0006\u0012\b\b\u0002\u0010\r\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0011\u0012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u0011\u0012\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u0006\u0012\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0017J\t\u00100\u001a\u00020\u0003H\u00c6\u0003J\t\u00101\u001a\u00020\u0006H\u00c6\u0003J\t\u00102\u001a\u00020\u0006H\u00c6\u0003J\t\u00103\u001a\u00020\u0011H\u00c6\u0003J\u0010\u00104\u001a\u0004\u0018\u00010\u0011H\u00c6\u0003\u00a2\u0006\u0002\u0010!J\u000b\u00105\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u00106\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u00107\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u00108\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u00109\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010:\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u000b\u0010;\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\u0010\u0010<\u001a\u0004\u0018\u00010\u0003H\u00c6\u0003\u00a2\u0006\u0002\u0010(J\u0010\u0010=\u001a\u0004\u0018\u00010\nH\u00c6\u0003\u00a2\u0006\u0002\u0010,J\u0010\u0010>\u001a\u0004\u0018\u00010\nH\u00c6\u0003\u00a2\u0006\u0002\u0010,J\u000b\u0010?\u001a\u0004\u0018\u00010\u0006H\u00c6\u0003J\t\u0010@\u001a\u00020\u0006H\u00c6\u0003J\u00ce\u0001\u0010A\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\n\b\u0002\u0010\u0005\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00032\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\u000b\u001a\u0004\u0018\u00010\n2\n\b\u0002\u0010\f\u001a\u0004\u0018\u00010\u00062\b\b\u0002\u0010\r\u001a\u00020\u00062\b\b\u0002\u0010\u000e\u001a\u00020\u00062\b\b\u0002\u0010\u000f\u001a\u00020\u00062\b\b\u0002\u0010\u0010\u001a\u00020\u00112\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00112\n\b\u0002\u0010\u0013\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0014\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0015\u001a\u0004\u0018\u00010\u00062\n\b\u0002\u0010\u0016\u001a\u0004\u0018\u00010\u0006H\u00c6\u0001\u00a2\u0006\u0002\u0010BJ\u0013\u0010C\u001a\u00020\u00112\b\u0010D\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010E\u001a\u00020\u0003H\u00d6\u0001J\t\u0010F\u001a\u00020\u0006H\u00d6\u0001R\u0011\u0010\r\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0018\u0010\u0019R\u0013\u0010\u0015\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\u0019R\u0013\u0010\u0016\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u0019R\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\u001dR\u0013\u0010\f\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u0019R\u0011\u0010\u000f\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001f\u0010\u0019R\u0016\u0010\u0002\u001a\u00020\u00038\u0006X\u0087\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001dR\u0015\u0010\u0012\u001a\u0004\u0018\u00010\u0011\u00a2\u0006\n\n\u0002\u0010\"\u001a\u0004\b\u0012\u0010!R\u0011\u0010\u0010\u001a\u00020\u0011\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010#R\u0011\u0010\u000e\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b$\u0010\u0019R\u0013\u0010\u0013\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u0019R\u0013\u0010\u0014\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b&\u0010\u0019R\u0015\u0010\b\u001a\u0004\u0018\u00010\u0003\u00a2\u0006\n\n\u0002\u0010)\u001a\u0004\b\'\u0010(R\u0013\u0010\u0007\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b*\u0010\u0019R\u0015\u0010\t\u001a\u0004\u0018\u00010\n\u00a2\u0006\n\n\u0002\u0010-\u001a\u0004\b+\u0010,R\u0013\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b.\u0010\u0019R\u0015\u0010\u000b\u001a\u0004\u0018\u00010\n\u00a2\u0006\n\n\u0002\u0010-\u001a\u0004\b/\u0010,\u00a8\u0006G"}, d2 = {"Lcom/calorieai/app/data/model/UserSettings;", "", "id", "", "dailyCalorieGoal", "userName", "", "userGender", "userAge", "userHeight", "", "userWeight", "dietaryPreference", "breakfastReminderTime", "lunchReminderTime", "dinnerReminderTime", "isNotificationEnabled", "", "isDarkMode", "seedColor", "selectedAIPresetId", "customAIEndpoint", "customAIModel", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Float;Ljava/lang/Float;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getBreakfastReminderTime", "()Ljava/lang/String;", "getCustomAIEndpoint", "getCustomAIModel", "getDailyCalorieGoal", "()I", "getDietaryPreference", "getDinnerReminderTime", "getId", "()Ljava/lang/Boolean;", "Ljava/lang/Boolean;", "()Z", "getLunchReminderTime", "getSeedColor", "getSelectedAIPresetId", "getUserAge", "()Ljava/lang/Integer;", "Ljava/lang/Integer;", "getUserGender", "getUserHeight", "()Ljava/lang/Float;", "Ljava/lang/Float;", "getUserName", "getUserWeight", "component1", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "copy", "(IILjava/lang/String;Ljava/lang/String;Ljava/lang/Integer;Ljava/lang/Float;Ljava/lang/Float;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;ZLjava/lang/Boolean;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Lcom/calorieai/app/data/model/UserSettings;", "equals", "other", "hashCode", "toString", "app_release"})
public final class UserSettings {
    @androidx.room.PrimaryKey()
    private final int id = 0;
    private final int dailyCalorieGoal = 0;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String userName = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String userGender = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Integer userAge = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float userHeight = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Float userWeight = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String dietaryPreference = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String breakfastReminderTime = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String lunchReminderTime = null;
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String dinnerReminderTime = null;
    private final boolean isNotificationEnabled = false;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.Boolean isDarkMode = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String seedColor = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String selectedAIPresetId = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String customAIEndpoint = null;
    @org.jetbrains.annotations.Nullable()
    private final java.lang.String customAIModel = null;
    
    @org.jetbrains.annotations.NotNull()
    public final com.calorieai.app.data.model.UserSettings copy(int id, int dailyCalorieGoal, @org.jetbrains.annotations.Nullable()
    java.lang.String userName, @org.jetbrains.annotations.Nullable()
    java.lang.String userGender, @org.jetbrains.annotations.Nullable()
    java.lang.Integer userAge, @org.jetbrains.annotations.Nullable()
    java.lang.Float userHeight, @org.jetbrains.annotations.Nullable()
    java.lang.Float userWeight, @org.jetbrains.annotations.Nullable()
    java.lang.String dietaryPreference, @org.jetbrains.annotations.NotNull()
    java.lang.String breakfastReminderTime, @org.jetbrains.annotations.NotNull()
    java.lang.String lunchReminderTime, @org.jetbrains.annotations.NotNull()
    java.lang.String dinnerReminderTime, boolean isNotificationEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isDarkMode, @org.jetbrains.annotations.Nullable()
    java.lang.String seedColor, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedAIPresetId, @org.jetbrains.annotations.Nullable()
    java.lang.String customAIEndpoint, @org.jetbrains.annotations.Nullable()
    java.lang.String customAIModel) {
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
    
    public UserSettings() {
        super();
    }
    
    public UserSettings(int id, int dailyCalorieGoal, @org.jetbrains.annotations.Nullable()
    java.lang.String userName, @org.jetbrains.annotations.Nullable()
    java.lang.String userGender, @org.jetbrains.annotations.Nullable()
    java.lang.Integer userAge, @org.jetbrains.annotations.Nullable()
    java.lang.Float userHeight, @org.jetbrains.annotations.Nullable()
    java.lang.Float userWeight, @org.jetbrains.annotations.Nullable()
    java.lang.String dietaryPreference, @org.jetbrains.annotations.NotNull()
    java.lang.String breakfastReminderTime, @org.jetbrains.annotations.NotNull()
    java.lang.String lunchReminderTime, @org.jetbrains.annotations.NotNull()
    java.lang.String dinnerReminderTime, boolean isNotificationEnabled, @org.jetbrains.annotations.Nullable()
    java.lang.Boolean isDarkMode, @org.jetbrains.annotations.Nullable()
    java.lang.String seedColor, @org.jetbrains.annotations.Nullable()
    java.lang.String selectedAIPresetId, @org.jetbrains.annotations.Nullable()
    java.lang.String customAIEndpoint, @org.jetbrains.annotations.Nullable()
    java.lang.String customAIModel) {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final int getId() {
        return 0;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final int getDailyCalorieGoal() {
        return 0;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUserName() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getUserGender() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer component5() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Integer getUserAge() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component6() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getUserHeight() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float component7() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Float getUserWeight() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component8() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getDietaryPreference() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component9() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getBreakfastReminderTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component10() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getLunchReminderTime() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String component11() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getDinnerReminderTime() {
        return null;
    }
    
    public final boolean component12() {
        return false;
    }
    
    public final boolean isNotificationEnabled() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean component13() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Boolean isDarkMode() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component14() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSeedColor() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component15() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getSelectedAIPresetId() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component16() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCustomAIEndpoint() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String component17() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final java.lang.String getCustomAIModel() {
        return null;
    }
}