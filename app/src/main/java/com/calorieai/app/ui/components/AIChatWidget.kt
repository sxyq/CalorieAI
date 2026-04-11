package com.calorieai.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.calorieai.app.ui.components.markdown.MarkdownConfig
import com.calorieai.app.ui.components.markdown.MarkdownText
import com.calorieai.app.ui.screens.ai.AIQuickAction
import com.calorieai.app.ui.screens.ai.AIChatViewModel
import com.calorieai.app.ui.screens.ai.ChatMessage
import com.calorieai.app.ui.screens.ai.QuickActionRouter
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
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
    modifier: Modifier = Modifier,
    viewModel: AIChatViewModel = hiltViewModel()
) {
    AnimatedContent(
        targetState = widgetState,
        label = "ai_widget_transition",
        modifier = modifier
    ) { state ->
        when (state) {
            AIWidgetState.FLOATING -> FloatingEntryButton(
                onClick = {
                    viewModel.startNewSession()
                    onWidgetStateChange(AIWidgetState.MINI)
                }
            )

            AIWidgetState.MINI -> AIChatMiniPanel(
                mode = mode,
                onDismiss = { onWidgetStateChange(AIWidgetState.FLOATING) },
                onExpand = { sessionId ->
                    onWidgetStateChange(AIWidgetState.FULLSCREEN)
                    onExpandToFullScreen(sessionId)
                },
                viewModel = viewModel
            )

            AIWidgetState.FULLSCREEN -> Box(modifier = Modifier.size(1.dp))
        }
    }
}

@Composable
private fun FloatingEntryButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI assistant",
            tint = Color.White
        )
    }
}

@Composable
private fun AIChatMiniPanel(
    mode: AIWidgetMode,
    onDismiss: () -> Unit,
    onExpand: (String) -> Unit,
    viewModel: AIChatViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    var inputText by remember(uiState.currentSessionId) { mutableStateOf("") }

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
                onExpand = {
                    viewModel.persistCurrentSession()
                    onExpand(uiState.currentSessionId)
                },
                onDismiss = onDismiss
            )

            if (uiState.messages.isEmpty()) {
                Box(modifier = Modifier.weight(1f)) {
                    QuickActions(
                        mode = mode,
                        onAction = { action -> QuickActionRouter.dispatch(viewModel, action) }
                    )
                }
            } else {
                MessageList(
                    messages = uiState.messages,
                    isLoading = uiState.isLoading,
                    listState = listState,
                    modifier = Modifier.weight(1f)
                )
            }

            InputRow(
                inputText = inputText,
                onInputChange = { inputText = it },
                isLoading = uiState.isLoading,
                onSend = {
                    if (inputText.isNotBlank()) {
                        viewModel.sendMessage(inputText.trim())
                        inputText = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun MiniHeader(
    isLoading: Boolean,
    onExpand: () -> Unit,
    onDismiss: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SmartToy,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "AI 助手",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (isLoading) "正在分析中..." else "随时可用",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onExpand) {
            Icon(Icons.Default.OpenInFull, contentDescription = "expand")
        }
        IconButton(onClick = onDismiss) {
            Icon(Icons.Default.Close, contentDescription = "close")
        }
    }
}

@Composable
private fun QuickActions(
    mode: AIWidgetMode,
    onAction: (AIQuickAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "快捷分析",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (mode == AIWidgetMode.HEALTH_ONLY) {
            QuickActionCard(
                title = "健康咨询",
                icon = Icons.Default.HealthAndSafety,
                onClick = { onAction(AIQuickAction.HEALTH_CONSULT) }
            )
        } else {
            QuickActionCard(
                title = "热量评估",
                icon = Icons.Default.Assessment,
                onClick = { onAction(AIQuickAction.CALORIE_ASSESSMENT) }
            )
            QuickActionCard(
                title = "今日餐次规划",
                icon = Icons.Default.RestaurantMenu,
                onClick = { onAction(AIQuickAction.MEAL_PLANNING) }
            )
            QuickActionCard(
                title = "健康咨询",
                icon = Icons.Default.HealthAndSafety,
                onClick = { onAction(AIQuickAction.HEALTH_CONSULT) }
            )
        }

        Text(
            text = "快捷方式会带入最近本地记录进行分析。",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.65f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun MessageList(
    messages: List<ChatMessage>,
    isLoading: Boolean,
    listState: androidx.compose.foundation.lazy.LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(messages, key = { it.id }) { message ->
            MessageBubble(message = message)
        }
        if (isLoading) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI 正在生成中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.isFromUser
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 260.dp)
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
                        MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.65f)
                    }
                )
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            if (isUser) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
            } else {
                MarkdownText(
                    text = message.content,
                    config = MarkdownConfig.Compact
                )
            }
        }
    }
}

@Composable
private fun InputRow(
    inputText: String,
    onInputChange: (String) -> Unit,
    isLoading: Boolean,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = inputText,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            enabled = !isLoading,
            singleLine = true,
            placeholder = {
                Text(
                    text = if (isLoading) "AI 回答中..." else "输入你的问题",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSend,
            enabled = inputText.isNotBlank() && !isLoading
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "send",
                tint = if (inputText.isNotBlank() && !isLoading) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
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
