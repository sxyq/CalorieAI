package com.calorieai.app.ui.screens.ai

/**
 * AI 回答可读性优化器
 * 在不改变原意的前提下，对纯文本回答做轻量结构化。
 */
object AIResponseFormatter {

    private val headingLikePattern = Regex(
        "(?<!\\n)(建议|总结|结论|方案|注意事项?|步骤|分析|原因|饮食建议|运动建议|今日建议|执行建议)[：:]"
    )
    private val inlineNumberedPattern = Regex("(?<=[。！？；:：])\\s*(\\d+(?:\\.|\\)|、))\\s*")
    private val inlineBulletPattern = Regex("(?<=[。！？；:：])\\s*([•●-])\\s+")
    private val redundantBlankLinesPattern = Regex("\\n{3,}")

    fun toReadableMarkdown(raw: String): String {
        if (raw.isBlank()) return raw

        var text = raw.replace("\r\n", "\n").replace('\r', '\n')

        // 如果已经是 Markdown 结构，尽量少改动，只做基础清理
        if (looksLikeStructuredMarkdown(text)) {
            return cleanup(text)
        }

        // 1) 将常见“标题：内容”提升为独立段落
        text = headingLikePattern.replace(text) { "\n${it.value}" }

        // 2) 将行内编号/项目符号拆成独立行
        text = inlineNumberedPattern.replace(text) { "\n${it.groupValues[1]} " }
        text = inlineBulletPattern.replace(text) { "\n${it.groupValues[1]} " }

        // 3) 针对超长单段落，按句号类标点做温和分段（每2句一段）
        text = splitLongParagraphs(text)

        return cleanup(text)
    }

    private fun looksLikeStructuredMarkdown(text: String): Boolean {
        val hasHeading = Regex("(?m)^#{1,4}\\s+").containsMatchIn(text)
        val hasList = Regex("(?m)^\\s*(?:[-*+•●]|\\d+(?:\\.|\\)|、))\\s+").containsMatchIn(text)
        val hasCodeBlock = text.contains("```")
        return hasHeading || hasList || hasCodeBlock
    }

    private fun splitLongParagraphs(text: String): String {
        val lines = text.lines()
        val out = mutableListOf<String>()

        lines.forEach { line ->
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                out.add("")
                return@forEach
            }

            // 对非常短的行不做处理
            if (trimmed.length < 120) {
                out.add(trimmed)
                return@forEach
            }

            val sentences = splitToSentences(trimmed)
            if (sentences.size <= 2) {
                out.add(trimmed)
                return@forEach
            }

            val grouped = sentences.chunked(2).map { it.joinToString("") }
            out.addAll(grouped)
        }

        return out.joinToString("\n")
    }

    private fun splitToSentences(text: String): List<String> {
        val result = mutableListOf<String>()
        val buffer = StringBuilder()

        text.forEach { ch ->
            buffer.append(ch)
            if (ch == '。' || ch == '！' || ch == '？' || ch == ';' || ch == '；' || ch == '!' || ch == '?') {
                result.add(buffer.toString().trim())
                buffer.clear()
            }
        }
        if (buffer.isNotEmpty()) {
            result.add(buffer.toString().trim())
        }
        return result.filter { it.isNotBlank() }
    }

    private fun cleanup(text: String): String {
        return text
            .lines()
            .joinToString("\n") { it.trimEnd() }
            .replace(redundantBlankLinesPattern, "\n\n")
            .trim()
    }
}
