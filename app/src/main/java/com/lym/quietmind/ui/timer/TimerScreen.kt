package com.lym.quietmind.ui.timer

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
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
        Dialog(
            onDismissRequest = { /* Cannot be dismissed directly */ },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            val dialogWindow = (LocalView.current.parent as? DialogWindowProvider)?.window
            SideEffect {
                // Completely hide Dialog system bars and force black colors
                dialogWindow?.let { window ->
                    window.attributes = window.attributes.apply { 
                        screenBrightness = 0.01f 
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                    }
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = android.graphics.Color.BLACK
                    window.navigationBarColor = android.graphics.Color.BLACK
                    WindowCompat.getInsetsController(window, window.decorView).run {
                        hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
                // Also completely hide Activity system bars and force black colors
                activity?.window?.let { window ->
                    window.attributes = window.attributes.apply { 
                        screenBrightness = 0.01f 
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                        }
                    }
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.statusBarColor = android.graphics.Color.BLACK
                    window.navigationBarColor = android.graphics.Color.BLACK
                    WindowCompat.getInsetsController(window, view).run {
                        hide(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                        systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            )
        }
        return // Render NOTHING else, just deep black.
    } else {
        // Restore screen brightness and status bar colors when not focusing
        SideEffect {
            activity?.window?.let { window ->
                window.attributes = window.attributes.apply { 
                    screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE 
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
                    }
                }
                window.statusBarColor = android.graphics.Color.TRANSPARENT // Let theme handle it
                window.navigationBarColor = android.graphics.Color.TRANSPARENT // Let theme handle it
                WindowCompat.getInsetsController(window, view).run {
                    show(WindowInsetsCompat.Type.statusBars() or WindowInsetsCompat.Type.navigationBars())
                }
            }
        }
    }

    // Normal Timer UI (Light Theme)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.15f))

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
                    if (uiState.status == TimerStatus.ENTERTAINING) {
                        Text(
                            text = "放纵计时中",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        val mm = uiState.elapsedSeconds / 60
                        val ss = uiState.elapsedSeconds % 60
                        Text(
                            text = String.format("%02d:%02d", mm, ss),
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    } else if (uiState.status == TimerStatus.IDLE || uiState.status == TimerStatus.COMPLETED) {
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
                    BiDirectionalSlider(
                        onSlideLeftComplete = { viewModel.startPending() },
                        onSlideRightComplete = { viewModel.startEntertainment() },
                        onImpulseRestrained = { viewModel.recordImpulse() }
                    )
                }
                TimerStatus.ENTERTAINING -> {
                    Button(
                        onClick = { viewModel.stopEntertainment() },
                        modifier = Modifier.size(80.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Stop Entertainment",
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onError
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("点击停止并保存", color = MaterialTheme.colorScheme.error)
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

@Composable
fun BiDirectionalSlider(
    onSlideLeftComplete: () -> Unit,
    onSlideRightComplete: () -> Unit,
    onImpulseRestrained: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    var containerWidth by remember { mutableFloatStateOf(0f) }
    var thumbWidth by remember { mutableFloatStateOf(0f) }
    
    // Threshold is 80% of draggable distance
    val maxDrag = (containerWidth - thumbWidth) / 2f
    val threshold = maxDrag * 0.8f

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(64.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .onSizeChanged { containerWidth = it.width.toFloat() },
        contentAlignment = Alignment.Center
    ) {
        // Background Texts
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← 专注",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = "娱乐 →",
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        // Draggable Thumb
        Box(
            modifier = Modifier
                .offset(x = with(LocalDensity.current) { offsetX.value.toDp() })
                .size(56.dp)
                .onSizeChanged { thumbWidth = it.width.toFloat() }
                .shadow(4.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            coroutineScope.launch {
                                val currentX = offsetX.value
                                if (currentX <= -threshold) {
                                    // Trigger Left
                                    offsetX.animateTo(-maxDrag, spring())
                                    onSlideLeftComplete()
                                } else if (currentX >= threshold) {
                                    // Trigger Right
                                    offsetX.animateTo(maxDrag, spring())
                                    onSlideRightComplete()
                                } else {
                                    // Restrained Impulse -> Snapping back to center
                                    if (currentX > 20f) {
                                        // Count as impulse if they dragged it far enough right but let go
                                        onImpulseRestrained()
                                    }
                                    offsetX.animateTo(0f, spring(dampingRatio = 0.6f, stiffness = 400f))
                                }
                            }
                        },
                        onDragCancel = {
                            coroutineScope.launch {
                                offsetX.animateTo(0f, spring())
                            }
                        }
                    ) { change, dragAmount ->
                        change.consume()
                        coroutineScope.launch {
                            // Boundary clamp
                            val newX = (offsetX.value + dragAmount).coerceIn(-maxDrag, maxDrag)
                            offsetX.snapTo(newX)
                        }
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            // Thumb inner icon or styling
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
            )
        }
    }
}
