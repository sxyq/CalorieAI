package com.calorieai.app.data.local;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile FoodRecordDao _foodRecordDao;

  private volatile UserSettingsDao _userSettingsDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `food_records` (`id` TEXT NOT NULL, `foodName` TEXT NOT NULL, `userInput` TEXT NOT NULL, `totalCalories` INTEGER NOT NULL, `protein` REAL NOT NULL, `carbs` REAL NOT NULL, `fat` REAL NOT NULL, `ingredients` TEXT NOT NULL, `mealType` TEXT NOT NULL, `recordTime` INTEGER NOT NULL, `iconUrl` TEXT, `iconLocalPath` TEXT, `isStarred` INTEGER NOT NULL, `confidence` TEXT NOT NULL, `notes` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `user_settings` (`id` INTEGER NOT NULL, `dailyCalorieGoal` INTEGER NOT NULL, `userName` TEXT, `userGender` TEXT, `userAge` INTEGER, `userHeight` REAL, `userWeight` REAL, `dietaryPreference` TEXT, `breakfastReminderTime` TEXT NOT NULL, `lunchReminderTime` TEXT NOT NULL, `dinnerReminderTime` TEXT NOT NULL, `isNotificationEnabled` INTEGER NOT NULL, `isDarkMode` INTEGER, `seedColor` TEXT, `selectedAIPresetId` TEXT, `customAIEndpoint` TEXT, `customAIModel` TEXT, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'd06877b44fe663df308dea58a5ed1a09')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `food_records`");
        db.execSQL("DROP TABLE IF EXISTS `user_settings`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsFoodRecords = new HashMap<String, TableInfo.Column>(15);
        _columnsFoodRecords.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("foodName", new TableInfo.Column("foodName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("userInput", new TableInfo.Column("userInput", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("totalCalories", new TableInfo.Column("totalCalories", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("protein", new TableInfo.Column("protein", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("carbs", new TableInfo.Column("carbs", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("fat", new TableInfo.Column("fat", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("ingredients", new TableInfo.Column("ingredients", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("mealType", new TableInfo.Column("mealType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("recordTime", new TableInfo.Column("recordTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("iconUrl", new TableInfo.Column("iconUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("iconLocalPath", new TableInfo.Column("iconLocalPath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("isStarred", new TableInfo.Column("isStarred", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("confidence", new TableInfo.Column("confidence", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFoodRecords.put("notes", new TableInfo.Column("notes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFoodRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFoodRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFoodRecords = new TableInfo("food_records", _columnsFoodRecords, _foreignKeysFoodRecords, _indicesFoodRecords);
        final TableInfo _existingFoodRecords = TableInfo.read(db, "food_records");
        if (!_infoFoodRecords.equals(_existingFoodRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "food_records(com.calorieai.app.data.model.FoodRecord).\n"
                  + " Expected:\n" + _infoFoodRecords + "\n"
                  + " Found:\n" + _existingFoodRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsUserSettings = new HashMap<String, TableInfo.Column>(17);
        _columnsUserSettings.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("dailyCalorieGoal", new TableInfo.Column("dailyCalorieGoal", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("userName", new TableInfo.Column("userName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("userGender", new TableInfo.Column("userGender", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("userAge", new TableInfo.Column("userAge", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("userHeight", new TableInfo.Column("userHeight", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("userWeight", new TableInfo.Column("userWeight", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("dietaryPreference", new TableInfo.Column("dietaryPreference", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("breakfastReminderTime", new TableInfo.Column("breakfastReminderTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("lunchReminderTime", new TableInfo.Column("lunchReminderTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("dinnerReminderTime", new TableInfo.Column("dinnerReminderTime", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("isNotificationEnabled", new TableInfo.Column("isNotificationEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("isDarkMode", new TableInfo.Column("isDarkMode", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("seedColor", new TableInfo.Column("seedColor", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("selectedAIPresetId", new TableInfo.Column("selectedAIPresetId", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("customAIEndpoint", new TableInfo.Column("customAIEndpoint", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsUserSettings.put("customAIModel", new TableInfo.Column("customAIModel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysUserSettings = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesUserSettings = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoUserSettings = new TableInfo("user_settings", _columnsUserSettings, _foreignKeysUserSettings, _indicesUserSettings);
        final TableInfo _existingUserSettings = TableInfo.read(db, "user_settings");
        if (!_infoUserSettings.equals(_existingUserSettings)) {
          return new RoomOpenHelper.ValidationResult(false, "user_settings(com.calorieai.app.data.model.UserSettings).\n"
                  + " Expected:\n" + _infoUserSettings + "\n"
                  + " Found:\n" + _existingUserSettings);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "d06877b44fe663df308dea58a5ed1a09", "3b59a3188e32b98e881b59ca59af0285");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "food_records","user_settings");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `food_records`");
      _db.execSQL("DELETE FROM `user_settings`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(FoodRecordDao.class, FoodRecordDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(UserSettingsDao.class, UserSettingsDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public FoodRecordDao foodRecordDao() {
    if (_foodRecordDao != null) {
      return _foodRecordDao;
    } else {
      synchronized(this) {
        if(_foodRecordDao == null) {
          _foodRecordDao = new FoodRecordDao_Impl(this);
        }
        return _foodRecordDao;
      }
    }
  }

  @Override
  public UserSettingsDao userSettingsDao() {
    if (_userSettingsDao != null) {
      return _userSettingsDao;
    } else {
      synchronized(this) {
        if(_userSettingsDao == null) {
          _userSettingsDao = new UserSettingsDao_Impl(this);
        }
        return _userSettingsDao;
      }
    }
  }
}
