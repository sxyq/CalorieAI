package com.calorieai.app.ui.components.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.calorieai.app.ui.theme.*

/**
 * Markdown 配置 - Glass 主题
 */
data class MarkdownConfig(
    val textStyle: TextStyle,
    val h1Style: TextStyle,
    val h2Style: TextStyle,
    val h3Style: TextStyle,
    val h4Style: TextStyle,
    val codeFontFamily: FontFamily,
    val codeBlockBackgroundColor: Color,
    val codeBlockTextColor: Color,
    val inlineCodeBackgroundColor: Color,
    val inlineCodeTextColor: Color,
    val quoteBackgroundColor: Color,
    val quoteBorderColor: Color,
    val quoteTextColor: Color,
    val linkColor: Color,
    val linkUnderline: Boolean,
    val listBulletColor: Color,
    val listNumberColor: Color,
    val dividerColor: Color,
    val dividerThickness: Float,
    val paragraphSpacing: Int,
    val headingSpacing: Int
) {
    companion object {
        val Default: MarkdownConfig
            @Composable get() = MarkdownConfig(
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = GlassLightColors.OnSurface
                ),
                h1Style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GlassLightColors.OnSurface
                ),
                h2Style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GlassLightColors.OnSurface
                ),
                h3Style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = GlassLightColors.OnSurface
                ),
                h4Style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = GlassLightColors.OnSurface
                ),
                codeFontFamily = FontFamily.Monospace,
                codeBlockBackgroundColor = GlassLightColors.SurfaceContainerHigh.copy(alpha = 0.8f),
                codeBlockTextColor = GlassLightColors.OnSurface,
                inlineCodeBackgroundColor = GlassLightColors.SurfaceContainer.copy(alpha = 0.6f),
                inlineCodeTextColor = GlassLightColors.Primary,
                quoteBackgroundColor = GlassLightColors.SurfaceContainerLow.copy(alpha = 0.5f),
                quoteBorderColor = GlassLightColors.Primary.copy(alpha = 0.6f),
                quoteTextColor = GlassLightColors.OnSurfaceVariant,
                linkColor = GlassLightColors.Primary,
                linkUnderline = true,
                listBulletColor = GlassLightColors.Primary,
                listNumberColor = GlassLightColors.Primary,
                dividerColor = GlassLightColors.OutlineVariant,
                dividerThickness = 1f,
                paragraphSpacing = 8,
                headingSpacing = 12
            )

        val Dark: MarkdownConfig
            @Composable get() = MarkdownConfig(
                textStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = GlassDarkColors.OnSurface
                ),
                h1Style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = GlassDarkColors.OnSurface
                ),
                h2Style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = GlassDarkColors.OnSurface
                ),
                h3Style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = GlassDarkColors.OnSurface
                ),
                h4Style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = GlassDarkColors.OnSurface
                ),
                codeFontFamily = FontFamily.Monospace,
                codeBlockBackgroundColor = GlassDarkColors.SurfaceContainerHigh.copy(alpha = 0.8f),
                codeBlockTextColor = GlassDarkColors.OnSurface,
                inlineCodeBackgroundColor = GlassDarkColors.SurfaceContainer.copy(alpha = 0.6f),
                inlineCodeTextColor = GlassDarkColors.Primary,
                quoteBackgroundColor = GlassDarkColors.SurfaceContainerLow.copy(alpha = 0.5f),
                quoteBorderColor = GlassDarkColors.Primary.copy(alpha = 0.6f),
                quoteTextColor = GlassDarkColors.OnSurfaceVariant,
                linkColor = GlassDarkColors.Primary,
                linkUnderline = true,
                listBulletColor = GlassDarkColors.Primary,
                listNumberColor = GlassDarkColors.Primary,
                dividerColor = GlassDarkColors.OutlineVariant,
                dividerThickness = 1f,
                paragraphSpacing = 8,
                headingSpacing = 12
            )

        val Compact: MarkdownConfig
            @Composable get() = Default.copy(
                textStyle = MaterialTheme.typography.bodySmall,
                h1Style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                h2Style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                h3Style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                h4Style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold),
                paragraphSpacing = 4,
                headingSpacing = 8
            )

        val ChatReadable: MarkdownConfig
            @Composable get() = Default.copy(
                textStyle = MaterialTheme.typography.bodyMedium.copy(lineHeight = 24.sp),
                h1Style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                h2Style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                h3Style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                h4Style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                paragraphSpacing = 10,
                headingSpacing = 14
            )
    }
}

/**
 * Markdown文本渲染组件 - Glass 风格
 */
@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    config: MarkdownConfig = MarkdownConfig.Default,
    onLinkClick: ((String) -> Unit)? = null,
    isDark: Boolean = false
) {
    val actualConfig = if (isDark) {
        config.copy(
            textStyle = config.textStyle.copy(color = GlassDarkColors.OnSurface),
            h1Style = config.h1Style.copy(color = GlassDarkColors.OnSurface),
            h2Style = config.h2Style.copy(color = GlassDarkColors.OnSurface),
            h3Style = config.h3Style.copy(color = GlassDarkColors.OnSurface),
            h4Style = config.h4Style.copy(color = GlassDarkColors.OnSurface),
            codeBlockBackgroundColor = GlassDarkColors.SurfaceContainerHigh.copy(alpha = 0.8f),
            codeBlockTextColor = GlassDarkColors.OnSurface,
            inlineCodeBackgroundColor = GlassDarkColors.SurfaceContainer.copy(alpha = 0.6f),
            inlineCodeTextColor = GlassDarkColors.Primary,
            quoteBackgroundColor = GlassDarkColors.SurfaceContainerLow.copy(alpha = 0.5f),
            quoteBorderColor = GlassDarkColors.Primary.copy(alpha = 0.6f),
            quoteTextColor = GlassDarkColors.OnSurfaceVariant,
            linkColor = GlassDarkColors.Primary,
            listBulletColor = GlassDarkColors.Primary,
            listNumberColor = GlassDarkColors.Primary,
            dividerColor = GlassDarkColors.OutlineVariant
        )
    } else {
        config
    }
    val parsedContent = parseMarkdown(text, actualConfig)
    
    Column(modifier = modifier) {
        parsedContent.forEach { element ->
            when (element) {
                is MarkdownElement.Heading -> {
                    HeadingElement(element, actualConfig)
                    Spacer(modifier = Modifier.height(actualConfig.headingSpacing.dp))
                }
                is MarkdownElement.Paragraph -> {
                    ParagraphElement(element, actualConfig, onLinkClick)
                    Spacer(modifier = Modifier.height(actualConfig.paragraphSpacing.dp))
                }
                is MarkdownElement.CodeBlock -> {
                    CodeBlockElement(element, actualConfig, isDark)
                    Spacer(modifier = Modifier.height(actualConfig.paragraphSpacing.dp))
                }
                is MarkdownElement.Quote -> {
                    QuoteElement(element, actualConfig, onLinkClick, isDark)
                    Spacer(modifier = Modifier.height(actualConfig.paragraphSpacing.dp))
                }
                is MarkdownElement.BulletList -> {
                    BulletListElement(element, actualConfig, onLinkClick)
                    Spacer(modifier = Modifier.height(actualConfig.paragraphSpacing.dp))
                }
                is MarkdownElement.NumberedList -> {
                    NumberedListElement(element, actualConfig, onLinkClick)
                    Spacer(modifier = Modifier.height(actualConfig.paragraphSpacing.dp))
                }
                is MarkdownElement.Divider -> {
                    DividerElement(actualConfig)
                    Spacer(modifier = Modifier.height(actualConfig.paragraphSpacing.dp))
                }
            }
        }
    }
}

/**
 * Markdown元素密封类
 */
private sealed class MarkdownElement {
    data class Heading(val level: Int, val text: String) : MarkdownElement()
    data class Paragraph(val text: String) : MarkdownElement()
    data class CodeBlock(val language: String?, val code: String) : MarkdownElement()
    data class Quote(val content: List<MarkdownElement>) : MarkdownElement()
    data class BulletList(val items: List<String>) : MarkdownElement()
    data class NumberedList(val items: List<String>) : MarkdownElement()
    object Divider : MarkdownElement()
}

/**
 * 解析Markdown文本
 */
private fun parseMarkdown(text: String, config: MarkdownConfig): List<MarkdownElement> {
    val elements = mutableListOf<MarkdownElement>()
    val lines = text.lines()
    var i = 0
    
    while (i < lines.size) {
        val line = lines[i]
        
        when {
            line.matches(Regex("^ {0,3}(-{3,}|\\*{3,}|_{3,})\\s*$")) -> {
                elements.add(MarkdownElement.Divider)
                i++
            }
            line.startsWith("#") -> {
                val level = line.takeWhile { it == '#' }.length.coerceAtMost(6)
                val text = line.drop(level).trim()
                elements.add(MarkdownElement.Heading(level, text))
                i++
            }
            line.startsWith("```") -> {
                val language = line.drop(3).trim().takeIf { it.isNotEmpty() }
                val codeLines = mutableListOf<String>()
                i++
                while (i < lines.size && !lines[i].startsWith("```")) {
                    codeLines.add(lines[i])
                    i++
                }
                elements.add(MarkdownElement.CodeBlock(language, codeLines.joinToString("\n")))
                i++
            }
            line.startsWith(">") -> {
                val quoteLines = mutableListOf<String>()
                while (i < lines.size && lines[i].startsWith(">")) {
                    quoteLines.add(lines[i].drop(1).trim())
                    i++
                }
                val quoteContent = parseMarkdown(quoteLines.joinToString("\n"), config)
                elements.add(MarkdownElement.Quote(quoteContent))
            }
            line.matches(Regex("^\\s*[-*+•●]\\s+.+")) -> {
                val items = mutableListOf<String>()
                while (i < lines.size && lines[i].matches(Regex("^\\s*[-*+•●]\\s+.+"))) {
                    items.add(lines[i].replaceFirst(Regex("^\\s*[-*+•●]\\s+"), ""))
                    i++
                }
                elements.add(MarkdownElement.BulletList(items))
            }
            line.matches(Regex("^\\s*\\d+(\\.|\\)|、)\\s*.+")) -> {
                val items = mutableListOf<String>()
                while (i < lines.size && lines[i].matches(Regex("^\\s*\\d+(\\.|\\)|、)\\s*.+"))) {
                    items.add(lines[i].replaceFirst(Regex("^\\s*\\d+(\\.|\\)|、)\\s*"), ""))
                    i++
                }
                elements.add(MarkdownElement.NumberedList(items))
            }
            line.isBlank() -> {
                i++
            }
            else -> {
                val paragraphLines = mutableListOf<String>()
                while (i < lines.size && lines[i].isNotBlank() && 
                       !lines[i].startsWith("#") && 
                       !lines[i].startsWith("```") &&
                       !lines[i].startsWith(">") &&
                       !lines[i].matches(Regex("^\\s*[-*+•●]\\s+.+")) &&
                       !lines[i].matches(Regex("^\\s*\\d+(\\.|\\)|、)\\s*.+"))) {
                    paragraphLines.add(lines[i])
                    i++
                }
                elements.add(MarkdownElement.Paragraph(paragraphLines.joinToString("\n")))
            }
        }
    }
    
    return elements
}

/**
 * 标题元素
 */
@Composable
private fun HeadingElement(element: MarkdownElement.Heading, config: MarkdownConfig) {
    val style = when (element.level) {
        1 -> config.h1Style
        2 -> config.h2Style
        3 -> config.h3Style
        else -> config.h4Style
    }
    
    Text(
        text = parseInlineMarkdown(element.text, config),
        style = style
    )
}

/**
 * 段落元素
 */
@Composable
private fun ParagraphElement(
    element: MarkdownElement.Paragraph,
    config: MarkdownConfig,
    onLinkClick: ((String) -> Unit)?
) {
    val annotatedString = parseInlineMarkdown(element.text, config)
    
    if (onLinkClick != null) {
        ClickableText(
            text = annotatedString,
            style = config.textStyle,
            onClick = { offset ->
                annotatedString.getStringAnnotations("URL", offset, offset)
                    .firstOrNull()?.let { annotation ->
                        onLinkClick(annotation.item)
                    }
            }
        )
    } else {
        Text(
            text = annotatedString,
            style = config.textStyle
        )
    }
}

/**
 * 代码块元素 - Glass 毛玻璃风格
 */
@Composable
private fun CodeBlockElement(
    element: MarkdownElement.CodeBlock,
    config: MarkdownConfig,
    isDark: Boolean
) {
    val backgroundColor = if (isDark) {
        GlassDarkColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    } else {
        GlassLightColors.SurfaceContainerHigh.copy(alpha = GlassAlpha.CARD_BACKGROUND)
    }
    
    val borderColor = if (isDark) {
        Color.White.copy(alpha = 0.1f)
    } else {
        Color.White.copy(alpha = 0.25f)
    }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            element.language?.let { lang ->
                Text(
                    text = lang,
                    style = TextStyle(
                        fontSize = 12.sp,
                        color = config.codeBlockTextColor.copy(alpha = 0.7f)
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Text(
                text = element.code,
                style = TextStyle(
                    fontFamily = config.codeFontFamily,
                    fontSize = 13.sp,
                    color = config.codeBlockTextColor,
                    lineHeight = 20.sp
                )
            )
        }
    }
}

/**
 * 引用元素 - Glass 风格
 */
@Composable
private fun QuoteElement(
    element: MarkdownElement.Quote,
    config: MarkdownConfig,
    onLinkClick: ((String) -> Unit)?,
    isDark: Boolean
) {
    val backgroundColor = if (isDark) {
        GlassDarkColors.SurfaceContainerLow.copy(alpha = 0.5f)
    } else {
        GlassLightColors.SurfaceContainerLow.copy(alpha = 0.5f)
    }
    
    val borderColor = if (isDark) {
        GlassDarkColors.Primary.copy(alpha = 0.6f)
    } else {
        GlassLightColors.Primary.copy(alpha = 0.6f)
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .background(backgroundColor, RoundedCornerShape(0.dp, 12.dp, 12.dp, 0.dp))
            .padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 12.dp)
    ) {
        Column {
            element.content.forEach { childElement ->
                when (childElement) {
                    is MarkdownElement.Paragraph -> {
                        Text(
                            text = parseInlineMarkdown(childElement.text, config),
                            style = config.textStyle.copy(color = config.quoteTextColor)
                        )
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * 无序列表元素
 */
@Composable
private fun BulletListElement(
    element: MarkdownElement.BulletList,
    config: MarkdownConfig,
    onLinkClick: ((String) -> Unit)?
) {
    Column {
        element.items.forEach { item ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = "•",
                    style = config.textStyle.copy(
                        color = config.listBulletColor,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.width(24.dp)
                )
                
                val annotatedString = parseInlineMarkdown(item, config)
                if (onLinkClick != null) {
                    ClickableText(
                        text = annotatedString,
                        style = config.textStyle,
                        modifier = Modifier.weight(1f),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    onLinkClick(annotation.item)
                                }
                        }
                    )
                } else {
                    Text(
                        text = annotatedString,
                        style = config.textStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 有序列表元素
 */
@Composable
private fun NumberedListElement(
    element: MarkdownElement.NumberedList,
    config: MarkdownConfig,
    onLinkClick: ((String) -> Unit)?
) {
    Column {
        element.items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Text(
                    text = "${index + 1}.",
                    style = config.textStyle.copy(color = config.listNumberColor),
                    modifier = Modifier.width(24.dp)
                )
                
                val annotatedString = parseInlineMarkdown(item, config)
                if (onLinkClick != null) {
                    ClickableText(
                        text = annotatedString,
                        style = config.textStyle,
                        modifier = Modifier.weight(1f),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations("URL", offset, offset)
                                .firstOrNull()?.let { annotation ->
                                    onLinkClick(annotation.item)
                                }
                        }
                    )
                } else {
                    Text(
                        text = annotatedString,
                        style = config.textStyle,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * 分割线元素
 */
@Composable
private fun DividerElement(config: MarkdownConfig) {
    Divider(
        color = config.dividerColor,
        thickness = config.dividerThickness.dp
    )
}

/**
 * 解析行内Markdown
 */
private fun parseInlineMarkdown(text: String, config: MarkdownConfig): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        
        while (remaining.isNotEmpty()) {
            when {
                remaining.startsWith("**") -> {
                    val endIndex = remaining.indexOf("**", 2)
                    if (endIndex != -1) {
                        val boldText = remaining.substring(2, endIndex)
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                            append(parseInlineMarkdown(boldText, config))
                        }
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("**")
                        remaining = remaining.drop(2)
                    }
                }
                remaining.startsWith("*") && !remaining.startsWith("**") -> {
                    val endIndex = remaining.indexOf("*", 1)
                    if (endIndex != -1) {
                        val italicText = remaining.substring(1, endIndex)
                        withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                            append(parseInlineMarkdown(italicText, config))
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("*")
                        remaining = remaining.drop(1)
                    }
                }
                remaining.startsWith("~~") -> {
                    val endIndex = remaining.indexOf("~~", 2)
                    if (endIndex != -1) {
                        val strikeText = remaining.substring(2, endIndex)
                        withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                            append(parseInlineMarkdown(strikeText, config))
                        }
                        remaining = remaining.substring(endIndex + 2)
                    } else {
                        append("~~")
                        remaining = remaining.drop(2)
                    }
                }
                remaining.startsWith("`") && !remaining.startsWith("``") -> {
                    val endIndex = remaining.indexOf("`", 1)
                    if (endIndex != -1) {
                        val codeText = remaining.substring(1, endIndex)
                        withStyle(
                            SpanStyle(
                                fontFamily = config.codeFontFamily,
                                background = config.inlineCodeBackgroundColor,
                                color = config.inlineCodeTextColor
                            )
                        ) {
                            append(codeText)
                        }
                        remaining = remaining.substring(endIndex + 1)
                    } else {
                        append("`")
                        remaining = remaining.drop(1)
                    }
                }
                remaining.startsWith("[") -> {
                    val closeBracket = remaining.indexOf("]")
                    val openParen = remaining.indexOf("(", closeBracket)
                    val closeParen = remaining.indexOf(")", openParen)
                    
                    if (closeBracket != -1 && openParen == closeBracket + 1 && closeParen != -1) {
                        val linkText = remaining.substring(1, closeBracket)
                        val url = remaining.substring(openParen + 1, closeParen)
                        
                        pushStringAnnotation("URL", url)
                        withStyle(
                            SpanStyle(
                                color = config.linkColor,
                                textDecoration = if (config.linkUnderline) TextDecoration.Underline else null
                            )
                        ) {
                            append(linkText)
                        }
                        pop()
                        
                        remaining = remaining.substring(closeParen + 1)
                    } else {
                        append("[")
                        remaining = remaining.drop(1)
                    }
                }
                else -> {
                    val nextSpecial = remaining.indexOfAny(charArrayOf('*', '_', '~', '`', '['))
                    if (nextSpecial == -1) {
                        append(remaining)
                        remaining = ""
                    } else {
                        append(remaining.substring(0, nextSpecial))
                        remaining = remaining.substring(nextSpecial)
                    }
                }
            }
        }
    }
}
