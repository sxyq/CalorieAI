package com.calorieai.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.components.markdown.MarkdownText
import com.calorieai.app.ui.screens.ai.AIChatViewModel
import com.calorieai.app.ui.screens.ai.AIQuickAction
import com.calorieai.app.ui.screens.ai.ChatMessage
import com.calorieai.app.ui.screens.ai.QuickActionRouter
import com.calorieai.app.ui.screens.ai.sharedAIChatViewModel
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class AIWidgetState {
    FLOATING,
    MINI,
    FULLSCREEN
}

enum class AIWidgetMode {
    HEALTH_ONLY,
    RECIPE_ASSISTANT
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AIChatWidget(
    onExpandToFullScreen: (String) -> Unit,
    mode: AIWidgetMode = AIWidgetMode.HEALTH_ONLY,
    widgetState: AIWidgetState = AIWidgetState.FLOATING,
    onWidgetStateChange: (AIWidgetState) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val viewModel = sharedAIChatViewModel()

    AnimatedContent(
        targetState = widgetState,
        transitionSpec = {
            val fromFloating = initialState == AIWidgetState.FLOATING
            val toFloating = targetState == AIWidgetState.FLOATING

            scaleIn(
                animationSpec = spring(
                    dampingRatio = if (fromFloating) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialScale = if (fromFloating) 0.3f else 0.8f,
                transformOrigin = TransformOrigin(1f, 1f)
            ) + fadeIn(animationSpec = tween(250)) togetherWith
                scaleOut(
                    animationSpec = tween(200, easing = EaseInCubic),
                    targetScale = if (toFloating) 0.2f else 0.8f,
                    transformOrigin = TransformOrigin(1f, 1f)
                ) + fadeOut(animationSpec = tween(150))
        },
        label = "ai_widget_state",
        modifier = modifier
    ) { state ->
        when (state) {
            AIWidgetState.FLOATING -> FloatingButton(
                onClick = { onWidgetStateChange(AIWidgetState.MINI) }
            )

            AIWidgetState.MINI -> AIChatMiniWindow(
                mode = mode,
                onDismiss = { onWidgetStateChange(AIWidgetState.FLOATING) },
                onExpand = {
                    viewModel.persistCurrentSession { sessionId ->
                        onWidgetStateChange(AIWidgetState.FULLSCREEN)
                        onExpandToFullScreen(sessionId)
                    }
                },
                viewModel = viewModel
            )

            AIWidgetState.FULLSCREEN -> Box(Modifier.size(1.dp))
        }
    }
}

@Composable
private fun FloatingButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDark = isSystemInDarkTheme()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ai_widget_floating_scale"
    )

    val pulse by rememberInfiniteTransition(label = "ai_widget_pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ai_widget_pulse_value"
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Box(
                modifier = Modifier
                .fillMaxSize()
                .scale(pulse)
                .glassEffect(isDark = isDark, cornerRadius = 32.dp, alpha = 0.3f)
        )

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .glassEffect(isDark = isDark, cornerRadius = 28.dp, alpha = 0.9f)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPressed) Icons.Default.ChatBubbleOutline else Icons.Default.AutoAwesome,
                    contentDescription = "AI assistant",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AIChatMiniWindow(
    mode: AIWidgetMode,
    onDismiss: () -> Unit,
    onExpand: () -> Unit,
    viewModel: AIChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember(uiState.currentSessionId) { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(uiState.messages.size, uiState.isLoading) {
        val index = uiState.messages.lastIndex
        if (index >= 0) {
            scope.launch { listState.animateScrollToItem(index) }
        }
    }

    Box(
        modifier = Modifier
            .width(360.dp)
            .heightIn(max = 520.dp)
            .clip(RoundedCornerShape(24.dp))
            .glassEffect(isDark = isDark, cornerRadius = 24.dp, alpha = 0.95f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MiniHeader(
                isLoading = uiState.isLoading,
                onExpand = onExpand,
                onDismiss = onDismiss,
                isDark = isDark
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (uiState.messages.isEmpty()) {
                    QuickActions(
                        mode = mode,
                        onAction = { action -> QuickActionRouter.dispatch(viewModel, action) },
                        isDark = isDark
                    )
                } else {
                    MessageList(
                        messages = uiState.messages,
                        isLoading = uiState.isLoading,
                        listState = listState,
                        scope = scope,
                        isDark = isDark
                    )
                }
            }

            GlassInput(
                inputText = inputText,
                onInputChange = { inputText = it },
                isLoading = uiState.isLoading,
                onSend = {
                    val message = inputText.trim()
                    if (message.isNotBlank()) {
                        viewModel.sendMessage(message)
                        inputText = ""
                    }
                },
                isDark = isDark
            )
        }
    }
}

@Composable
private fun MiniHeader(
    isLoading: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "AI助手",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLoading) Color(0xFFFFA726) else Color(0xFF4CAF50))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isLoading) "思考中..." else "在线",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row {
            GlassIconButton(
                onClick = onExpand,
                isDark = isDark,
                icon = Icons.Default.OpenInFull,
                desc = "全屏"
            )
            Spacer(modifier = Modifier.width(4.dp))
            GlassIconButton(
                onClick = onDismiss,
                isDark = isDark,
                icon = Icons.Default.Close,
                desc = "关闭"
            )
        }
    }
}

@Composable
private fun GlassIconButton(
    onClick: () -> Unit,
    isDark: Boolean,
    icon: ImageVector,
    desc: String
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .glassEffect(isDark = isDark, cornerRadius = 18.dp, alpha = 0.3f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickActions(
    mode: AIWidgetMode,
    onAction: (AIQuickAction) -> Unit,
    isDark: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "快捷功能",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (mode == AIWidgetMode.HEALTH_ONLY) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.HealthAndSafety,
                    title = "健康咨询",
                    color = Color(0xFFFFE66D),
                    onClick = { onAction(AIQuickAction.HEALTH_CONSULT) },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionCard(
                    icon = Icons.Default.Assessment,
                    title = "热量评估",
                    color = Color(0xFFFF6B6B),
                    onClick = { onAction(AIQuickAction.CALORIE_ASSESSMENT) },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                QuickActionCard(
                    icon = Icons.Default.RestaurantMenu,
                    title = "菜谱规划",
                    color = Color(0xFF4ECDC4),
                    onClick = { onAction(AIQuickAction.MEAL_PLANNING) },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
                QuickActionCard(
                    icon = Icons.Default.HealthAndSafety,
                    title = "健康咨询",
                    color = Color(0xFFFFE66D),
                    onClick = { onAction(AIQuickAction.HEALTH_CONSULT) },
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .glassEffect(isDark = isDark, cornerRadius = 16.dp, alpha = 0.5f)
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = if (mode == AIWidgetMode.HEALTH_ONLY) "你好！我是你的AI助手" else "你好！我是你的饮食助手",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (mode == AIWidgetMode.HEALTH_ONLY) {
                        "💬 解答营养问题\n📊 评估近期状态\n🩺 提供健康建议"
                    } else {
                        "📊 评估热量消耗\n🍽️ 规划健康菜谱\n💬 解答营养问题"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ai_widget_card_scale"
    )

    Box(
        modifier = modifier
            .height(90.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .glassEffect(isDark = isDark, cornerRadius = 16.dp, alpha = 0.6f)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: LazyListState,
    scope: CoroutineScope,
    isDark: Boolean
) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageItem(message = message, isDark = isDark)
        }
        if (isLoading) {
            item {
                TypingIndicator(isDark = isDark)
            }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.lastIndex) }
        }
    }
}

@Composable
private fun MessageItem(message: ChatMessage, isDark: Boolean) {
    val isUser = message.isFromUser
    val miniMarkdownConfig = MarkdownConfig.Compact.copy(
        textStyle = TextStyle(fontSize = 13.sp, lineHeight = 19.sp),
        h1Style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
        h2Style = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),
        h3Style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
        h4Style = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold),
        paragraphSpacing = 3,
        headingSpacing = 6
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 252.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 14.dp,
                        topEnd = 14.dp,
                        bottomStart = if (isUser) 14.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 14.dp
                    )
                )
                .background(
                    if (isUser) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                    }
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            if (isUser) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp, lineHeight = 18.sp),
                    color = Color.White
                )
            } else {
                MarkdownText(
                    text = message.content,
                    isDark = isDark,
                    modifier = Modifier.fillMaxWidth(),
                    config = miniMarkdownConfig
                )
            }
        }
    }
}

@Composable
private fun GlassInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit,
    isDark: Boolean
) {
    val isLocked = isLoading
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .glassEffect(isDark = isDark, cornerRadius = 24.dp, alpha = 0.6f)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputIconButton(
            onClick = {},
            icon = Icons.Default.AutoAwesome,
            desc = "AI",
            tint = MaterialTheme.colorScheme.primary
        )

        TextField(
            value = inputText,
            onValueChange = { if (!isLocked) onInputChange(it) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !isLocked,
            textStyle = MaterialTheme.typography.bodySmall,
            placeholder = {
                Text(
                    text = if (isLocked) "等待回复中..." else "输入问题...",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )

        Spacer(modifier = Modifier.width(8.dp))

        val enabled = inputText.isNotBlank() && !isLoading
        val scale by animateFloatAsState(
            targetValue = if (enabled) 1f else 0.85f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "ai_widget_send_scale"
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    if (enabled) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    }
                )
                .clickable(enabled = enabled, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "发送",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun InputIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    desc: String,
    tint: Color
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = desc,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun TypingIndicator(isDark: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .glassEffect(isDark = isDark, cornerRadius = 14.dp, alpha = 0.6f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by rememberInfiniteTransition(label = "typing$index").animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 100),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "typing_alpha_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = alpha))
                    )
                }
            }
        }
    }
}

@Stable
fun Modifier.glassEffect(
    isDark: Boolean,
    cornerRadius: Dp,
    alpha: Float = 0.8f
): Modifier {
    return clip(RoundedCornerShape(cornerRadius))
        .background((if (isDark) GlassDarkColors.Surface else GlassLightColors.Surface).copy(alpha = alpha))
}
