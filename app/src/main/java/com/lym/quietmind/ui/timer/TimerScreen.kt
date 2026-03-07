package com.lym.quietmind.ui.timer

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.lym.quietmind.viewmodel.TimerStatus
import com.lym.quietmind.viewmodel.TimerViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TimerScreen(
    viewModel: TimerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as? Activity
    val view = LocalView.current

    // Keep screen on while actively focusing or pending
    DisposableEffect(uiState.status) {
        if (uiState.status == TimerStatus.FOCUSING || uiState.status == TimerStatus.PENDING_TURN) {
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        } else {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Full Black Overlay Logic for Focus Mode
    if (uiState.status == TimerStatus.FOCUSING) {
        SideEffect {
            // Drop brightness to 0.01 to mimic screen off (requires activity context)
            val config = activity?.window?.attributes
            config?.screenBrightness = 0.01f
            activity?.window?.attributes = config
            WindowCompat.getInsetsController(activity?.window!!, view).run {
                hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            }
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        )
        return // Render NOTHING else, just deep black.
    } else {
        // Restore screen brightness when not focusing
        SideEffect {
            val config = activity?.window?.attributes
            config?.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            activity?.window?.attributes = config
            WindowCompat.getInsetsController(activity?.window!!, view).run {
                show(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
            }
        }
    }

    // Normal Timer UI (Light Theme)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // The minimalist circular progress
            val targetSeconds = uiState.targetDurationMinutes * 60.0
            val progressRaw = (uiState.elapsedSeconds / targetSeconds).toFloat().coerceIn(0f, 1f)
            val animatedProgress by animateFloatAsState(
                targetValue = progressRaw,
                animationSpec = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
                label = "progressAnim"
            )

            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(240.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    strokeWidth = 12.dp,
                    strokeCap = StrokeCap.Round
                )
                
                // Content inside the circle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (uiState.status == TimerStatus.IDLE || uiState.status == TimerStatus.COMPLETED) {
                        Text(
                            text = "Target",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${uiState.targetDurationMinutes.toInt()} min",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (uiState.status == TimerStatus.PENDING_TURN) {
                        Text(
                            text = "倒扣手机启动",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.pendingCountdownSeconds}s",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else {
                        // Interrupted state
                        Text(
                            text = "中断记录",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Action Buttons
            when (uiState.status) {
                TimerStatus.IDLE -> {
                    Button(
                        onClick = { viewModel.startPending() },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("开始专注", fontSize = 18.sp)
                    }
                }
                TimerStatus.PENDING_TURN -> {
                    OutlinedButton(
                        onClick = { viewModel.resetTimer() },
                        modifier = Modifier.fillMaxWidth(0.6f)
                    ) {
                        Text("取消准备")
                    }
                }
                TimerStatus.COMPLETED -> {
                    Text(
                        "本次收获: FQS ${String.format("%.1f", uiState.fqsResult)} | EFD ${String.format("%.1f", uiState.efdResult)}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { viewModel.resetTimer() }) {
                        Text("完成总结")
                    }
                }
                else -> {}
            }
        }
    }

    // Interruption Dialog Modals
    if (uiState.status == TimerStatus.INTERRUPTED) {
        var interruptionReason by remember { mutableStateOf("") }
        var showEndConfirm by remember { mutableStateOf(false) }

        if (!showEndConfirm) {
            AlertDialog(
                onDismissRequest = { /* Must force explicit input */ },
                title = { Text("记录打断源") },
                text = {
                    Column {
                        Text("您的专注已被中断。请诚实记录走神原因以恢复计时：")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = interruptionReason,
                            onValueChange = { interruptionReason = it },
                            placeholder = { Text("例如：看微信信息、开小差") },
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        TextButton(
                            onClick = { showEndConfirm = true },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("提前结束专注")
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (interruptionReason.isNotBlank()) {
                                viewModel.submitInterruptionReasonAndResume(interruptionReason)
                            }
                        },
                        enabled = interruptionReason.isNotBlank()
                    ) {
                        Text("恢复潜航")
                    }
                }
            )
        } else {
            // End Reason confirmation
            var endReason by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showEndConfirm = false },
                title = { Text("总结并结束", color = MaterialTheme.colorScheme.error) },
                text = {
                    Column {
                        Text("您确定要提前结束本次记录吗？")
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = endReason,
                            onValueChange = { endReason = it },
                            placeholder = { Text("例如：任务已完成、太累了") },
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.endSessionAndSave(if (endReason.isBlank()) "提前结束" else endReason)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("结算提交")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showEndConfirm = false }) {
                        Text("返回")
                    }
                }
            )
        }
    }
}
