package com.onlyjesus

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class VerseAnnotation(
    val versionPath: String,
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val bookmarked: Boolean = false,
    val highlighted: Boolean = false,
    val noteMarkdown: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

data class ReadingHistoryEntry(
    val versionPath: String,
    val versionLabel: String,
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val timestamp: Long
) {
    fun matches(other: ReadingHistoryEntry): Boolean {
        return versionPath == other.versionPath && book == other.book && chapter == other.chapter && verse == other.verse
    }
}

enum class LibrarySection {
    Bookmarks,
    History,
    Notes,
    Highlights,
    Plans
}

data class VerseReference(val book: Int, val chapter: Int, val verse: Int)

class VerseLibraryStore(private val context: Context) {
    private val annotationsFile = File(context.filesDir, "verse_annotations.json")
    private val historyFile = File(context.filesDir, "reading_history.json")

    suspend fun loadAnnotations(): List<VerseAnnotation> = withContext(Dispatchers.IO) {
        readAnnotations()
    }

    suspend fun saveAnnotations(items: List<VerseAnnotation>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("versionPath", item.versionPath)
                    .put("book", item.book)
                    .put("chapter", item.chapter)
                    .put("verse", item.verse)
                    .put("bookmarked", item.bookmarked)
                    .put("highlighted", item.highlighted)
                    .put("noteMarkdown", item.noteMarkdown)
                    .put("updatedAt", item.updatedAt)
            )
        }
        annotationsFile.writeText(array.toString())
    }

    suspend fun loadHistory(): List<ReadingHistoryEntry> = withContext(Dispatchers.IO) {
        readHistory()
    }

    suspend fun saveHistory(items: List<ReadingHistoryEntry>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        items.forEach { item ->
            array.put(
                JSONObject()
                    .put("versionPath", item.versionPath)
                    .put("versionLabel", item.versionLabel)
                    .put("book", item.book)
                    .put("chapter", item.chapter)
                    .put("verse", item.verse)
                    .put("timestamp", item.timestamp)
            )
        }
        historyFile.writeText(array.toString())
    }

    private fun readAnnotations(): List<VerseAnnotation> {
        if (!annotationsFile.exists()) return emptyList()
        val array = JSONArray(annotationsFile.readText())
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    VerseAnnotation(
                        versionPath = item.optString("versionPath"),
                        book = item.optInt("book", 1),
                        chapter = item.optInt("chapter", 1),
                        verse = item.optInt("verse", 1),
                        bookmarked = item.optBoolean("bookmarked", false),
                        highlighted = item.optBoolean("highlighted", false),
                        noteMarkdown = item.optString("noteMarkdown", ""),
                        updatedAt = item.optLong("updatedAt", System.currentTimeMillis())
                    )
                )
            }
        }
    }

    private fun readHistory(): List<ReadingHistoryEntry> {
        if (!historyFile.exists()) return emptyList()
        val array = JSONArray(historyFile.readText())
        return buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    ReadingHistoryEntry(
                        versionPath = item.optString("versionPath"),
                        versionLabel = item.optString("versionLabel"),
                        book = item.optInt("book", 1),
                        chapter = item.optInt("chapter", 1),
                        verse = item.optInt("verse", 1),
                        timestamp = item.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        }
    }
}

@Composable
fun MarkdownPreviewPane(
    markdown: String,
    modifier: Modifier = Modifier,
    themeAccent: Color,
    themeBorder: Color,
    fontFamily: FontFamily,
    fontSizeSp: Float
) {
    val annotated = rememberMarkdownPreview(markdown, themeAccent)
    Box(
        modifier = modifier
            .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
            .background(themeAccent.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        val displayText = if (annotated.text.isBlank()) {
            buildAnnotatedString { append("Preview your markdown here.") }
        } else {
            annotated
        }
        Text(
            text = displayText,
            fontFamily = fontFamily,
            fontSize = fontSizeSp.sp,
            color = themeAccent
        )
    }
}

private fun rememberMarkdownPreview(markdown: String, themeAccent: Color) = buildAnnotatedString {
    if (markdown.isBlank()) return@buildAnnotatedString

    val lines = markdown.replace("\r\n", "\n").split('\n')
    var inCodeBlock = false

    lines.forEachIndexed { index, line ->
        when {
            line.trim().startsWith("```") -> {
                inCodeBlock = !inCodeBlock
                if (index != lines.lastIndex) append('\n')
            }
            inCodeBlock -> {
                withStyle(SpanStyle(color = themeAccent.copy(alpha = 0.88f))) { append(line) }
                if (index != lines.lastIndex) append('\n')
            }
            line.startsWith("# ") -> {
                withStyle(SpanStyle(color = themeAccent)) { append(line.removePrefix("# ")) }
                if (index != lines.lastIndex) append('\n')
            }
            line.startsWith("## ") -> {
                withStyle(SpanStyle(color = themeAccent.copy(alpha = 0.92f))) { append(line.removePrefix("## ")) }
                if (index != lines.lastIndex) append('\n')
            }
            line.startsWith("- ") || line.startsWith("* ") -> {
                append("• ")
                append(renderInlineMarkdown(line.drop(2), themeAccent))
                if (index != lines.lastIndex) append('\n')
            }
            line.matches(Regex("\\d+\\. .*")) -> {
                append(renderInlineMarkdown(line, themeAccent))
                if (index != lines.lastIndex) append('\n')
            }
            else -> {
                append(renderInlineMarkdown(line, themeAccent))
                if (index != lines.lastIndex) append('\n')
            }
        }
    }
}

private fun renderInlineMarkdown(text: String, themeAccent: Color) = buildAnnotatedString {
    var index = 0
    while (index < text.length) {
        when {
            text.startsWith("**", index) -> {
                val end = text.indexOf("**", index + 2)
                if (end > index + 2) {
                    withStyle(SpanStyle(color = themeAccent)) {
                        append(text.substring(index + 2, end))
                    }
                    index = end + 2
                } else {
                    append(text[index])
                    index++
                }
            }
            text.startsWith("*", index) -> {
                val end = text.indexOf('*', index + 1)
                if (end > index + 1) {
                    withStyle(SpanStyle(color = themeAccent.copy(alpha = 0.88f))) {
                        append(text.substring(index + 1, end))
                    }
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }
            text[index] == '`' -> {
                val end = text.indexOf('`', index + 1)
                if (end > index + 1) {
                    withStyle(SpanStyle(color = themeAccent.copy(alpha = 0.85f))) {
                        append(text.substring(index + 1, end))
                    }
                    index = end + 1
                } else {
                    append(text[index])
                    index++
                }
            }
            else -> {
                append(text[index])
                index++
            }
        }
    }
}
