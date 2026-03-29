package com.calorieai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.calorieai.app.service.voice.VoiceState

/**
 * 语音输入对话框
 * 包含录音动画和实时波形效果
 */
@Composable
fun VoiceInputDialog(
    isVisible: Boolean,
    voiceState: VoiceState,
    onDismiss: () -> Unit,
    onStopRecording: () -> Unit,
    showDoneButton: Boolean = false,
    onDone: (() -> Unit)? = null
) {
    if (!isVisible) return

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 录音动画
                VoiceRecordingAnimation(voiceState = voiceState)

                Spacer(modifier = Modifier.height(24.dp))

                // 状态文字
                val statusText = when (voiceState) {
                    is VoiceState.Idle -> "准备录音..."
                    is VoiceState.Listening -> "正在聆听..."
                    is VoiceState.Processing -> "处理中..."
                    is VoiceState.Partial -> "识别中..."
                    is VoiceState.Success -> "识别成功"
                    is VoiceState.Error -> voiceState.message
                }

                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyLarge,
                    color = when (voiceState) {
                        is VoiceState.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurface
                    },
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 实时识别结果显示
                val displayText = when (voiceState) {
                    is VoiceState.Partial -> voiceState.text
                    is VoiceState.Success -> voiceState.text
                    else -> ""
                }
                
                if (displayText.isNotEmpty()) {
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // 提示文字
                Text(
                    text = when (voiceState) {
                        is VoiceState.Listening -> "请说出您吃的食物"
                        is VoiceState.Processing -> "正在识别语音内容"
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 停止录音按钮
                if (voiceState is VoiceState.Listening || voiceState is VoiceState.Processing) {
                    FilledIconButton(
                        onClick = onStopRecording,
                        modifier = Modifier.size(64.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "停止录音",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                if (showDoneButton && (voiceState is VoiceState.Success || voiceState is VoiceState.Partial)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { onDone?.invoke() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("完成")
                    }
                }
            }
        }
    }
}

/**
 * 录音动画组件
 * 包含脉冲波纹和波形效果
 */
@Composable
private fun VoiceRecordingAnimation(voiceState: VoiceState) {
    val isListening = voiceState is VoiceState.Listening
    val isProcessing = voiceState is VoiceState.Processing

    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.3f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = if (isListening) 0.3f else 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        // 外圈脉冲
        if (isListening) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = alpha),
                                MaterialTheme.colorScheme.primary.copy(alpha = 0f)
                            )
                        ),
                        shape = CircleShape
                    )
            )
        }

        // 中间圈
        Box(
            modifier = Modifier
                .size(90.dp)
                .background(
                    color = if (isListening) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else if (isProcessing) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant
                    },
                    shape = CircleShape
                )
        )

        // 内圈图标
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = when {
                        isListening -> MaterialTheme.colorScheme.primary
                        isProcessing -> MaterialTheme.colorScheme.secondary
                        voiceState is VoiceState.Error -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                isListening -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
                isProcessing -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        strokeWidth = 3.dp
                    )
                }
                voiceState is VoiceState.Error -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(32.dp)
                    )
                }
                else -> {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }

        // 波形动画（仅在录音时显示）
        if (isListening) {
            VoiceWaveform(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .align(Alignment.BottomCenter)
                    .offset(y = 20.dp)
            )
        }
    }
}

/**
 * 语音波形动画
 */
@Composable
private fun VoiceWaveform(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")

    // 多个条形的高度动画
    val barHeights = List(5) { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.3f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 400 + index * 100,
                    easing = EaseInOutCubic
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bar$index"
        )
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        barHeights.forEach { height ->
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(height.value)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    }
}

/**
 * 语音输入按钮
 * 带录音状态指示器
 */
@Composable
fun VoiceInputButton(
    isListening: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilledIconButton(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (isListening) {
                MaterialTheme.colorScheme.errorContainer
            } else {
                MaterialTheme.colorScheme.primaryContainer
            },
            contentColor = if (isListening) {
                MaterialTheme.colorScheme.onErrorContainer
            } else {
                MaterialTheme.colorScheme.onPrimaryContainer
            }
        )
    ) {
        Icon(
            imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = if (isListening) "停止录音" else "语音输入",
            modifier = Modifier.size(24.dp)
        )
    }
}
