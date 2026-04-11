package com.calorieai.app.ui.screens.add

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.navigation.FeatureVisibilityViewModel
import com.calorieai.app.ui.feedback.rememberAppHapticController

@Composable
fun AddMethodSelectorScreen(
    onNavigateBack: () -> Unit,
    onNavigateToManual: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToFavoriteRecipes: () -> Unit = {},
    onNavigateToWeight: () -> Unit = {},
    onNavigateToExercise: () -> Unit = {},
    onNavigateToWaterHistory: () -> Unit = {},
    featureVisibilityViewModel: FeatureVisibilityViewModel = hiltViewModel()
) {
    val featureState by featureVisibilityViewModel.uiState.collectAsState()
    val haptics = rememberAppHapticController()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    BackHandler(enabled = visible) {
        visible = false
        onNavigateBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(280)) + slideInVertically(
                    animationSpec = tween(360, easing = EaseOutCubic),
                    initialOffsetY = { it / 2 }
                ),
                exit = fadeOut(tween(180)) + slideOutVertically(
                    animationSpec = tween(180),
                    targetOffsetY = { it / 2 }
                )
            ) {
                Text(
                    text = "添加记录",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(320, delayMillis = 80)),
                exit = fadeOut(tween(120))
            ) {
                Text(
                    text = "选择记录方式",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(34.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(360, delayMillis = 100)),
                exit = fadeOut(tween(120))
            ) {
                PrimaryMethodCard(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI 智能识别",
                    subtitle = "拍照或描述，AI 自动分析热量",
                    backgroundColor = MaterialTheme.colorScheme.primary,
                    onClick = {
                        haptics.confirm()
                        onNavigateToAI()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(360, delayMillis = 140)),
                exit = fadeOut(tween(120))
            ) {
                PrimaryMethodCard(
                    icon = Icons.Default.MenuBook,
                    title = "手动录入",
                    subtitle = "自己填写食物和营养数据",
                    backgroundColor = MaterialTheme.colorScheme.secondary,
                    onClick = {
                        haptics.click()
                        onNavigateToManual()
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(360, delayMillis = 180)),
                exit = fadeOut(tween(120))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryMethodCard(
                            icon = Icons.Default.MenuBook,
                            title = "菜谱",
                            subtitle = "快捷复用",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                haptics.click()
                                onNavigateToFavoriteRecipes()
                            },
                            isDark = isDark
                        )
                        SecondaryMethodCard(
                            icon = Icons.Default.Scale,
                            title = "体重",
                            subtitle = "记录体重",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                haptics.click()
                                onNavigateToWeight()
                            },
                            isDark = isDark
                        )
                    }

                    if (featureState.showWaterFeatures) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            SecondaryMethodCard(
                                icon = Icons.Default.FitnessCenter,
                                title = "运动",
                                subtitle = "添加运动",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    haptics.click()
                                    onNavigateToExercise()
                                },
                                isDark = isDark
                            )
                            SecondaryMethodCard(
                                icon = Icons.Default.WaterDrop,
                                title = "饮水",
                                subtitle = "记录饮水",
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    haptics.click()
                                    onNavigateToWaterHistory()
                                },
                                isDark = isDark
                            )
                        }
                    } else {
                        SecondaryMethodCard(
                            icon = Icons.Default.FitnessCenter,
                            title = "运动",
                            subtitle = "添加运动",
                            modifier = Modifier.fillMaxWidth(),
                            onClick = {
                                haptics.click()
                                onNavigateToExercise()
                            },
                            isDark = isDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            FilledTonalButton(
                onClick = {
                    haptics.click()
                    visible = false
                    onNavigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .height(48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "取消",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrimaryMethodCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "primary_card_scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
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
            Column {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun SecondaryMethodCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier,
    onClick: () -> Unit,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "secondary_card_scale"
    )

    val surfaceVariant = if (isDark) Color(0xFF2A2A2A) else Color(0xFFF5F5F5)
    val onSurfaceVariant = if (isDark) Color(0xFFB0B0B0) else Color(0xFF666666)

    Column(
        modifier = modifier
            .height(108.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(surfaceVariant)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = onSurfaceVariant.copy(alpha = 0.75f)
        )
    }
}
