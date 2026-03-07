package com.lym.quietmind.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lym.quietmind.data.AppDatabase
import com.lym.quietmind.data.entity.FocusSessionEntity
import com.lym.quietmind.domain.FocusAlgorithms
import com.lym.quietmind.sensor.DeviceOrientationTracker
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class TimerStatus {
    IDLE,              // 空闲，等待启动
    PENDING_TURN,      // 已启动，等待 60 秒内将手机倒扣
    FOCUSING,          // 专注中，处于黑屏状态，静默计时
    INTERRUPTED,       // 专注被意外打断（手机被翻起），等待输入原因
    COMPLETED          // 正常完成（也可以在 INTERRUPTED 状态强制终结）
}

data class TimerUiState(
    val status: TimerStatus = TimerStatus.IDLE,
    val targetDurationMinutes: Double = 60.0,
    val elapsedSeconds: Long = 0L,         // 已经专注的有效秒数
    val pendingCountdownSeconds: Int = 60, // 启动后等待翻转的倒数
    val interruptionCount: Int = 0,
    val firstInterruptionTimeMinutes: Double? = null,
    val currentInterruptionsLog: List<String> = emptyList(),
    val fqsResult: Double = 0.0,
    val efdResult: Double = 0.0
)

class TimerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val sessionDao = db.focusSessionDao()
    private val orientationTracker = DeviceOrientationTracker(application)

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var orientationJob: Job? = null
    private var timerJob: Job? = null
    private var pendingCountdownJob: Job? = null

    init {
        // Initial setup: pull the latest base target from history
        viewModelScope.launch {
            // For now, default to 60.0, but in real app we'd query history and FocusAlgorithms.calculateDailyBaseTarget
            _uiState.value = _uiState.value.copy(targetDurationMinutes = 60.0)
        }
    }

    fun startPending() {
        if (_uiState.value.status != TimerStatus.IDLE) return
        _uiState.value = _uiState.value.copy(
            status = TimerStatus.PENDING_TURN,
            pendingCountdownSeconds = 60,
            elapsedSeconds = 0,
            interruptionCount = 0,
            firstInterruptionTimeMinutes = null,
            currentInterruptionsLog = emptyList()
        )
        startPendingCountdown()
        observeDeviceOrientation()
    }

    private fun startPendingCountdown() {
        pendingCountdownJob?.cancel()
        pendingCountdownJob = viewModelScope.launch {
            while (_uiState.value.status == TimerStatus.PENDING_TURN && _uiState.value.pendingCountdownSeconds > 0) {
                delay(1000L)
                _uiState.value = _uiState.value.copy(
                    pendingCountdownSeconds = _uiState.value.pendingCountdownSeconds - 1
                )
            }
            if (_uiState.value.status == TimerStatus.PENDING_TURN && _uiState.value.pendingCountdownSeconds <= 0) {
                // Failed to turn device down in time
                resetTimer()
            }
        }
    }

    private fun observeDeviceOrientation() {
        orientationJob?.cancel()
        orientationJob = viewModelScope.launch {
            orientationTracker.getFaceDownFlow().collect { isFaceDown ->
                val state = _uiState.value
                when (state.status) {
                    TimerStatus.PENDING_TURN -> {
                        if (isFaceDown) {
                            // Device flipped down successfully, start real focusing!
                            pendingCountdownJob?.cancel()
                            switchToFocusing()
                        }
                    }
                    TimerStatus.FOCUSING -> {
                        if (!isFaceDown) {
                            // User picked up the device! INTERRUPT
                            interruptFocusing()
                        }
                    }
                    TimerStatus.INTERRUPTED -> {
                        if (isFaceDown) {
                            // User flipped the device back down WITHOUT explicitly clicking "Resume"
                            // We can auto-resume if desired, or require explicit button press.
                            // Let's require explicit UI action to resume.
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    private fun switchToFocusing() {
        _uiState.value = _uiState.value.copy(status = TimerStatus.FOCUSING)
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.status == TimerStatus.FOCUSING) {
                delay(1000L)
                val newElapsed = _uiState.value.elapsedSeconds + 1
                _uiState.value = _uiState.value.copy(elapsedSeconds = newElapsed)
                
                // If we surpassed target (we just continue counting, encourage over-performing)
                // We don't auto-stop in QuietMind philosophy. We let them go as long as they want until they flip it up.
            }
        }
    }

    private fun interruptFocusing() {
        timerJob?.cancel()
        val currentMinutes = _uiState.value.elapsedSeconds / 60.0
        val firstDistraction = _uiState.value.firstInterruptionTimeMinutes ?: currentMinutes
        
        _uiState.value = _uiState.value.copy(
            status = TimerStatus.INTERRUPTED,
            interruptionCount = _uiState.value.interruptionCount + 1,
            firstInterruptionTimeMinutes = firstDistraction
        )
    }

    fun submitInterruptionReasonAndResume(reason: String) {
        val updatedLog = _uiState.value.currentInterruptionsLog + reason
        _uiState.value = _uiState.value.copy(
            currentInterruptionsLog = updatedLog,
            status = TimerStatus.PENDING_TURN,
            pendingCountdownSeconds = 10 // Give them 10s to flip it back down
        )
        startPendingCountdown()
    }

    fun endSessionAndSave(endReason: String) {
        timerJob?.cancel()
        orientationJob?.cancel()
        pendingCountdownJob?.cancel()
        
        val state = _uiState.value
        val actualMins = state.elapsedSeconds / 60.0
        
        // Let's calculate the stats!
        val fqs = FocusAlgorithms.calculateFQS(
            actualDuration = actualMins,
            targetDuration = state.targetDurationMinutes,
            distractionCount = state.interruptionCount
        )
        
        val efd = FocusAlgorithms.calculateEFD(
            actualDuration = actualMins,
            fqs = fqs,
            taskWeight = 2.5 // using default weight right now
        )

        _uiState.value = state.copy(
            status = TimerStatus.COMPLETED,
            fqsResult = fqs,
            efdResult = efd
        )

        val finalFirstDistraction = state.firstInterruptionTimeMinutes ?: actualMins

        // Save to DB
        viewModelScope.launch {
            sessionDao.insertSession(
                FocusSessionEntity(
                    dayIndex = 0, // Migrating to timestamp
                    targetDuration = state.targetDurationMinutes,
                    actualDuration = actualMins,
                    taskWeight = 2.5,
                    distractionCount = state.interruptionCount,
                    firstDistractionTime = finalFirstDistraction,
                    interruptionReasons = state.currentInterruptionsLog,
                    endReason = endReason,
                    fqs = fqs,
                    efd = efd
                )
            )
        }
    }

    fun resetTimer() {
        timerJob?.cancel()
        orientationJob?.cancel()
        pendingCountdownJob?.cancel()
        _uiState.value = TimerUiState(targetDurationMinutes = _uiState.value.targetDurationMinutes)
    }
}
