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
import com.calorieai.app.data.model.ConfidenceLevel;
import com.calorieai.app.data.model.Converters;
import com.calorieai.app.data.model.FoodRecord;
import com.calorieai.app.data.model.Ingredient;
import com.calorieai.app.data.model.MealType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class FoodRecordDao_Impl implements FoodRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FoodRecord> __insertionAdapterOfFoodRecord;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<FoodRecord> __deletionAdapterOfFoodRecord;

  private final EntityDeletionOrUpdateAdapter<FoodRecord> __updateAdapterOfFoodRecord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecordById;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStarredStatus;

  public FoodRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFoodRecord = new EntityInsertionAdapter<FoodRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `food_records` (`id`,`foodName`,`userInput`,`totalCalories`,`protein`,`carbs`,`fat`,`ingredients`,`mealType`,`recordTime`,`iconUrl`,`iconLocalPath`,`isStarred`,`confidence`,`notes`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodRecord entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFoodName());
        statement.bindString(3, entity.getUserInput());
        statement.bindLong(4, entity.getTotalCalories());
        statement.bindDouble(5, entity.getProtein());
        statement.bindDouble(6, entity.getCarbs());
        statement.bindDouble(7, entity.getFat());
        final String _tmp = __converters.fromIngredientsList(entity.getIngredients());
        statement.bindString(8, _tmp);
        final String _tmp_1 = __converters.fromMealType(entity.getMealType());
        statement.bindString(9, _tmp_1);
        statement.bindLong(10, entity.getRecordTime());
        if (entity.getIconUrl() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getIconUrl());
        }
        if (entity.getIconLocalPath() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getIconLocalPath());
        }
        final int _tmp_2 = entity.isStarred() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        final String _tmp_3 = __converters.fromConfidenceLevel(entity.getConfidence());
        statement.bindString(14, _tmp_3);
        if (entity.getNotes() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getNotes());
        }
      }
    };
    this.__deletionAdapterOfFoodRecord = new EntityDeletionOrUpdateAdapter<FoodRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `food_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodRecord entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfFoodRecord = new EntityDeletionOrUpdateAdapter<FoodRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `food_records` SET `id` = ?,`foodName` = ?,`userInput` = ?,`totalCalories` = ?,`protein` = ?,`carbs` = ?,`fat` = ?,`ingredients` = ?,`mealType` = ?,`recordTime` = ?,`iconUrl` = ?,`iconLocalPath` = ?,`isStarred` = ?,`confidence` = ?,`notes` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FoodRecord entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getFoodName());
        statement.bindString(3, entity.getUserInput());
        statement.bindLong(4, entity.getTotalCalories());
        statement.bindDouble(5, entity.getProtein());
        statement.bindDouble(6, entity.getCarbs());
        statement.bindDouble(7, entity.getFat());
        final String _tmp = __converters.fromIngredientsList(entity.getIngredients());
        statement.bindString(8, _tmp);
        final String _tmp_1 = __converters.fromMealType(entity.getMealType());
        statement.bindString(9, _tmp_1);
        statement.bindLong(10, entity.getRecordTime());
        if (entity.getIconUrl() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getIconUrl());
        }
        if (entity.getIconLocalPath() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getIconLocalPath());
        }
        final int _tmp_2 = entity.isStarred() ? 1 : 0;
        statement.bindLong(13, _tmp_2);
        final String _tmp_3 = __converters.fromConfidenceLevel(entity.getConfidence());
        statement.bindString(14, _tmp_3);
        if (entity.getNotes() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getNotes());
        }
        statement.bindString(16, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteRecordById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM food_records WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfUpdateStarredStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE food_records SET isStarred = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecord(final FoodRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFoodRecord.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecord(final FoodRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfFoodRecord.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRecord(final FoodRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfFoodRecord.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecordById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteRecordById.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfDeleteRecordById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStarredStatus(final String id, final boolean isStarred,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStarredStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isStarred ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateStarredStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<FoodRecord>> getAllRecords() {
    final String _sql = "SELECT * FROM food_records ORDER BY recordTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_records"}, new Callable<List<FoodRecord>>() {
      @Override
      @NonNull
      public List<FoodRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfUserInput = CursorUtil.getColumnIndexOrThrow(_cursor, "userInput");
          final int _cursorIndexOfTotalCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCalories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfIngredients = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredients");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfIconLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "iconLocalPath");
          final int _cursorIndexOfIsStarred = CursorUtil.getColumnIndexOrThrow(_cursor, "isStarred");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<FoodRecord> _result = new ArrayList<FoodRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final String _tmpUserInput;
            _tmpUserInput = _cursor.getString(_cursorIndexOfUserInput);
            final int _tmpTotalCalories;
            _tmpTotalCalories = _cursor.getInt(_cursorIndexOfTotalCalories);
            final float _tmpProtein;
            _tmpProtein = _cursor.getFloat(_cursorIndexOfProtein);
            final float _tmpCarbs;
            _tmpCarbs = _cursor.getFloat(_cursorIndexOfCarbs);
            final float _tmpFat;
            _tmpFat = _cursor.getFloat(_cursorIndexOfFat);
            final List<Ingredient> _tmpIngredients;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfIngredients);
            _tmpIngredients = __converters.toIngredientsList(_tmp);
            final MealType _tmpMealType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp_1);
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final String _tmpIconLocalPath;
            if (_cursor.isNull(_cursorIndexOfIconLocalPath)) {
              _tmpIconLocalPath = null;
            } else {
              _tmpIconLocalPath = _cursor.getString(_cursorIndexOfIconLocalPath);
            }
            final boolean _tmpIsStarred;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsStarred);
            _tmpIsStarred = _tmp_2 != 0;
            final ConfidenceLevel _tmpConfidence;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfConfidence);
            _tmpConfidence = __converters.toConfidenceLevel(_tmp_3);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new FoodRecord(_tmpId,_tmpFoodName,_tmpUserInput,_tmpTotalCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpIngredients,_tmpMealType,_tmpRecordTime,_tmpIconUrl,_tmpIconLocalPath,_tmpIsStarred,_tmpConfidence,_tmpNotes);
            _result.add(_item);
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
  public Flow<List<FoodRecord>> getRecordsBetween(final long startTime, final long endTime) {
    final String _sql = "SELECT * FROM food_records WHERE recordTime BETWEEN ? AND ? ORDER BY recordTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_records"}, new Callable<List<FoodRecord>>() {
      @Override
      @NonNull
      public List<FoodRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfUserInput = CursorUtil.getColumnIndexOrThrow(_cursor, "userInput");
          final int _cursorIndexOfTotalCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCalories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfIngredients = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredients");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfIconLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "iconLocalPath");
          final int _cursorIndexOfIsStarred = CursorUtil.getColumnIndexOrThrow(_cursor, "isStarred");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<FoodRecord> _result = new ArrayList<FoodRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final String _tmpUserInput;
            _tmpUserInput = _cursor.getString(_cursorIndexOfUserInput);
            final int _tmpTotalCalories;
            _tmpTotalCalories = _cursor.getInt(_cursorIndexOfTotalCalories);
            final float _tmpProtein;
            _tmpProtein = _cursor.getFloat(_cursorIndexOfProtein);
            final float _tmpCarbs;
            _tmpCarbs = _cursor.getFloat(_cursorIndexOfCarbs);
            final float _tmpFat;
            _tmpFat = _cursor.getFloat(_cursorIndexOfFat);
            final List<Ingredient> _tmpIngredients;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfIngredients);
            _tmpIngredients = __converters.toIngredientsList(_tmp);
            final MealType _tmpMealType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp_1);
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final String _tmpIconLocalPath;
            if (_cursor.isNull(_cursorIndexOfIconLocalPath)) {
              _tmpIconLocalPath = null;
            } else {
              _tmpIconLocalPath = _cursor.getString(_cursorIndexOfIconLocalPath);
            }
            final boolean _tmpIsStarred;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsStarred);
            _tmpIsStarred = _tmp_2 != 0;
            final ConfidenceLevel _tmpConfidence;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfConfidence);
            _tmpConfidence = __converters.toConfidenceLevel(_tmp_3);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new FoodRecord(_tmpId,_tmpFoodName,_tmpUserInput,_tmpTotalCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpIngredients,_tmpMealType,_tmpRecordTime,_tmpIconUrl,_tmpIconLocalPath,_tmpIsStarred,_tmpConfidence,_tmpNotes);
            _result.add(_item);
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
  public Flow<List<FoodRecord>> getRecordsByMealType(final MealType mealType, final long startTime,
      final long endTime) {
    final String _sql = "SELECT * FROM food_records WHERE mealType = ? AND recordTime BETWEEN ? AND ? ORDER BY recordTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    final String _tmp = __converters.fromMealType(mealType);
    _statement.bindString(_argIndex, _tmp);
    _argIndex = 2;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 3;
    _statement.bindLong(_argIndex, endTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_records"}, new Callable<List<FoodRecord>>() {
      @Override
      @NonNull
      public List<FoodRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfUserInput = CursorUtil.getColumnIndexOrThrow(_cursor, "userInput");
          final int _cursorIndexOfTotalCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCalories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfIngredients = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredients");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfIconLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "iconLocalPath");
          final int _cursorIndexOfIsStarred = CursorUtil.getColumnIndexOrThrow(_cursor, "isStarred");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final List<FoodRecord> _result = new ArrayList<FoodRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FoodRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final String _tmpUserInput;
            _tmpUserInput = _cursor.getString(_cursorIndexOfUserInput);
            final int _tmpTotalCalories;
            _tmpTotalCalories = _cursor.getInt(_cursorIndexOfTotalCalories);
            final float _tmpProtein;
            _tmpProtein = _cursor.getFloat(_cursorIndexOfProtein);
            final float _tmpCarbs;
            _tmpCarbs = _cursor.getFloat(_cursorIndexOfCarbs);
            final float _tmpFat;
            _tmpFat = _cursor.getFloat(_cursorIndexOfFat);
            final List<Ingredient> _tmpIngredients;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfIngredients);
            _tmpIngredients = __converters.toIngredientsList(_tmp_1);
            final MealType _tmpMealType;
            final String _tmp_2;
            _tmp_2 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp_2);
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final String _tmpIconLocalPath;
            if (_cursor.isNull(_cursorIndexOfIconLocalPath)) {
              _tmpIconLocalPath = null;
            } else {
              _tmpIconLocalPath = _cursor.getString(_cursorIndexOfIconLocalPath);
            }
            final boolean _tmpIsStarred;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsStarred);
            _tmpIsStarred = _tmp_3 != 0;
            final ConfidenceLevel _tmpConfidence;
            final String _tmp_4;
            _tmp_4 = _cursor.getString(_cursorIndexOfConfidence);
            _tmpConfidence = __converters.toConfidenceLevel(_tmp_4);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _item = new FoodRecord(_tmpId,_tmpFoodName,_tmpUserInput,_tmpTotalCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpIngredients,_tmpMealType,_tmpRecordTime,_tmpIconUrl,_tmpIconLocalPath,_tmpIsStarred,_tmpConfidence,_tmpNotes);
            _result.add(_item);
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
  public Object getRecordById(final String id, final Continuation<? super FoodRecord> $completion) {
    final String _sql = "SELECT * FROM food_records WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<FoodRecord>() {
      @Override
      @Nullable
      public FoodRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfFoodName = CursorUtil.getColumnIndexOrThrow(_cursor, "foodName");
          final int _cursorIndexOfUserInput = CursorUtil.getColumnIndexOrThrow(_cursor, "userInput");
          final int _cursorIndexOfTotalCalories = CursorUtil.getColumnIndexOrThrow(_cursor, "totalCalories");
          final int _cursorIndexOfProtein = CursorUtil.getColumnIndexOrThrow(_cursor, "protein");
          final int _cursorIndexOfCarbs = CursorUtil.getColumnIndexOrThrow(_cursor, "carbs");
          final int _cursorIndexOfFat = CursorUtil.getColumnIndexOrThrow(_cursor, "fat");
          final int _cursorIndexOfIngredients = CursorUtil.getColumnIndexOrThrow(_cursor, "ingredients");
          final int _cursorIndexOfMealType = CursorUtil.getColumnIndexOrThrow(_cursor, "mealType");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final int _cursorIndexOfIconUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "iconUrl");
          final int _cursorIndexOfIconLocalPath = CursorUtil.getColumnIndexOrThrow(_cursor, "iconLocalPath");
          final int _cursorIndexOfIsStarred = CursorUtil.getColumnIndexOrThrow(_cursor, "isStarred");
          final int _cursorIndexOfConfidence = CursorUtil.getColumnIndexOrThrow(_cursor, "confidence");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final FoodRecord _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpFoodName;
            _tmpFoodName = _cursor.getString(_cursorIndexOfFoodName);
            final String _tmpUserInput;
            _tmpUserInput = _cursor.getString(_cursorIndexOfUserInput);
            final int _tmpTotalCalories;
            _tmpTotalCalories = _cursor.getInt(_cursorIndexOfTotalCalories);
            final float _tmpProtein;
            _tmpProtein = _cursor.getFloat(_cursorIndexOfProtein);
            final float _tmpCarbs;
            _tmpCarbs = _cursor.getFloat(_cursorIndexOfCarbs);
            final float _tmpFat;
            _tmpFat = _cursor.getFloat(_cursorIndexOfFat);
            final List<Ingredient> _tmpIngredients;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfIngredients);
            _tmpIngredients = __converters.toIngredientsList(_tmp);
            final MealType _tmpMealType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfMealType);
            _tmpMealType = __converters.toMealType(_tmp_1);
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            final String _tmpIconUrl;
            if (_cursor.isNull(_cursorIndexOfIconUrl)) {
              _tmpIconUrl = null;
            } else {
              _tmpIconUrl = _cursor.getString(_cursorIndexOfIconUrl);
            }
            final String _tmpIconLocalPath;
            if (_cursor.isNull(_cursorIndexOfIconLocalPath)) {
              _tmpIconLocalPath = null;
            } else {
              _tmpIconLocalPath = _cursor.getString(_cursorIndexOfIconLocalPath);
            }
            final boolean _tmpIsStarred;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsStarred);
            _tmpIsStarred = _tmp_2 != 0;
            final ConfidenceLevel _tmpConfidence;
            final String _tmp_3;
            _tmp_3 = _cursor.getString(_cursorIndexOfConfidence);
            _tmpConfidence = __converters.toConfidenceLevel(_tmp_3);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            _result = new FoodRecord(_tmpId,_tmpFoodName,_tmpUserInput,_tmpTotalCalories,_tmpProtein,_tmpCarbs,_tmpFat,_tmpIngredients,_tmpMealType,_tmpRecordTime,_tmpIconUrl,_tmpIconLocalPath,_tmpIsStarred,_tmpConfidence,_tmpNotes);
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
  public Flow<Integer> getTotalCaloriesBetween(final long startTime, final long endTime) {
    final String _sql = "SELECT SUM(totalCalories) FROM food_records WHERE recordTime BETWEEN ? AND ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"food_records"}, new Callable<Integer>() {
      @Override
      @Nullable
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final Integer _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getInt(0);
            }
            _result = _tmp;
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
