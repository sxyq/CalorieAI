package com.calorieai.app.ui.components.chat

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 聊天会话数据类
 */
data class ChatSession(
    val id: String,
    val title: String,
    val lastMessage: String,
    val timestamp: Long,
    val messageCount: Int,
    val isPinned: Boolean = false,
    val category: ChatCategory = ChatCategory.GENERAL
)

/**
 * 聊天分类
 */
enum class ChatCategory(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    GENERAL("一般", Icons.Default.Chat),
    DIET("饮食建议", Icons.Default.Restaurant),
    EXERCISE("运动计划", Icons.Default.FitnessCenter),
    WEIGHT("体重管理", Icons.Default.MonitorWeight),
    HEALTH("健康咨询", Icons.Default.Favorite)
}

/**
 * 历史对话管理器 - Glass 毛玻璃风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryManager(
    sessions: List<ChatSession>,
    onSessionClick: (ChatSession) -> Unit,
    onSessionDelete: (ChatSession) -> Unit,
    onSessionPin: (ChatSession) -> Unit,
    onNewChat: () -> Unit,
    onSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedSessionId: String? = null,
    isDark: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    
    val backgroundColor = if (isDark) GlassDarkColors.Background else GlassLightColors.Background
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // 顶部栏
        if (showSearch) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    onSearch(it)
                },
                onClose = { 
                    showSearch = false
                    searchQuery = ""
                    onSearch("")
                },
                isDark = isDark
            )
        } else {
            TopAppBar(
                title = { 
                    Text(
                        "历史对话",
                        color = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
                    ) 
                },
                actions = {
                    IconButton(onClick = { showSearch = true }) {
                        Icon(
                            Icons.Default.Search, 
                            contentDescription = "搜索",
                            tint = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
        
        // 新建对话按钮
        NewChatButton(onClick = onNewChat, isDark = isDark)
        
        // 分类筛选
        CategoryFilter(
            categories = ChatCategory.values().toList(),
            selectedCategory = null,
            onCategorySelected = { },
            isDark = isDark
        )
        
        // 会话列表
        if (sessions.isEmpty()) {
            EmptyHistoryView(isDark = isDark)
        } else {
            ChatSessionList(
                sessions = sessions,
                selectedSessionId = selectedSessionId,
                onSessionClick = onSessionClick,
                onSessionDelete = onSessionDelete,
                onSessionPin = onSessionPin,
                isDark = isDark
            )
        }
    }
}

/**
 * 搜索栏 - Glass 风格
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onClose: () -> Unit,
    isDark: Boolean
) {
    val containerColor = if (isDark) {
        GlassDarkColors.SurfaceContainerHigh
    } else {
        GlassLightColors.SurfaceContainerHigh
    }
    
    TextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { 
            Text(
                "搜索对话...",
                color = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
            ) 
        },
        leadingIcon = {
            Icon(
                Icons.Default.Search, 
                contentDescription = null,
                tint = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
            )
        },
        trailingIcon = {
            IconButton(onClick = onClose) {
                Icon(
                    Icons.Default.Close, 
                    contentDescription = "关闭",
                    tint = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface,
            unfocusedTextColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
        )
    )
}

/**
 * 新建对话按钮 - Glass 毛玻璃风格
 */
@Composable
private fun NewChatButton(onClick: () -> Unit, isDark: Boolean) {
    val containerColor = if (isDark) {
        GlassDarkColors.PrimaryContainer
    } else {
        GlassLightColors.PrimaryContainer
    }
    
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val onPrimaryColor = if (isDark) GlassDarkColors.OnPrimary else GlassLightColors.OnPrimary
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = primaryColor,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = onPrimaryColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "新建对话",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
                )
                Text(
                    text = "开始与AI助手的新对话",
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor
                )
            }
            
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = onSurfaceVariantColor
            )
        }
    }
}

/**
 * 分类筛选 - Glass 风格
 */
@Composable
private fun CategoryFilter(
    categories: List<ChatCategory>,
    selectedCategory: ChatCategory?,
    onCategorySelected: (ChatCategory?) -> Unit,
    isDark: Boolean
) {
    var selected by remember { mutableStateOf(selectedCategory) }
    
    val selectedContainerColor = if (isDark) GlassDarkColors.PrimaryContainer else GlassLightColors.PrimaryContainer
    val selectedLabelColor = if (isDark) GlassDarkColors.OnPrimaryContainer else GlassLightColors.OnPrimaryContainer
    val unselectedContainerColor = if (isDark) {
        GlassDarkColors.SurfaceContainer.copy(alpha = 0.5f)
    } else {
        GlassLightColors.SurfaceContainer.copy(alpha = 0.5f)
    }
    val unselectedLabelColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selected == null,
            onClick = { 
                selected = null
                onCategorySelected(null)
            },
            label = { Text("全部") },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = selectedContainerColor,
                selectedLabelColor = selectedLabelColor,
                containerColor = unselectedContainerColor,
                labelColor = unselectedLabelColor
            )
        )
        
        categories.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { 
                    selected = category
                    onCategorySelected(category)
                },
                label = { Text(category.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = selectedContainerColor,
                    selectedLabelColor = selectedLabelColor,
                    containerColor = unselectedContainerColor,
                    labelColor = unselectedLabelColor
                )
            )
        }
    }
}

/**
 * 会话列表 - Glass 风格带动效
 */
@Composable
private fun ChatSessionList(
    sessions: List<ChatSession>,
    selectedSessionId: String?,
    onSessionClick: (ChatSession) -> Unit,
    onSessionDelete: (ChatSession) -> Unit,
    onSessionPin: (ChatSession) -> Unit,
    isDark: Boolean
) {
    val (pinned, unpinned) = sessions.partition { it.isPinned }
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 置顶会话
        if (pinned.isNotEmpty()) {
            item {
                Text(
                    text = "置顶",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(pinned, key = { it.id }) { session ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + 
                           slideInVertically(animationSpec = tween(300)) { it / 2 }
                ) {
                    ChatSessionItem(
                        session = session,
                        isSelected = session.id == selectedSessionId,
                        onClick = { onSessionClick(session) },
                        onDelete = { onSessionDelete(session) },
                        onPin = { onSessionPin(session) },
                        isDark = isDark
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        
        // 普通会话
        if (unpinned.isNotEmpty()) {
            item {
                Text(
                    text = "最近对话",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            
            items(unpinned, key = { it.id }) { session ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)) + 
                           slideInVertically(animationSpec = tween(300)) { it / 2 }
                ) {
                    ChatSessionItem(
                        session = session,
                        isSelected = session.id == selectedSessionId,
                        onClick = { onSessionClick(session) },
                        onDelete = { onSessionDelete(session) },
                        onPin = { onSessionPin(session) },
                        isDark = isDark
                    )
                }
            }
        }
    }
}

/**
 * 会话项 - Glass 毛玻璃风格
 */
@Composable
private fun ChatSessionItem(
    session: ChatSession,
    isSelected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onPin: () -> Unit,
    isDark: Boolean
) {
    var showMenu by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "itemScale"
    )
    
    val backgroundColor = when {
        isSelected -> if (isDark) {
            GlassDarkColors.PrimaryContainer.copy(alpha = 0.6f)
        } else {
            GlassLightColors.PrimaryContainer.copy(alpha = 0.6f)
        }
        isDark -> GlassDarkColors.SurfaceContainer.copy(alpha = 0.5f)
        else -> GlassLightColors.SurfaceContainer.copy(alpha = 0.5f)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }
    
    val primaryColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val onSurfaceColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    val onSurfaceVariantColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 分类图标
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = primaryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = session.category.icon,
                    contentDescription = null,
                    tint = primaryColor
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 内容
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        color = onSurfaceColor
                    )
                    
                    if (session.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "置顶",
                            tint = primaryColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Text(
                    text = session.lastMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = onSurfaceVariantColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "${formatTimestamp(session.timestamp)} · ${session.messageCount}条消息",
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariantColor.copy(alpha = 0.7f)
                )
            }
            
            // 菜单按钮
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "更多",
                        tint = onSurfaceVariantColor
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(
                        if (isDark) {
                            GlassDarkColors.SurfaceContainerHigh
                        } else {
                            GlassLightColors.SurfaceContainerHigh
                        }
                    )
                ) {
                    DropdownMenuItem(
                        text = { Text(if (session.isPinned) "取消置顶" else "置顶") },
                        leadingIcon = {
                            Icon(Icons.Default.PushPin, contentDescription = null)
                        },
                        onClick = {
                            onPin()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("删除") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = if (isDark) GlassDarkColors.Error else GlassLightColors.Error
                            )
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
 * 空历史视图 - Glass 风格
 */
@Composable
private fun EmptyHistoryView(isDark: Boolean) {
    val iconColor = if (isDark) {
        GlassDarkColors.OnSurfaceVariant.copy(alpha = 0.5f)
    } else {
        GlassLightColors.OnSurfaceVariant.copy(alpha = 0.5f)
    }
    
    val textColor = if (isDark) GlassDarkColors.OnSurfaceVariant else GlassLightColors.OnSurfaceVariant
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ChatBubbleOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = iconColor
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "暂无历史对话",
            style = MaterialTheme.typography.titleMedium,
            color = textColor
        )
        
        Text(
            text = "点击上方按钮开始新对话",
            style = MaterialTheme.typography.bodyMedium,
            color = textColor.copy(alpha = 0.7f)
        )
    }
}

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    val date = Date(timestamp)
    val now = Date()
    val diff = now.time - date.time
    
    return when {
        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)}分钟前"
        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)}小时前"
        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)}天前"
        else -> SimpleDateFormat("MM/dd", Locale.getDefault()).format(date)
    }
}
