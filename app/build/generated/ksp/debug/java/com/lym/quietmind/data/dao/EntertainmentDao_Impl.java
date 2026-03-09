package com.lym.quietmind.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.lym.quietmind.data.entity.EntertainmentRecordEntity;
import java.lang.Class;
import java.lang.Exception;
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
public final class EntertainmentDao_Impl implements EntertainmentDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<EntertainmentRecordEntity> __insertionAdapterOfEntertainmentRecordEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public EntertainmentDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfEntertainmentRecordEntity = new EntityInsertionAdapter<EntertainmentRecordEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `entertainment_records` (`id`,`startTime`,`type`,`durationSeconds`) VALUES (nullif(?, 0),?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final EntertainmentRecordEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getStartTime());
        statement.bindString(3, entity.getType());
        statement.bindLong(4, entity.getDurationSeconds());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM entertainment_records";
        return _query;
      }
    };
  }

  @Override
  public Object insertRecord(final EntertainmentRecordEntity record,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfEntertainmentRecordEntity.insert(record);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<EntertainmentRecordEntity>> getAllHistory() {
    final String _sql = "SELECT * FROM entertainment_records ORDER BY startTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"entertainment_records"}, new Callable<List<EntertainmentRecordEntity>>() {
      @Override
      @NonNull
      public List<EntertainmentRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final List<EntertainmentRecordEntity> _result = new ArrayList<EntertainmentRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EntertainmentRecordEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            _item = new EntertainmentRecordEntity(_tmpId,_tmpStartTime,_tmpType,_tmpDurationSeconds);
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
  public Object getAllHistoryRaw(
      final Continuation<? super List<EntertainmentRecordEntity>> $completion) {
    final String _sql = "SELECT * FROM entertainment_records ORDER BY startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<EntertainmentRecordEntity>>() {
      @Override
      @NonNull
      public List<EntertainmentRecordEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final List<EntertainmentRecordEntity> _result = new ArrayList<EntertainmentRecordEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final EntertainmentRecordEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final String _tmpType;
            _tmpType = _cursor.getString(_cursorIndexOfType);
            final long _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getLong(_cursorIndexOfDurationSeconds);
            _item = new EntertainmentRecordEntity(_tmpId,_tmpStartTime,_tmpType,_tmpDurationSeconds);
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
