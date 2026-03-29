package com.calorieai.app.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.markdown.MarkdownText
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.screens.ai.AIChatViewModel
import com.calorieai.app.ui.screens.ai.ChatMessage
import com.calorieai.app.service.voice.VoiceInputHelper
import com.calorieai.app.service.voice.VoiceModelManager
import com.calorieai.app.service.voice.VoiceState
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
import kotlinx.coroutines.launch

/** 三种形态：悬浮球、迷你窗口、全屏（外部处理） */
enum class AIWidgetState { FLOATING, MINI, FULLSCREEN }
enum class AIWidgetMode { HEALTH_ONLY, RECIPE_ASSISTANT }

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AIChatWidget(
    onExpandToFullScreen: (String) -> Unit,
    mode: AIWidgetMode = AIWidgetMode.HEALTH_ONLY,
    widgetState: AIWidgetState = AIWidgetState.FLOATING,
    onWidgetStateChange: (AIWidgetState) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

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
            AIWidgetState.FLOATING -> FloatingButton {
                // 悬浮窗展开到小窗时，默认新开对话
                viewModel.startNewSession()
                onWidgetStateChange(AIWidgetState.MINI)
            }
            AIWidgetState.MINI -> AIChatMiniWindow(
                mode = mode,
                onDismiss = { onWidgetStateChange(AIWidgetState.FLOATING) },
                onExpand = { sessionId ->
                    onWidgetStateChange(AIWidgetState.FULLSCREEN)
                    onExpandToFullScreen(sessionId)
                },
                viewModel = viewModel
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

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
    mode: AIWidgetMode,
    onDismiss: () -> Unit,
    onExpand: (String) -> Unit,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val voiceHelper = remember { VoiceInputHelper(VoiceModelManager(context.applicationContext)) }
    val voiceState by voiceHelper.voiceState.collectAsState()
    var showVoiceDialog by remember { mutableStateOf(false) }
    var isVoiceListening by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startWidgetVoiceInput(
                context = context,
                voiceHelper = voiceHelper,
                onStart = {
                    isVoiceListening = true
                    showVoiceDialog = true
                },
                onText = { recognized ->
                    inputText = if (inputText.isBlank()) recognized else "$inputText $recognized"
                }
            )
        } else {
            showPermissionDialog = true
        }
    }

    LaunchedEffect(voiceState) {
        when (voiceState) {
            is VoiceState.Success -> {
                isVoiceListening = false
                showVoiceDialog = false
            }
            is VoiceState.Error -> {
                isVoiceListening = false
            }
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .width(360.dp)
            .heightIn(max = 520.dp)
            .clip(RoundedCornerShape(24.dp))
            .glassEffect(isDark, 24.dp, 0.95f)
    ) {
        Column(Modifier.fillMaxSize()) {
            // 头部
            MiniHeader(
                isLoading = uiState.isLoading,
                onExpand = {
                    viewModel.persistCurrentSession()
                    onExpand(it)
                },
                onDismiss = onDismiss,
                isDark = isDark,
                sessionId = uiState.currentSessionId
            )

            // 内容区
            Box(Modifier.weight(1f).fillMaxWidth()) {
                if (uiState.messages.isEmpty()) {
                    QuickActions(
                        mode = mode,
                        onAction = { prompt ->
                            if (mode == AIWidgetMode.HEALTH_ONLY) {
                                viewModel.startHealthConsult()
                            } else {
                                viewModel.sendMessage(prompt)
                            }
                        },
                        isDark = isDark
                    )
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
                onVoiceClick = {
                    when {
                        isVoiceListening -> {
                            voiceHelper.stopListening()
                            isVoiceListening = false
                            showVoiceDialog = false
                        }
                        ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            startWidgetVoiceInput(
                                context = context,
                                voiceHelper = voiceHelper,
                                onStart = {
                                    isVoiceListening = true
                                    showVoiceDialog = true
                                },
                                onText = { recognized ->
                                    inputText = if (inputText.isBlank()) recognized else "$inputText $recognized"
                                }
                            )
                        }
                        else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                isDark = isDark
            )
        }
    }

    VoiceInputDialog(
        isVisible = showVoiceDialog,
        voiceState = voiceState,
        onDismiss = {
            voiceHelper.stopListening()
            isVoiceListening = false
            showVoiceDialog = false
        },
        onStopRecording = {
            voiceHelper.stopListening()
            isVoiceListening = false
        }
    )

    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("需要录音权限") },
            text = { Text("语音输入功能需要录音权限，请在系统设置中开启后重试。") },
            confirmButton = {
                TextButton(onClick = { showPermissionDialog = false }) { Text("知道了") }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
        }
    }
}

/** 迷你窗口头部 */
@Composable
private fun MiniHeader(
    isLoading: Boolean,
    onExpand: (String) -> Unit,
    onDismiss: () -> Unit,
    isDark: Boolean,
    sessionId: String
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
            GlassIconButton({ onExpand(sessionId) }, isDark, Icons.Default.OpenInFull, "全屏")
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
private fun QuickActions(mode: AIWidgetMode, onAction: (String) -> Unit, isDark: Boolean) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "快捷功能",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (mode == AIWidgetMode.HEALTH_ONLY) {
                QuickActionCard(
                    icon = Icons.Default.HealthAndSafety,
                    title = "健康咨询",
                    color = Color(0xFFFFE66D),
                    prompt = "我想咨询一些营养健康问题",
                    onClick = onAction,
                    modifier = Modifier.weight(1f),
                    isDark = isDark
                )
            } else {
                QuickActionCard(Icons.Default.Assessment, "热量评估", Color(0xFFFF6B6B),
                    "帮我评估一下最近一周的热量摄入是否合理", onAction, Modifier.weight(1f), isDark)
                QuickActionCard(Icons.Default.RestaurantMenu, "菜谱规划", Color(0xFF4ECDC4),
                    "帮我规划一下今天的健康菜谱", onAction, Modifier.weight(1f), isDark)
                QuickActionCard(Icons.Default.HealthAndSafety, "健康咨询", Color(0xFFFFE66D),
                    "我想咨询一些营养健康问题", onAction, Modifier.weight(1f), isDark)
            }
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
                    if (mode == AIWidgetMode.HEALTH_ONLY)
                        "💬 健康咨询\n📈 近期记录解读\n✅ 可执行建议"
                    else
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
    val compactMarkdownConfig = MarkdownConfig.Compact
    val miniMarkdownConfig = remember(compactMarkdownConfig) {
        compactMarkdownConfig.copy(
            textStyle = TextStyle(
                fontSize = 13.sp,
                lineHeight = 19.sp
            ),
            h1Style = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Bold),
            h2Style = TextStyle(fontSize = 15.sp, lineHeight = 21.sp, fontWeight = FontWeight.Bold),
            h3Style = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, fontWeight = FontWeight.SemiBold),
            h4Style = TextStyle(fontSize = 13.sp, lineHeight = 19.sp, fontWeight = FontWeight.SemiBold),
            paragraphSpacing = 3,
            headingSpacing = 6
        )
    }

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
                .widthIn(max = 252.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = if (isUser) 14.dp else 4.dp, bottomEnd = if (isUser) 4.dp else 14.dp))
                .background(if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(0.6f))
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            if (isUser) {
                Text(
                    message.content,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    ),
                    color = Color.White
                )
            } else {
                // AI消息使用Markdown渲染提高可读性
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

/** 输入区域 */
@Composable
private fun GlassInput(
    inputText: String,
    onInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit,
    onVoiceClick: () -> Unit,
    isDark: Boolean
) {
    val isLocked = isLoading
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
        InputIconButton(onVoiceClick, Icons.Default.Mic, "语音", MaterialTheme.colorScheme.primary)

        androidx.compose.foundation.text.BasicTextField(
            value = inputText,
            onValueChange = { if (!isLocked) onInputChange(it) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            enabled = !isLocked,
            textStyle = MaterialTheme.typography.bodySmall.copy(
                color = if (isLocked) MaterialTheme.colorScheme.onSurface.copy(0.5f) 
                       else MaterialTheme.colorScheme.onSurface
            ),
            decorationBox = { innerTextField ->
                Box {
                    if (inputText.isEmpty()) {
                        Text(
                            if (isLocked) "等待回复中..." else "输入问题...", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = if (isLocked) 0.5f else 1f
                            )
                        )
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

private fun startWidgetVoiceInput(
    context: android.content.Context,
    voiceHelper: VoiceInputHelper,
    onStart: () -> Unit,
    onText: (String) -> Unit
) {
    onStart()
    voiceHelper.startListening(
        context = context,
        onResult = { result -> onText(result) },
        onError = { error ->
            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
        },
        onPartialResult = { partial -> onText(partial) }
    )
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
    val transition = rememberInfiniteTransition(label = "widgetTyping")

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
                    val alpha by transition.animateFloat(
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
