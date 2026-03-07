package com.lym.quietmind.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * FocusSessionEntity：单次专注记录 (Data Layer)
 * 记录算法所需的所有原始指标。
 */
@Entity(tableName = "focus_sessions")
data class FocusSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    
    // 沙盒中的测试天数 (第 N 天)，未来可视情况逐步废弃，现阶段保留以向前兼容
    val dayIndex: Int,
    
    // 真实追踪时间 (Timestamp)
    val startTime: Long = System.currentTimeMillis(),
    
    // 基础输入指标
    val targetDuration: Double,       // 设定的目标时长 (T_target)
    val actualDuration: Double,       // 实际执行时长 (T_actual)
    val taskWeight: Double,           // 任务权重 (TaskWeight)
    val distractionCount: Int,        // 打断频率
    val firstDistractionTime: Double, // 首次打断发生时间 (若无打断等于 actualDuration)
    
    // 打断归因字典 (JSON List 字符串形式存储)
    val interruptionReasons: List<String> = emptyList(),
    
    // 结束原因
    val endReason: String = "completed",
    
    // 衍生的算法结果 (归档冗余存储便于快速查询)
    val fqs: Double,                  // 本次 FQS 分数
    val efd: Double                   // 本次 EFD 做功分数
)
