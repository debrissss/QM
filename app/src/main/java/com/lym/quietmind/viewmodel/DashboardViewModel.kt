package com.lym.quietmind.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lym.quietmind.data.AppDatabase
import com.lym.quietmind.data.entity.FocusSessionEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val currentTab: Int = 0, // 0=日, 1=周, 2=月
    val totalDuration: Double = 0.0,
    val avgDuration: Double = 0.0,
    val totalInterruptions: Int = 0,
    val avgInterruptions: Double = 0.0,
    val gnpsEndurance: Double = 0.0,
    val gnpsPurity: Double = 0.0,
    val gnpsResistance: Double = 0.0,
    val recentSessions: List<FocusSessionEntity> = emptyList()
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionDao = db.focusSessionDao()

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDataForTab(0)
    }

    fun refreshData() {
        loadDataForTab(_uiState.value.currentTab)
    }

    fun setTab(index: Int) {
        _uiState.value = _uiState.value.copy(currentTab = index)
        loadDataForTab(index)
    }

    private fun loadDataForTab(tabIndex: Int) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply { timeInMillis = now }

            val startTime = when (tabIndex) {
                0 -> {
                    // Daily (today starting from 00:00)
                    calendar.set(Calendar.HOUR_OF_DAY, 0)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.timeInMillis
                }
                1 -> {
                    // Weekly (last 7 days for simplicity)
                    calendar.add(Calendar.DAY_OF_YEAR, -7)
                    calendar.timeInMillis
                }
                else -> {
                    // Monthly (last 30 days)
                    calendar.add(Calendar.DAY_OF_YEAR, -30)
                    calendar.timeInMillis
                }
            }

            // Since we don't have a specific time query in DAO yet, let's pull all and filter or add DAO method.
            // For now, let's pull all from DAO (in real app add `SELECT * FROM ... WHERE startTime >= :start`)
            val allHistory = sessionDao.getAllHistoryRaw()
            
            val filtered = allHistory.filter { it.startTime >= startTime }

            val totalDur = filtered.sumOf { it.actualDuration }
            val avgDur = if (filtered.isEmpty()) 0.0 else totalDur / filtered.size
            val totalInt = filtered.sumOf { it.distractionCount }
            val avgInt = if (filtered.isEmpty()) 0.0 else totalInt.toDouble() / filtered.size

            // GNPS calculation abstraction for Dashboard View
            val efdTotal = filtered.sumOf { it.efd }
            val avgDr = if (filtered.isEmpty()) 0.0 else filtered.map { it.firstDistractionTime }.average()

            // Pseudo-GNPS parts for radar visualization
            val endurance = (efdTotal / 100.0).coerceAtMost(400.0) // simplified mock
            val density = if (totalDur > 0) totalInt / totalDur else 0.0
            val purity = ((1.0 - density).coerceIn(0.0, 1.0) * 300.0)
            val resistance = (avgDr * 10.0).coerceAtMost(300.0)

            _uiState.value = _uiState.value.copy(
                totalDuration = totalDur,
                avgDuration = avgDur,
                totalInterruptions = totalInt,
                avgInterruptions = avgInt,
                recentSessions = filtered,
                gnpsEndurance = endurance,
                gnpsPurity = purity,
                gnpsResistance = resistance
            )
        }
    }
}
