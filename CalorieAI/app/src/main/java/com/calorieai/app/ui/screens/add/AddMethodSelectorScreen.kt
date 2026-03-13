package com.calorieai.app.ui.screens.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import androidx.compose.foundation.interaction.MutableInteractionSource

/**
 * 添加方式选择页面
 * 从加号按钮扩展至全屏的过渡动画
 * 包含：AI识别、手动录入、体重记录、运动添加
 */
@Composable
fun AddMethodSelectorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManual: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToWeight: () -> Unit = {},
    onNavigateToExercise: () -> Unit = {}
) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        visible = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                )
            )
            .clickable { onNavigateBack() }
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(animationSpec = tween(300)) +
                    expandIn(
                        animationSpec = tween(400),
                        expandFrom = Alignment.BottomCenter
                    ),
            exit = fadeOut(animationSpec = tween(200)) +
                   shrinkOut(animationSpec = tween(200))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "选择记录方式",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(48.dp))

                // AI录入选项
                MethodCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI 智能识别",
                    subtitle = "拍照或描述，AI自动识别热量",
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer
                    ),
                    onClick = onNavigateToAI
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 手动录入选项
                MethodCard(
                    icon = Icons.Default.Edit,
                    title = "手动录入",
                    subtitle = "手动输入食物名称和热量",
                    gradientColors = listOf(
                        MaterialTheme.colorScheme.secondary,
                        MaterialTheme.colorScheme.secondaryContainer
                    ),
                    onClick = onNavigateToManual
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 分隔线
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 32.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // 其他记录选项（小卡片）
                Text(
                    text = "其他记录",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // 体重记录和运动添加（横向排列）
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 体重记录
                    SmallMethodCard(
                        icon = Icons.Default.Scale,
                        title = "记录体重",
                        subtitle = "追踪体重变化",
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        onClick = onNavigateToWeight,
                        modifier = Modifier.weight(1f)
                    )

                    // 运动添加
                    SmallMethodCard(
                        icon = Icons.Default.FitnessCenter,
                        title = "添加运动",
                        subtitle = "记录运动消耗",
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        onClick = onNavigateToExercise,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // 取消按钮
                TextButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text(
                        text = "取消",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 小方式选择卡片（用于体重记录和运动添加）
 */
@Composable
private fun SmallMethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(16.dp))
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = containerColor.copy(alpha = 0.6f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 方式选择卡片
 */
@Composable
private fun MethodCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(24.dp))
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = RoundedCornerShape(24.dp),
                tint = Color.Transparent
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(gradientColors)
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 图标
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))

                // 文字
                Column {
                    Text(
                        text = title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
