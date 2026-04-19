package com.onlyjesus

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private val Context.dataStore by preferencesDataStore(name = "reader_settings")

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AmoledTheme {
                ReaderScreen(this)
            }
        }
    }
}

@Composable
private fun ReaderScreen(context: Context) {
    val scope = rememberCoroutineScope()
    val prefs = remember { ReaderPreferencesStore(context) }
    val repository = remember { BibleRepository(context) }
    val reader = remember { SQLiteBibleReader() }

    var selectedVersion by remember { mutableStateOf<InstalledVersion?>(null) }
    var currentBook by remember { mutableStateOf(1) }
    var currentChapter by remember { mutableStateOf(1) }
    var fontFamilyKey by remember { mutableStateOf("serif") }
    var fontSizeSp by remember { mutableStateOf(20f) }
    var status by remember { mutableStateOf("No offline Bible selected.") }
    var isBusy by remember { mutableStateOf(false) }
    var installedExpanded by remember { mutableStateOf(false) }
    var remoteExpanded by remember { mutableStateOf(false) }
    val verses = remember { mutableStateListOf<Verse>() }
    val installedVersions = remember { mutableStateListOf<InstalledVersion>() }
    val remoteVersions = remember { mutableStateListOf<RemoteVersion>() }

    fun selectedFontFamily(): FontFamily = when (fontFamilyKey) {
        "sans" -> FontFamily.SansSerif
        "mono" -> FontFamily.Monospace
        else -> FontFamily.Serif
    }

    fun refreshInstalled() {
        installedVersions.clear()
        installedVersions.addAll(repository.installedVersions())
    }

    fun loadChapter() {
        val version = selectedVersion ?: return
        scope.launch {
            isBusy = true
            val chapterText = withContext(Dispatchers.IO) {
                reader.readChapter(version.file, currentBook, currentChapter)
            }
            verses.clear()
            verses.addAll(chapterText)
            status = if (chapterText.isEmpty()) {
                "No text found at Book $currentBook Chapter $currentChapter."
            } else {
                "${version.label}: Book $currentBook Chapter $currentChapter"
            }
            prefs.savePosition(currentBook, currentChapter)
            isBusy = false
        }
    }

    LaunchedEffect(Unit) {
        val saved = prefs.load()
        currentBook = saved.book
        currentChapter = saved.chapter
        fontFamilyKey = saved.fontFamily
        fontSizeSp = saved.fontSize

        refreshInstalled()

        selectedVersion = installedVersions.firstOrNull {
            it.file.absolutePath == saved.versionPath
        } ?: installedVersions.firstOrNull()

        selectedVersion?.let {
            prefs.saveVersion(it.file.absolutePath, it.label)
            loadChapter()
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        color = Color.Black,
        contentColor = Color(0xFFE8E6E3)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("OnlyJesus Bible Reader", style = MaterialTheme.typography.titleLarge)

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = { installedExpanded = true }) {
                    Text(selectedVersion?.label ?: "Select offline version")
                }
                DropdownMenu(expanded = installedExpanded, onDismissRequest = { installedExpanded = false }) {
                    installedVersions.forEach { version ->
                        DropdownMenuItem(
                            text = { Text(version.label) },
                            onClick = {
                                installedExpanded = false
                                selectedVersion = version
                                scope.launch { prefs.saveVersion(version.file.absolutePath, version.label) }
                                loadChapter()
                            }
                        )
                    }
                }

                Button(enabled = !isBusy, onClick = {
                    scope.launch {
                        isBusy = true
                        status = "Loading version catalog..."
                        val fetched = repository.fetchRemoteVersions()
                        remoteVersions.clear()
                        remoteVersions.addAll(fetched)
                        status = if (fetched.isEmpty()) "No versions found from scrollmapper." else "Choose a version to download."
                        isBusy = false
                        remoteExpanded = fetched.isNotEmpty()
                    }
                }) { Text("Sync") }

                DropdownMenu(expanded = remoteExpanded, onDismissRequest = { remoteExpanded = false }) {
                    remoteVersions.forEach { remote ->
                        DropdownMenuItem(
                            text = { Text("${remote.displayName} (${remote.sizeMb} MB)") },
                            onClick = {
                                remoteExpanded = false
                                scope.launch {
                                    isBusy = true
                                    status = "Downloading ${remote.displayName}..."
                                    val installed = repository.installRemoteVersion(remote)
                                    refreshInstalled()
                                    selectedVersion = installed
                                    prefs.saveVersion(installed.file.absolutePath, installed.label)
                                    status = "Downloaded ${installed.label}."
                                    loadChapter()
                                    isBusy = false
                                }
                            }
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(enabled = !isBusy && selectedVersion != null, onClick = {
                    scope.launch {
                        val version = selectedVersion ?: return@launch
                        val adjacent = withContext(Dispatchers.IO) {
                            reader.findAdjacent(version.file, currentBook, currentChapter, -1)
                        }
                        adjacent?.let {
                            currentBook = it.book
                            currentChapter = it.chapter
                            loadChapter()
                        }
                    }
                }) { Text("Prev") }

                Button(enabled = !isBusy && selectedVersion != null, onClick = {
                    scope.launch {
                        val version = selectedVersion ?: return@launch
                        val adjacent = withContext(Dispatchers.IO) {
                            reader.findAdjacent(version.file, currentBook, currentChapter, 1)
                        }
                        adjacent?.let {
                            currentBook = it.book
                            currentChapter = it.chapter
                            loadChapter()
                        }
                    }
                }) { Text("Next") }

                Text("Book $currentBook Chapter $currentChapter", modifier = Modifier.padding(top = 10.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = {
                    fontFamilyKey = when (fontFamilyKey) {
                        "serif" -> "sans"
                        "sans" -> "mono"
                        else -> "serif"
                    }
                    scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
                }) {
                    Text("Font: ${fontFamilyLabel(fontFamilyKey)}")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Size: ${fontSizeSp.toInt()}sp")
                    Slider(
                        value = fontSizeSp,
                        onValueChange = {
                            fontSizeSp = it
                            scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
                        },
                        valueRange = 14f..34f
                    )
                }
            }

            Text(status, color = Color(0xFF9DB7A6))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(verses) { verse ->
                    Text(
                        text = "${verse.number}. ${verse.text}",
                        color = Color(0xFFE8E6E3),
                        fontSize = fontSizeSp.sp,
                        fontFamily = selectedFontFamily()
                    )
                }
            }
        }
    }
}

@Composable
private fun AmoledTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme.copy(
            background = Color.Black,
            surface = Color.Black,
            onBackground = Color(0xFFE8E6E3),
            onSurface = Color(0xFFE8E6E3),
            primary = Color(0xFF82A98E)
        ),
        content = content
    )
}

data class Verse(val number: Int, val text: String)
data class ChapterLocation(val book: Int, val chapter: Int)
data class InstalledVersion(val label: String, val file: File)
data class RemoteVersion(val branch: String, val path: String, val sizeBytes: Long) {
    val displayName: String = displayNameFromPath(path)
    val sizeMb: String = String.format("%.1f", sizeBytes / 1024.0 / 1024.0)
    val downloadUrl: String = "https://raw.githubusercontent.com/scrollmapper/bible_databases/$branch/$path"
}

private data class ReaderSettings(
    val versionPath: String,
    val book: Int,
    val chapter: Int,
    val fontFamily: String,
    val fontSize: Float
)

private class ReaderPreferencesStore(private val context: Context) {
    private val versionPathKey = stringPreferencesKey("version_path")
    private val versionNameKey = stringPreferencesKey("version_name")
    private val bookKey = intPreferencesKey("book")
    private val chapterKey = intPreferencesKey("chapter")
    private val fontFamilyKey = stringPreferencesKey("font_family")
    private val fontSizeKey = floatPreferencesKey("font_size")

    suspend fun load(): ReaderSettings {
        val prefs = context.dataStore.data.first()
        return ReaderSettings(
            versionPath = prefs[versionPathKey] ?: "",
            book = prefs[bookKey] ?: 1,
            chapter = prefs[chapterKey] ?: 1,
            fontFamily = prefs[fontFamilyKey] ?: "serif",
            fontSize = prefs[fontSizeKey] ?: 20f
        )
    }

    suspend fun saveVersion(path: String, name: String) {
        context.dataStore.edit {
            it[versionPathKey] = path
            it[versionNameKey] = name
        }
    }

    suspend fun savePosition(book: Int, chapter: Int) {
        context.dataStore.edit {
            it[bookKey] = book
            it[chapterKey] = chapter
        }
    }

    suspend fun saveFont(fontFamily: String, fontSize: Float) {
        context.dataStore.edit {
            it[fontFamilyKey] = fontFamily
            it[fontSizeKey] = fontSize
        }
    }
}

private class BibleRepository(private val context: Context) {
    private val versionsDir: File = File(context.filesDir, "versions").apply { mkdirs() }

    fun installedVersions(): List<InstalledVersion> = versionsDir.listFiles()
        ?.filter { it.isFile && it.extension.lowercase() in listOf("db", "sqlite", "sqlite3") }
        ?.sortedBy { it.name.lowercase() }
        ?.map { InstalledVersion(displayNameFromPath(it.name), it) }
        .orEmpty()

    suspend fun fetchRemoteVersions(): List<RemoteVersion> = withContext(Dispatchers.IO) {
        val branches = listOf("main", "master")
        for (branch in branches) {
            val versions = runCatching { fetchTree(branch) }.getOrDefault(emptyList())
            if (versions.isNotEmpty()) return@withContext versions
        }
        emptyList()
    }

    suspend fun installRemoteVersion(remote: RemoteVersion): InstalledVersion = withContext(Dispatchers.IO) {
        val target = File(versionsDir, remote.path.substringAfterLast('/'))
        if (!target.exists()) {
            downloadFile(remote.downloadUrl, target)
        }
        InstalledVersion(displayNameFromPath(target.name), target)
    }

    private fun fetchTree(branch: String): List<RemoteVersion> {
        val endpoint = "https://api.github.com/repos/scrollmapper/bible_databases/git/trees/$branch?recursive=1"
        val payload = getText(endpoint)
        val root = JSONObject(payload)
        val tree = root.optJSONArray("tree") ?: return emptyList()

        val versions = mutableListOf<RemoteVersion>()
        for (i in 0 until tree.length()) {
            val node = tree.getJSONObject(i)
            if (node.optString("type") != "blob") continue
            val path = node.optString("path")
            val lower = path.lowercase()
            val isBibleDb = lower.endsWith(".db") || lower.endsWith(".sqlite") || lower.endsWith(".sqlite3")
            if (!isBibleDb) continue
            versions += RemoteVersion(branch = branch, path = path, sizeBytes = node.optLong("size", 0))
        }
        return versions.sortedBy { it.displayName }
    }

    private fun downloadFile(url: String, file: File) {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 60_000
            requestMethod = "GET"
        }
        connection.inputStream.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }
        connection.disconnect()
    }

    private fun getText(url: String): String {
        val connection = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 60_000
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github+json")
        }
        val result = connection.inputStream.bufferedReader().use { it.readText() }
        connection.disconnect()
        return result
    }
}

private class SQLiteBibleReader {
    fun readChapter(dbFile: File, book: Int, chapter: Int): List<Verse> {
        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        db.use {
            val table = resolveVerseTable(it) ?: return emptyList()
            val rows = mutableListOf<Verse>()
            val query = "SELECT ${table.verseColumn}, ${table.textColumn} FROM ${table.tableName} WHERE ${table.bookColumn} = ? AND ${table.chapterColumn} = ? ORDER BY ${table.verseColumn}"
            it.rawQuery(query, arrayOf(book.toString(), chapter.toString())).use { cursor ->
                while (cursor.moveToNext()) {
                    rows += Verse(
                        number = cursor.getInt(0),
                        text = cursor.getString(1) ?: ""
                    )
                }
            }
            return rows
        }
    }

    fun findAdjacent(dbFile: File, book: Int, chapter: Int, direction: Int): ChapterLocation? {
        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        db.use {
            val table = resolveVerseTable(it) ?: return null
            val refs = mutableListOf<ChapterLocation>()
            val query = "SELECT DISTINCT ${table.bookColumn}, ${table.chapterColumn} FROM ${table.tableName} ORDER BY ${table.bookColumn}, ${table.chapterColumn}"
            it.rawQuery(query, emptyArray()).use { cursor ->
                while (cursor.moveToNext()) {
                    refs += ChapterLocation(cursor.getInt(0), cursor.getInt(1))
                }
            }
            val index = refs.indexOfFirst { it.book == book && it.chapter == chapter }
            if (index == -1) return refs.firstOrNull()
            val targetIndex = index + direction
            return refs.getOrNull(targetIndex)
        }
    }

    private fun resolveVerseTable(db: SQLiteDatabase): VerseTable? {
        val tables = mutableListOf<String>()
        db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", emptyArray()).use { cursor ->
            while (cursor.moveToNext()) {
                val name = cursor.getString(0)
                if (!name.startsWith("sqlite_")) tables += name
            }
        }

        for (table in tables) {
            val columns = mutableListOf<String>()
            db.rawQuery("PRAGMA table_info($table)", emptyArray()).use { cursor ->
                while (cursor.moveToNext()) {
                    columns += cursor.getString(1)
                }
            }
            val mapping = mapColumns(columns) ?: continue
            return VerseTable(table, mapping.book, mapping.chapter, mapping.verse, mapping.text)
        }
        return null
    }

    private fun mapColumns(columns: List<String>): MappedColumns? {
        fun pick(vararg names: String): String? = columns.firstOrNull { col -> names.any { it.equals(col, true) } }
        val book = pick("book", "book_number", "book_id", "b") ?: return null
        val chapter = pick("chapter", "chapter_number", "chapter_id", "c") ?: return null
        val verse = pick("verse", "verse_number", "verse_id", "v") ?: return null
        val text = pick("text", "scripture", "content", "t") ?: return null
        return MappedColumns(book, chapter, verse, text)
    }

    private data class MappedColumns(val book: String, val chapter: String, val verse: String, val text: String)
    private data class VerseTable(
        val tableName: String,
        val bookColumn: String,
        val chapterColumn: String,
        val verseColumn: String,
        val textColumn: String
    )
}

fun displayNameFromPath(path: String): String {
    val name = path.substringAfterLast('/').substringBeforeLast('.')
    return name
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { token -> token.replaceFirstChar { c -> c.uppercase() } }
        .ifBlank { "Bible" }
}

private fun fontFamilyLabel(key: String): String = when (key) {
    "sans" -> "Sans"
    "mono" -> "Mono"
    else -> "Serif"
}
