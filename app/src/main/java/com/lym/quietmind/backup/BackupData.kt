package com.lym.quietmind.backup

import com.lym.quietmind.data.entity.EntertainmentRecordEntity
import com.lym.quietmind.data.entity.FocusSessionEntity

data class BackupData(
    val focusSessions: List<FocusSessionEntity>,
    val entertainmentRecords: List<EntertainmentRecordEntity>
)
