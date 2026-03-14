package com.calorieai.app.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Brush
import com.calorieai.app.ui.components.liquidGlass
import com.calorieai.app.ui.components.interactiveScale
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.theme.DeadlinerColors
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * AI助手全屏页面（重构版）
 * 支持历史对话记录管理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showNewChatConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(DeadlinerColors.accentBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "AI助手",
                                style = MaterialTheme.typography.titleMedium
                            )
                            if (uiState.currentSessionTitle.isNotBlank()) {
                                Text(
                                    text = uiState.currentSessionTitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    // 新对话按钮
                    IconButton(onClick = { showNewChatConfirm = true }) {
                        Icon(Icons.Default.AddComment, contentDescription = "新对话")
                    }
                    // 历史记录按钮
                    IconButton(onClick = { showHistoryDialog = true }) {
                        Icon(Icons.Default.History, contentDescription = "历史记录")
                    }
                    // 更多选项
                    var showMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("清空当前对话") },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteSweep, null)
                            },
                            onClick = {
                                viewModel.clearCurrentChat()
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除当前对话") },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            },
                            onClick = {
                                viewModel.deleteCurrentSession()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 快捷功能区域
            QuickActionsRow(
                onCalorieAssessment = { viewModel.startCalorieAssessment() },
                onMealPlanning = { viewModel.startMealPlanning() },
                onHealthConsult = { viewModel.startHealthConsult() }
            )
            
            HorizontalDivider()
            
            // 聊天消息列表
            Box(
                modifier = Modifier.weight(1f)
            ) {
                if (uiState.messages.isEmpty()) {
                    // 空状态提示
                    EmptyChatState(
                        onQuickStart = { viewModel.startHealthConsult() }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.messages) { message ->
                            ChatMessageItem(message = message)
                        }
                        
                        // 正在输入指示器
                        if (uiState.isTyping) {
                            item {
                                TypingIndicator()
                            }
                        }
                    }
                }
                
                // 自动滚动到底部
                LaunchedEffect(uiState.messages.size, uiState.isTyping) {
                    if (uiState.messages.isNotEmpty()) {
                        scope.launch {
                            listState.animateScrollToItem(uiState.messages.size - 1)
                        }
                    }
                }
            }
            
            // 输入区域
            ChatInputArea(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChange,
                onSend = {
                    viewModel.sendMessage()
                },
                isLoading = uiState.isLoading,
                remainingCalls = uiState.remainingCalls,
                dailyLimit = uiState.dailyLimit
            )
        }
    }

    // 历史记录对话框
    if (showHistoryDialog) {
        ChatHistoryDialog(
            sessions = uiState.chatSessions,
            currentSessionId = uiState.currentSessionId,
            onDismiss = { showHistoryDialog = false },
            onSessionSelected = { sessionId ->
                viewModel.loadSession(sessionId)
                showHistoryDialog = false
            },
            onSessionDeleted = { sessionId ->
                viewModel.deleteSession(sessionId)
            },
            onSessionPinned = { sessionId ->
                viewModel.togglePinSession(sessionId)
            }
        )
    }

    // 新对话确认对话框
    if (showNewChatConfirm) {
        AlertDialog(
            onDismissRequest = { showNewChatConfirm = false },
            title = { Text("开始新对话") },
            text = { Text("当前对话将被保存到历史记录，确定要开始新对话吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.startNewSession()
                        showNewChatConfirm = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewChatConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }

}

/**
 * 空聊天状态
 */
@Composable
private fun EmptyChatState(
    onQuickStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "AI助手",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "你的专属营养健康顾问",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "我可以帮你：",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FeatureItem(icon = Icons.Default.Assessment, text = "评估今日热量消耗是否合理")
            FeatureItem(icon = Icons.Default.RestaurantMenu, text = "规划健康菜谱")
            FeatureItem(icon = Icons.Default.HealthAndSafety, text = "解答营养健康问题")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onQuickStart,
            modifier = Modifier.fillMaxWidth(0.7f)
        ) {
            Text("开始对话")
        }
    }
}

/**
 * 功能项
 */
@Composable
private fun FeatureItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 历史记录对话框
 */
@Composable
private fun ChatHistoryDialog(
    sessions: List<ChatSessionInfo>,
    currentSessionId: String,
    onDismiss: () -> Unit,
    onSessionSelected: (String) -> Unit,
    onSessionDeleted: (String) -> Unit,
    onSessionPinned: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("历史对话")
                Text(
                    text = "${sessions.size} 个对话",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (sessions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无历史对话",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(sessions) { session ->
                        SessionItem(
                            session = session,
                            isCurrent = session.sessionId == currentSessionId,
                            onClick = { onSessionSelected(session.sessionId) },
                            onDelete = { onSessionDeleted(session.sessionId) },
                            onPin = { onSessionPinned(session.sessionId) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}

/**
 * 会话项
 */
@Composable
private fun SessionItem(
    session: ChatSessionInfo,
    isCurrent: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .interactiveScale(interactionSource)
            .liquidGlass(
                shape = RoundedCornerShape(16.dp),
                tint = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f) 
                       else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.foundation.LocalIndication.current,
                onClick = onClick
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 置顶标记
            if (session.isPinned) {
                Icon(
                    imageVector = Icons.Default.PushPin,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = session.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = session.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${session.messageCount} 条消息 · ${formatTime(session.updatedAt)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
            
            // 更多操作
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(if (session.isPinned) "取消置顶" else "置顶") },
                        leadingIcon = {
                            Icon(
                                if (session.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                                null
                            )
                        },
                        onClick = {
                            onPin()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}

/**
 * 格式化时间
 */
private fun formatTime(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60 * 1000 -> "刚刚"
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> {
            val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

/**
 * 快捷功能行
 */
@Composable
private fun QuickActionsRow(
    onCalorieAssessment: () -> Unit,
    onMealPlanning: () -> Unit,
    onHealthConsult: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionChip(
            icon = Icons.Default.Assessment,
            label = "热量评估",
            onClick = onCalorieAssessment
        )
        QuickActionChip(
            icon = Icons.Default.RestaurantMenu,
            label = "菜谱规划",
            onClick = onMealPlanning
        )
        QuickActionChip(
            icon = Icons.Default.HealthAndSafety,
            label = "健康咨询",
            onClick = onHealthConsult
        )
    }
}

/**
 * 快捷功能芯片
 */
@Composable
private fun QuickActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(label) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    )
}

/**
 * 聊天消息项
 */
@Composable
private fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.isFromUser
    
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        
        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            Box(
                modifier = Modifier.liquidGlass(
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    ),
                    tint = if (isUser) MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                           else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    blurRadius = 30f
                )
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(12.dp),
                    color = if (isUser) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            
            Text(
                text = formatMessageTime(message.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 4.dp, start = 4.dp, end = 4.dp)
            )
        }
        
        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // 用户头像
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * 格式化消息时间
 */
private fun formatMessageTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

/**
 * 正在输入指示器
 */
@Composable
private fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
        
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(3) { index ->
                    val infiniteTransition = rememberInfiniteTransition(label = "")
                    val scale by infiniteTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 100),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = ""
                    )
                    
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    )
                }
            }
        }
    }
}

/**
 * 聊天输入区域
 */
@Composable
private fun ChatInputArea(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    remainingCalls: Int,
    dailyLimit: Int
) {
    Column {
        // 剩余调用次数提示
        if (remainingCalls <= 3) {
            Surface(
                color = if (remainingCalls == 0) {
                    MaterialTheme.colorScheme.errorContainer
                } else {
                    MaterialTheme.colorScheme.tertiaryContainer
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (remainingCalls == 0) {
                        "今日API调用次数已用完，请明天再试"
                    } else {
                        "今日剩余 $remainingCalls 次调用"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remainingCalls == 0) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else {
                        MaterialTheme.colorScheme.onTertiaryContainer
                    },
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
        
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("输入问题或需求...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = remainingCalls > 0 && !isLoading
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                FloatingActionButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp),
                    containerColor = if (isLoading || value.isBlank() || remainingCalls == 0) {
                        MaterialTheme.colorScheme.surfaceVariant
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    contentColor = if (isLoading || value.isBlank() || remainingCalls == 0) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    } else {
                        MaterialTheme.colorScheme.onPrimary
                    }
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "发送"
                        )
                    }
                }
            }
        }
    }
}

/**
 * 聊天消息数据类
 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * 会话信息数据类
 */
data class ChatSessionInfo(
    val sessionId: String,
    val title: String,
    val lastMessage: String,
    val updatedAt: Long,
    val messageCount: Int,
    val isPinned: Boolean
)
