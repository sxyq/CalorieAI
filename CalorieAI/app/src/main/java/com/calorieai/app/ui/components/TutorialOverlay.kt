package com.calorieai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.calorieai.app.data.model.TutorialConfig
import com.calorieai.app.data.model.TutorialStep

/**
 * 引导教程遮罩层
 */
@Composable
fun TutorialOverlay(
    isVisible: Boolean,
    currentStep: Int,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit,
    highlightArea: @Composable (() -> Unit)? = null
) {
    if (!isVisible) return

    val step = TutorialConfig.steps.getOrNull(currentStep) ?: return
    val isLastStep = currentStep == TutorialConfig.steps.size - 1
    val isFirstStep = currentStep == 0

    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(enabled = false) { }
        ) {
            // 高亮区域（如果有）
            highlightArea?.invoke()

            // 教程内容卡片
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    slideInHorizontally { width -> width } + fadeIn() togetherWith
                    slideOutHorizontally { width -> -width } + fadeOut()
                },
                label = "TutorialStep"
            ) { stepIndex ->
                val currentStepData = TutorialConfig.steps[stepIndex]
                TutorialCard(
                    step = currentStepData,
                    currentStep = stepIndex,
                    totalSteps = TutorialConfig.steps.size,
                    isFirstStep = stepIndex == 0,
                    isLastStep = stepIndex == TutorialConfig.steps.size - 1,
                    onNext = onNext,
                    onPrevious = onPrevious,
                    onSkip = onSkip,
                    onComplete = onComplete
                )
            }

            // 跳过按钮（右上角）
            if (!isLastStep) {
                IconButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "跳过教程",
                        tint = Color.White
                    )
                }
            }

            // 步骤指示器（底部）
            StepIndicator(
                currentStep = currentStep,
                totalSteps = TutorialConfig.steps.size,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
            )
        }
    }
}

/**
 * 教程卡片
 */
@Composable
private fun TutorialCard(
    step: TutorialStep,
    currentStep: Int,
    totalSteps: Int,
    isFirstStep: Boolean,
    isLastStep: Boolean,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSkip: () -> Unit,
    onComplete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Emoji图标
                Text(
                    text = step.emoji,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 标题
                Text(
                    text = step.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 描述
                Text(
                    text = step.description,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 按钮区域
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // 上一步按钮（如果不是第一步）
                    if (!isFirstStep) {
                        OutlinedButton(
                            onClick = onPrevious
                        ) {
                            Text("上一步")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    // 下一步/完成按钮
                    if (isLastStep) {
                        Button(
                            onClick = onComplete,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("开始使用")
                        }
                    } else {
                        Button(
                            onClick = onNext,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("下一步")
                        }
                    }
                }

                // 跳过按钮（文字）
                if (!isLastStep) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = onSkip
                    ) {
                        Text(
                            "跳过教程",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * 步骤指示器
 */
@Composable
private fun StepIndicator(
    currentStep: Int,
    totalSteps: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(totalSteps) { index ->
            val isActive = index == currentStep
            val isCompleted = index < currentStep

            Box(
                modifier = Modifier
                    .width(if (isActive) 24.dp else 8.dp)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        when {
                            isActive -> MaterialTheme.colorScheme.primary
                            isCompleted -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            else -> Color.White.copy(alpha = 0.3f)
                        }
                    )
                    .animateContentSize()
            )
        }
    }
}

/**
 * 高亮框
 */
@Composable
fun HighlightBox(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
    ) {
        // 脉冲动画边框
        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
        val scale by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutCubic),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .scale(scale)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
        )

        content()
    }
}
