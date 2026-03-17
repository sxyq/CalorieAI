package com.calorieai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.screens.ai.AIChatViewModel
import com.calorieai.app.ui.screens.ai.ChatMessage
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
import kotlinx.coroutines.launch

/** 三种形态：悬浮球、迷你窗口、全屏（外部处理） */
enum class AIWidgetState { FLOATING, MINI, FULLSCREEN }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AIChatWidget(
    onExpandToFullScreen: () -> Unit,
    widgetState: AIWidgetState = AIWidgetState.FLOATING,
    onWidgetStateChange: (AIWidgetState) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()

    AnimatedContent(
        targetState = widgetState,
        transitionSpec = {
            val fromFloating = initialState == AIWidgetState.FLOATING
            val toFloating = targetState == AIWidgetState.FLOATING

            scaleIn(
                spring(if (fromFloating) Spring.DampingRatioMediumBouncy else Spring.DampingRatioNoBouncy, Spring.StiffnessLow),
                if (fromFloating) 0.3f else 0.8f,
                TransformOrigin(1f, 1f)
            ) + fadeIn(tween(250)) with
            scaleOut(
                tween(200, easing = EaseInCubic),
                if (toFloating) 0.2f else 0.8f,
                TransformOrigin(1f, 1f)
            ) + fadeOut(tween(150))
        },
        label = "widgetState",
        modifier = modifier
    ) { state ->
        when (state) {
            AIWidgetState.FLOATING -> FloatingButton { onWidgetStateChange(AIWidgetState.MINI) }
            AIWidgetState.MINI -> AIChatMiniWindow(
                onDismiss = { onWidgetStateChange(AIWidgetState.FLOATING) },
                onExpand = {
                    onWidgetStateChange(AIWidgetState.FULLSCREEN)
                    onExpandToFullScreen()
                }
            )
            else -> Box(Modifier.size(1.dp))
        }
    }
}

/** 悬浮球 */
@Composable
private fun FloatingButton(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val isDark = isSystemInDarkTheme()

    val scale by animateFloatAsState(
        if (isPressed) 0.9f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
    )

    val pulse by rememberInfiniteTransition("pulse").animateFloat(
        1f, 1.2f, infiniteRepeatable(tween(1500, easing = EaseInOutCubic), RepeatMode.Reverse)
    )

    Box(modifier = Modifier.size(64.dp).scale(scale), contentAlignment = Alignment.Center) {
        // 脉冲光环
        Box(
            Modifier
                .fillMaxSize()
                .scale(pulse)
                .glassEffect(isDark, 32.dp, 0.3f)
        )
        // 主按钮
        Box(
            Modifier
                .size(56.dp)
                .clip(CircleShape)
                .glassEffect(isDark, 28.dp, 0.9f)
                .clickable(interactionSource, null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Box(
                Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(0.7f)))),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isPressed) Icons.Default.ChatBubbleOutline else Icons.Default.AutoAwesome,
                    contentDescription = "AI助手",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/** 迷你窗口 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIChatMiniWindow(
    onDismiss: () -> Unit,
    onExpand: () -> Unit,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .width(360.dp)
            .heightIn(max = 520.dp)
            .clip(RoundedCornerShape(24.dp))
            .glassEffect(isDark, 24.dp, 0.95f)
    ) {
        Column(Modifier.fillMaxSize()) {
            // 头部
            MiniHeader(uiState.isLoading, onExpand, onDismiss, isDark)

            // 内容区
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (uiState.messages.isEmpty()) {
                    QuickActions({ viewModel.sendMessage(it) }, isDark)
                } else {
                    MessageList(uiState.messages, uiState.isLoading, listState, scope, isDark)
                }
            }

            // 输入区
            GlassInput(
                inputText = inputText,
                onInputChange = { inputText = it },
                isLoading = uiState.isLoading,
                onSend = {
                    viewModel.sendMessage(inputText)
                    inputText = ""
                },
                isDark = isDark
            )
        }
    }
}

/** 迷你窗口头部 */
@Composable
private fun MiniHeader(
    isLoading: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(0.7f)))
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("AI助手", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isLoading) Color(0xFFFFA726) else Color(0xFF4CAF50))
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isLoading) "思考中..." else "在线",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Row {
            GlassIconButton(onExpand, isDark, Icons.Default.OpenInFull, "全屏")
            Spacer(Modifier.width(4.dp))
            GlassIconButton(onDismiss, isDark, Icons.Default.Close, "关闭")
        }
    }
}

/** 玻璃图标按钮 */
@Composable
private fun GlassIconButton(onClick: () -> Unit, isDark: Boolean, icon: ImageVector, desc: String) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .glassEffect(isDark, 18.dp, 0.3f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, desc, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 快捷操作 */
@Composable
private fun QuickActions(onAction: (String) -> Unit, isDark: Boolean) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "快捷功能",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            QuickActionCard(Icons.Default.Assessment, "热量评估", Color(0xFFFF6B6B),
                "帮我评估一下最近一周的热量摄入是否合理", onAction, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.RestaurantMenu, "菜谱规划", Color(0xFF4ECDC4),
                "帮我规划一下今天的健康菜谱", onAction, Modifier.weight(1f), isDark)
            QuickActionCard(Icons.Default.HealthAndSafety, "健康咨询", Color(0xFFFFE66D),
                "我想咨询一些营养健康问题", onAction, Modifier.weight(1f), isDark)
        }

        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .glassEffect(isDark, 16.dp, 0.5f)
                .padding(16.dp)
        ) {
            Column {
                Text("你好！我是你的AI助手", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))
                Text(
                    "📊 评估热量消耗\n🍽️ 规划健康菜谱\n💬 解答营养问题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** 快捷操作卡片 */
@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    color: Color,
    prompt: String,
    onClick: (String) -> Unit,
    modifier: Modifier,
    isDark: Boolean
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))

    Box(
        modifier = modifier
            .height(90.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .glassEffect(isDark, 16.dp, 0.6f)
            .clickable(interactionSource, null) { onClick(prompt) }
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, fontSize = 11.sp)
        }
    }
}

/** 消息列表 */
@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    scope: kotlinx.coroutines.CoroutineScope,
    isDark: Boolean
) {
    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageItem(message, isDark)
        }
        if (isLoading) {
            item { TypingIndicator(isDark) }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) scope.launch { listState.animateScrollToItem(messages.size - 1) }
    }
}

/** 消息项 */
@Composable
private fun MessageItem(message: ChatMessage, isDark: Boolean) {
    val isUser = message.isFromUser

    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        if (!isUser) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 240.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = if (isUser) 14.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 14.dp))
                .background(if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(0.6f))
                .padding(12.dp)
        ) {
            Text(message.content, style = MaterialTheme.typography.bodySmall, color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface)
        }
    }
}

/** 输入区域 */
@Composable
private fun GlassInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit,
    isDark: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .glassEffect(isDark, 24.dp, 0.6f)
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InputIconButton({ }, Icons.Default.Mic, "语音", MaterialTheme.colorScheme.primary)

        androidx.compose.foundation.text.BasicTextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
            decorationBox = { innerTextField ->
                Box {
                    if (inputText.isEmpty()) {
                        Text("输入问题...", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    innerTextField()
                }
            }
        )

        val enabled = inputText.isNotBlank() && !isLoading
        val scale by animateFloatAsState(if (enabled) 1f else 0.85f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))

        Box(
            modifier = Modifier
                .size(40.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(0.3f))
                .clickable(enabled = enabled, onClick = onSend),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(18.dp), Color.White, strokeWidth = 2.dp)
            } else {
                Icon(Icons.Default.Send, "发送", tint = Color.White, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/** 输入图标按钮 */
@Composable
private fun InputIconButton(onClick: () -> Unit, icon: ImageVector, desc: String, tint: Color) {
    Box(
        Modifier.size(40.dp).clip(CircleShape).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, desc, tint = tint, modifier = Modifier.size(20.dp))
    }
}

/** 打字指示器 */
@Composable
private fun TypingIndicator(isDark: Boolean) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
        Spacer(Modifier.width(8.dp))

        Box(
            Modifier
                .clip(RoundedCornerShape(14.dp))
                .glassEffect(isDark, 14.dp, 0.6f)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                repeat(3) { index ->
                    val alpha by rememberInfiniteTransition("typing$index").animateFloat(
                        0.3f, 1f, infiniteRepeatable(tween(600, delayMillis = index * 100), RepeatMode.Reverse)
                    )
                    Box(Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha)))
                }
            }
        }
    }
}

/** 毛玻璃效果修饰符 */
@Stable
fun Modifier.glassEffect(isDark: Boolean, cornerRadius: androidx.compose.ui.unit.Dp, alpha: Float = 0.8f): Modifier =
    clip(RoundedCornerShape(cornerRadius))
        .background((if (isDark) GlassDarkColors.Surface else GlassLightColors.Surface).copy(alpha = alpha))
