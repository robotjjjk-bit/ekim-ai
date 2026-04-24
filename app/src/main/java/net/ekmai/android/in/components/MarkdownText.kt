package net.ekmai.android.`in`.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ── Token model ───────────────────────────────────────────────────────────────

private sealed class MdBlock {
    data class Paragraph(val spans: List<MdSpan>) : MdBlock()
    data class CodeBlock(val language: String, val code: String) : MdBlock()
    data class Heading(val level: Int, val spans: List<MdSpan>) : MdBlock()
    data class BulletItem(val spans: List<MdSpan>, val indent: Int = 0) : MdBlock()
    data class NumberedItem(val number: Int, val spans: List<MdSpan>, val indent: Int = 0) : MdBlock()
    data class BlockQuote(val spans: List<MdSpan>) : MdBlock()
    object HorizontalRule : MdBlock()
    object EmptyLine : MdBlock()
}

private sealed class MdSpan {
    data class Plain(val text: String) : MdSpan()
    data class Bold(val text: String) : MdSpan()
    data class Italic(val text: String) : MdSpan()
    data class BoldItalic(val text: String) : MdSpan()
    data class Code(val text: String) : MdSpan()
    data class Strikethrough(val text: String) : MdSpan()
    data class Link(val label: String, val url: String) : MdSpan()
}

// ── Parser ────────────────────────────────────────────────────────────────────

private fun parseMarkdown(input: String): List<MdBlock> {
    val blocks = mutableListOf<MdBlock>()
    val lines = input.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Fenced code block ```
        if (line.trimStart().startsWith("```")) {
            val lang = line.trimStart().removePrefix("```").trim()
            val code = StringBuilder()
            i++
            while (i < lines.size && !lines[i].trimStart().startsWith("```")) {
                code.appendLine(lines[i])
                i++
            }
            blocks.add(MdBlock.CodeBlock(lang, code.toString().trimEnd()))
            i++
            continue
        }

        // Horizontal rule
        if (line.trim().matches(Regex("^[-*_]{3,}$"))) {
            blocks.add(MdBlock.HorizontalRule)
            i++
            continue
        }

        // Heading
        val headingMatch = Regex("^(#{1,6})\\s+(.+)$").find(line.trim())
        if (headingMatch != null) {
            val level = headingMatch.groupValues[1].length
            val text = headingMatch.groupValues[2]
            blocks.add(MdBlock.Heading(level, parseInline(text)))
            i++
            continue
        }

        // Blockquote
        if (line.trimStart().startsWith("> ")) {
            val content = line.trimStart().removePrefix("> ")
            blocks.add(MdBlock.BlockQuote(parseInline(content)))
            i++
            continue
        }

        // Bullet list
        val bulletMatch = Regex("^(\\s*)[-*+]\\s+(.+)$").find(line)
        if (bulletMatch != null) {
            val indent = bulletMatch.groupValues[1].length / 2
            val content = bulletMatch.groupValues[2]
            blocks.add(MdBlock.BulletItem(parseInline(content), indent))
            i++
            continue
        }

        // Numbered list
        val numberedMatch = Regex("^(\\s*)(\\d+)\\.\\s+(.+)$").find(line)
        if (numberedMatch != null) {
            val indent = numberedMatch.groupValues[1].length / 2
            val number = numberedMatch.groupValues[2].toInt()
            val content = numberedMatch.groupValues[3]
            blocks.add(MdBlock.NumberedItem(number, parseInline(content), indent))
            i++
            continue
        }

        // Empty line
        if (line.isBlank()) {
            if (blocks.lastOrNull() !is MdBlock.EmptyLine) {
                blocks.add(MdBlock.EmptyLine)
            }
            i++
            continue
        }

        // Paragraph
        blocks.add(MdBlock.Paragraph(parseInline(line)))
        i++
    }

    // Strip leading/trailing empty lines
    return blocks.dropWhile { it is MdBlock.EmptyLine }
        .dropLastWhile { it is MdBlock.EmptyLine }
}

private fun parseInline(input: String): List<MdSpan> {
    val spans = mutableListOf<MdSpan>()
    var remaining = input

    // Regex patterns ordered by priority
    val patterns = listOf(
        Regex("\\*\\*\\*(.+?)\\*\\*\\*"),           // ***bold italic***
        Regex("___(.+?)___"),                         // ___bold italic___
        Regex("\\*\\*(.+?)\\*\\*"),                   // **bold**
        Regex("__(.+?)__"),                           // __bold__
        Regex("\\*(.+?)\\*"),                         // *italic*
        Regex("_(.+?)_"),                             // _italic_
        Regex("~~(.+?)~~"),                           // ~~strikethrough~~
        Regex("`(.+?)`"),                             // `code`
        Regex("\\[(.+?)\\]\\((.+?)\\)")              // [link](url)
    )

    while (remaining.isNotEmpty()) {
        // Find earliest match across all patterns
        var earliestMatch: MatchResult? = null
        var earliestPattern: Regex? = null
        var earliestIndex = Int.MAX_VALUE

        for (pattern in patterns) {
            val match = pattern.find(remaining)
            if (match != null && match.range.first < earliestIndex) {
                earliestIndex = match.range.first
                earliestMatch = match
                earliestPattern = pattern
            }
        }

        if (earliestMatch == null) {
            spans.add(MdSpan.Plain(remaining))
            break
        }

        // Text before match
        if (earliestIndex > 0) {
            spans.add(MdSpan.Plain(remaining.substring(0, earliestIndex)))
        }

        // The matched span
        val raw = earliestMatch.value
        val group1 = earliestMatch.groupValues.getOrElse(1) { "" }
        val group2 = earliestMatch.groupValues.getOrElse(2) { "" }

        val span = when {
            raw.startsWith("***") || raw.startsWith("___") -> MdSpan.BoldItalic(group1)
            raw.startsWith("**") || raw.startsWith("__") -> MdSpan.Bold(group1)
            raw.startsWith("~~") -> MdSpan.Strikethrough(group1)
            raw.startsWith("`") -> MdSpan.Code(group1)
            raw.startsWith("[") -> MdSpan.Link(group1, group2)
            raw.startsWith("*") || raw.startsWith("_") -> MdSpan.Italic(group1)
            else -> MdSpan.Plain(raw)
        }

        spans.add(span)
        remaining = remaining.substring(earliestMatch.range.last + 1)
    }

    return spans
}

// ── Composable renderer ───────────────────────────────────────────────────────

@Composable
fun MarkdownText(
    text: String,
    isUserMessage: Boolean,
    modifier: Modifier = Modifier
) {
    val blocks = remember(text) { parseMarkdown(text) }
    val textColor = if (isUserMessage)
        MaterialTheme.colorScheme.onPrimary
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        blocks.forEach { block ->
            when (block) {

                is MdBlock.EmptyLine -> Spacer(modifier = Modifier.height(4.dp))

                is MdBlock.HorizontalRule -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(textColor.copy(alpha = 0.2f))
                )

                is MdBlock.Heading -> {
                    val (fontSize, topPad) = when (block.level) {
                        1 -> Pair(20.sp, 6.dp)
                        2 -> Pair(17.sp, 4.dp)
                        3 -> Pair(15.sp, 2.dp)
                        else -> Pair(14.sp, 2.dp)
                    }
                    Text(
                        text = buildAnnotatedString { appendSpans(block.spans, textColor) },
                        fontSize = fontSize,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        lineHeight = (fontSize.value * 1.4).sp,
                        modifier = Modifier.padding(top = topPad)
                    )
                }

                is MdBlock.Paragraph -> Text(
                    text = buildAnnotatedString { appendSpans(block.spans, textColor) },
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = textColor
                )

                is MdBlock.BulletItem -> Row(
                    modifier = Modifier.padding(start = (block.indent * 16).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "•",
                        fontSize = 15.sp,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Text(
                        text = buildAnnotatedString { appendSpans(block.spans, textColor) },
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                is MdBlock.NumberedItem -> Row(
                    modifier = Modifier.padding(start = (block.indent * 16).dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${block.number}.",
                        fontSize = 15.sp,
                        color = textColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 1.dp)
                    )
                    Text(
                        text = buildAnnotatedString { appendSpans(block.spans, textColor) },
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = textColor,
                        modifier = Modifier.weight(1f)
                    )
                }

                is MdBlock.BlockQuote -> Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(textColor.copy(alpha = 0.08f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(IntrinsicSize.Max)
                            .clip(RoundedCornerShape(2.dp))
                            .background(textColor.copy(alpha = 0.4f))
                            .align(Alignment.CenterVertically)
                    )
                    Text(
                        text = buildAnnotatedString { appendSpans(block.spans, textColor) },
                        fontSize = 14.sp,
                        lineHeight = 21.sp,
                        color = textColor.copy(alpha = 0.85f),
                        fontStyle = FontStyle.Italic
                    )
                }

                is MdBlock.CodeBlock -> CodeBlockView(
                    language = block.language,
                    code = block.code,
                    isUserMessage = isUserMessage
                )

                else -> {}
            }
        }
    }
}

// ── Code block UI ─────────────────────────────────────────────────────────────

@Composable
private fun CodeBlockView(
    language: String,
    code: String,
    isUserMessage: Boolean
) {
    val bgColor = if (isUserMessage) Color(0xFF1E1E2E)
    else MaterialTheme.colorScheme.surface

    val codeTextColor = if (isUserMessage) Color(0xFFCDD6F4)
    else MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()      // always full width of parent
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
    ) {
        if (language.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(codeTextColor.copy(alpha = 0.06f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = language.lowercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = codeTextColor.copy(alpha = 0.5f),
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Key fix — fillMaxWidth on scroll container, let text be as wide as it needs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())   // scroll horizontally if code is wider
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = code,
                fontSize = 13.sp,
                lineHeight = 20.sp,
                fontFamily = FontFamily.Monospace,
                color = codeTextColor,
                softWrap = false   // 👈 this is the critical fix — no wrapping, scroll instead
            )
        }
    }
}

// ── AnnotatedString builder ───────────────────────────────────────────────────

private fun AnnotatedString.Builder.appendSpans(
    spans: List<MdSpan>,
    textColor: Color
) {
    spans.forEach { span ->
        when (span) {
            is MdSpan.Plain -> append(span.text)

            is MdSpan.Bold -> withStyle(
                SpanStyle(fontWeight = FontWeight.Bold)
            ) { append(span.text) }

            is MdSpan.Italic -> withStyle(
                SpanStyle(fontStyle = FontStyle.Italic)
            ) { append(span.text) }

            is MdSpan.BoldItalic -> withStyle(
                SpanStyle(fontWeight = FontWeight.Bold, fontStyle = FontStyle.Italic)
            ) { append(span.text) }

            is MdSpan.Strikethrough -> withStyle(
                SpanStyle(textDecoration = TextDecoration.LineThrough)
            ) { append(span.text) }

            is MdSpan.Code -> withStyle(
                SpanStyle(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp,
                    background = textColor.copy(alpha = 0.12f),
                    color = textColor
                )
            ) { append(" ${span.text} ") }

            is MdSpan.Link -> withStyle(
                SpanStyle(
                    color = Color(0xFF378ADD),
                    textDecoration = TextDecoration.Underline
                )
            ) { append(span.label) }
        }
    }
}