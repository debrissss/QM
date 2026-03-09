package com.lym.quietmind.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * EntertainmentRecordEntity：娱乐及冲动记录 (Data Layer)
 * 记录多巴胺释放行为（真正的娱乐或克制的冲动）。
 */
@Entity(tableName = "entertainment_records")
data class EntertainmentRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // 真实追踪时间 (Timestamp)
    val startTime: Long = System.currentTimeMillis(),
    
    // 记录的类型："SESSION" (确实参与了娱乐) 或 "IMPULSE" (产生了冲动但克制住了)
    val type: String,
    
    // 娱乐时长 (仅当 type == "SESSION" 时有意义，对 "IMPULSE" 则为 0)
    val durationSeconds: Long = 0L
)
