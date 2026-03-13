package com.calorieai.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.calorieai.app.data.model.UserSettings;
import java.lang.Boolean;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class UserSettingsDao_Impl implements UserSettingsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserSettings> __insertionAdapterOfUserSettings;

  private final EntityDeletionOrUpdateAdapter<UserSettings> __updateAdapterOfUserSettings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDailyGoal;

  private final SharedSQLiteStatement __preparedStmtOfUpdateNotificationEnabled;

  public UserSettingsDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserSettings = new EntityInsertionAdapter<UserSettings>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `user_settings` (`id`,`dailyCalorieGoal`,`userName`,`userId`,`userGender`,`userAge`,`userHeight`,`userWeight`,`activityLevel`,`dietaryPreference`,`breakfastReminderTime`,`lunchReminderTime`,`dinnerReminderTime`,`isNotificationEnabled`,`isDarkMode`,`seedColor`,`selectedAIPresetId`,`customAIEndpoint`,`customAIModel`,`themeMode`,`useDeadlinerStyle`,`hideDividers`,`fontSize`,`enableAnimations`,`feedbackType`,`enableVibration`,`enableSound`,`backgroundBehavior`,`startupPage`,`enableQuickAdd`,`enableGoalReminder`,`enableStreakReminder`,`enableAutoBackup`,`lastBackupTime`,`enableCloudSync`,`showAIWidget`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserSettings entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDailyCalorieGoal());
        if (entity.getUserName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getUserName());
        }
        if (entity.getUserId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getUserId());
        }
        if (entity.getUserGender() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUserGender());
        }
        if (entity.getUserAge() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getUserAge());
        }
        if (entity.getUserHeight() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getUserHeight());
        }
        if (entity.getUserWeight() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getUserWeight());
        }
        statement.bindString(9, entity.getActivityLevel());
        if (entity.getDietaryPreference() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getDietaryPreference());
        }
        statement.bindString(11, entity.getBreakfastReminderTime());
        statement.bindString(12, entity.getLunchReminderTime());
        statement.bindString(13, entity.getDinnerReminderTime());
        final int _tmp = entity.isNotificationEnabled() ? 1 : 0;
        statement.bindLong(14, _tmp);
        final Integer _tmp_1 = entity.isDarkMode() == null ? null : (entity.isDarkMode() ? 1 : 0);
        if (_tmp_1 == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, _tmp_1);
        }
        if (entity.getSeedColor() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getSeedColor());
        }
        if (entity.getSelectedAIPresetId() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getSelectedAIPresetId());
        }
        if (entity.getCustomAIEndpoint() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getCustomAIEndpoint());
        }
        if (entity.getCustomAIModel() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCustomAIModel());
        }
        statement.bindString(20, entity.getThemeMode());
        final int _tmp_2 = entity.getUseDeadlinerStyle() ? 1 : 0;
        statement.bindLong(21, _tmp_2);
        final int _tmp_3 = entity.getHideDividers() ? 1 : 0;
        statement.bindLong(22, _tmp_3);
        statement.bindString(23, entity.getFontSize());
        final int _tmp_4 = entity.getEnableAnimations() ? 1 : 0;
        statement.bindLong(24, _tmp_4);
        statement.bindString(25, entity.getFeedbackType());
        final int _tmp_5 = entity.getEnableVibration() ? 1 : 0;
        statement.bindLong(26, _tmp_5);
        final int _tmp_6 = entity.getEnableSound() ? 1 : 0;
        statement.bindLong(27, _tmp_6);
        statement.bindString(28, entity.getBackgroundBehavior());
        statement.bindString(29, entity.getStartupPage());
        final int _tmp_7 = entity.getEnableQuickAdd() ? 1 : 0;
        statement.bindLong(30, _tmp_7);
        final int _tmp_8 = entity.getEnableGoalReminder() ? 1 : 0;
        statement.bindLong(31, _tmp_8);
        final int _tmp_9 = entity.getEnableStreakReminder() ? 1 : 0;
        statement.bindLong(32, _tmp_9);
        final int _tmp_10 = entity.getEnableAutoBackup() ? 1 : 0;
        statement.bindLong(33, _tmp_10);
        if (entity.getLastBackupTime() == null) {
          statement.bindNull(34);
        } else {
          statement.bindString(34, entity.getLastBackupTime());
        }
        final int _tmp_11 = entity.getEnableCloudSync() ? 1 : 0;
        statement.bindLong(35, _tmp_11);
        final int _tmp_12 = entity.getShowAIWidget() ? 1 : 0;
        statement.bindLong(36, _tmp_12);
      }
    };
    this.__updateAdapterOfUserSettings = new EntityDeletionOrUpdateAdapter<UserSettings>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `user_settings` SET `id` = ?,`dailyCalorieGoal` = ?,`userName` = ?,`userId` = ?,`userGender` = ?,`userAge` = ?,`userHeight` = ?,`userWeight` = ?,`activityLevel` = ?,`dietaryPreference` = ?,`breakfastReminderTime` = ?,`lunchReminderTime` = ?,`dinnerReminderTime` = ?,`isNotificationEnabled` = ?,`isDarkMode` = ?,`seedColor` = ?,`selectedAIPresetId` = ?,`customAIEndpoint` = ?,`customAIModel` = ?,`themeMode` = ?,`useDeadlinerStyle` = ?,`hideDividers` = ?,`fontSize` = ?,`enableAnimations` = ?,`feedbackType` = ?,`enableVibration` = ?,`enableSound` = ?,`backgroundBehavior` = ?,`startupPage` = ?,`enableQuickAdd` = ?,`enableGoalReminder` = ?,`enableStreakReminder` = ?,`enableAutoBackup` = ?,`lastBackupTime` = ?,`enableCloudSync` = ?,`showAIWidget` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final UserSettings entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDailyCalorieGoal());
        if (entity.getUserName() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getUserName());
        }
        if (entity.getUserId() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getUserId());
        }
        if (entity.getUserGender() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getUserGender());
        }
        if (entity.getUserAge() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getUserAge());
        }
        if (entity.getUserHeight() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getUserHeight());
        }
        if (entity.getUserWeight() == null) {
          statement.bindNull(8);
        } else {
          statement.bindDouble(8, entity.getUserWeight());
        }
        statement.bindString(9, entity.getActivityLevel());
        if (entity.getDietaryPreference() == null) {
          statement.bindNull(10);
        } else {
          statement.bindString(10, entity.getDietaryPreference());
        }
        statement.bindString(11, entity.getBreakfastReminderTime());
        statement.bindString(12, entity.getLunchReminderTime());
        statement.bindString(13, entity.getDinnerReminderTime());
        final int _tmp = entity.isNotificationEnabled() ? 1 : 0;
        statement.bindLong(14, _tmp);
        final Integer _tmp_1 = entity.isDarkMode() == null ? null : (entity.isDarkMode() ? 1 : 0);
        if (_tmp_1 == null) {
          statement.bindNull(15);
        } else {
          statement.bindLong(15, _tmp_1);
        }
        if (entity.getSeedColor() == null) {
          statement.bindNull(16);
        } else {
          statement.bindString(16, entity.getSeedColor());
        }
        if (entity.getSelectedAIPresetId() == null) {
          statement.bindNull(17);
        } else {
          statement.bindString(17, entity.getSelectedAIPresetId());
        }
        if (entity.getCustomAIEndpoint() == null) {
          statement.bindNull(18);
        } else {
          statement.bindString(18, entity.getCustomAIEndpoint());
        }
        if (entity.getCustomAIModel() == null) {
          statement.bindNull(19);
        } else {
          statement.bindString(19, entity.getCustomAIModel());
        }
        statement.bindString(20, entity.getThemeMode());
        final int _tmp_2 = entity.getUseDeadlinerStyle() ? 1 : 0;
        statement.bindLong(21, _tmp_2);
        final int _tmp_3 = entity.getHideDividers() ? 1 : 0;
        statement.bindLong(22, _tmp_3);
        statement.bindString(23, entity.getFontSize());
        final int _tmp_4 = entity.getEnableAnimations() ? 1 : 0;
        statement.bindLong(24, _tmp_4);
        statement.bindString(25, entity.getFeedbackType());
        final int _tmp_5 = entity.getEnableVibration() ? 1 : 0;
        statement.bindLong(26, _tmp_5);
        final int _tmp_6 = entity.getEnableSound() ? 1 : 0;
        statement.bindLong(27, _tmp_6);
        statement.bindString(28, entity.getBackgroundBehavior());
        statement.bindString(29, entity.getStartupPage());
        final int _tmp_7 = entity.getEnableQuickAdd() ? 1 : 0;
        statement.bindLong(30, _tmp_7);
        final int _tmp_8 = entity.getEnableGoalReminder() ? 1 : 0;
        statement.bindLong(31, _tmp_8);
        final int _tmp_9 = entity.getEnableStreakReminder() ? 1 : 0;
        statement.bindLong(32, _tmp_9);
        final int _tmp_10 = entity.getEnableAutoBackup() ? 1 : 0;
        statement.bindLong(33, _tmp_10);
        if (entity.getLastBackupTime() == null) {
          statement.bindNull(34);
        } else {
          statement.bindString(34, entity.getLastBackupTime());
        }
        final int _tmp_11 = entity.getEnableCloudSync() ? 1 : 0;
        statement.bindLong(35, _tmp_11);
        final int _tmp_12 = entity.getShowAIWidget() ? 1 : 0;
        statement.bindLong(36, _tmp_12);
        statement.bindLong(37, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateDailyGoal = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_settings SET dailyCalorieGoal = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateNotificationEnabled = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE user_settings SET isNotificationEnabled = ? WHERE id = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insertSettings(final UserSettings settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserSettings.insert(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object insertOrUpdate(final UserSettings settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfUserSettings.insert(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSettings(final UserSettings settings,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfUserSettings.handle(settings);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateDailyGoal(final int goal, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDailyGoal.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, goal);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateDailyGoal.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateNotificationEnabled(final boolean enabled,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateNotificationEnabled.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateNotificationEnabled.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<UserSettings> getSettings() {
    final String _sql = "SELECT * FROM user_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"user_settings"}, new Callable<UserSettings>() {
      @Override
      @Nullable
      public UserSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDailyCalorieGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyCalorieGoal");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "userName");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserGender = CursorUtil.getColumnIndexOrThrow(_cursor, "userGender");
          final int _cursorIndexOfUserAge = CursorUtil.getColumnIndexOrThrow(_cursor, "userAge");
          final int _cursorIndexOfUserHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userHeight");
          final int _cursorIndexOfUserWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userWeight");
          final int _cursorIndexOfActivityLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "activityLevel");
          final int _cursorIndexOfDietaryPreference = CursorUtil.getColumnIndexOrThrow(_cursor, "dietaryPreference");
          final int _cursorIndexOfBreakfastReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastReminderTime");
          final int _cursorIndexOfLunchReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchReminderTime");
          final int _cursorIndexOfDinnerReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerReminderTime");
          final int _cursorIndexOfIsNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotificationEnabled");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfSeedColor = CursorUtil.getColumnIndexOrThrow(_cursor, "seedColor");
          final int _cursorIndexOfSelectedAIPresetId = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAIPresetId");
          final int _cursorIndexOfCustomAIEndpoint = CursorUtil.getColumnIndexOrThrow(_cursor, "customAIEndpoint");
          final int _cursorIndexOfCustomAIModel = CursorUtil.getColumnIndexOrThrow(_cursor, "customAIModel");
          final int _cursorIndexOfThemeMode = CursorUtil.getColumnIndexOrThrow(_cursor, "themeMode");
          final int _cursorIndexOfUseDeadlinerStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "useDeadlinerStyle");
          final int _cursorIndexOfHideDividers = CursorUtil.getColumnIndexOrThrow(_cursor, "hideDividers");
          final int _cursorIndexOfFontSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fontSize");
          final int _cursorIndexOfEnableAnimations = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAnimations");
          final int _cursorIndexOfFeedbackType = CursorUtil.getColumnIndexOrThrow(_cursor, "feedbackType");
          final int _cursorIndexOfEnableVibration = CursorUtil.getColumnIndexOrThrow(_cursor, "enableVibration");
          final int _cursorIndexOfEnableSound = CursorUtil.getColumnIndexOrThrow(_cursor, "enableSound");
          final int _cursorIndexOfBackgroundBehavior = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundBehavior");
          final int _cursorIndexOfStartupPage = CursorUtil.getColumnIndexOrThrow(_cursor, "startupPage");
          final int _cursorIndexOfEnableQuickAdd = CursorUtil.getColumnIndexOrThrow(_cursor, "enableQuickAdd");
          final int _cursorIndexOfEnableGoalReminder = CursorUtil.getColumnIndexOrThrow(_cursor, "enableGoalReminder");
          final int _cursorIndexOfEnableStreakReminder = CursorUtil.getColumnIndexOrThrow(_cursor, "enableStreakReminder");
          final int _cursorIndexOfEnableAutoBackup = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAutoBackup");
          final int _cursorIndexOfLastBackupTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastBackupTime");
          final int _cursorIndexOfEnableCloudSync = CursorUtil.getColumnIndexOrThrow(_cursor, "enableCloudSync");
          final int _cursorIndexOfShowAIWidget = CursorUtil.getColumnIndexOrThrow(_cursor, "showAIWidget");
          final UserSettings _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDailyCalorieGoal;
            _tmpDailyCalorieGoal = _cursor.getInt(_cursorIndexOfDailyCalorieGoal);
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUserGender;
            if (_cursor.isNull(_cursorIndexOfUserGender)) {
              _tmpUserGender = null;
            } else {
              _tmpUserGender = _cursor.getString(_cursorIndexOfUserGender);
            }
            final Integer _tmpUserAge;
            if (_cursor.isNull(_cursorIndexOfUserAge)) {
              _tmpUserAge = null;
            } else {
              _tmpUserAge = _cursor.getInt(_cursorIndexOfUserAge);
            }
            final Float _tmpUserHeight;
            if (_cursor.isNull(_cursorIndexOfUserHeight)) {
              _tmpUserHeight = null;
            } else {
              _tmpUserHeight = _cursor.getFloat(_cursorIndexOfUserHeight);
            }
            final Float _tmpUserWeight;
            if (_cursor.isNull(_cursorIndexOfUserWeight)) {
              _tmpUserWeight = null;
            } else {
              _tmpUserWeight = _cursor.getFloat(_cursorIndexOfUserWeight);
            }
            final String _tmpActivityLevel;
            _tmpActivityLevel = _cursor.getString(_cursorIndexOfActivityLevel);
            final String _tmpDietaryPreference;
            if (_cursor.isNull(_cursorIndexOfDietaryPreference)) {
              _tmpDietaryPreference = null;
            } else {
              _tmpDietaryPreference = _cursor.getString(_cursorIndexOfDietaryPreference);
            }
            final String _tmpBreakfastReminderTime;
            _tmpBreakfastReminderTime = _cursor.getString(_cursorIndexOfBreakfastReminderTime);
            final String _tmpLunchReminderTime;
            _tmpLunchReminderTime = _cursor.getString(_cursorIndexOfLunchReminderTime);
            final String _tmpDinnerReminderTime;
            _tmpDinnerReminderTime = _cursor.getString(_cursorIndexOfDinnerReminderTime);
            final boolean _tmpIsNotificationEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNotificationEnabled);
            _tmpIsNotificationEnabled = _tmp != 0;
            final Boolean _tmpIsDarkMode;
            final Integer _tmp_1;
            if (_cursor.isNull(_cursorIndexOfIsDarkMode)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsDarkMode);
            }
            _tmpIsDarkMode = _tmp_1 == null ? null : _tmp_1 != 0;
            final String _tmpSeedColor;
            if (_cursor.isNull(_cursorIndexOfSeedColor)) {
              _tmpSeedColor = null;
            } else {
              _tmpSeedColor = _cursor.getString(_cursorIndexOfSeedColor);
            }
            final String _tmpSelectedAIPresetId;
            if (_cursor.isNull(_cursorIndexOfSelectedAIPresetId)) {
              _tmpSelectedAIPresetId = null;
            } else {
              _tmpSelectedAIPresetId = _cursor.getString(_cursorIndexOfSelectedAIPresetId);
            }
            final String _tmpCustomAIEndpoint;
            if (_cursor.isNull(_cursorIndexOfCustomAIEndpoint)) {
              _tmpCustomAIEndpoint = null;
            } else {
              _tmpCustomAIEndpoint = _cursor.getString(_cursorIndexOfCustomAIEndpoint);
            }
            final String _tmpCustomAIModel;
            if (_cursor.isNull(_cursorIndexOfCustomAIModel)) {
              _tmpCustomAIModel = null;
            } else {
              _tmpCustomAIModel = _cursor.getString(_cursorIndexOfCustomAIModel);
            }
            final String _tmpThemeMode;
            _tmpThemeMode = _cursor.getString(_cursorIndexOfThemeMode);
            final boolean _tmpUseDeadlinerStyle;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfUseDeadlinerStyle);
            _tmpUseDeadlinerStyle = _tmp_2 != 0;
            final boolean _tmpHideDividers;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfHideDividers);
            _tmpHideDividers = _tmp_3 != 0;
            final String _tmpFontSize;
            _tmpFontSize = _cursor.getString(_cursorIndexOfFontSize);
            final boolean _tmpEnableAnimations;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfEnableAnimations);
            _tmpEnableAnimations = _tmp_4 != 0;
            final String _tmpFeedbackType;
            _tmpFeedbackType = _cursor.getString(_cursorIndexOfFeedbackType);
            final boolean _tmpEnableVibration;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfEnableVibration);
            _tmpEnableVibration = _tmp_5 != 0;
            final boolean _tmpEnableSound;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfEnableSound);
            _tmpEnableSound = _tmp_6 != 0;
            final String _tmpBackgroundBehavior;
            _tmpBackgroundBehavior = _cursor.getString(_cursorIndexOfBackgroundBehavior);
            final String _tmpStartupPage;
            _tmpStartupPage = _cursor.getString(_cursorIndexOfStartupPage);
            final boolean _tmpEnableQuickAdd;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfEnableQuickAdd);
            _tmpEnableQuickAdd = _tmp_7 != 0;
            final boolean _tmpEnableGoalReminder;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfEnableGoalReminder);
            _tmpEnableGoalReminder = _tmp_8 != 0;
            final boolean _tmpEnableStreakReminder;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfEnableStreakReminder);
            _tmpEnableStreakReminder = _tmp_9 != 0;
            final boolean _tmpEnableAutoBackup;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfEnableAutoBackup);
            _tmpEnableAutoBackup = _tmp_10 != 0;
            final String _tmpLastBackupTime;
            if (_cursor.isNull(_cursorIndexOfLastBackupTime)) {
              _tmpLastBackupTime = null;
            } else {
              _tmpLastBackupTime = _cursor.getString(_cursorIndexOfLastBackupTime);
            }
            final boolean _tmpEnableCloudSync;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfEnableCloudSync);
            _tmpEnableCloudSync = _tmp_11 != 0;
            final boolean _tmpShowAIWidget;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfShowAIWidget);
            _tmpShowAIWidget = _tmp_12 != 0;
            _result = new UserSettings(_tmpId,_tmpDailyCalorieGoal,_tmpUserName,_tmpUserId,_tmpUserGender,_tmpUserAge,_tmpUserHeight,_tmpUserWeight,_tmpActivityLevel,_tmpDietaryPreference,_tmpBreakfastReminderTime,_tmpLunchReminderTime,_tmpDinnerReminderTime,_tmpIsNotificationEnabled,_tmpIsDarkMode,_tmpSeedColor,_tmpSelectedAIPresetId,_tmpCustomAIEndpoint,_tmpCustomAIModel,_tmpThemeMode,_tmpUseDeadlinerStyle,_tmpHideDividers,_tmpFontSize,_tmpEnableAnimations,_tmpFeedbackType,_tmpEnableVibration,_tmpEnableSound,_tmpBackgroundBehavior,_tmpStartupPage,_tmpEnableQuickAdd,_tmpEnableGoalReminder,_tmpEnableStreakReminder,_tmpEnableAutoBackup,_tmpLastBackupTime,_tmpEnableCloudSync,_tmpShowAIWidget);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getSettingsSync(final Continuation<? super UserSettings> $completion) {
    final String _sql = "SELECT * FROM user_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserSettings>() {
      @Override
      @Nullable
      public UserSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDailyCalorieGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyCalorieGoal");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "userName");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserGender = CursorUtil.getColumnIndexOrThrow(_cursor, "userGender");
          final int _cursorIndexOfUserAge = CursorUtil.getColumnIndexOrThrow(_cursor, "userAge");
          final int _cursorIndexOfUserHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userHeight");
          final int _cursorIndexOfUserWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userWeight");
          final int _cursorIndexOfActivityLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "activityLevel");
          final int _cursorIndexOfDietaryPreference = CursorUtil.getColumnIndexOrThrow(_cursor, "dietaryPreference");
          final int _cursorIndexOfBreakfastReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastReminderTime");
          final int _cursorIndexOfLunchReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchReminderTime");
          final int _cursorIndexOfDinnerReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerReminderTime");
          final int _cursorIndexOfIsNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotificationEnabled");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfSeedColor = CursorUtil.getColumnIndexOrThrow(_cursor, "seedColor");
          final int _cursorIndexOfSelectedAIPresetId = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAIPresetId");
          final int _cursorIndexOfCustomAIEndpoint = CursorUtil.getColumnIndexOrThrow(_cursor, "customAIEndpoint");
          final int _cursorIndexOfCustomAIModel = CursorUtil.getColumnIndexOrThrow(_cursor, "customAIModel");
          final int _cursorIndexOfThemeMode = CursorUtil.getColumnIndexOrThrow(_cursor, "themeMode");
          final int _cursorIndexOfUseDeadlinerStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "useDeadlinerStyle");
          final int _cursorIndexOfHideDividers = CursorUtil.getColumnIndexOrThrow(_cursor, "hideDividers");
          final int _cursorIndexOfFontSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fontSize");
          final int _cursorIndexOfEnableAnimations = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAnimations");
          final int _cursorIndexOfFeedbackType = CursorUtil.getColumnIndexOrThrow(_cursor, "feedbackType");
          final int _cursorIndexOfEnableVibration = CursorUtil.getColumnIndexOrThrow(_cursor, "enableVibration");
          final int _cursorIndexOfEnableSound = CursorUtil.getColumnIndexOrThrow(_cursor, "enableSound");
          final int _cursorIndexOfBackgroundBehavior = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundBehavior");
          final int _cursorIndexOfStartupPage = CursorUtil.getColumnIndexOrThrow(_cursor, "startupPage");
          final int _cursorIndexOfEnableQuickAdd = CursorUtil.getColumnIndexOrThrow(_cursor, "enableQuickAdd");
          final int _cursorIndexOfEnableGoalReminder = CursorUtil.getColumnIndexOrThrow(_cursor, "enableGoalReminder");
          final int _cursorIndexOfEnableStreakReminder = CursorUtil.getColumnIndexOrThrow(_cursor, "enableStreakReminder");
          final int _cursorIndexOfEnableAutoBackup = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAutoBackup");
          final int _cursorIndexOfLastBackupTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastBackupTime");
          final int _cursorIndexOfEnableCloudSync = CursorUtil.getColumnIndexOrThrow(_cursor, "enableCloudSync");
          final int _cursorIndexOfShowAIWidget = CursorUtil.getColumnIndexOrThrow(_cursor, "showAIWidget");
          final UserSettings _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDailyCalorieGoal;
            _tmpDailyCalorieGoal = _cursor.getInt(_cursorIndexOfDailyCalorieGoal);
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUserGender;
            if (_cursor.isNull(_cursorIndexOfUserGender)) {
              _tmpUserGender = null;
            } else {
              _tmpUserGender = _cursor.getString(_cursorIndexOfUserGender);
            }
            final Integer _tmpUserAge;
            if (_cursor.isNull(_cursorIndexOfUserAge)) {
              _tmpUserAge = null;
            } else {
              _tmpUserAge = _cursor.getInt(_cursorIndexOfUserAge);
            }
            final Float _tmpUserHeight;
            if (_cursor.isNull(_cursorIndexOfUserHeight)) {
              _tmpUserHeight = null;
            } else {
              _tmpUserHeight = _cursor.getFloat(_cursorIndexOfUserHeight);
            }
            final Float _tmpUserWeight;
            if (_cursor.isNull(_cursorIndexOfUserWeight)) {
              _tmpUserWeight = null;
            } else {
              _tmpUserWeight = _cursor.getFloat(_cursorIndexOfUserWeight);
            }
            final String _tmpActivityLevel;
            _tmpActivityLevel = _cursor.getString(_cursorIndexOfActivityLevel);
            final String _tmpDietaryPreference;
            if (_cursor.isNull(_cursorIndexOfDietaryPreference)) {
              _tmpDietaryPreference = null;
            } else {
              _tmpDietaryPreference = _cursor.getString(_cursorIndexOfDietaryPreference);
            }
            final String _tmpBreakfastReminderTime;
            _tmpBreakfastReminderTime = _cursor.getString(_cursorIndexOfBreakfastReminderTime);
            final String _tmpLunchReminderTime;
            _tmpLunchReminderTime = _cursor.getString(_cursorIndexOfLunchReminderTime);
            final String _tmpDinnerReminderTime;
            _tmpDinnerReminderTime = _cursor.getString(_cursorIndexOfDinnerReminderTime);
            final boolean _tmpIsNotificationEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNotificationEnabled);
            _tmpIsNotificationEnabled = _tmp != 0;
            final Boolean _tmpIsDarkMode;
            final Integer _tmp_1;
            if (_cursor.isNull(_cursorIndexOfIsDarkMode)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsDarkMode);
            }
            _tmpIsDarkMode = _tmp_1 == null ? null : _tmp_1 != 0;
            final String _tmpSeedColor;
            if (_cursor.isNull(_cursorIndexOfSeedColor)) {
              _tmpSeedColor = null;
            } else {
              _tmpSeedColor = _cursor.getString(_cursorIndexOfSeedColor);
            }
            final String _tmpSelectedAIPresetId;
            if (_cursor.isNull(_cursorIndexOfSelectedAIPresetId)) {
              _tmpSelectedAIPresetId = null;
            } else {
              _tmpSelectedAIPresetId = _cursor.getString(_cursorIndexOfSelectedAIPresetId);
            }
            final String _tmpCustomAIEndpoint;
            if (_cursor.isNull(_cursorIndexOfCustomAIEndpoint)) {
              _tmpCustomAIEndpoint = null;
            } else {
              _tmpCustomAIEndpoint = _cursor.getString(_cursorIndexOfCustomAIEndpoint);
            }
            final String _tmpCustomAIModel;
            if (_cursor.isNull(_cursorIndexOfCustomAIModel)) {
              _tmpCustomAIModel = null;
            } else {
              _tmpCustomAIModel = _cursor.getString(_cursorIndexOfCustomAIModel);
            }
            final String _tmpThemeMode;
            _tmpThemeMode = _cursor.getString(_cursorIndexOfThemeMode);
            final boolean _tmpUseDeadlinerStyle;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfUseDeadlinerStyle);
            _tmpUseDeadlinerStyle = _tmp_2 != 0;
            final boolean _tmpHideDividers;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfHideDividers);
            _tmpHideDividers = _tmp_3 != 0;
            final String _tmpFontSize;
            _tmpFontSize = _cursor.getString(_cursorIndexOfFontSize);
            final boolean _tmpEnableAnimations;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfEnableAnimations);
            _tmpEnableAnimations = _tmp_4 != 0;
            final String _tmpFeedbackType;
            _tmpFeedbackType = _cursor.getString(_cursorIndexOfFeedbackType);
            final boolean _tmpEnableVibration;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfEnableVibration);
            _tmpEnableVibration = _tmp_5 != 0;
            final boolean _tmpEnableSound;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfEnableSound);
            _tmpEnableSound = _tmp_6 != 0;
            final String _tmpBackgroundBehavior;
            _tmpBackgroundBehavior = _cursor.getString(_cursorIndexOfBackgroundBehavior);
            final String _tmpStartupPage;
            _tmpStartupPage = _cursor.getString(_cursorIndexOfStartupPage);
            final boolean _tmpEnableQuickAdd;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfEnableQuickAdd);
            _tmpEnableQuickAdd = _tmp_7 != 0;
            final boolean _tmpEnableGoalReminder;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfEnableGoalReminder);
            _tmpEnableGoalReminder = _tmp_8 != 0;
            final boolean _tmpEnableStreakReminder;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfEnableStreakReminder);
            _tmpEnableStreakReminder = _tmp_9 != 0;
            final boolean _tmpEnableAutoBackup;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfEnableAutoBackup);
            _tmpEnableAutoBackup = _tmp_10 != 0;
            final String _tmpLastBackupTime;
            if (_cursor.isNull(_cursorIndexOfLastBackupTime)) {
              _tmpLastBackupTime = null;
            } else {
              _tmpLastBackupTime = _cursor.getString(_cursorIndexOfLastBackupTime);
            }
            final boolean _tmpEnableCloudSync;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfEnableCloudSync);
            _tmpEnableCloudSync = _tmp_11 != 0;
            final boolean _tmpShowAIWidget;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfShowAIWidget);
            _tmpShowAIWidget = _tmp_12 != 0;
            _result = new UserSettings(_tmpId,_tmpDailyCalorieGoal,_tmpUserName,_tmpUserId,_tmpUserGender,_tmpUserAge,_tmpUserHeight,_tmpUserWeight,_tmpActivityLevel,_tmpDietaryPreference,_tmpBreakfastReminderTime,_tmpLunchReminderTime,_tmpDinnerReminderTime,_tmpIsNotificationEnabled,_tmpIsDarkMode,_tmpSeedColor,_tmpSelectedAIPresetId,_tmpCustomAIEndpoint,_tmpCustomAIModel,_tmpThemeMode,_tmpUseDeadlinerStyle,_tmpHideDividers,_tmpFontSize,_tmpEnableAnimations,_tmpFeedbackType,_tmpEnableVibration,_tmpEnableSound,_tmpBackgroundBehavior,_tmpStartupPage,_tmpEnableQuickAdd,_tmpEnableGoalReminder,_tmpEnableStreakReminder,_tmpEnableAutoBackup,_tmpLastBackupTime,_tmpEnableCloudSync,_tmpShowAIWidget);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSettingsOnce(final Continuation<? super UserSettings> $completion) {
    final String _sql = "SELECT * FROM user_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserSettings>() {
      @Override
      @Nullable
      public UserSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDailyCalorieGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyCalorieGoal");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "userName");
          final int _cursorIndexOfUserId = CursorUtil.getColumnIndexOrThrow(_cursor, "userId");
          final int _cursorIndexOfUserGender = CursorUtil.getColumnIndexOrThrow(_cursor, "userGender");
          final int _cursorIndexOfUserAge = CursorUtil.getColumnIndexOrThrow(_cursor, "userAge");
          final int _cursorIndexOfUserHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userHeight");
          final int _cursorIndexOfUserWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userWeight");
          final int _cursorIndexOfActivityLevel = CursorUtil.getColumnIndexOrThrow(_cursor, "activityLevel");
          final int _cursorIndexOfDietaryPreference = CursorUtil.getColumnIndexOrThrow(_cursor, "dietaryPreference");
          final int _cursorIndexOfBreakfastReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "breakfastReminderTime");
          final int _cursorIndexOfLunchReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lunchReminderTime");
          final int _cursorIndexOfDinnerReminderTime = CursorUtil.getColumnIndexOrThrow(_cursor, "dinnerReminderTime");
          final int _cursorIndexOfIsNotificationEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isNotificationEnabled");
          final int _cursorIndexOfIsDarkMode = CursorUtil.getColumnIndexOrThrow(_cursor, "isDarkMode");
          final int _cursorIndexOfSeedColor = CursorUtil.getColumnIndexOrThrow(_cursor, "seedColor");
          final int _cursorIndexOfSelectedAIPresetId = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedAIPresetId");
          final int _cursorIndexOfCustomAIEndpoint = CursorUtil.getColumnIndexOrThrow(_cursor, "customAIEndpoint");
          final int _cursorIndexOfCustomAIModel = CursorUtil.getColumnIndexOrThrow(_cursor, "customAIModel");
          final int _cursorIndexOfThemeMode = CursorUtil.getColumnIndexOrThrow(_cursor, "themeMode");
          final int _cursorIndexOfUseDeadlinerStyle = CursorUtil.getColumnIndexOrThrow(_cursor, "useDeadlinerStyle");
          final int _cursorIndexOfHideDividers = CursorUtil.getColumnIndexOrThrow(_cursor, "hideDividers");
          final int _cursorIndexOfFontSize = CursorUtil.getColumnIndexOrThrow(_cursor, "fontSize");
          final int _cursorIndexOfEnableAnimations = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAnimations");
          final int _cursorIndexOfFeedbackType = CursorUtil.getColumnIndexOrThrow(_cursor, "feedbackType");
          final int _cursorIndexOfEnableVibration = CursorUtil.getColumnIndexOrThrow(_cursor, "enableVibration");
          final int _cursorIndexOfEnableSound = CursorUtil.getColumnIndexOrThrow(_cursor, "enableSound");
          final int _cursorIndexOfBackgroundBehavior = CursorUtil.getColumnIndexOrThrow(_cursor, "backgroundBehavior");
          final int _cursorIndexOfStartupPage = CursorUtil.getColumnIndexOrThrow(_cursor, "startupPage");
          final int _cursorIndexOfEnableQuickAdd = CursorUtil.getColumnIndexOrThrow(_cursor, "enableQuickAdd");
          final int _cursorIndexOfEnableGoalReminder = CursorUtil.getColumnIndexOrThrow(_cursor, "enableGoalReminder");
          final int _cursorIndexOfEnableStreakReminder = CursorUtil.getColumnIndexOrThrow(_cursor, "enableStreakReminder");
          final int _cursorIndexOfEnableAutoBackup = CursorUtil.getColumnIndexOrThrow(_cursor, "enableAutoBackup");
          final int _cursorIndexOfLastBackupTime = CursorUtil.getColumnIndexOrThrow(_cursor, "lastBackupTime");
          final int _cursorIndexOfEnableCloudSync = CursorUtil.getColumnIndexOrThrow(_cursor, "enableCloudSync");
          final int _cursorIndexOfShowAIWidget = CursorUtil.getColumnIndexOrThrow(_cursor, "showAIWidget");
          final UserSettings _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDailyCalorieGoal;
            _tmpDailyCalorieGoal = _cursor.getInt(_cursorIndexOfDailyCalorieGoal);
            final String _tmpUserName;
            if (_cursor.isNull(_cursorIndexOfUserName)) {
              _tmpUserName = null;
            } else {
              _tmpUserName = _cursor.getString(_cursorIndexOfUserName);
            }
            final String _tmpUserId;
            if (_cursor.isNull(_cursorIndexOfUserId)) {
              _tmpUserId = null;
            } else {
              _tmpUserId = _cursor.getString(_cursorIndexOfUserId);
            }
            final String _tmpUserGender;
            if (_cursor.isNull(_cursorIndexOfUserGender)) {
              _tmpUserGender = null;
            } else {
              _tmpUserGender = _cursor.getString(_cursorIndexOfUserGender);
            }
            final Integer _tmpUserAge;
            if (_cursor.isNull(_cursorIndexOfUserAge)) {
              _tmpUserAge = null;
            } else {
              _tmpUserAge = _cursor.getInt(_cursorIndexOfUserAge);
            }
            final Float _tmpUserHeight;
            if (_cursor.isNull(_cursorIndexOfUserHeight)) {
              _tmpUserHeight = null;
            } else {
              _tmpUserHeight = _cursor.getFloat(_cursorIndexOfUserHeight);
            }
            final Float _tmpUserWeight;
            if (_cursor.isNull(_cursorIndexOfUserWeight)) {
              _tmpUserWeight = null;
            } else {
              _tmpUserWeight = _cursor.getFloat(_cursorIndexOfUserWeight);
            }
            final String _tmpActivityLevel;
            _tmpActivityLevel = _cursor.getString(_cursorIndexOfActivityLevel);
            final String _tmpDietaryPreference;
            if (_cursor.isNull(_cursorIndexOfDietaryPreference)) {
              _tmpDietaryPreference = null;
            } else {
              _tmpDietaryPreference = _cursor.getString(_cursorIndexOfDietaryPreference);
            }
            final String _tmpBreakfastReminderTime;
            _tmpBreakfastReminderTime = _cursor.getString(_cursorIndexOfBreakfastReminderTime);
            final String _tmpLunchReminderTime;
            _tmpLunchReminderTime = _cursor.getString(_cursorIndexOfLunchReminderTime);
            final String _tmpDinnerReminderTime;
            _tmpDinnerReminderTime = _cursor.getString(_cursorIndexOfDinnerReminderTime);
            final boolean _tmpIsNotificationEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsNotificationEnabled);
            _tmpIsNotificationEnabled = _tmp != 0;
            final Boolean _tmpIsDarkMode;
            final Integer _tmp_1;
            if (_cursor.isNull(_cursorIndexOfIsDarkMode)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getInt(_cursorIndexOfIsDarkMode);
            }
            _tmpIsDarkMode = _tmp_1 == null ? null : _tmp_1 != 0;
            final String _tmpSeedColor;
            if (_cursor.isNull(_cursorIndexOfSeedColor)) {
              _tmpSeedColor = null;
            } else {
              _tmpSeedColor = _cursor.getString(_cursorIndexOfSeedColor);
            }
            final String _tmpSelectedAIPresetId;
            if (_cursor.isNull(_cursorIndexOfSelectedAIPresetId)) {
              _tmpSelectedAIPresetId = null;
            } else {
              _tmpSelectedAIPresetId = _cursor.getString(_cursorIndexOfSelectedAIPresetId);
            }
            final String _tmpCustomAIEndpoint;
            if (_cursor.isNull(_cursorIndexOfCustomAIEndpoint)) {
              _tmpCustomAIEndpoint = null;
            } else {
              _tmpCustomAIEndpoint = _cursor.getString(_cursorIndexOfCustomAIEndpoint);
            }
            final String _tmpCustomAIModel;
            if (_cursor.isNull(_cursorIndexOfCustomAIModel)) {
              _tmpCustomAIModel = null;
            } else {
              _tmpCustomAIModel = _cursor.getString(_cursorIndexOfCustomAIModel);
            }
            final String _tmpThemeMode;
            _tmpThemeMode = _cursor.getString(_cursorIndexOfThemeMode);
            final boolean _tmpUseDeadlinerStyle;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfUseDeadlinerStyle);
            _tmpUseDeadlinerStyle = _tmp_2 != 0;
            final boolean _tmpHideDividers;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfHideDividers);
            _tmpHideDividers = _tmp_3 != 0;
            final String _tmpFontSize;
            _tmpFontSize = _cursor.getString(_cursorIndexOfFontSize);
            final boolean _tmpEnableAnimations;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfEnableAnimations);
            _tmpEnableAnimations = _tmp_4 != 0;
            final String _tmpFeedbackType;
            _tmpFeedbackType = _cursor.getString(_cursorIndexOfFeedbackType);
            final boolean _tmpEnableVibration;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfEnableVibration);
            _tmpEnableVibration = _tmp_5 != 0;
            final boolean _tmpEnableSound;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfEnableSound);
            _tmpEnableSound = _tmp_6 != 0;
            final String _tmpBackgroundBehavior;
            _tmpBackgroundBehavior = _cursor.getString(_cursorIndexOfBackgroundBehavior);
            final String _tmpStartupPage;
            _tmpStartupPage = _cursor.getString(_cursorIndexOfStartupPage);
            final boolean _tmpEnableQuickAdd;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfEnableQuickAdd);
            _tmpEnableQuickAdd = _tmp_7 != 0;
            final boolean _tmpEnableGoalReminder;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfEnableGoalReminder);
            _tmpEnableGoalReminder = _tmp_8 != 0;
            final boolean _tmpEnableStreakReminder;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfEnableStreakReminder);
            _tmpEnableStreakReminder = _tmp_9 != 0;
            final boolean _tmpEnableAutoBackup;
            final int _tmp_10;
            _tmp_10 = _cursor.getInt(_cursorIndexOfEnableAutoBackup);
            _tmpEnableAutoBackup = _tmp_10 != 0;
            final String _tmpLastBackupTime;
            if (_cursor.isNull(_cursorIndexOfLastBackupTime)) {
              _tmpLastBackupTime = null;
            } else {
              _tmpLastBackupTime = _cursor.getString(_cursorIndexOfLastBackupTime);
            }
            final boolean _tmpEnableCloudSync;
            final int _tmp_11;
            _tmp_11 = _cursor.getInt(_cursorIndexOfEnableCloudSync);
            _tmpEnableCloudSync = _tmp_11 != 0;
            final boolean _tmpShowAIWidget;
            final int _tmp_12;
            _tmp_12 = _cursor.getInt(_cursorIndexOfShowAIWidget);
            _tmpShowAIWidget = _tmp_12 != 0;
            _result = new UserSettings(_tmpId,_tmpDailyCalorieGoal,_tmpUserName,_tmpUserId,_tmpUserGender,_tmpUserAge,_tmpUserHeight,_tmpUserWeight,_tmpActivityLevel,_tmpDietaryPreference,_tmpBreakfastReminderTime,_tmpLunchReminderTime,_tmpDinnerReminderTime,_tmpIsNotificationEnabled,_tmpIsDarkMode,_tmpSeedColor,_tmpSelectedAIPresetId,_tmpCustomAIEndpoint,_tmpCustomAIModel,_tmpThemeMode,_tmpUseDeadlinerStyle,_tmpHideDividers,_tmpFontSize,_tmpEnableAnimations,_tmpFeedbackType,_tmpEnableVibration,_tmpEnableSound,_tmpBackgroundBehavior,_tmpStartupPage,_tmpEnableQuickAdd,_tmpEnableGoalReminder,_tmpEnableStreakReminder,_tmpEnableAutoBackup,_tmpLastBackupTime,_tmpEnableCloudSync,_tmpShowAIWidget);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
