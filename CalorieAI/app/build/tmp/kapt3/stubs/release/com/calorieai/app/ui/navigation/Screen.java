package com.calorieai.app.ui.navigation;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u00002\u00020\u0001:\u0006\u0007\b\t\n\u000b\fB\u000f\b\u0004\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006\u0082\u0001\u0006\r\u000e\u000f\u0010\u0011\u0012\u00a8\u0006\u0013"}, d2 = {"Lcom/calorieai/app/ui/navigation/Screen;", "", "route", "", "(Ljava/lang/String;)V", "getRoute", "()Ljava/lang/String;", "AddFood", "Camera", "Home", "Result", "Settings", "Stats", "Lcom/calorieai/app/ui/navigation/Screen$Home;", "Lcom/calorieai/app/ui/navigation/Screen$AddFood;", "Lcom/calorieai/app/ui/navigation/Screen$Camera;", "Lcom/calorieai/app/ui/navigation/Screen$Result;", "Lcom/calorieai/app/ui/navigation/Screen$Stats;", "Lcom/calorieai/app/ui/navigation/Screen$Settings;", "app_release"})
public abstract class Screen {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String route = null;
    
    private Screen(java.lang.String route) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getRoute() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/calorieai/app/ui/navigation/Screen$Home;", "Lcom/calorieai/app/ui/navigation/Screen;", "()V", "app_release"})
    public static final class Home extends com.calorieai.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.calorieai.app.ui.navigation.Screen.Home INSTANCE = null;
        
        private Home() {
            super(null);
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/calorieai/app/ui/navigation/Screen$AddFood;", "Lcom/calorieai/app/ui/navigation/Screen;", "()V", "app_release"})
    public static final class AddFood extends com.calorieai.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.calorieai.app.ui.navigation.Screen.AddFood INSTANCE = null;
        
        private AddFood() {
            super(null);
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/calorieai/app/ui/navigation/Screen$Camera;", "Lcom/calorieai/app/ui/navigation/Screen;", "()V", "app_release"})
    public static final class Camera extends com.calorieai.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.calorieai.app.ui.navigation.Screen.Camera INSTANCE = null;
        
        private Camera() {
            super(null);
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u0004\u00a8\u0006\u0006"}, d2 = {"Lcom/calorieai/app/ui/navigation/Screen$Result;", "Lcom/calorieai/app/ui/navigation/Screen;", "()V", "createRoute", "", "recordId", "app_release"})
    public static final class Result extends com.calorieai.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.calorieai.app.ui.navigation.Screen.Result INSTANCE = null;
        
        private Result() {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String createRoute(@org.jetbrains.annotations.NotNull()
        java.lang.String recordId) {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/calorieai/app/ui/navigation/Screen$Stats;", "Lcom/calorieai/app/ui/navigation/Screen;", "()V", "app_release"})
    public static final class Stats extends com.calorieai.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.calorieai.app.ui.navigation.Screen.Stats INSTANCE = null;
        
        private Stats() {
            super(null);
        }
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002\u00a8\u0006\u0003"}, d2 = {"Lcom/calorieai/app/ui/navigation/Screen$Settings;", "Lcom/calorieai/app/ui/navigation/Screen;", "()V", "app_release"})
    public static final class Settings extends com.calorieai.app.ui.navigation.Screen {
        @org.jetbrains.annotations.NotNull()
        public static final com.calorieai.app.ui.navigation.Screen.Settings INSTANCE = null;
        
        private Settings() {
            super(null);
        }
    }
}