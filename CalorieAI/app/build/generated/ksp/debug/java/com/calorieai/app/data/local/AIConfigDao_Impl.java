package com.calorieai.app.data.local;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.calorieai.app.data.model.AIConfig;
import com.calorieai.app.data.model.AIProtocol;
import com.calorieai.app.data.model.IconType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalArgumentException;
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
public final class AIConfigDao_Impl implements AIConfigDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AIConfig> __insertionAdapterOfAIConfig;

  private final EntityDeletionOrUpdateAdapter<AIConfig> __deletionAdapterOfAIConfig;

  private final EntityDeletionOrUpdateAdapter<AIConfig> __updateAdapterOfAIConfig;

  private final SharedSQLiteStatement __preparedStmtOfDeleteConfigById;

  private final SharedSQLiteStatement __preparedStmtOfClearDefaultConfig;

  private final SharedSQLiteStatement __preparedStmtOfSetDefaultConfig;

  public AIConfigDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAIConfig = new EntityInsertionAdapter<AIConfig>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `ai_configs` (`id`,`name`,`icon`,`iconType`,`protocol`,`apiUrl`,`apiKey`,`modelId`,`isImageUnderstanding`,`isDefault`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AIConfig entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIcon());
        statement.bindString(4, __IconType_enumToString(entity.getIconType()));
        statement.bindString(5, __AIProtocol_enumToString(entity.getProtocol()));
        statement.bindString(6, entity.getApiUrl());
        statement.bindString(7, entity.getApiKey());
        statement.bindString(8, entity.getModelId());
        final int _tmp = entity.isImageUnderstanding() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isDefault() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
      }
    };
    this.__deletionAdapterOfAIConfig = new EntityDeletionOrUpdateAdapter<AIConfig>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `ai_configs` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AIConfig entity) {
        statement.bindString(1, entity.getId());
      }
    };
    this.__updateAdapterOfAIConfig = new EntityDeletionOrUpdateAdapter<AIConfig>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `ai_configs` SET `id` = ?,`name` = ?,`icon` = ?,`iconType` = ?,`protocol` = ?,`apiUrl` = ?,`apiKey` = ?,`modelId` = ?,`isImageUnderstanding` = ?,`isDefault` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AIConfig entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getIcon());
        statement.bindString(4, __IconType_enumToString(entity.getIconType()));
        statement.bindString(5, __AIProtocol_enumToString(entity.getProtocol()));
        statement.bindString(6, entity.getApiUrl());
        statement.bindString(7, entity.getApiKey());
        statement.bindString(8, entity.getModelId());
        final int _tmp = entity.isImageUnderstanding() ? 1 : 0;
        statement.bindLong(9, _tmp);
        final int _tmp_1 = entity.isDefault() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        statement.bindString(11, entity.getId());
      }
    };
    this.__preparedStmtOfDeleteConfigById = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM ai_configs WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearDefaultConfig = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE ai_configs SET isDefault = 0";
        return _query;
      }
    };
    this.__preparedStmtOfSetDefaultConfig = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE ai_configs SET isDefault = 1 WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertConfig(final AIConfig config, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAIConfig.insert(config);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteConfig(final AIConfig config, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfAIConfig.handle(config);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateConfig(final AIConfig config, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAIConfig.handle(config);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object setDefaultConfigExclusive(final String id,
      final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> AIConfigDao.DefaultImpls.setDefaultConfigExclusive(AIConfigDao_Impl.this, id, __cont), $completion);
  }

  @Override
  public Object deleteConfigById(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteConfigById.acquire();
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
          __preparedStmtOfDeleteConfigById.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearDefaultConfig(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearDefaultConfig.acquire();
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
          __preparedStmtOfClearDefaultConfig.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object setDefaultConfig(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfSetDefaultConfig.acquire();
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
          __preparedStmtOfSetDefaultConfig.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<AIConfig>> getAllConfigs() {
    final String _sql = "SELECT * FROM ai_configs";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ai_configs"}, new Callable<List<AIConfig>>() {
      @Override
      @NonNull
      public List<AIConfig> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "icon");
          final int _cursorIndexOfIconType = CursorUtil.getColumnIndexOrThrow(_cursor, "iconType");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfApiUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "apiUrl");
          final int _cursorIndexOfApiKey = CursorUtil.getColumnIndexOrThrow(_cursor, "apiKey");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfIsImageUnderstanding = CursorUtil.getColumnIndexOrThrow(_cursor, "isImageUnderstanding");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "isDefault");
          final List<AIConfig> _result = new ArrayList<AIConfig>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AIConfig _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIcon;
            _tmpIcon = _cursor.getString(_cursorIndexOfIcon);
            final IconType _tmpIconType;
            _tmpIconType = __IconType_stringToEnum(_cursor.getString(_cursorIndexOfIconType));
            final AIProtocol _tmpProtocol;
            _tmpProtocol = __AIProtocol_stringToEnum(_cursor.getString(_cursorIndexOfProtocol));
            final String _tmpApiUrl;
            _tmpApiUrl = _cursor.getString(_cursorIndexOfApiUrl);
            final String _tmpApiKey;
            _tmpApiKey = _cursor.getString(_cursorIndexOfApiKey);
            final String _tmpModelId;
            _tmpModelId = _cursor.getString(_cursorIndexOfModelId);
            final boolean _tmpIsImageUnderstanding;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsImageUnderstanding);
            _tmpIsImageUnderstanding = _tmp != 0;
            final boolean _tmpIsDefault;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp_1 != 0;
            _item = new AIConfig(_tmpId,_tmpName,_tmpIcon,_tmpIconType,_tmpProtocol,_tmpApiUrl,_tmpApiKey,_tmpModelId,_tmpIsImageUnderstanding,_tmpIsDefault);
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
  public Flow<AIConfig> getDefaultConfig() {
    final String _sql = "SELECT * FROM ai_configs WHERE isDefault = 1 LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"ai_configs"}, new Callable<AIConfig>() {
      @Override
      @Nullable
      public AIConfig call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "icon");
          final int _cursorIndexOfIconType = CursorUtil.getColumnIndexOrThrow(_cursor, "iconType");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfApiUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "apiUrl");
          final int _cursorIndexOfApiKey = CursorUtil.getColumnIndexOrThrow(_cursor, "apiKey");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfIsImageUnderstanding = CursorUtil.getColumnIndexOrThrow(_cursor, "isImageUnderstanding");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "isDefault");
          final AIConfig _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIcon;
            _tmpIcon = _cursor.getString(_cursorIndexOfIcon);
            final IconType _tmpIconType;
            _tmpIconType = __IconType_stringToEnum(_cursor.getString(_cursorIndexOfIconType));
            final AIProtocol _tmpProtocol;
            _tmpProtocol = __AIProtocol_stringToEnum(_cursor.getString(_cursorIndexOfProtocol));
            final String _tmpApiUrl;
            _tmpApiUrl = _cursor.getString(_cursorIndexOfApiUrl);
            final String _tmpApiKey;
            _tmpApiKey = _cursor.getString(_cursorIndexOfApiKey);
            final String _tmpModelId;
            _tmpModelId = _cursor.getString(_cursorIndexOfModelId);
            final boolean _tmpIsImageUnderstanding;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsImageUnderstanding);
            _tmpIsImageUnderstanding = _tmp != 0;
            final boolean _tmpIsDefault;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp_1 != 0;
            _result = new AIConfig(_tmpId,_tmpName,_tmpIcon,_tmpIconType,_tmpProtocol,_tmpApiUrl,_tmpApiKey,_tmpModelId,_tmpIsImageUnderstanding,_tmpIsDefault);
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
  public Object getConfigById(final String id, final Continuation<? super AIConfig> $completion) {
    final String _sql = "SELECT * FROM ai_configs WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AIConfig>() {
      @Override
      @Nullable
      public AIConfig call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfIcon = CursorUtil.getColumnIndexOrThrow(_cursor, "icon");
          final int _cursorIndexOfIconType = CursorUtil.getColumnIndexOrThrow(_cursor, "iconType");
          final int _cursorIndexOfProtocol = CursorUtil.getColumnIndexOrThrow(_cursor, "protocol");
          final int _cursorIndexOfApiUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "apiUrl");
          final int _cursorIndexOfApiKey = CursorUtil.getColumnIndexOrThrow(_cursor, "apiKey");
          final int _cursorIndexOfModelId = CursorUtil.getColumnIndexOrThrow(_cursor, "modelId");
          final int _cursorIndexOfIsImageUnderstanding = CursorUtil.getColumnIndexOrThrow(_cursor, "isImageUnderstanding");
          final int _cursorIndexOfIsDefault = CursorUtil.getColumnIndexOrThrow(_cursor, "isDefault");
          final AIConfig _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpIcon;
            _tmpIcon = _cursor.getString(_cursorIndexOfIcon);
            final IconType _tmpIconType;
            _tmpIconType = __IconType_stringToEnum(_cursor.getString(_cursorIndexOfIconType));
            final AIProtocol _tmpProtocol;
            _tmpProtocol = __AIProtocol_stringToEnum(_cursor.getString(_cursorIndexOfProtocol));
            final String _tmpApiUrl;
            _tmpApiUrl = _cursor.getString(_cursorIndexOfApiUrl);
            final String _tmpApiKey;
            _tmpApiKey = _cursor.getString(_cursorIndexOfApiKey);
            final String _tmpModelId;
            _tmpModelId = _cursor.getString(_cursorIndexOfModelId);
            final boolean _tmpIsImageUnderstanding;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsImageUnderstanding);
            _tmpIsImageUnderstanding = _tmp != 0;
            final boolean _tmpIsDefault;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsDefault);
            _tmpIsDefault = _tmp_1 != 0;
            _result = new AIConfig(_tmpId,_tmpName,_tmpIcon,_tmpIconType,_tmpProtocol,_tmpApiUrl,_tmpApiKey,_tmpModelId,_tmpIsImageUnderstanding,_tmpIsDefault);
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

  private String __IconType_enumToString(@NonNull final IconType _value) {
    switch (_value) {
      case EMOJI: return "EMOJI";
      case RESOURCE: return "RESOURCE";
      case URL: return "URL";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private String __AIProtocol_enumToString(@NonNull final AIProtocol _value) {
    switch (_value) {
      case OPENAI: return "OPENAI";
      case CLAUDE: return "CLAUDE";
      case KIMI: return "KIMI";
      case GLM: return "GLM";
      case QWEN: return "QWEN";
      case DEEPSEEK: return "DEEPSEEK";
      case GEMINI: return "GEMINI";
      default: throw new IllegalArgumentException("Can't convert enum to string, unknown enum value: " + _value);
    }
  }

  private IconType __IconType_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "EMOJI": return IconType.EMOJI;
      case "RESOURCE": return IconType.RESOURCE;
      case "URL": return IconType.URL;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }

  private AIProtocol __AIProtocol_stringToEnum(@NonNull final String _value) {
    switch (_value) {
      case "OPENAI": return AIProtocol.OPENAI;
      case "CLAUDE": return AIProtocol.CLAUDE;
      case "KIMI": return AIProtocol.KIMI;
      case "GLM": return AIProtocol.GLM;
      case "QWEN": return AIProtocol.QWEN;
      case "DEEPSEEK": return AIProtocol.DEEPSEEK;
      case "GEMINI": return AIProtocol.GEMINI;
      default: throw new IllegalArgumentException("Can't convert value to enum, unknown value: " + _value);
    }
  }
}
