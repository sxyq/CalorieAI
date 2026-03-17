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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.liquidGlass
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/** 全屏AI聊天界面 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(onNavigateBack: () -> Unit, viewModel: AIChatViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showHistory by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { HeaderTitle(uiState.currentSessionTitle) },
                navigationIcon = { IconButton(onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") } },
                actions = {
                    IconButton({ viewModel.startNewSession() }) { Icon(Icons.Default.AddComment, "新对话") }
                    IconButton({ showHistory = true }) { Icon(Icons.Default.History, "历史") }
                    OverflowMenu(viewModel)
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // 快捷功能
            QuickActionsRow(viewModel)
            HorizontalDivider()

            // 消息列表
            Box(Modifier.weight(1f)) {
                if (uiState.messages.isEmpty()) {
                    EmptyState { viewModel.startHealthConsult() }
                } else {
                    MessageList(uiState.messages, uiState.isTyping, listState)
                }
            }

            // 输入区
            ChatInput(uiState.inputText, viewModel::onInputChange, viewModel::sendMessage, uiState.isLoading, uiState.remainingCalls)
        }
    }

    if (showHistory) {
        HistoryDialog(uiState.chatSessions, uiState.currentSessionId, { showHistory = false }, viewModel)
    }
}

/** 标题 */
@Composable
private fun HeaderTitle(sessionTitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text("AI助手", style = MaterialTheme.typography.titleMedium)
            if (sessionTitle.isNotBlank()) {
                Text(sessionTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

/** 更多菜单 */
@Composable
private fun OverflowMenu(viewModel: AIChatViewModel) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        IconButton({ showMenu = true }) { Icon(Icons.Default.MoreVert, "更多") }
        DropdownMenu(showMenu, { showMenu = false }) {
            DropdownMenuItem(
                text = { Text("清空对话") },
                leadingIcon = { Icon(Icons.Default.DeleteSweep, null) },
                onClick = { viewModel.clearCurrentChat(); showMenu = false }
            )
            DropdownMenuItem(
                text = { Text("删除对话") },
                leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                onClick = { viewModel.deleteCurrentSession(); showMenu = false }
            )
        }
    }
}

/** 快捷功能行 */
@Composable
private fun QuickActionsRow(viewModel: AIChatViewModel) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
        QuickChip(Icons.Default.Assessment, "热量评估") { viewModel.startCalorieAssessment() }
        QuickChip(Icons.Default.RestaurantMenu, "菜谱规划") { viewModel.startMealPlanning() }
        QuickChip(Icons.Default.HealthAndSafety, "健康咨询") { viewModel.startHealthConsult() }
    }
}

/** 快捷功能芯片 */
@Composable
private fun QuickChip(icon: ImageVector, label: String, onClick: () -> Unit) {
    AssistChip(onClick, { Text(label) }, leadingIcon = { Icon(icon, null, Modifier.size(18.dp)) })
}

/** 空状态 */
@Composable
private fun EmptyState(onStart: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.SmartToy, null, Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary.copy(0.5f))
        Spacer(Modifier.height(24.dp))
        Text("AI助手", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("你的专属营养健康顾问", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Button(onStart, Modifier.fillMaxWidth(0.7f)) { Text("开始对话") }
    }
}

/** 消息列表 */
@Composable
private fun MessageList(messages: List<ChatMessage>, isTyping: Boolean, listState: androidx.compose.foundation.lazy.LazyListState) {
    val scope = rememberCoroutineScope()

    LazyColumn(state = listState, modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items(messages, key = { it.id }) { msg ->
            MessageItem(msg)
        }
        if (isTyping) {
            item { TypingIndicator() }
        }
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) scope.launch { listState.animateScrollToItem(messages.size - 1) }
    }
}

/** 消息项 */
@Composable
private fun MessageItem(msg: ChatMessage) {
    val isUser = msg.isFromUser
    Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start) {
        if (!isUser) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start, modifier = Modifier.weight(1f, fill = false)) {
            Box(
                Modifier
                    .liquidGlass(
                        RoundedCornerShape(16.dp),
                        if (isUser) MaterialTheme.colorScheme.primary.copy(0.6f) else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                        30f
                    )
                    .padding(12.dp)
            ) {
                Text(msg.content, color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(formatTime(msg.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f), modifier = Modifier.padding(top = 4.dp))
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }
    }
}

/** 输入区域 */
@Composable
private fun ChatInput(value: String, onValueChange: (String) -> Unit, onSend: () -> Unit, isLoading: Boolean, remaining: Int) {
    Column {
        // 剩余次数提示
        if (remaining <= 3) {
            Surface(color = if (remaining == 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
                Text(
                    if (remaining == 0) "今日API调用次数已用完" else "今日剩余 $remaining 次调用",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining == 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }

        Surface(tonalElevation = 2.dp, color = MaterialTheme.colorScheme.surface) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { Text("输入问题...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = remaining > 0 && !isLoading
                )
                Spacer(Modifier.width(8.dp))
                SendButton(isLoading, value.isBlank() || remaining == 0, onSend)
            }
        }
    }
}

/** 发送按钮 */
@Composable
private fun SendButton(isLoading: Boolean, disabled: Boolean, onSend: () -> Unit) {
    val scale by animateFloatAsState(if (disabled) 0.85f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    FloatingActionButton(
        onSend,
        Modifier.size(48.dp).scale(scale),
        containerColor = if (disabled) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
        contentColor = if (disabled) MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f) else MaterialTheme.colorScheme.onPrimary
    ) {
        if (isLoading) {
            CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Default.Send, "发送")
        }
    }
}

/** 打字指示器 */
@Composable
private fun TypingIndicator() {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.SmartToy, null, tint = Color.White, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(8.dp))
        Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant, tonalElevation = 2.dp) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) { index ->
                    val scale by rememberInfiniteTransition("typing$index").animateFloat(0.5f, 1f, infiniteRepeatable(tween(600, delayMillis = index * 100), RepeatMode.Reverse))
                    Box(Modifier.size(8.dp).scale(scale).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(0.6f)))
                }
            }
        }
    }
}

/** 历史对话框 */
@Composable
private fun HistoryDialog(sessions: List<ChatSessionInfo>, currentId: String, onDismiss: () -> Unit, viewModel: AIChatViewModel) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("历史对话"); Text("${sessions.size}个", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
        text = {
            if (sessions.isEmpty()) {
                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.History, null, Modifier.size(48.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(0.5f))
                        Spacer(Modifier.height(8.dp))
                        Text("暂无历史对话", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(sessions) { session ->
                        SessionItem(session, session.sessionId == currentId, viewModel, onDismiss)
                    }
                }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("关闭") } }
    )
}

/** 会话项 */
@Composable
private fun SessionItem(session: ChatSessionInfo, isCurrent: Boolean, viewModel: AIChatViewModel, onSelect: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(0.6f) else MaterialTheme.colorScheme.surface.copy(0.5f))
            .clickable { viewModel.loadSession(session.sessionId); onSelect() }
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (session.isPinned) {
                Icon(Icons.Default.PushPin, null, Modifier.size(16.dp), MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(session.title, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Medium)
                Text(session.lastMessage, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${session.messageCount}条 · ${formatTime(session.updatedAt)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.7f))
            }
            Box {
                IconButton({ showMenu = true }, Modifier.size(32.dp)) { Icon(Icons.Default.MoreVert, null, Modifier.size(20.dp)) }
                DropdownMenu(showMenu, { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (session.isPinned) "取消置顶" else "置顶") },
                        leadingIcon = { Icon(Icons.Default.PushPin, null) },
                        onClick = { viewModel.togglePinSession(session.sessionId); showMenu = false }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                        onClick = { viewModel.deleteSession(session.sessionId); showMenu = false }
                    )
                }
            }
        }
    }
}

/** 格式化时间 */
private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60_000 -> "刚刚"
        diff < 3_600_000 -> "${diff / 60_000}分钟前"
        diff < 86_400_000 -> "${diff / 3_600_000}小时前"
        diff < 604_800_000 -> "${diff / 86_400_000}天前"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(Date(timestamp))
    }
}

/** 数据类 */
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatSessionInfo(
    val sessionId: String,
    val title: String,
    val lastMessage: String,
    val updatedAt: Long,
    val messageCount: Int,
    val isPinned: Boolean
)
