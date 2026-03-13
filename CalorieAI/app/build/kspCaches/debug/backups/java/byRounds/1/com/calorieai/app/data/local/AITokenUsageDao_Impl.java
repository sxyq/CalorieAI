package com.calorieai.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.calorieai.app.data.model.AITokenUsage;
import java.lang.Class;
import java.lang.Double;
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
public final class AITokenUsageDao_Impl implements AITokenUsageDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AITokenUsage> __insertionAdapterOfAITokenUsage;

  private final SharedSQLiteStatement __preparedStmtOfDeleteTokenUsageBefore;

  public AITokenUsageDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAITokenUsage = new EntityInsertionAdapter<AITokenUsage>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `ai_token_usage` (`id`,`timestamp`,`configId`,`configName`,`promptTokens`,`completionTokens`,`totalTokens`,`cost`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AITokenUsage entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getTimestamp());
        statement.bindString(3, entity.getConfigId());
        statement.bindString(4, entity.getConfigName());
        statement.bindLong(5, entity.getPromptTokens());
        statement.bindLong(6, entity.getCompletionTokens());
        statement.bindLong(7, entity.getTotalTokens());
        statement.bindDouble(8, entity.getCost());
      }
    };
    this.__preparedStmtOfDeleteTokenUsageBefore = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ai_token_usage WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertTokenUsage(final AITokenUsage tokenUsage,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAITokenUsage.insert(tokenUsage);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteTokenUsageBefore(final long beforeTime,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteTokenUsageBefore.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, beforeTime);
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
          __preparedStmtOfDeleteTokenUsageBefore.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AITokenUsage>> getAllTokenUsage() {
    final String _sql = "SELECT * FROM ai_token_usage ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ai_token_usage"}, new Callable<List<AITokenUsage>>() {
      @Override
      @NonNull
      public List<AITokenUsage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfConfigId = CursorUtil.getColumnIndexOrThrow(_cursor, "configId");
          final int _cursorIndexOfConfigName = CursorUtil.getColumnIndexOrThrow(_cursor, "configName");
          final int _cursorIndexOfPromptTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "promptTokens");
          final int _cursorIndexOfCompletionTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "completionTokens");
          final int _cursorIndexOfTotalTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTokens");
          final int _cursorIndexOfCost = CursorUtil.getColumnIndexOrThrow(_cursor, "cost");
          final List<AITokenUsage> _result = new ArrayList<AITokenUsage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AITokenUsage _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpConfigId;
            _tmpConfigId = _cursor.getString(_cursorIndexOfConfigId);
            final String _tmpConfigName;
            _tmpConfigName = _cursor.getString(_cursorIndexOfConfigName);
            final int _tmpPromptTokens;
            _tmpPromptTokens = _cursor.getInt(_cursorIndexOfPromptTokens);
            final int _tmpCompletionTokens;
            _tmpCompletionTokens = _cursor.getInt(_cursorIndexOfCompletionTokens);
            final int _tmpTotalTokens;
            _tmpTotalTokens = _cursor.getInt(_cursorIndexOfTotalTokens);
            final double _tmpCost;
            _tmpCost = _cursor.getDouble(_cursorIndexOfCost);
            _item = new AITokenUsage(_tmpId,_tmpTimestamp,_tmpConfigId,_tmpConfigName,_tmpPromptTokens,_tmpCompletionTokens,_tmpTotalTokens,_tmpCost);
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
  public Flow<List<AITokenUsage>> getTokenUsageBetween(final long startTime, final long endTime) {
    final String _sql = "SELECT * FROM ai_token_usage WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ai_token_usage"}, new Callable<List<AITokenUsage>>() {
      @Override
      @NonNull
      public List<AITokenUsage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfConfigId = CursorUtil.getColumnIndexOrThrow(_cursor, "configId");
          final int _cursorIndexOfConfigName = CursorUtil.getColumnIndexOrThrow(_cursor, "configName");
          final int _cursorIndexOfPromptTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "promptTokens");
          final int _cursorIndexOfCompletionTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "completionTokens");
          final int _cursorIndexOfTotalTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTokens");
          final int _cursorIndexOfCost = CursorUtil.getColumnIndexOrThrow(_cursor, "cost");
          final List<AITokenUsage> _result = new ArrayList<AITokenUsage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AITokenUsage _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpConfigId;
            _tmpConfigId = _cursor.getString(_cursorIndexOfConfigId);
            final String _tmpConfigName;
            _tmpConfigName = _cursor.getString(_cursorIndexOfConfigName);
            final int _tmpPromptTokens;
            _tmpPromptTokens = _cursor.getInt(_cursorIndexOfPromptTokens);
            final int _tmpCompletionTokens;
            _tmpCompletionTokens = _cursor.getInt(_cursorIndexOfCompletionTokens);
            final int _tmpTotalTokens;
            _tmpTotalTokens = _cursor.getInt(_cursorIndexOfTotalTokens);
            final double _tmpCost;
            _tmpCost = _cursor.getDouble(_cursorIndexOfCost);
            _item = new AITokenUsage(_tmpId,_tmpTimestamp,_tmpConfigId,_tmpConfigName,_tmpPromptTokens,_tmpCompletionTokens,_tmpTotalTokens,_tmpCost);
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
  public Object getTokenUsageBetweenSync(final long startTime, final long endTime,
      final Continuation<? super List<AITokenUsage>> $completion) {
    final String _sql = "SELECT * FROM ai_token_usage WHERE timestamp >= ? AND timestamp < ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AITokenUsage>>() {
      @Override
      @NonNull
      public List<AITokenUsage> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfConfigId = CursorUtil.getColumnIndexOrThrow(_cursor, "configId");
          final int _cursorIndexOfConfigName = CursorUtil.getColumnIndexOrThrow(_cursor, "configName");
          final int _cursorIndexOfPromptTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "promptTokens");
          final int _cursorIndexOfCompletionTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "completionTokens");
          final int _cursorIndexOfTotalTokens = CursorUtil.getColumnIndexOrThrow(_cursor, "totalTokens");
          final int _cursorIndexOfCost = CursorUtil.getColumnIndexOrThrow(_cursor, "cost");
          final List<AITokenUsage> _result = new ArrayList<AITokenUsage>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AITokenUsage _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpConfigId;
            _tmpConfigId = _cursor.getString(_cursorIndexOfConfigId);
            final String _tmpConfigName;
            _tmpConfigName = _cursor.getString(_cursorIndexOfConfigName);
            final int _tmpPromptTokens;
            _tmpPromptTokens = _cursor.getInt(_cursorIndexOfPromptTokens);
            final int _tmpCompletionTokens;
            _tmpCompletionTokens = _cursor.getInt(_cursorIndexOfCompletionTokens);
            final int _tmpTotalTokens;
            _tmpTotalTokens = _cursor.getInt(_cursorIndexOfTotalTokens);
            final double _tmpCost;
            _tmpCost = _cursor.getDouble(_cursorIndexOfCost);
            _item = new AITokenUsage(_tmpId,_tmpTimestamp,_tmpConfigId,_tmpConfigName,_tmpPromptTokens,_tmpCompletionTokens,_tmpTotalTokens,_tmpCost);
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
  public Object getTotalTokensBetween(final long startTime, final long endTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT SUM(totalTokens) FROM ai_token_usage WHERE timestamp >= ? AND timestamp < ?";
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
  public Object getPromptTokensBetween(final long startTime, final long endTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT SUM(promptTokens) FROM ai_token_usage WHERE timestamp >= ? AND timestamp < ?";
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
  public Object getCompletionTokensBetween(final long startTime, final long endTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT SUM(completionTokens) FROM ai_token_usage WHERE timestamp >= ? AND timestamp < ?";
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
  public Object getTotalCostBetween(final long startTime, final long endTime,
      final Continuation<? super Double> $completion) {
    final String _sql = "SELECT SUM(cost) FROM ai_token_usage WHERE timestamp >= ? AND timestamp < ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startTime);
    _argIndex = 2;
    _statement.bindLong(_argIndex, endTime);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Double>() {
      @Override
      @Nullable
      public Double call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Double _result;
          if (_cursor.moveToFirst()) {
            final Double _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getDouble(0);
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
  public Object getRequestCountBetween(final long startTime, final long endTime,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM ai_token_usage WHERE timestamp >= ? AND timestamp < ?";
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
