package com.lym.quietmind.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.lym.quietmind.backup.BackupEngine
import com.lym.quietmind.data.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class BackupState {
    object Idle : BackupState()
    object Loading : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private val backupEngine = BackupEngine(db)

    private val _backupState = MutableStateFlow<BackupState>(BackupState.Idle)
    val backupState: StateFlow<BackupState> = _backupState

    fun resetState() {
        _backupState.value = BackupState.Idle
    }

    fun exportDataToUri(uri: Uri) {
        _backupState.value = BackupState.Loading
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val outputStream = context.contentResolver.openOutputStream(uri)
                if (outputStream == null) {
                    _backupState.value = BackupState.Error("无法开启系统文件写入流！")
                    return@launch
                }
                
                val result = backupEngine.exportData(outputStream)
                if (result.isSuccess) {
                    _backupState.value = BackupState.Success("数据全量导出成功！")
                } else {
                    _backupState.value = BackupState.Error(result.exceptionOrNull()?.message ?: "导出失败")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "未知错误")
            }
        }
    }

    fun importDataFromUri(uri: Uri) {
        _backupState.value = BackupState.Loading
        viewModelScope.launch {
            try {
                val context = getApplication<Application>()
                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream == null) {
                    _backupState.value = BackupState.Error("无法开启系统文件读取流！请检查权限。")
                    return@launch
                }
                
                val result = backupEngine.importData(inputStream)
                if (result.isSuccess) {
                    val count = result.getOrNull() ?: 0
                    _backupState.value = BackupState.Success("成功导入覆盖了 $count 条统计算法记录！")
                } else {
                    _backupState.value = BackupState.Error(result.exceptionOrNull()?.message ?: "导入解析失败，该文件可能不是有效的 QuietMind 备份格式")
                }
            } catch (e: Exception) {
                _backupState.value = BackupState.Error(e.message ?: "未知错误")
            }
        }
    }
}
