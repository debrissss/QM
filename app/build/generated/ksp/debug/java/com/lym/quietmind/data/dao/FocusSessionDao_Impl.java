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
import com.lym.quietmind.data.Converters;
import com.lym.quietmind.data.entity.FocusSessionEntity;
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
public final class FocusSessionDao_Impl implements FocusSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<FocusSessionEntity> __insertionAdapterOfFocusSessionEntity;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public FocusSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfFocusSessionEntity = new EntityInsertionAdapter<FocusSessionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `focus_sessions` (`id`,`dayIndex`,`startTime`,`targetDuration`,`actualDuration`,`taskWeight`,`distractionCount`,`firstDistractionTime`,`interruptionReasons`,`endReason`,`fqs`,`efd`) VALUES (nullif(?, 0),?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final FocusSessionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getDayIndex());
        statement.bindLong(3, entity.getStartTime());
        statement.bindDouble(4, entity.getTargetDuration());
        statement.bindDouble(5, entity.getActualDuration());
        statement.bindDouble(6, entity.getTaskWeight());
        statement.bindLong(7, entity.getDistractionCount());
        statement.bindDouble(8, entity.getFirstDistractionTime());
        final String _tmp = __converters.fromStringList(entity.getInterruptionReasons());
        statement.bindString(9, _tmp);
        statement.bindString(10, entity.getEndReason());
        statement.bindDouble(11, entity.getFqs());
        statement.bindDouble(12, entity.getEfd());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM focus_sessions";
        return _query;
      }
    };
  }

  @Override
  public Object insertSession(final FocusSessionEntity session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfFocusSessionEntity.insert(session);
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
  public Flow<List<FocusSessionEntity>> getAllHistory() {
    final String _sql = "SELECT * FROM focus_sessions ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"focus_sessions"}, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "dayIndex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfTargetDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDuration");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfTaskWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "taskWeight");
          final int _cursorIndexOfDistractionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "distractionCount");
          final int _cursorIndexOfFirstDistractionTime = CursorUtil.getColumnIndexOrThrow(_cursor, "firstDistractionTime");
          final int _cursorIndexOfInterruptionReasons = CursorUtil.getColumnIndexOrThrow(_cursor, "interruptionReasons");
          final int _cursorIndexOfEndReason = CursorUtil.getColumnIndexOrThrow(_cursor, "endReason");
          final int _cursorIndexOfFqs = CursorUtil.getColumnIndexOrThrow(_cursor, "fqs");
          final int _cursorIndexOfEfd = CursorUtil.getColumnIndexOrThrow(_cursor, "efd");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayIndex;
            _tmpDayIndex = _cursor.getInt(_cursorIndexOfDayIndex);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final double _tmpTargetDuration;
            _tmpTargetDuration = _cursor.getDouble(_cursorIndexOfTargetDuration);
            final double _tmpActualDuration;
            _tmpActualDuration = _cursor.getDouble(_cursorIndexOfActualDuration);
            final double _tmpTaskWeight;
            _tmpTaskWeight = _cursor.getDouble(_cursorIndexOfTaskWeight);
            final int _tmpDistractionCount;
            _tmpDistractionCount = _cursor.getInt(_cursorIndexOfDistractionCount);
            final double _tmpFirstDistractionTime;
            _tmpFirstDistractionTime = _cursor.getDouble(_cursorIndexOfFirstDistractionTime);
            final List<String> _tmpInterruptionReasons;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfInterruptionReasons);
            _tmpInterruptionReasons = __converters.toStringList(_tmp);
            final String _tmpEndReason;
            _tmpEndReason = _cursor.getString(_cursorIndexOfEndReason);
            final double _tmpFqs;
            _tmpFqs = _cursor.getDouble(_cursorIndexOfFqs);
            final double _tmpEfd;
            _tmpEfd = _cursor.getDouble(_cursorIndexOfEfd);
            _item = new FocusSessionEntity(_tmpId,_tmpDayIndex,_tmpStartTime,_tmpTargetDuration,_tmpActualDuration,_tmpTaskWeight,_tmpDistractionCount,_tmpFirstDistractionTime,_tmpInterruptionReasons,_tmpEndReason,_tmpFqs,_tmpEfd);
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
  public Object getAllHistoryRaw(final Continuation<? super List<FocusSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM focus_sessions ORDER BY id DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "dayIndex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfTargetDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDuration");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfTaskWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "taskWeight");
          final int _cursorIndexOfDistractionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "distractionCount");
          final int _cursorIndexOfFirstDistractionTime = CursorUtil.getColumnIndexOrThrow(_cursor, "firstDistractionTime");
          final int _cursorIndexOfInterruptionReasons = CursorUtil.getColumnIndexOrThrow(_cursor, "interruptionReasons");
          final int _cursorIndexOfEndReason = CursorUtil.getColumnIndexOrThrow(_cursor, "endReason");
          final int _cursorIndexOfFqs = CursorUtil.getColumnIndexOrThrow(_cursor, "fqs");
          final int _cursorIndexOfEfd = CursorUtil.getColumnIndexOrThrow(_cursor, "efd");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayIndex;
            _tmpDayIndex = _cursor.getInt(_cursorIndexOfDayIndex);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final double _tmpTargetDuration;
            _tmpTargetDuration = _cursor.getDouble(_cursorIndexOfTargetDuration);
            final double _tmpActualDuration;
            _tmpActualDuration = _cursor.getDouble(_cursorIndexOfActualDuration);
            final double _tmpTaskWeight;
            _tmpTaskWeight = _cursor.getDouble(_cursorIndexOfTaskWeight);
            final int _tmpDistractionCount;
            _tmpDistractionCount = _cursor.getInt(_cursorIndexOfDistractionCount);
            final double _tmpFirstDistractionTime;
            _tmpFirstDistractionTime = _cursor.getDouble(_cursorIndexOfFirstDistractionTime);
            final List<String> _tmpInterruptionReasons;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfInterruptionReasons);
            _tmpInterruptionReasons = __converters.toStringList(_tmp);
            final String _tmpEndReason;
            _tmpEndReason = _cursor.getString(_cursorIndexOfEndReason);
            final double _tmpFqs;
            _tmpFqs = _cursor.getDouble(_cursorIndexOfFqs);
            final double _tmpEfd;
            _tmpEfd = _cursor.getDouble(_cursorIndexOfEfd);
            _item = new FocusSessionEntity(_tmpId,_tmpDayIndex,_tmpStartTime,_tmpTargetDuration,_tmpActualDuration,_tmpTaskWeight,_tmpDistractionCount,_tmpFirstDistractionTime,_tmpInterruptionReasons,_tmpEndReason,_tmpFqs,_tmpEfd);
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
  public Object getAllSessions(final Continuation<? super List<FocusSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM focus_sessions ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "dayIndex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfTargetDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDuration");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfTaskWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "taskWeight");
          final int _cursorIndexOfDistractionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "distractionCount");
          final int _cursorIndexOfFirstDistractionTime = CursorUtil.getColumnIndexOrThrow(_cursor, "firstDistractionTime");
          final int _cursorIndexOfInterruptionReasons = CursorUtil.getColumnIndexOrThrow(_cursor, "interruptionReasons");
          final int _cursorIndexOfEndReason = CursorUtil.getColumnIndexOrThrow(_cursor, "endReason");
          final int _cursorIndexOfFqs = CursorUtil.getColumnIndexOrThrow(_cursor, "fqs");
          final int _cursorIndexOfEfd = CursorUtil.getColumnIndexOrThrow(_cursor, "efd");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayIndex;
            _tmpDayIndex = _cursor.getInt(_cursorIndexOfDayIndex);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final double _tmpTargetDuration;
            _tmpTargetDuration = _cursor.getDouble(_cursorIndexOfTargetDuration);
            final double _tmpActualDuration;
            _tmpActualDuration = _cursor.getDouble(_cursorIndexOfActualDuration);
            final double _tmpTaskWeight;
            _tmpTaskWeight = _cursor.getDouble(_cursorIndexOfTaskWeight);
            final int _tmpDistractionCount;
            _tmpDistractionCount = _cursor.getInt(_cursorIndexOfDistractionCount);
            final double _tmpFirstDistractionTime;
            _tmpFirstDistractionTime = _cursor.getDouble(_cursorIndexOfFirstDistractionTime);
            final List<String> _tmpInterruptionReasons;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfInterruptionReasons);
            _tmpInterruptionReasons = __converters.toStringList(_tmp);
            final String _tmpEndReason;
            _tmpEndReason = _cursor.getString(_cursorIndexOfEndReason);
            final double _tmpFqs;
            _tmpFqs = _cursor.getDouble(_cursorIndexOfFqs);
            final double _tmpEfd;
            _tmpEfd = _cursor.getDouble(_cursorIndexOfEfd);
            _item = new FocusSessionEntity(_tmpId,_tmpDayIndex,_tmpStartTime,_tmpTargetDuration,_tmpActualDuration,_tmpTaskWeight,_tmpDistractionCount,_tmpFirstDistractionTime,_tmpInterruptionReasons,_tmpEndReason,_tmpFqs,_tmpEfd);
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
  public Object getSessionsByDay(final int dayIndex,
      final Continuation<? super List<FocusSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM focus_sessions WHERE dayIndex = ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, dayIndex);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "dayIndex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfTargetDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDuration");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfTaskWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "taskWeight");
          final int _cursorIndexOfDistractionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "distractionCount");
          final int _cursorIndexOfFirstDistractionTime = CursorUtil.getColumnIndexOrThrow(_cursor, "firstDistractionTime");
          final int _cursorIndexOfInterruptionReasons = CursorUtil.getColumnIndexOrThrow(_cursor, "interruptionReasons");
          final int _cursorIndexOfEndReason = CursorUtil.getColumnIndexOrThrow(_cursor, "endReason");
          final int _cursorIndexOfFqs = CursorUtil.getColumnIndexOrThrow(_cursor, "fqs");
          final int _cursorIndexOfEfd = CursorUtil.getColumnIndexOrThrow(_cursor, "efd");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayIndex;
            _tmpDayIndex = _cursor.getInt(_cursorIndexOfDayIndex);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final double _tmpTargetDuration;
            _tmpTargetDuration = _cursor.getDouble(_cursorIndexOfTargetDuration);
            final double _tmpActualDuration;
            _tmpActualDuration = _cursor.getDouble(_cursorIndexOfActualDuration);
            final double _tmpTaskWeight;
            _tmpTaskWeight = _cursor.getDouble(_cursorIndexOfTaskWeight);
            final int _tmpDistractionCount;
            _tmpDistractionCount = _cursor.getInt(_cursorIndexOfDistractionCount);
            final double _tmpFirstDistractionTime;
            _tmpFirstDistractionTime = _cursor.getDouble(_cursorIndexOfFirstDistractionTime);
            final List<String> _tmpInterruptionReasons;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfInterruptionReasons);
            _tmpInterruptionReasons = __converters.toStringList(_tmp);
            final String _tmpEndReason;
            _tmpEndReason = _cursor.getString(_cursorIndexOfEndReason);
            final double _tmpFqs;
            _tmpFqs = _cursor.getDouble(_cursorIndexOfFqs);
            final double _tmpEfd;
            _tmpEfd = _cursor.getDouble(_cursorIndexOfEfd);
            _item = new FocusSessionEntity(_tmpId,_tmpDayIndex,_tmpStartTime,_tmpTargetDuration,_tmpActualDuration,_tmpTaskWeight,_tmpDistractionCount,_tmpFirstDistractionTime,_tmpInterruptionReasons,_tmpEndReason,_tmpFqs,_tmpEfd);
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
  public Object getFirstWeekSessions(
      final Continuation<? super List<FocusSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM focus_sessions WHERE dayIndex BETWEEN 1 AND 7 ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "dayIndex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfTargetDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDuration");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfTaskWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "taskWeight");
          final int _cursorIndexOfDistractionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "distractionCount");
          final int _cursorIndexOfFirstDistractionTime = CursorUtil.getColumnIndexOrThrow(_cursor, "firstDistractionTime");
          final int _cursorIndexOfInterruptionReasons = CursorUtil.getColumnIndexOrThrow(_cursor, "interruptionReasons");
          final int _cursorIndexOfEndReason = CursorUtil.getColumnIndexOrThrow(_cursor, "endReason");
          final int _cursorIndexOfFqs = CursorUtil.getColumnIndexOrThrow(_cursor, "fqs");
          final int _cursorIndexOfEfd = CursorUtil.getColumnIndexOrThrow(_cursor, "efd");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayIndex;
            _tmpDayIndex = _cursor.getInt(_cursorIndexOfDayIndex);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final double _tmpTargetDuration;
            _tmpTargetDuration = _cursor.getDouble(_cursorIndexOfTargetDuration);
            final double _tmpActualDuration;
            _tmpActualDuration = _cursor.getDouble(_cursorIndexOfActualDuration);
            final double _tmpTaskWeight;
            _tmpTaskWeight = _cursor.getDouble(_cursorIndexOfTaskWeight);
            final int _tmpDistractionCount;
            _tmpDistractionCount = _cursor.getInt(_cursorIndexOfDistractionCount);
            final double _tmpFirstDistractionTime;
            _tmpFirstDistractionTime = _cursor.getDouble(_cursorIndexOfFirstDistractionTime);
            final List<String> _tmpInterruptionReasons;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfInterruptionReasons);
            _tmpInterruptionReasons = __converters.toStringList(_tmp);
            final String _tmpEndReason;
            _tmpEndReason = _cursor.getString(_cursorIndexOfEndReason);
            final double _tmpFqs;
            _tmpFqs = _cursor.getDouble(_cursorIndexOfFqs);
            final double _tmpEfd;
            _tmpEfd = _cursor.getDouble(_cursorIndexOfEfd);
            _item = new FocusSessionEntity(_tmpId,_tmpDayIndex,_tmpStartTime,_tmpTargetDuration,_tmpActualDuration,_tmpTaskWeight,_tmpDistractionCount,_tmpFirstDistractionTime,_tmpInterruptionReasons,_tmpEndReason,_tmpFqs,_tmpEfd);
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
  public Object getSessionsFromDay(final int startDayIndex,
      final Continuation<? super List<FocusSessionEntity>> $completion) {
    final String _sql = "SELECT * FROM focus_sessions WHERE dayIndex >= ? ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, startDayIndex);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<FocusSessionEntity>>() {
      @Override
      @NonNull
      public List<FocusSessionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfDayIndex = CursorUtil.getColumnIndexOrThrow(_cursor, "dayIndex");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfTargetDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "targetDuration");
          final int _cursorIndexOfActualDuration = CursorUtil.getColumnIndexOrThrow(_cursor, "actualDuration");
          final int _cursorIndexOfTaskWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "taskWeight");
          final int _cursorIndexOfDistractionCount = CursorUtil.getColumnIndexOrThrow(_cursor, "distractionCount");
          final int _cursorIndexOfFirstDistractionTime = CursorUtil.getColumnIndexOrThrow(_cursor, "firstDistractionTime");
          final int _cursorIndexOfInterruptionReasons = CursorUtil.getColumnIndexOrThrow(_cursor, "interruptionReasons");
          final int _cursorIndexOfEndReason = CursorUtil.getColumnIndexOrThrow(_cursor, "endReason");
          final int _cursorIndexOfFqs = CursorUtil.getColumnIndexOrThrow(_cursor, "fqs");
          final int _cursorIndexOfEfd = CursorUtil.getColumnIndexOrThrow(_cursor, "efd");
          final List<FocusSessionEntity> _result = new ArrayList<FocusSessionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final FocusSessionEntity _item;
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final int _tmpDayIndex;
            _tmpDayIndex = _cursor.getInt(_cursorIndexOfDayIndex);
            final long _tmpStartTime;
            _tmpStartTime = _cursor.getLong(_cursorIndexOfStartTime);
            final double _tmpTargetDuration;
            _tmpTargetDuration = _cursor.getDouble(_cursorIndexOfTargetDuration);
            final double _tmpActualDuration;
            _tmpActualDuration = _cursor.getDouble(_cursorIndexOfActualDuration);
            final double _tmpTaskWeight;
            _tmpTaskWeight = _cursor.getDouble(_cursorIndexOfTaskWeight);
            final int _tmpDistractionCount;
            _tmpDistractionCount = _cursor.getInt(_cursorIndexOfDistractionCount);
            final double _tmpFirstDistractionTime;
            _tmpFirstDistractionTime = _cursor.getDouble(_cursorIndexOfFirstDistractionTime);
            final List<String> _tmpInterruptionReasons;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfInterruptionReasons);
            _tmpInterruptionReasons = __converters.toStringList(_tmp);
            final String _tmpEndReason;
            _tmpEndReason = _cursor.getString(_cursorIndexOfEndReason);
            final double _tmpFqs;
            _tmpFqs = _cursor.getDouble(_cursorIndexOfFqs);
            final double _tmpEfd;
            _tmpEfd = _cursor.getDouble(_cursorIndexOfEfd);
            _item = new FocusSessionEntity(_tmpId,_tmpDayIndex,_tmpStartTime,_tmpTargetDuration,_tmpActualDuration,_tmpTaskWeight,_tmpDistractionCount,_tmpFirstDistractionTime,_tmpInterruptionReasons,_tmpEndReason,_tmpFqs,_tmpEfd);
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
