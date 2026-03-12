package com.calorieai.app.service.backup;

import java.lang.System;

@kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0006\b\u0007\u0018\u00002\u00020\u0001:\u0001\u0012B\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J*\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\rH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0000\u00f8\u0001\u0001\u00f8\u0001\u0002\u00a2\u0006\u0004\b\u000e\u0010\u000fJ*\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000b0\n2\u0006\u0010\f\u001a\u00020\rH\u0086@\u00f8\u0001\u0000\u00f8\u0001\u0000\u00f8\u0001\u0001\u00f8\u0001\u0002\u00a2\u0006\u0004\b\u0011\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000f\n\u0002\b\u0019\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006\u0013"}, d2 = {"Lcom/calorieai/app/service/backup/BackupManager;", "", "context", "Landroid/content/Context;", "foodRecordDao", "Lcom/calorieai/app/data/local/FoodRecordDao;", "(Landroid/content/Context;Lcom/calorieai/app/data/local/FoodRecordDao;)V", "gson", "Lcom/google/gson/Gson;", "exportToJson", "Lkotlin/Result;", "", "uri", "Landroid/net/Uri;", "exportToJson-gIAlu-s", "(Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "importFromJson", "importFromJson-gIAlu-s", "BackupData", "app_release"})
@javax.inject.Singleton()
public final class BackupManager {
    private final android.content.Context context = null;
    private final com.calorieai.app.data.local.FoodRecordDao foodRecordDao = null;
    private final com.google.gson.Gson gson = null;
    
    @javax.inject.Inject()
    public BackupManager(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.calorieai.app.data.local.FoodRecordDao foodRecordDao) {
        super();
    }
    
    @kotlin.Metadata(mv = {1, 6, 0}, k = 1, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\f\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B#\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\f\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0011\u001a\u00020\u0005H\u00c6\u0003J\u000f\u0010\u0012\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0003J-\u0010\u0013\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\u000e\b\u0002\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007H\u00c6\u0001J\u0013\u0010\u0014\u001a\u00020\u00152\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u0018\u001a\u00020\u0019H\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0017\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001a"}, d2 = {"Lcom/calorieai/app/service/backup/BackupManager$BackupData;", "", "version", "", "exportTime", "", "records", "", "Lcom/calorieai/app/data/model/FoodRecord;", "(IJLjava/util/List;)V", "getExportTime", "()J", "getRecords", "()Ljava/util/List;", "getVersion", "()I", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "toString", "", "app_release"})
    public static final class BackupData {
        private final int version = 0;
        private final long exportTime = 0L;
        @org.jetbrains.annotations.NotNull()
        private final java.util.List<com.calorieai.app.data.model.FoodRecord> records = null;
        
        @org.jetbrains.annotations.NotNull()
        public final com.calorieai.app.service.backup.BackupManager.BackupData copy(int version, long exportTime, @org.jetbrains.annotations.NotNull()
        java.util.List<com.calorieai.app.data.model.FoodRecord> records) {
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
        
        public BackupData(int version, long exportTime, @org.jetbrains.annotations.NotNull()
        java.util.List<com.calorieai.app.data.model.FoodRecord> records) {
            super();
        }
        
        public final int component1() {
            return 0;
        }
        
        public final int getVersion() {
            return 0;
        }
        
        public final long component2() {
            return 0L;
        }
        
        public final long getExportTime() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.calorieai.app.data.model.FoodRecord> component3() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.util.List<com.calorieai.app.data.model.FoodRecord> getRecords() {
            return null;
        }
    }
}