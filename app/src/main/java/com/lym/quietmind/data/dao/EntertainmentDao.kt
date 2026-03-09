package com.lym.quietmind.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.lym.quietmind.data.entity.EntertainmentRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EntertainmentDao {

    @Insert
    suspend fun insertRecord(record: EntertainmentRecordEntity)

    // 获取所有娱乐及冲动历史数据
    @Query("SELECT * FROM entertainment_records ORDER BY startTime DESC")
    fun getAllHistory(): Flow<List<EntertainmentRecordEntity>>

    // 用于备份引擎的无包装数据流
    @Query("SELECT * FROM entertainment_records ORDER BY startTime ASC")
    suspend fun getAllHistoryRaw(): List<EntertainmentRecordEntity>

    // 快捷清空记录
    @Query("DELETE FROM entertainment_records")
    suspend fun clearAll()
}
