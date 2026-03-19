package com.calorieai.app.ui.screens.ai

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.theme.GlassLightColors
import com.calorieai.app.ui.theme.GlassDarkColors
import kotlinx.coroutines.launch
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalContext
import java.text.SimpleDateFormat
import java.util.*
import com.calorieai.app.ui.components.markdown.MarkdownText
import com.calorieai.app.ui.components.markdown.MarkdownConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIChatScreen(
    onNavigateBack: () -> Unit,
    initialSessionId: String? = null,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showHistory by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(initialSessionId) {
        val sessionId = initialSessionId?.takeIf { it.isNotBlank() } ?: return@LaunchedEffect
        if (uiState.currentSessionId != sessionId) {
            viewModel.loadSession(sessionId)
        }
    }

    Scaffold(
        containerColor = if (isDark) Color(0xFF0D0D0D) else Color(0xFFFAFAFA),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.tertiary
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "AI助手",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton({ viewModel.startNewSession() }) {
                        Icon(Icons.Outlined.AddComment, "新对话")
                    }
                    IconButton({ showHistory = true }) {
                        Icon(Icons.Outlined.History, "历史")
                    }
                    var showMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton({ showMenu = true }) {
                            Icon(Icons.Default.MoreVert, "更多")
                        }
                        DropdownMenu(showMenu, { showMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("清空对话") },
                                leadingIcon = { Icon(Icons.Outlined.DeleteSweep, null) },
                                onClick = { viewModel.clearCurrentChat(); showMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("删除对话") },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                                onClick = { viewModel.deleteCurrentSession(); showMenu = false }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (uiState.messages.isEmpty()) {
                EmptyStateContent(viewModel, isDark)
            } else {
                QuickActionsBar(viewModel, isDark)
                Spacer(Modifier.height(8.dp))
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.messages, key = { it.id }) { msg ->
                        AnimatedMessageItem(msg, isDark)
                    }
                    if (uiState.isTyping) {
                        item { TypingIndicator(isDark) }
                    }
                }
            }

            ChatInputBar(
                value = uiState.inputText,
                onValueChange = viewModel::onInputChange,
                onSend = viewModel::sendMessage,
                isLoading = uiState.isLoading,
                remaining = uiState.remainingCalls,
                isDark = isDark
            )
        }
    }

    if (showHistory) {
        HistoryDialog(uiState.chatSessions, uiState.currentSessionId, { showHistory = false }, viewModel)
    }
}

@Composable
private fun EmptyStateContent(viewModel: AIChatViewModel, isDark: Boolean) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))
        
        Box(
            Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(0.2f),
                            MaterialTheme.colorScheme.tertiary.copy(0.2f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                null,
                Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Spacer(Modifier.height(24.dp))
        
        Text(
            "你好，我是AI助手",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            "你的专属营养健康顾问\n可以帮你分析饮食、规划菜谱",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        
        Spacer(Modifier.height(32.dp))
        
        Text(
            "快捷功能",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(Modifier.height(12.dp))
        
        QuickActionCards(viewModel, isDark)
        
        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun QuickActionCards(viewModel: AIChatViewModel, isDark: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        QuickActionCard(
            icon = Icons.Default.Assessment,
            title = "热量评估",
            subtitle = "分析一周饮食，评估热量摄入",
            gradientColors = listOf(
                Color(0xFF6366F1),
                Color(0xFF8B5CF6)
            ),
            onClick = { viewModel.startCalorieAssessment() }
        )
        
        QuickActionCard(
            icon = Icons.Default.RestaurantMenu,
            title = "菜谱规划",
            subtitle = "根据你的饮食习惯推荐健康菜谱",
            gradientColors = listOf(
                Color(0xFF10B981),
                Color(0xFF059669)
            ),
            onClick = { viewModel.startMealPlanning() }
        )
        
        QuickActionCard(
            icon = Icons.Default.HealthAndSafety,
            title = "健康咨询",
            subtitle = "解答营养健康相关问题",
            gradientColors = listOf(
                Color(0xFFF59E0B),
                Color(0xFFD97706)
            ),
            onClick = { viewModel.startHealthConsult() }
        )
    }
}

@Composable
private fun QuickActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardScale"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.linearGradient(gradientColors))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                null,
                Modifier.size(24.dp),
                tint = Color.White
            )
        }
        
        Spacer(Modifier.width(16.dp))
        
        Column {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(Modifier.height(2.dp))
            Text(
                subtitle,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f)
            )
        }
        
        Spacer(Modifier.weight(1f))
        
        Icon(
            Icons.Default.ChevronRight,
            null,
            tint = Color.White.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun QuickActionsBar(viewModel: AIChatViewModel, isDark: Boolean) {
    val actions = listOf(
        Triple(Icons.Default.Assessment, "热量评估") { viewModel.startCalorieAssessment() },
        Triple(Icons.Default.RestaurantMenu, "菜谱规划") { viewModel.startMealPlanning() },
        Triple(Icons.Default.HealthAndSafety, "健康咨询") { viewModel.startHealthConsult() }
    )
    
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        actions.forEach { (icon, label, action) ->
            CompactActionChip(icon, label, action, isDark, Modifier.weight(1f))
        }
    }
}

@Composable
private fun CompactActionChip(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipScale"
    )
    
    val bgColor = if (isDark) Color(0xFF1A1A1A) else Color(0xFFF0F0F0)
    
    Row(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            icon,
            null,
            Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun AnimatedMessageItem(msg: ChatMessage, isDark: Boolean) {
    val isUser = msg.isFromUser
    val bgColor = if (isUser) {
        MaterialTheme.colorScheme.primary
    } else {
        if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
    }
    val textColor = if (isUser) {
        Color.White
    } else {
        if (isDark) Color(0xFFE0E0E0) else Color(0xFF1A1A1A)
    }

    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
            modifier = Modifier.weight(1f, fill = false)
        ) {
            val displayContent = msg.content
            Box(
                Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isUser) {
                    Text(
                        displayContent,
                        color = textColor,
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                } else {
                    AssistantMessageContent(
                        messageId = msg.id,
                        text = displayContent,
                        isDark = isDark
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                formatTime(msg.timestamp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
        }

        if (isUser) {
            Spacer(Modifier.width(8.dp))
            Box(
                Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Person,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun AssistantMessageContent(
    messageId: String,
    text: String,
    isDark: Boolean
) {
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val sections = remember(text) { text.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() } }
    val needCollapse = sections.size > 4 || text.length > 420
    var expanded by rememberSaveable(messageId) { mutableStateOf(false) }
    val visibleText = if (!needCollapse || expanded) {
        text
    } else {
        sections.take(3).joinToString("\n\n")
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        MarkdownText(
            text = visibleText,
            isDark = isDark,
            config = MarkdownConfig.ChatReadable
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(
                onClick = {
                    clipboardManager.setText(AnnotatedString(stripMarkdownForCopy(text)))
                    Toast.makeText(context, "已复制内容", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("复制内容", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            if (needCollapse) {
                TextButton(
                    onClick = { expanded = !expanded },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(if (expanded) "收起详情" else "展开详情", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun NormalAssistantSectionCard(
    section: AssistantSection,
    isDark: Boolean,
    onCopy: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF1F2230) else Color(0xFFF3F7FF)
    val cardBorder = if (isDark) Color(0xFF3A4B6A) else Color(0xFFBFD7FF)
    val titleColor = if (isDark) Color(0xFF9CC2FF) else Color(0xFF285EA8)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
        color = cardBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            section.title?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            MarkdownText(
                text = section.body.ifBlank { section.fullText },
                isDark = isDark,
                config = MarkdownConfig.ChatReadable
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("复制本段", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

private fun stripMarkdownForCopy(text: String): String {
    return text
        .replace("**", "")
        .replace("__", "")
        .replace("`", "")
        .replace(Regex("(?m)^#{1,6}\\s*"), "")
        .replace(Regex("(?m)^\\s*>\\s?"), "")
        .replace(Regex("\\[([^\\]]+)]\\(([^)]+)\\)"), "$1")
        .replace(Regex("(?m)^\\s*[-*+]\\s+"), "• ")
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}

@Composable
private fun HighlightedAssistantSectionCard(
    section: AssistantSection,
    isDark: Boolean,
    onCopy: () -> Unit
) {
    val cardBg = if (isDark) Color(0xFF242417) else Color(0xFFFFF8E8)
    val cardBorder = if (isDark) Color(0xFF5F5322) else Color(0xFFFFD777)
    val titleColor = if (isDark) Color(0xFFFFD777) else Color(0xFF8A5D00)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
        color = cardBg,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            section.title?.let {
                Text(
                    text = it,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = titleColor
                )
            }

            MarkdownText(
                text = section.body.ifBlank { section.fullText },
                isDark = isDark,
                config = MarkdownConfig.ChatReadable
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("复制本段", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun TypingIndicator(isDark: Boolean) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                null,
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(Modifier.width(8.dp))
        
        val bgColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF5F5F5)
        
        Row(
            Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(3) { index ->
                val infiniteTransition = rememberInfiniteTransition(label = "typing$index")
                val scale by infiniteTransition.animateFloat(
                    initialValue = 0.5f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(600, delayMillis = index * 120),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "scale$index"
                )
                Box(
                    Modifier
                        .size(8.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                )
            }
        }
    }
}

@Composable
private fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean,
    remaining: Int,
    isDark: Boolean
) {
    Column {
        if (remaining <= 3) {
            Surface(
                color = if (remaining == 0) 
                    MaterialTheme.colorScheme.errorContainer 
                else 
                    MaterialTheme.colorScheme.tertiaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (remaining == 0) "今日API调用次数已用完" else "今日剩余 $remaining 次调用",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining == 0) 
                        MaterialTheme.colorScheme.onErrorContainer 
                    else 
                        MaterialTheme.colorScheme.onTertiaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }

        val bgColor = if (isDark) Color(0xFF1A1A1A) else Color.White
        
        Surface(
            tonalElevation = 2.dp,
            color = bgColor,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    placeholder = { 
                        Text(
                            "输入问题...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        ) 
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    enabled = remaining > 0 && !isLoading,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = if (isDark) Color(0xFF333333) else Color(0xFFE0E0E0)
                    )
                )
                Spacer(Modifier.width(12.dp))
                
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.9f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "sendScale"
                )
                val disabled = value.isBlank() || remaining == 0
                
                FloatingActionButton(
                    onClick = onSend,
                    modifier = Modifier.size(48.dp).scale(scale),
                    containerColor = if (disabled) 
                        MaterialTheme.colorScheme.surfaceVariant 
                    else 
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (disabled) 
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f) 
                    else 
                        MaterialTheme.colorScheme.onPrimary
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Send, "发送")
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryDialog(
    sessions: List<ChatSessionInfo>,
    currentId: String,
    onDismiss: () -> Unit,
    viewModel: AIChatViewModel
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) { 
                Text("历史对话")
                Text(
                    "${sessions.size}个",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            if (sessions.isEmpty()) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Outlined.History,
                            null,
                            Modifier.size(48.dp),
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("暂无历史对话", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(sessions) { session ->
                        SessionItem(session, session.sessionId == currentId, viewModel, onDismiss)
                    }
                }
            }
        },
        confirmButton = { TextButton(onDismiss) { Text("关闭") } }
    )
}

@Composable
private fun SessionItem(
    session: ChatSessionInfo,
    isCurrent: Boolean,
    viewModel: AIChatViewModel,
    onSelect: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isCurrent) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) 
                else 
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
            .clickable { viewModel.loadSession(session.sessionId); onSelect() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (session.isPinned) {
            Icon(
                Icons.Default.PushPin,
                null,
                Modifier.size(14.dp),
                MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(8.dp))
        }
        
        Column(Modifier.weight(1f)) {
            Text(
                session.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                session.lastMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Text(
                "${session.messageCount}条 · ${formatTime(session.updatedAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
        
        Box {
            IconButton(
                { showMenu = true },
                Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.MoreVert, null, Modifier.size(18.dp))
            }
            DropdownMenu(showMenu, { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text(if (session.isPinned) "取消置顶" else "置顶") },
                    leadingIcon = { Icon(Icons.Outlined.PushPin, null) },
                    onClick = { viewModel.togglePinSession(session.sessionId); showMenu = false }
                )
                DropdownMenuItem(
                    text = { Text("删除") },
                    leadingIcon = { 
                        Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) 
                    },
                    onClick = { viewModel.deleteSession(session.sessionId); showMenu = false }
                )
            }
        }
    }
}

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

private data class AssistantSection(
    val fullText: String,
    val title: String?,
    val body: String,
    val highlighted: Boolean
)

private val highlightedSectionKeywords = listOf(
    "总结",
    "结论",
    "执行步骤",
    "步骤",
    "建议",
    "注意事项",
    "行动计划",
    "下一步",
    "提醒"
)

private fun parseAssistantSections(text: String): List<AssistantSection> {
    val rawSections = text.split(Regex("\\n\\s*\\n")).filter { it.isNotBlank() }
    return rawSections.map { raw ->
        val trimmed = raw.trim()
        val lines = trimmed.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) {
            return@map AssistantSection(
                fullText = trimmed,
                title = null,
                body = trimmed,
                highlighted = false
            )
        }

        val firstLine = lines.first()
        val headingMatch = Regex("^#{1,4}\\s*(.+)$").find(firstLine)
        val plainTitleMatch = Regex("^([\\u4e00-\\u9fa5A-Za-z0-9 ]{2,20})\\s*[：:]\\s*(.*)$").find(firstLine)

        val extractedTitle = when {
            headingMatch != null -> headingMatch.groupValues[1].trim()
            plainTitleMatch != null -> plainTitleMatch.groupValues[1].trim()
            else -> null
        }

        val body = when {
            headingMatch != null -> lines.drop(1).joinToString("\n").trim()
            plainTitleMatch != null -> {
                val firstBody = plainTitleMatch.groupValues[2].trim()
                val tail = lines.drop(1).joinToString("\n").trim()
                listOf(firstBody, tail).filter { it.isNotBlank() }.joinToString("\n")
            }
            else -> trimmed
        }

        val titleForCheck = extractedTitle ?: firstLine
        val highlighted = highlightedSectionKeywords.any { keyword ->
            titleForCheck.contains(keyword, ignoreCase = false)
        }

        AssistantSection(
            fullText = trimmed,
            title = extractedTitle,
            body = body.ifBlank { trimmed },
            highlighted = highlighted
        )
    }
}
