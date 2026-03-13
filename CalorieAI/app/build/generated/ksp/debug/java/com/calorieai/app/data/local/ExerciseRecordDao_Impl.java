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
import com.calorieai.app.data.model.Converters;
import com.calorieai.app.data.model.ExerciseRecord;
import com.calorieai.app.data.model.ExerciseType;
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
public final class ExerciseRecordDao_Impl implements ExerciseRecordDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ExerciseRecord> __insertionAdapterOfExerciseRecord;

  private final Converters __converters = new Converters();

  private final EntityDeletionOrUpdateAdapter<ExerciseRecord> __deletionAdapterOfExerciseRecord;

  private final EntityDeletionOrUpdateAdapter<ExerciseRecord> __updateAdapterOfExerciseRecord;

  private final SharedSQLiteStatement __preparedStmtOfDeleteRecordById;

  public ExerciseRecordDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfExerciseRecord = new EntityInsertionAdapter<ExerciseRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `exercise_records` (`id`,`exerciseType`,`durationMinutes`,`caloriesBurned`,`notes`,`recordTime`) VALUES (?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExerciseRecord entity) {
        statement.bindString(1, entity.getId());
        final String _tmp = __converters.fromExerciseType(entity.getExerciseType());
        statement.bindString(2, _tmp);
        statement.bindLong(3, entity.getDurationMinutes());
        statement.bindLong(4, entity.getCaloriesBurned());
        if (entity.getNotes() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getNotes());
        }
        statement.bindLong(6, entity.getRecordTime());
      }
    };
    this.__deletionAdapterOfExerciseRecord = new EntityDeletionOrUpdateAdapter<ExerciseRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `exercise_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExerciseRecord entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfExerciseRecord = new EntityDeletionOrUpdateAdapter<ExerciseRecord>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `exercise_records` SET `id` = ?,`exerciseType` = ?,`durationMinutes` = ?,`caloriesBurned` = ?,`notes` = ?,`recordTime` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ExerciseRecord entity) {
        statement.bindString(1, entity.getId());
        final String _tmp = __converters.fromExerciseType(entity.getExerciseType());
        statement.bindString(2, _tmp);
        statement.bindLong(3, entity.getDurationMinutes());
        statement.bindLong(4, entity.getCaloriesBurned());
        if (entity.getNotes() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getNotes());
        }
        statement.bindLong(6, entity.getRecordTime());
        statement.bindString(7, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteRecordById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM exercise_records WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecord(final ExerciseRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfExerciseRecord.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteRecord(final ExerciseRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfExerciseRecord.handle(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateRecord(final ExerciseRecord record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfExerciseRecord.handle(record);
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
  public Flow<List<ExerciseRecord>> getAllRecords() {
    final String _sql = "SELECT * FROM exercise_records ORDER BY recordTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exercise_records"}, new Callable<List<ExerciseRecord>>() {
      @Override
      @NonNull
      public List<ExerciseRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseType = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final List<ExerciseRecord> _result = new ArrayList<ExerciseRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final ExerciseType _tmpExerciseType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfExerciseType);
            _tmpExerciseType = __converters.toExerciseType(_tmp);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            _item = new ExerciseRecord(_tmpId,_tmpExerciseType,_tmpDurationMinutes,_tmpCaloriesBurned,_tmpNotes,_tmpRecordTime);
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
  public Flow<List<ExerciseRecord>> getRecordsBetween(final long startTime, final long endTime) {
    final String _sql = "SELECT * FROM exercise_records WHERE recordTime >= ? AND recordTime < ? ORDER BY recordTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"exercise_records"}, new Callable<List<ExerciseRecord>>() {
      @Override
      @NonNull
      public List<ExerciseRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseType = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final List<ExerciseRecord> _result = new ArrayList<ExerciseRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final ExerciseType _tmpExerciseType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfExerciseType);
            _tmpExerciseType = __converters.toExerciseType(_tmp);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            _item = new ExerciseRecord(_tmpId,_tmpExerciseType,_tmpDurationMinutes,_tmpCaloriesBurned,_tmpNotes,_tmpRecordTime);
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
  public Object getRecordsBetweenSync(final long startTime, final long endTime,
      final Continuation<? super List<ExerciseRecord>> $completion) {
    final String _sql = "SELECT * FROM exercise_records WHERE recordTime >= ? AND recordTime < ? ORDER BY recordTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExerciseRecord>>() {
      @Override
      @NonNull
      public List<ExerciseRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseType = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final List<ExerciseRecord> _result = new ArrayList<ExerciseRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final ExerciseType _tmpExerciseType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfExerciseType);
            _tmpExerciseType = __converters.toExerciseType(_tmp);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            _item = new ExerciseRecord(_tmpId,_tmpExerciseType,_tmpDurationMinutes,_tmpCaloriesBurned,_tmpNotes,_tmpRecordTime);
            _result.add(_item);
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
  public Object getRecordById(final String id,
      final Continuation<? super ExerciseRecord> $completion) {
    final String _sql = "SELECT * FROM exercise_records WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ExerciseRecord>() {
      @Override
      @Nullable
      public ExerciseRecord call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseType = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final ExerciseRecord _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final ExerciseType _tmpExerciseType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfExerciseType);
            _tmpExerciseType = __converters.toExerciseType(_tmp);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            _result = new ExerciseRecord(_tmpId,_tmpExerciseType,_tmpDurationMinutes,_tmpCaloriesBurned,_tmpNotes,_tmpRecordTime);
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
  public Object getTotalCaloriesBurnedBetween(final long startTime, final long endTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT SUM(caloriesBurned) FROM exercise_records WHERE recordTime >= ? AND recordTime < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getTotalDurationBetween(final long startTime, final long endTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT SUM(durationMinutes) FROM exercise_records WHERE recordTime >= ? AND recordTime < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
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
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getMostFrequentExerciseTypes(
      final Continuation<? super List<ExerciseTypeCount>> $completion) {
    final String _sql = "SELECT exerciseType, COUNT(*) as count FROM exercise_records GROUP BY exerciseType ORDER BY count DESC LIMIT 5";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExerciseTypeCount>>() {
      @Override
      @NonNull
      public List<ExerciseTypeCount> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfExerciseType = 0;
          final int _cursorIndexOfCount = 1;
          final List<ExerciseTypeCount> _result = new ArrayList<ExerciseTypeCount>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseTypeCount _item;
            final ExerciseType _tmpExerciseType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfExerciseType);
            _tmpExerciseType = __converters.toExerciseType(_tmp);
            final int _tmpCount;
            _tmpCount = _cursor.getInt(_cursorIndexOfCount);
            _item = new ExerciseTypeCount(_tmpExerciseType,_tmpCount);
            _result.add(_item);
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
  public Object getAllRecordsOnce(final Continuation<? super List<ExerciseRecord>> $completion) {
    final String _sql = "SELECT * FROM exercise_records ORDER BY recordTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ExerciseRecord>>() {
      @Override
      @NonNull
      public List<ExerciseRecord> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfExerciseType = CursorUtil.getColumnIndexOrThrow(_cursor, "exerciseType");
          final int _cursorIndexOfDurationMinutes = CursorUtil.getColumnIndexOrThrow(_cursor, "durationMinutes");
          final int _cursorIndexOfCaloriesBurned = CursorUtil.getColumnIndexOrThrow(_cursor, "caloriesBurned");
          final int _cursorIndexOfNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "notes");
          final int _cursorIndexOfRecordTime = CursorUtil.getColumnIndexOrThrow(_cursor, "recordTime");
          final List<ExerciseRecord> _result = new ArrayList<ExerciseRecord>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ExerciseRecord _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final ExerciseType _tmpExerciseType;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfExerciseType);
            _tmpExerciseType = __converters.toExerciseType(_tmp);
            final int _tmpDurationMinutes;
            _tmpDurationMinutes = _cursor.getInt(_cursorIndexOfDurationMinutes);
            final int _tmpCaloriesBurned;
            _tmpCaloriesBurned = _cursor.getInt(_cursorIndexOfCaloriesBurned);
            final String _tmpNotes;
            if (_cursor.isNull(_cursorIndexOfNotes)) {
              _tmpNotes = null;
            } else {
              _tmpNotes = _cursor.getString(_cursorIndexOfNotes);
            }
            final long _tmpRecordTime;
            _tmpRecordTime = _cursor.getLong(_cursorIndexOfRecordTime);
            _item = new ExerciseRecord(_tmpId,_tmpExerciseType,_tmpDurationMinutes,_tmpCaloriesBurned,_tmpNotes,_tmpRecordTime);
            _result.add(_item);
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
