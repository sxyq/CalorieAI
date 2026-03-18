package com.calorieai.app.ui.components.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.*
import kotlinx.coroutines.delay

/**
 * AI助手形态枚举
 */
enum class AIAvatarState {
    COLLAPSED,
    EXPANDING,
    EXPANDED,
    MINIMIZED,
    THINKING
}

/**
 * AI助手头像组件 - Glass 毛玻璃风格
 */
@Composable
fun AIAvatar(
    state: AIAvatarState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isThinking: Boolean = false,
    hasNewMessage: Boolean = false,
    isDark: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val primaryContainerColor = if (isDark) GlassDarkColors.PrimaryContainer else GlassLightColors.PrimaryContainer
    val onPrimaryColor = if (isDark) GlassDarkColors.OnPrimary else GlassLightColors.OnPrimary
    
    // 动画值
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            state == AIAvatarState.THINKING -> 1.1f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "avatarScale"
    )
    
    val rotation by animateFloatAsState(
        targetValue = if (state == AIAvatarState.THINKING) 360f else 0f,
        animationSpec = if (state == AIAvatarState.THINKING) {
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        } else {
            tween(300)
        },
        label = "avatarRotation"
    )
    
    val pulseAnimation by animateFloatAsState(
        targetValue = if (state == AIAvatarState.THINKING) 1.3f else 1f,
        animationSpec = if (state == AIAvatarState.THINKING) {
            infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(300)
        },
        label = "avatarPulse"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { rotationZ = rotation }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        // 脉冲光环（思考状态）
        if (state == AIAvatarState.THINKING) {
            Box(
                modifier = Modifier
                    .size(80.dp * pulseAnimation)
                    .background(
                        color = primaryColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    )
            )
            
            // 外圈毛玻璃效果
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        color = if (isDark) {
                            GlassDarkColors.SurfaceContainer.copy(alpha = 0.6f)
                        } else {
                            GlassLightColors.SurfaceContainer.copy(alpha = 0.6f)
                        },
                        shape = CircleShape
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape
                    )
            )
        }
        
        // 新消息指示器
        if (hasNewMessage) {
            Badge(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 4.dp, y = (-4).dp)
            ) {
                Text("1")
            }
        }
        
        // 主头像容器 - Glass 毛玻璃风格
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor,
                            primaryContainerColor
                        )
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 2.dp,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "AI助手",
                modifier = Modifier.size(28.dp),
                tint = onPrimaryColor
            )
        }
        
        // 思考动画点
        if (state == AIAvatarState.THINKING) {
            ThinkingDots(isDark = isDark)
        }
    }
}

/**
 * 思考动画点 - Glass 风格
 */
@Composable
private fun ThinkingDots(isDark: Boolean) {
    val surfaceColor = if (isDark) {
        GlassDarkColors.SurfaceContainerHigh.copy(alpha = 0.9f)
    } else {
        GlassLightColors.SurfaceContainerHigh.copy(alpha = 0.9f)
    }
    
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    
    Row(
        modifier = Modifier
            .offset(y = 38.dp)
            .background(
                color = surfaceColor,
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        repeat(3) { index ->
            val delay = index * 150
            val animatedValue by animateFloatAsState(
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = delay, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "thinkingDot$index"
            )
            
            Box(
                modifier = Modifier
                    .size(8.dp * animatedValue)
                    .background(
                        color = primaryColor,
                        shape = CircleShape
                    )
            )
        }
    }
}

/**
 * AI助手形态过渡容器 - Glass 风格
 */
@Composable
fun AIMorphingContainer(
    state: AIAvatarState,
    onStateChange: (AIAvatarState) -> Unit,
    modifier: Modifier = Modifier,
    isDark: Boolean = false,
    collapsedContent: @Composable () -> Unit = {},
    expandedContent: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        AnimatedContent(
            targetState = state,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) +
                scaleIn(
                    initialScale = 0.8f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                ) togetherWith
                fadeOut(animationSpec = tween(200)) +
                scaleOut(
                    targetScale = 0.8f,
                    animationSpec = tween(200)
                )
            },
            label = "aiMorphing"
        ) { targetState ->
            when (targetState) {
                AIAvatarState.COLLAPSED, AIAvatarState.THINKING -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        AIAvatar(
                            state = targetState,
                            onClick = { onStateChange(AIAvatarState.EXPANDING) },
                            modifier = Modifier.padding(20.dp),
                            isDark = isDark
                        )
                        collapsedContent()
                    }
                }
                AIAvatarState.EXPANDING, AIAvatarState.EXPANDED -> {
                    expandedContent()
                }
                AIAvatarState.MINIMIZED -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        MinimizedAIWindow(
                            onExpand = { onStateChange(AIAvatarState.EXPANDED) },
                            onClose = { onStateChange(AIAvatarState.COLLAPSED) },
                            isDark = isDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * 最小化AI窗口 - Glass 毛玻璃风格
 */
@Composable
private fun MinimizedAIWindow(
    onExpand: () -> Unit,
    onClose: () -> Unit,
    isDark: Boolean
) {
    val backgroundColor = if (isDark) {
        GlassDarkColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    } else {
        GlassLightColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }
    
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val onPrimaryColor = if (isDark) GlassDarkColors.OnPrimary else GlassLightColors.OnPrimary
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Card(
        modifier = Modifier
            .width(220.dp)
            .padding(20.dp)
            .clickable { onExpand() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI头像
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = primaryColor,
                        shape = CircleShape
                    )
                    .border(
                        width = 1.5.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = onPrimaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI助手",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = onSurfaceColor
                )
                Text(
                    text = "点击展开",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor
                )
            }
            
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "关闭",
                    modifier = Modifier.size(20.dp),
                    tint = onSurfaceVariantColor
                )
            }
        }
    }
}

/**
 * AI助手展开动画
 */
@Composable
fun AIExpandAnimation(
    isVisible: Boolean,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            expandFrom = Alignment.BottomEnd
        ) + fadeIn(animationSpec = tween(300)),
        exit = shrinkOut(
            animationSpec = tween(200),
            shrinkTowards = Alignment.BottomEnd
        ) + fadeOut(animationSpec = tween(200))
    ) {
        content()
    }
}

/**
 * AI助手浮动输入框 - Glass 毛玻璃风格
 */
@Composable
fun AIFloatingInput(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "询问AI助手...",
    isDark: Boolean = false
) {
    var text by remember { mutableStateOf("") }
    
    val backgroundColor = if (isDark) {
        GlassDarkColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    } else {
        GlassLightColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }
    
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // AI图标
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = primaryColor.copy(alpha = 0.15f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 输入框
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.weight(1f),
                placeholder = { 
                    Text(
                        placeholder,
                        color = onSurfaceVariantColor
                    ) 
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedTextColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface,
                    unfocusedTextColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
                )
            )
            
            // 发送按钮
            IconButton(
                onClick = {
                    if (text.isNotBlank()) {
                        onSend(text)
                        text = ""
                    }
                },
                enabled = text.isNotBlank()
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送",
                    tint = if (text.isNotBlank()) {
                        primaryColor
                    } else {
                        onSurfaceVariantColor.copy(alpha = 0.5f)
                    }
                )
            }
        }
    }
}

/**
 * AI助手欢迎动画 - Glass 风格
 */
@Composable
fun AIWelcomeAnimation(
    onAnimationComplete: () -> Unit = {},
    isDark: Boolean = false
) {
    var startAnimation by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        startAnimation = true
        delay(1500)
        onAnimationComplete()
    }
    
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "welcomeScale"
    )
    
    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(500),
        label = "welcomeAlpha"
    )
    
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val primaryContainerColor = if (isDark) GlassDarkColors.PrimaryContainer else GlassLightColors.PrimaryContainer
    val onPrimaryColor = if (isDark) GlassDarkColors.OnPrimary else GlassLightColors.OnPrimary
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            }
        ) {
            // AI头像 - Glass 风格
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                primaryColor,
                                primaryContainerColor
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 3.dp,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = onPrimaryColor
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "AI健康助手",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor
            )
            
            Text(
                text = "随时为您提供健康建议",
                style = MaterialTheme.typography.bodyMedium,
                color = onSurfaceVariantColor
            )
        }
    }
}
