package com.calorieai.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.theme.DeadlinerColors
import com.calorieai.app.ui.screens.ai.AIChatViewModel
import kotlinx.coroutines.launch

/**
 * AI聊天小窗口（悬浮按钮形式）
 * 支持丝滑的状态切换动画
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AIChatWidget(
    onExpandToFullScreen: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    
    // 悬浮按钮动画
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // 按钮缩放动画
    val buttonScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.9f
            isExpanded -> 0f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )
    
    // 脉冲动画
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomEnd
    ) {
        // 悬浮按钮
        AnimatedVisibility(
            visible = !isExpanded,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialScale = 0.5f
            ) + fadeIn(),
            exit = scaleOut(
                animationSpec = tween(200, easing = EaseInCubic),
                targetScale = 0.5f
            ) + fadeOut(animationSpec = tween(150))
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .scale(buttonScale),
                contentAlignment = Alignment.Center
            ) {
                // 脉冲波纹
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(pulseScale)
                        .background(
                            color = DeadlinerColors.accentBlue.copy(alpha = pulseAlpha),
                            shape = CircleShape
                        )
                )
                
                // 主按钮
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(
                                    DeadlinerColors.accentBlue,
                                    DeadlinerColors.accentBlue.copy(alpha = 0.8f)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = interactionSource,
                            indication = null
                        ) { 
                            isExpanded = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    // 图标切换动画
                    AnimatedContent(
                        targetState = isPressed,
                        transitionSpec = {
                            scaleIn(animationSpec = tween(150)) + fadeIn() with
                            scaleOut(animationSpec = tween(150)) + fadeOut()
                        },
                        label = "iconAnimation"
                    ) { pressed ->
                        Icon(
                            imageVector = if (pressed) Icons.Default.ChatBubbleOutline else Icons.Default.Chat,
                            contentDescription = "AI助手",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
        
        // 展开的迷你窗口
        AnimatedVisibility(
            visible = isExpanded,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                initialScale = 0.3f,
                transformOrigin = TransformOrigin(1f, 1f)
            ) + fadeIn(animationSpec = tween(200)),
            exit = scaleOut(
                animationSpec = tween(200, easing = EaseInCubic),
                targetScale = 0.3f,
                transformOrigin = TransformOrigin(1f, 1f)
            ) + fadeOut(animationSpec = tween(150))
        ) {
            AIChatMiniWindow(
                onDismiss = { isExpanded = false },
                onExpandToFullScreen = {
                    isExpanded = false
                    onExpandToFullScreen()
                }
            )
        }
    }
}

/**
 * AI聊天迷你窗口 - 支持直接对话
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AIChatMiniWindow(
    onDismiss: () -> Unit,
    onExpandToFullScreen: () -> Unit,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    
    // 窗口进入动画状态
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        showContent = true
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            Card(
                modifier = Modifier
                    .width(360.dp)
                    .height(500.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // 渐变背景标题栏
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { -it }
                        ) + fadeIn()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF2196F3),
                                            Color(0xFF64B5F6)
                                        )
                                    )
                                )
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // AI头像动画
                                    val avatarScale by animateFloatAsState(
                                        targetValue = if (uiState.isLoading) 1.1f else 1f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioMediumBouncy,
                                            stiffness = Spring.StiffnessLow
                                        ),
                                        label = "avatarScale"
                                    )
                                    
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .scale(avatarScale)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SmartToy,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "AI助手",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        // 状态指示器动画
                                        AnimatedContent(
                                            targetState = uiState.isLoading,
                                            transitionSpec = {
                                                fadeIn() with fadeOut()
                                            },
                                            label = "statusAnimation"
                                        ) { loading ->
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(6.dp)
                                                        .background(
                                                            color = if (loading) 
                                                                Color(0xFFFFA726) 
                                                            else 
                                                                Color(0xFF4CAF50),
                                                            shape = CircleShape
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (loading) "思考中..." else "在线",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = Color.White.copy(alpha = 0.8f)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                Row {
                                    IconButton(
                                        onClick = onExpandToFullScreen,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.OpenInFull,
                                            contentDescription = "全屏",
                                            tint = Color.White
                                        )
                                    }
                                    IconButton(
                                        onClick = onDismiss,
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "关闭",
                                            tint = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    // 消息列表区域
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        AnimatedContent(
                            targetState = uiState.messages.isEmpty(),
                            transitionSpec = {
                                fadeIn(animationSpec = tween(300)) with
                                fadeOut(animationSpec = tween(150))
                            },
                            label = "contentAnimation"
                        ) { isEmpty ->
                            if (isEmpty) {
                                // 空状态 - 显示快捷功能
                                QuickActionsContent(
                                    onActionClick = { action ->
                                        viewModel.sendMessage(action)
                                    }
                                )
                            } else {
                                // 显示消息列表
                                MessagesList(
                                    messages = uiState.messages,
                                    isLoading = uiState.isLoading,
                                    listState = listState,
                                    scope = scope
                                )
                            }
                        }
                    }
                    
                    // 输入框区域
                    AnimatedVisibility(
                        visible = showContent,
                        enter = slideInVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            initialOffsetY = { it }
                        ) + fadeIn()
                    ) {
                        InputArea(
                            inputText = inputText,
                            onInputChange = { inputText = it },
                            isLoading = uiState.isLoading,
                            onSend = {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 快捷功能内容
 */
@Composable
private fun QuickActionsContent(
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "快捷功能",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        // 快捷功能卡片动画
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            QuickActionCard(
                icon = Icons.Default.Assessment,
                title = "热量评估",
                color = Color(0xFFFF6B6B),
                delay = 0,
                onClick = { onActionClick("帮我评估一下最近一周的热量摄入是否合理") },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.RestaurantMenu,
                title = "菜谱规划",
                color = Color(0xFF4ECDC4),
                delay = 100,
                onClick = { onActionClick("帮我规划一下今天的健康菜谱") },
                modifier = Modifier.weight(1f)
            )
            QuickActionCard(
                icon = Icons.Default.HealthAndSafety,
                title = "健康咨询",
                color = Color(0xFFFFE66D),
                delay = 200,
                onClick = { onActionClick("我想咨询一些营养健康问题") },
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 欢迎消息
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "你好！我是你的AI助手。我可以帮你：",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "📊 评估热量消耗是否合理\n🍽️ 规划健康菜谱\n💬 解答营养健康问题",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "点击上方快捷按钮或输入你的问题开始吧！",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * 消息列表
 */
@Composable
private fun MessagesList(
    messages: List<com.calorieai.app.ui.screens.ai.ChatMessage>,
    isLoading: Boolean,
    listState: LazyListState,
    scope: kotlinx.coroutines.CoroutineScope,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = messages,
            key = { it.id }
        ) { message ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    ),
                    initialOffsetY = { it / 2 }
                ) + fadeIn(),
                exit = fadeOut()
            ) {
                MiniChatMessageItem(message = message)
            }
        }
        
        // 正在输入指示器
        if (isLoading) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f)
                ) {
                    TypingIndicator()
                }
            }
        }
    }
    
    // 自动滚动到底部
    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
}

/**
 * 输入区域
 */
@Composable
private fun InputArea(
    inputText: String,
    onInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 语音输入按钮
            IconButton(
                onClick = { },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "语音输入",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            // 输入框
            androidx.compose.foundation.text.BasicTextField(
                value = inputText,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                decorationBox = { innerTextField ->
                    Box {
                        if (inputText.isEmpty()) {
                            Text(
                                text = "输入问题或需求...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            // 发送按钮动画
            val sendButtonScale by animateFloatAsState(
                targetValue = if (inputText.isNotBlank() && !isLoading) 1f else 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "sendButtonScale"
            )
            
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .scale(sendButtonScale)
                    .clip(CircleShape)
                    .background(
                        if (inputText.isNotBlank() && !isLoading)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    .clickable(enabled = inputText.isNotBlank() && !isLoading, onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "发送",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * 迷你窗口消息项
 */
@Composable
private fun MiniChatMessageItem(
    message: com.calorieai.app.ui.screens.ai.ChatMessage,
    modifier: Modifier = Modifier
) {
    val isUser = message.isFromUser
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            // AI头像
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(DeadlinerColors.accentBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        // 消息气泡
        Card(
            modifier = Modifier.widthIn(max = 240.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser)
                    DeadlinerColors.accentBlue
                else
                    MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            )
        ) {
            Text(
                text = message.content,
                modifier = Modifier.padding(12.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 正在输入指示器
 */
@Composable
private fun TypingIndicator(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(DeadlinerColors.accentBlue),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "typing$index")
                    val alpha by infiniteTransition.animateFloat(
                        initialValue = 0.3f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 100),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "alpha$index"
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
                            )
                    )
                }
            }
        }
    }
}

/**
 * 快捷功能卡片 - 带动画
 */
@Composable
private fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    delay: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 入场动画
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(delay.toLong())
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            initialScale = 0.5f
        ) + fadeIn(
            animationSpec = tween(300, delayMillis = delay)
        )
    ) {
        val interactionSource = remember { MutableInteractionSource() }
        val isPressed by interactionSource.collectIsPressedAsState()
        
        val cardScale by animateFloatAsState(
            targetValue = if (isPressed) 0.95f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "cardScale"
        )
        
        Card(
            modifier = modifier
                .aspectRatio(1f)
                .scale(cardScale)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                ),
            colors = CardDefaults.cardColors(
                containerColor = color.copy(alpha = 0.1f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
