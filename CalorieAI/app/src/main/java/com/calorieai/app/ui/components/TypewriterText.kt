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

enum class TypewriterMode {
    PLAIN,
    MARKDOWN,
    BATCH
}

class TypewriterState {
    var isTyping by mutableStateOf(false)
    var isComplete by mutableStateOf(false)
    var displayedLength by mutableIntStateOf(0)
    var isPaused by mutableStateOf(false)
    
    fun reset() {
        isTyping = false
        isComplete = false
        displayedLength = 0
        isPaused = false
    }
    
    fun pause() { isPaused = true }
    fun resume() { isPaused = false }
}

@Composable
fun rememberTypewriterState(): TypewriterState {
    return remember { TypewriterState() }
}

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
    isDark: Boolean = false,
    mode: TypewriterMode = TypewriterMode.PLAIN,
    state: TypewriterState? = null,
    batchSize: Int = 1
) {
    val internalState = state ?: remember { TypewriterState() }
    var displayedText by remember { mutableStateOf("") }
    var cursorVisible by remember { mutableStateOf(true) }
    
    val actualCursorColor = cursorColor ?: if (isDark) GlassDarkColors.Primary else GlassLightColors.Primary
    val actualTextColor = if (color == Color.Unspecified) {
        if (isDark) GlassDarkColors.OnSurface else GlassLightColors.OnSurface
    } else color
    
    LaunchedEffect(text) {
        if (internalState.isPaused) return@LaunchedEffect
        
        displayedText = ""
        internalState.isTyping = true
        internalState.isComplete = false
        
        var index = 0
        while (index < text.length && !internalState.isPaused) {
            delay(typingSpeed * batchSize)
            val newIndex = minOf(index + batchSize, text.length)
            displayedText = text.substring(0, newIndex)
            internalState.displayedLength = newIndex
            index = newIndex
        }
        
        if (index >= text.length) {
            internalState.isTyping = false
            internalState.isComplete = true
            onTypingComplete?.invoke()
        }
    }
    
    LaunchedEffect(showCursor, internalState.isComplete) {
        if (showCursor && !internalState.isComplete) {
            while (true) {
                delay(530)
                cursorVisible = !cursorVisible
            }
        }
    }
    
    val cursorAlpha by animateFloatAsState(
        targetValue = if (cursorVisible && !internalState.isComplete) 1f else 0f,
        animationSpec = tween(150),
        label = "cursorAlpha"
    )
    
    when (mode) {
        TypewriterMode.MARKDOWN -> {
            val finalText = if (showCursor && !internalState.isComplete) {
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
        else -> {
            Box(modifier = modifier) {
                Text(
                    text = displayedText,
                    style = style,
                    color = actualTextColor,
                    overflow = TextOverflow.Visible
                )
                if (showCursor && !internalState.isComplete) {
                    Text(
                        text = "│",
                        style = style.copy(color = actualCursorColor.copy(alpha = cursorAlpha)),
                        modifier = Modifier.offset(x = with(androidx.compose.ui.platform.LocalDensity.current) {
                            style.fontSize.toDp() * displayedText.length * 0.6f
                        })
                    )
                }
            }
        }
    }
}

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
        completedTexts.forEach { text ->
            Text(text = text, style = style, color = actualTextColor)
            Spacer(modifier = Modifier.height(8.dp))
        }
        
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
