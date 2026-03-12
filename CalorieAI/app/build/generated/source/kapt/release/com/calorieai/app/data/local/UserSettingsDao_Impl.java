package com.calorieai.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
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
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@SuppressWarnings({"unchecked", "deprecation"})
public final class UserSettingsDao_Impl implements UserSettingsDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<UserSettings> __insertionAdapterOfUserSettings;

  private final EntityDeletionOrUpdateAdapter<UserSettings> __updateAdapterOfUserSettings;

  private final SharedSQLiteStatement __preparedStmtOfUpdateDailyGoal;

  private final SharedSQLiteStatement __preparedStmtOfUpdateNotificationEnabled;

  public UserSettingsDao_Impl(RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfUserSettings = new EntityInsertionAdapter<UserSettings>(__db) {
      @Override
      public String createQuery() {
        return "INSERT OR REPLACE INTO `user_settings` (`id`,`dailyCalorieGoal`,`userName`,`userGender`,`userAge`,`userHeight`,`userWeight`,`dietaryPreference`,`breakfastReminderTime`,`lunchReminderTime`,`dinnerReminderTime`,`isNotificationEnabled`,`isDarkMode`,`seedColor`,`selectedAIPresetId`,`customAIEndpoint`,`customAIModel`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, UserSettings value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDailyCalorieGoal());
        if (value.getUserName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getUserName());
        }
        if (value.getUserGender() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getUserGender());
        }
        if (value.getUserAge() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindLong(5, value.getUserAge());
        }
        if (value.getUserHeight() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindDouble(6, value.getUserHeight());
        }
        if (value.getUserWeight() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindDouble(7, value.getUserWeight());
        }
        if (value.getDietaryPreference() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getDietaryPreference());
        }
        if (value.getBreakfastReminderTime() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getBreakfastReminderTime());
        }
        if (value.getLunchReminderTime() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.getLunchReminderTime());
        }
        if (value.getDinnerReminderTime() == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, value.getDinnerReminderTime());
        }
        final int _tmp = value.isNotificationEnabled() ? 1 : 0;
        stmt.bindLong(12, _tmp);
        final Integer _tmp_1 = value.isDarkMode() == null ? null : (value.isDarkMode() ? 1 : 0);
        if (_tmp_1 == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindLong(13, _tmp_1);
        }
        if (value.getSeedColor() == null) {
          stmt.bindNull(14);
        } else {
          stmt.bindString(14, value.getSeedColor());
        }
        if (value.getSelectedAIPresetId() == null) {
          stmt.bindNull(15);
        } else {
          stmt.bindString(15, value.getSelectedAIPresetId());
        }
        if (value.getCustomAIEndpoint() == null) {
          stmt.bindNull(16);
        } else {
          stmt.bindString(16, value.getCustomAIEndpoint());
        }
        if (value.getCustomAIModel() == null) {
          stmt.bindNull(17);
        } else {
          stmt.bindString(17, value.getCustomAIModel());
        }
      }
    };
    this.__updateAdapterOfUserSettings = new EntityDeletionOrUpdateAdapter<UserSettings>(__db) {
      @Override
      public String createQuery() {
        return "UPDATE OR ABORT `user_settings` SET `id` = ?,`dailyCalorieGoal` = ?,`userName` = ?,`userGender` = ?,`userAge` = ?,`userHeight` = ?,`userWeight` = ?,`dietaryPreference` = ?,`breakfastReminderTime` = ?,`lunchReminderTime` = ?,`dinnerReminderTime` = ?,`isNotificationEnabled` = ?,`isDarkMode` = ?,`seedColor` = ?,`selectedAIPresetId` = ?,`customAIEndpoint` = ?,`customAIModel` = ? WHERE `id` = ?";
      }

      @Override
      public void bind(SupportSQLiteStatement stmt, UserSettings value) {
        stmt.bindLong(1, value.getId());
        stmt.bindLong(2, value.getDailyCalorieGoal());
        if (value.getUserName() == null) {
          stmt.bindNull(3);
        } else {
          stmt.bindString(3, value.getUserName());
        }
        if (value.getUserGender() == null) {
          stmt.bindNull(4);
        } else {
          stmt.bindString(4, value.getUserGender());
        }
        if (value.getUserAge() == null) {
          stmt.bindNull(5);
        } else {
          stmt.bindLong(5, value.getUserAge());
        }
        if (value.getUserHeight() == null) {
          stmt.bindNull(6);
        } else {
          stmt.bindDouble(6, value.getUserHeight());
        }
        if (value.getUserWeight() == null) {
          stmt.bindNull(7);
        } else {
          stmt.bindDouble(7, value.getUserWeight());
        }
        if (value.getDietaryPreference() == null) {
          stmt.bindNull(8);
        } else {
          stmt.bindString(8, value.getDietaryPreference());
        }
        if (value.getBreakfastReminderTime() == null) {
          stmt.bindNull(9);
        } else {
          stmt.bindString(9, value.getBreakfastReminderTime());
        }
        if (value.getLunchReminderTime() == null) {
          stmt.bindNull(10);
        } else {
          stmt.bindString(10, value.getLunchReminderTime());
        }
        if (value.getDinnerReminderTime() == null) {
          stmt.bindNull(11);
        } else {
          stmt.bindString(11, value.getDinnerReminderTime());
        }
        final int _tmp = value.isNotificationEnabled() ? 1 : 0;
        stmt.bindLong(12, _tmp);
        final Integer _tmp_1 = value.isDarkMode() == null ? null : (value.isDarkMode() ? 1 : 0);
        if (_tmp_1 == null) {
          stmt.bindNull(13);
        } else {
          stmt.bindLong(13, _tmp_1);
        }
        if (value.getSeedColor() == null) {
          stmt.bindNull(14);
        } else {
          stmt.bindString(14, value.getSeedColor());
        }
        if (value.getSelectedAIPresetId() == null) {
          stmt.bindNull(15);
        } else {
          stmt.bindString(15, value.getSelectedAIPresetId());
        }
        if (value.getCustomAIEndpoint() == null) {
          stmt.bindNull(16);
        } else {
          stmt.bindString(16, value.getCustomAIEndpoint());
        }
        if (value.getCustomAIModel() == null) {
          stmt.bindNull(17);
        } else {
          stmt.bindString(17, value.getCustomAIModel());
        }
        stmt.bindLong(18, value.getId());
      }
    };
    this.__preparedStmtOfUpdateDailyGoal = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE user_settings SET dailyCalorieGoal = ? WHERE id = 1";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateNotificationEnabled = new SharedSQLiteStatement(__db) {
      @Override
      public String createQuery() {
        final String _query = "UPDATE user_settings SET isNotificationEnabled = ? WHERE id = 1";
        return _query;
      }
    };
  }

  @Override
  public Object insertSettings(final UserSettings settings,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
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
    }, continuation);
  }

  @Override
  public Object updateSettings(final UserSettings settings,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
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
    }, continuation);
  }

  @Override
  public Object updateDailyGoal(final int goal, final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateDailyGoal.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, goal);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfUpdateDailyGoal.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object updateNotificationEnabled(final boolean enabled,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateNotificationEnabled.acquire();
        int _argIndex = 1;
        final int _tmp = enabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        __db.beginTransaction();
        try {
          _stmt.executeUpdateDelete();
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
          __preparedStmtOfUpdateNotificationEnabled.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Flow<UserSettings> getSettings() {
    final String _sql = "SELECT * FROM user_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[]{"user_settings"}, new Callable<UserSettings>() {
      @Override
      public UserSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDailyCalorieGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyCalorieGoal");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "userName");
          final int _cursorIndexOfUserGender = CursorUtil.getColumnIndexOrThrow(_cursor, "userGender");
          final int _cursorIndexOfUserAge = CursorUtil.getColumnIndexOrThrow(_cursor, "userAge");
          final int _cursorIndexOfUserHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userHeight");
          final int _cursorIndexOfUserWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userWeight");
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
          final UserSettings _result;
          if(_cursor.moveToFirst()) {
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
            final String _tmpDietaryPreference;
            if (_cursor.isNull(_cursorIndexOfDietaryPreference)) {
              _tmpDietaryPreference = null;
            } else {
              _tmpDietaryPreference = _cursor.getString(_cursorIndexOfDietaryPreference);
            }
            final String _tmpBreakfastReminderTime;
            if (_cursor.isNull(_cursorIndexOfBreakfastReminderTime)) {
              _tmpBreakfastReminderTime = null;
            } else {
              _tmpBreakfastReminderTime = _cursor.getString(_cursorIndexOfBreakfastReminderTime);
            }
            final String _tmpLunchReminderTime;
            if (_cursor.isNull(_cursorIndexOfLunchReminderTime)) {
              _tmpLunchReminderTime = null;
            } else {
              _tmpLunchReminderTime = _cursor.getString(_cursorIndexOfLunchReminderTime);
            }
            final String _tmpDinnerReminderTime;
            if (_cursor.isNull(_cursorIndexOfDinnerReminderTime)) {
              _tmpDinnerReminderTime = null;
            } else {
              _tmpDinnerReminderTime = _cursor.getString(_cursorIndexOfDinnerReminderTime);
            }
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
            _result = new UserSettings(_tmpId,_tmpDailyCalorieGoal,_tmpUserName,_tmpUserGender,_tmpUserAge,_tmpUserHeight,_tmpUserWeight,_tmpDietaryPreference,_tmpBreakfastReminderTime,_tmpLunchReminderTime,_tmpDinnerReminderTime,_tmpIsNotificationEnabled,_tmpIsDarkMode,_tmpSeedColor,_tmpSelectedAIPresetId,_tmpCustomAIEndpoint,_tmpCustomAIModel);
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
  public Object getSettingsSync(final Continuation<? super UserSettings> continuation) {
    final String _sql = "SELECT * FROM user_settings WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<UserSettings>() {
      @Override
      public UserSettings call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDailyCalorieGoal = CursorUtil.getColumnIndexOrThrow(_cursor, "dailyCalorieGoal");
          final int _cursorIndexOfUserName = CursorUtil.getColumnIndexOrThrow(_cursor, "userName");
          final int _cursorIndexOfUserGender = CursorUtil.getColumnIndexOrThrow(_cursor, "userGender");
          final int _cursorIndexOfUserAge = CursorUtil.getColumnIndexOrThrow(_cursor, "userAge");
          final int _cursorIndexOfUserHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userHeight");
          final int _cursorIndexOfUserWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "userWeight");
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
          final UserSettings _result;
          if(_cursor.moveToFirst()) {
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
            final String _tmpDietaryPreference;
            if (_cursor.isNull(_cursorIndexOfDietaryPreference)) {
              _tmpDietaryPreference = null;
            } else {
              _tmpDietaryPreference = _cursor.getString(_cursorIndexOfDietaryPreference);
            }
            final String _tmpBreakfastReminderTime;
            if (_cursor.isNull(_cursorIndexOfBreakfastReminderTime)) {
              _tmpBreakfastReminderTime = null;
            } else {
              _tmpBreakfastReminderTime = _cursor.getString(_cursorIndexOfBreakfastReminderTime);
            }
            final String _tmpLunchReminderTime;
            if (_cursor.isNull(_cursorIndexOfLunchReminderTime)) {
              _tmpLunchReminderTime = null;
            } else {
              _tmpLunchReminderTime = _cursor.getString(_cursorIndexOfLunchReminderTime);
            }
            final String _tmpDinnerReminderTime;
            if (_cursor.isNull(_cursorIndexOfDinnerReminderTime)) {
              _tmpDinnerReminderTime = null;
            } else {
              _tmpDinnerReminderTime = _cursor.getString(_cursorIndexOfDinnerReminderTime);
            }
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
            _result = new UserSettings(_tmpId,_tmpDailyCalorieGoal,_tmpUserName,_tmpUserGender,_tmpUserAge,_tmpUserHeight,_tmpUserWeight,_tmpDietaryPreference,_tmpBreakfastReminderTime,_tmpLunchReminderTime,_tmpDinnerReminderTime,_tmpIsNotificationEnabled,_tmpIsDarkMode,_tmpSeedColor,_tmpSelectedAIPresetId,_tmpCustomAIEndpoint,_tmpCustomAIModel);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
