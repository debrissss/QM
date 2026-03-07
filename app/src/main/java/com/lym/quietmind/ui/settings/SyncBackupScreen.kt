package com.lym.quietmind.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lym.quietmind.viewmodel.BackupState
import com.lym.quietmind.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SyncBackupScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val backupState by viewModel.backupState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Backup Launcher (Create Document)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportDataToUri(it) }
    }

    // Restore Launcher (Open Document)
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importDataFromUri(it) }
    }

    LaunchedEffect(backupState) {
        when (val state = backupState) {
            is BackupState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.resetState()
            }
            is BackupState.Error -> {
                snackbarHostState.showSnackbar("错误: ${state.message}")
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("同步与备份") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "本地备份",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "系统将生成包含所有打卡记录与核心指标算法计算结果的全量 JSON 备份包。如果您清除了应用缓存或更换了手机，可以通过此包完美还原全部数据。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { exportLauncher.launch("QuietMind_Backup.json") },
                modifier = Modifier.fillMaxWidth(),
                enabled = backupState !is BackupState.Loading
            ) {
                Text("导出本地备份")
            }

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "本地导入",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "警告：导入备份包后，您当前应用内的所有进度和统计数据都将被彻底清除收缩，并完全替换为备份文件的内容。",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer, 
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                ),
                enabled = backupState !is BackupState.Loading
            ) {
                Text("导入并覆盖数据")
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (backupState is BackupState.Loading) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("处理中...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
