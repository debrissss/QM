package com.lym.quietmind.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lym.quietmind.data.entity.FocusSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FocusSessionDao {

    @Insert
    suspend fun insertSession(session: FocusSessionEntity)

    // 获取所有专注记录
    @Query("SELECT * FROM focus_sessions ORDER BY id ASC")
    fun getAllHistory(): Flow<List<FocusSessionEntity>>

    @Query("SELECT * FROM focus_sessions ORDER BY id DESC")
    suspend fun getAllHistoryRaw(): List<FocusSessionEntity>
    
    @Query("SELECT * FROM focus_sessions ORDER BY id ASC")
    suspend fun getAllSessions(): List<FocusSessionEntity>

    // 获取特定某一天的所有记录
    @Query("SELECT * FROM focus_sessions WHERE dayIndex = :dayIndex ORDER BY id ASC")
    suspend fun getSessionsByDay(dayIndex: Int): List<FocusSessionEntity>

    // 获取第 1 天到第 7 天的记录 (用于模拟基准周 Endurance 计算)
    @Query("SELECT * FROM focus_sessions WHERE dayIndex BETWEEN 1 AND 7 ORDER BY id ASC")
    suspend fun getFirstWeekSessions(): List<FocusSessionEntity>

    // 获取最近 N 天的记录。传入当前天数 day - N。这里我们只是获取那些大于等于某个阈值的天的所有 session
    @Query("SELECT * FROM focus_sessions WHERE dayIndex >= :startDayIndex ORDER BY id ASC")
    suspend fun getSessionsFromDay(startDayIndex: Int): List<FocusSessionEntity>

    // 快捷清空 (沙盒调试用)
    @Query("DELETE FROM focus_sessions")
    suspend fun clearAll()
}
