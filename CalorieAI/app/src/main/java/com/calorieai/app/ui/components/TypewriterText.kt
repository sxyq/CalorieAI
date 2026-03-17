package com.calorieai.app.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.calorieai.app.ui.theme.GlassDarkColors
import com.calorieai.app.ui.theme.GlassLightColors
import kotlinx.coroutines.delay

/**
 * 打字机效果文本组件 - Glass 风格
 * 逐字显示文本，模拟打字效果
 */
@Composable
fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    typingSpeed: Long = 30L,
    onTypingComplete: (() -> Unit)? = null,
    showCursor: Boolean = true,
    cursorColor: Color? = null,
    isDark: Boolean = false
) {
    var displayedText by remember { mutableStateOf("") }
    var isTypingComplete by remember { mutableStateOf(false) }
    var showCursorState by remember { mutableStateOf(true) }
    
    val actualCursorColor = cursorColor ?: if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val actualTextColor = if (color == Color.Unspecified) {
        if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    } else color
    
    // 打字效果
    LaunchedEffect(text) {
        displayedText = ""
        isTypingComplete = false
        
        text.forEachIndexed { index, char ->
            delay(typingSpeed)
            displayedText = text.substring(0, index + 1)
        }
        
        isTypingComplete = true
        onTypingComplete?.invoke()
    }
    
    // 光标闪烁效果 - 使用更流畅的动画
    val cursorAlpha by animateFloatAsState(
        targetValue = if (showCursorState && !isTypingComplete) 1f else 0f,
        animationSpec = tween(200, easing = LinearEasing),
        label = "cursorAlpha"
    )
    
    LaunchedEffect(showCursor, isTypingComplete) {
        if (showCursor && !isTypingComplete) {
            while (true) {
                delay(530)
                showCursorState = !showCursorState
            }
        } else {
            showCursorState = false
        }
    }
    
    Box(modifier = modifier) {
        Text(
            text = displayedText,
            style = style,
            color = actualTextColor,
            overflow = TextOverflow.Visible
        )
        
        // 现代光标样式
        if (showCursor && !isTypingComplete) {
            Text(
                text = "│",
                style = style.copy(
                    color = actualCursorColor.copy(alpha = cursorAlpha)
                ),
                modifier = Modifier.offset(x = with(androidx.compose.ui.platform.LocalDensity.current) {
                    style.fontSize.toDp() * displayedText.length * 0.6f
                })
            )
        }
    }
}

/**
 * 带Markdown支持的打字机效果组件 - Glass 风格
 */
@Composable
fun TypewriterMarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    typingSpeed: Long = 30L,
    onTypingComplete: (() -> Unit)? = null,
    showCursor: Boolean = true,
    isDark: Boolean = false
) {
    var displayedText by remember { mutableStateOf("") }
    var isTypingComplete by remember { mutableStateOf(false) }
    var showCursorState by remember { mutableStateOf(true) }
    
    // 打字效果
    LaunchedEffect(text) {
        displayedText = ""
        isTypingComplete = false
        
        text.forEachIndexed { index, char ->
            delay(typingSpeed)
            displayedText = text.substring(0, index + 1)
        }
        
        isTypingComplete = true
        onTypingComplete?.invoke()
    }
    
    // 光标闪烁效果
    val cursorAlpha by animateFloatAsState(
        targetValue = if (showCursorState && !isTypingComplete) 1f else 0f,
        animationSpec = tween(200, easing = LinearEasing),
        label = "cursorAlpha"
    )
    
    LaunchedEffect(showCursor, isTypingComplete) {
        if (showCursor && !isTypingComplete) {
            while (true) {
                delay(530)
                showCursorState = !showCursorState
            }
        } else {
            showCursorState = false
        }
    }
    
    // 使用Markdown渲染
    val finalText = if (showCursor && !isTypingComplete) {
        displayedText + "│"
    } else {
        displayedText
    }
    
    com.calorieai.app.ui.components.markdown.MarkdownText(
        text = finalText,
        modifier = modifier,
        isDark = isDark
    )
}

/**
 * 打字机效果状态管理 - Glass 风格
 */
class TypewriterState {
    var isTyping by mutableStateOf(false)
    var isComplete by mutableStateOf(false)
    var displayedLength by mutableStateOf(0)
    
    fun reset() {
        isTyping = false
        isComplete = false
        displayedLength = 0
    }
}

@Composable
fun rememberTypewriterState(): TypewriterState {
    return remember { TypewriterState() }
}

/**
 * 可控打字机效果组件 - Glass 风格
 * 支持暂停、继续、重置等操作
 */
@Composable
fun ControlledTypewriterText(
    text: String,
    state: TypewriterState = rememberTypewriterState(),
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    typingSpeed: Long = 30L,
    onTypingComplete: (() -> Unit)? = null,
    autoStart: Boolean = true,
    isDark: Boolean = false
) {
    var displayedText by remember { mutableStateOf("") }
    var currentIndex by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    
    val actualTextColor = if (color == Color.Unspecified) {
        if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    } else color
    
    LaunchedEffect(text, autoStart) {
        if (autoStart && !state.isTyping && !state.isComplete) {
            state.isTyping = true
            
            while (currentIndex < text.length && !isPaused) {
                delay(typingSpeed)
                if (!isPaused) {
                    currentIndex++
                    displayedText = text.substring(0, currentIndex)
                    state.displayedLength = currentIndex
                }
            }
            
            if (currentIndex >= text.length) {
                state.isTyping = false
                state.isComplete = true
                onTypingComplete?.invoke()
            }
        }
    }
    
    Text(
        text = displayedText,
        modifier = modifier,
        style = style,
        color = actualTextColor
    )
}

/**
 * 批量打字机效果 - Glass 风格
 * 用于同时显示多个打字机文本
 */
@Composable
fun BatchTypewriterText(
    texts: List<String>,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    typingSpeed: Long = 30L,
    delayBetweenTexts: Long = 500L,
    onAllComplete: (() -> Unit)? = null,
    isDark: Boolean = false
) {
    var currentTextIndex by remember { mutableIntStateOf(0) }
    var completedTexts by remember { mutableStateOf<List<String>>(emptyList()) }
    
    val actualTextColor = if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    
    Column(modifier = modifier) {
        // 已完成的文本
        completedTexts.forEach { text ->
            Text(
                text = text,
                style = style,
                color = actualTextColor
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // 当前正在打字的文本
        if (currentTextIndex < texts.size) {
            TypewriterText(
                text = texts[currentTextIndex],
                style = style,
                typingSpeed = typingSpeed,
                onTypingComplete = {
                    completedTexts = completedTexts + texts[currentTextIndex]
                    currentTextIndex++
                    
                    if (currentTextIndex >= texts.size) {
                        onAllComplete?.invoke()
                    }
                },
                isDark = isDark
            )
        }
    }
}

/**
 * 高性能打字机效果 - 使用更少的重组
 */
@Composable
fun OptimizedTypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    color: Color = Color.Unspecified,
    typingSpeed: Long = 30L,
    onTypingComplete: (() -> Unit)? = null,
    showCursor: Boolean = true,
    isDark: Boolean = false
) {
    val displayedLength = remember { mutableIntStateOf(0) }
    var isComplete by remember { mutableStateOf(false) }
    var cursorVisible by remember { mutableStateOf(true) }
    
    val actualCursorColor = if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val actualTextColor = if (color == Color.Unspecified) {
        if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    } else color
    
    // 批量更新以提高性能
    LaunchedEffect(text) {
        displayedLength.intValue = 0
        isComplete = false
        
        val batchSize = 3 // 每批处理3个字符
        while (displayedLength.intValue < text.length) {
            delay(typingSpeed * batchSize)
            val newLength = minOf(displayedLength.intValue + batchSize, text.length)
            displayedLength.intValue = newLength
        }
        
        isComplete = true
        onTypingComplete?.invoke()
    }
    
    // 光标动画
    LaunchedEffect(showCursor, isComplete) {
        if (showCursor && !isComplete) {
            while (true) {
                delay(530)
                cursorVisible = !cursorVisible
            }
        }
    }
    
    val cursorAlpha by animateFloatAsState(
        targetValue = if (cursorVisible && !isComplete) 1f else 0f,
        animationSpec = tween(150),
        label = "cursorAlpha"
    )
    
    val displayText = text.take(displayedLength.intValue)
    
    Box(modifier = modifier) {
        Text(
            text = displayText,
            style = style,
            color = actualTextColor,
            overflow = TextOverflow.Visible
        )
        
        if (showCursor && !isComplete) {
            Text(
                text = "│",
                style = style.copy(
                    color = actualCursorColor.copy(alpha = cursorAlpha)
                ),
                modifier = Modifier.offset(x = with(androidx.compose.ui.platform.LocalDensity.current) {
                    style.fontSize.toDp() * displayText.length * 0.6f
                })
            )
        }
    }
}
