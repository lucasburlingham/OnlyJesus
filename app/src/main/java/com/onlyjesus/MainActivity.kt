package com.onlyjesus

import android.app.Activity
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

private const val SEARCH_RESULT_PREVIEW_LENGTH = 100
private const val CONTENT_BOTTOM_PADDING = 148
private val MenuBackgroundColor = Color(0xFF111111)
private val MenuTextColor = Color(0xFFE8E6E3)
private val BibleSources = listOf(
    BibleSource(
        owner = "scrollmapper",
        repo = "bible_databases",
        branch = "2024"
    )
)
private val BibleBookNames = listOf(
    "Genesis",
    "Exodus",
    "Leviticus",
    "Numbers",
    "Deuteronomy",
    "Joshua",
    "Judges",
    "Ruth",
    "1 Samuel",
    "2 Samuel",
    "1 Kings",
    "2 Kings",
    "1 Chronicles",
    "2 Chronicles",
    "Ezra",
    "Nehemiah",
    "Esther",
    "Job",
    "Psalms",
    "Proverbs",
    "Ecclesiastes",
    "Song of Solomon",
    "Isaiah",
    "Jeremiah",
    "Lamentations",
    "Ezekiel",
    "Daniel",
    "Hosea",
    "Joel",
    "Amos",
    "Obadiah",
    "Jonah",
    "Micah",
    "Nahum",
    "Habakkuk",
    "Zephaniah",
    "Haggai",
    "Zechariah",
    "Malachi",
    "Matthew",
    "Mark",
    "Luke",
    "John",
    "Acts",
    "Romans",
    "1 Corinthians",
    "2 Corinthians",
    "Galatians",
    "Ephesians",
    "Philippians",
    "Colossians",
    "1 Thessalonians",
    "2 Thessalonians",
    "1 Timothy",
    "2 Timothy",
    "Titus",
    "Philemon",
    "Hebrews",
    "James",
    "1 Peter",
    "2 Peter",
    "1 John",
    "2 John",
    "3 John",
    "Jude",
    "Revelation"
)
private val Context.dataStore by preferencesDataStore(name = "reader_settings")

private enum class ReaderPage {
    Scripture,
    Search,
    Settings
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            AmoledTheme {
                ReaderScreen(this)
            }
        }
    }
}

@Composable
private fun ReaderScreen(context: Context) {
    val view = LocalView.current
    DisposableEffect(view) {
        val activity = view.context as? Activity
        if (activity == null) {
            onDispose { }
        } else {
            val window = activity.window
            val controller = WindowCompat.getInsetsController(window, view)
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.statusBars())
            onDispose {
                controller.show(WindowInsetsCompat.Type.statusBars())
                WindowCompat.setDecorFitsSystemWindows(window, true)
            }
        }
    }

    val scope = rememberCoroutineScope()
    val prefs = remember { ReaderPreferencesStore(context) }
    val repository = remember { BibleRepository(context) }
    val reader = remember { JsonBibleReader() }

    var currentPage by remember { mutableStateOf(ReaderPage.Scripture) }
    var selectedVersion by remember { mutableStateOf<InstalledVersion?>(null) }
    var currentBook by remember { mutableStateOf(1) }
    var currentChapter by remember { mutableStateOf(1) }
    var currentVerse by remember { mutableStateOf(1) }
    var fontFamilyKey by remember { mutableStateOf("serif") }
    var fontSizeSp by remember { mutableStateOf(20f) }
    var status by remember { mutableStateOf("No offline Bible selected.") }
    var isBusy by remember { mutableStateOf(false) }
    var installedExpanded by remember { mutableStateOf(false) }
    var remoteExpanded by remember { mutableStateOf(false) }
    var bookExpanded by remember { mutableStateOf(false) }
    var chapterExpanded by remember { mutableStateOf(false) }
    var verseExpanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val verses = remember { mutableStateListOf<Verse>() }
    val verseListState = rememberLazyListState()
    val availableBooks = remember { mutableStateListOf<Int>() }
    val availableChapters = remember { mutableStateListOf<Int>() }
    val availableVerses = remember { mutableStateListOf<Int>() }
    val searchResults = remember { mutableStateListOf<VerseSearchHit>() }
    val installedVersions = remember { mutableStateListOf<InstalledVersion>() }
    val remoteVersions = remember { mutableStateListOf<RemoteVersion>() }

    fun selectedFontFamily(): FontFamily = when (fontFamilyKey) {
        "sans" -> FontFamily.SansSerif
        "mono" -> FontFamily.Monospace
        else -> FontFamily.Serif
    }

    fun scriptureReference(): String = "${bookName(currentBook)} $currentChapter:$currentVerse"

    fun refreshInstalled() {
        installedVersions.clear()
        installedVersions.addAll(repository.installedVersions())
    }

    fun loadChapter() {
        val version = selectedVersion ?: return
        scope.launch {
            isBusy = true
            val chapterLoad = withContext(Dispatchers.IO) {
                val books = reader.availableBooks(version.file)
                val normalizedBook = books.firstOrNull { it == currentBook } ?: books.firstOrNull() ?: 1
                val chapters = reader.availableChapters(version.file, normalizedBook)
                val normalizedChapter = chapters.firstOrNull { it == currentChapter } ?: chapters.firstOrNull() ?: 1
                val chapterText = reader.readChapter(version.file, normalizedBook, normalizedChapter)
                val verseNumbers = chapterText.map { it.number }
                val normalizedVerse = verseNumbers.firstOrNull { it == currentVerse } ?: verseNumbers.firstOrNull() ?: 1
                ChapterLoad(
                    book = normalizedBook,
                    chapter = normalizedChapter,
                    verse = normalizedVerse,
                    books = books,
                    chapters = chapters,
                    verses = verseNumbers,
                    chapterText = chapterText
                )
            }
            currentBook = chapterLoad.book
            currentChapter = chapterLoad.chapter
            currentVerse = chapterLoad.verse
            availableBooks.clear()
            availableBooks.addAll(chapterLoad.books)
            availableChapters.clear()
            availableChapters.addAll(chapterLoad.chapters)
            availableVerses.clear()
            availableVerses.addAll(chapterLoad.verses)
            verses.clear()
            verses.addAll(chapterLoad.chapterText)
            status = if (chapterLoad.chapterText.isEmpty()) {
                "No text found."
            } else {
                ""
            }
            prefs.savePosition(currentBook, currentChapter)
            isBusy = false
        }
    }

    fun navigateChapter(direction: Int) {
        val version = selectedVersion ?: return
        scope.launch {
            val adjacent = withContext(Dispatchers.IO) {
                reader.findAdjacent(version.file, currentBook, currentChapter, direction)
            }
            adjacent?.let {
                currentBook = it.book
                currentChapter = it.chapter
                currentVerse = 1
                loadChapter()
            }
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

    LaunchedEffect(currentPage, currentBook, currentChapter, currentVerse, verses) {
        if (currentPage != ReaderPage.Scripture || verses.isEmpty()) return@LaunchedEffect
        val targetIndex = verses.indexOfFirst { it.number == currentVerse }.let { index ->
            if (index >= 0) index else 0
        }
        verseListState.scrollToItem(targetIndex)
    }

    LaunchedEffect(currentPage, verses) {
        if (currentPage != ReaderPage.Scripture) return@LaunchedEffect
        snapshotFlow { verseListState.firstVisibleItemIndex }
            .collect { index ->
                val visibleVerse = verses.getOrNull(index)?.number ?: return@collect
                if (visibleVerse != currentVerse) {
                    currentVerse = visibleVerse
                }
            }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        color = Color.Black,
        contentColor = Color(0xFFE8E6E3)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                    .padding(bottom = CONTENT_BOTTOM_PADDING.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        TextButton(
                            onClick = { currentPage = ReaderPage.Settings },
                            modifier = Modifier.align(Alignment.CenterEnd)
                        ) {
                            Text(
                                text = "⚙",
                                color = Color(0xFFE8E6E3).copy(alpha = 0.75f),
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentPage) {
                        ReaderPage.Scripture -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .pointerInput(currentPage, selectedVersion, currentBook, currentChapter, isBusy) {
                                        var dragDistance = 0f
                                        detectHorizontalDragGestures(
                                            onHorizontalDrag = { _, dragAmount ->
                                                dragDistance += dragAmount
                                            },
                                            onDragEnd = {
                                                if (!isBusy && selectedVersion != null) {
                                                    if (dragDistance <= -120f) {
                                                        navigateChapter(1)
                                                    } else if (dragDistance >= 120f) {
                                                        navigateChapter(-1)
                                                    }
                                                }
                                                dragDistance = 0f
                                            },
                                            onDragCancel = {
                                                dragDistance = 0f
                                            }
                                        )
                                    }
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(enabled = !isBusy && selectedVersion != null, onClick = { navigateChapter(-1) }) { Text("Prev") }

                                        Button(enabled = !isBusy && selectedVersion != null, onClick = { navigateChapter(1) }) { Text("Next") }

                                        Text(
                                            text = scriptureReference(),
                                            modifier = Modifier.padding(top = 10.dp)
                                        )
                                    }

                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        TextButton(enabled = availableBooks.isNotEmpty(), onClick = { bookExpanded = true }) {
                                            Text("B: ${bookName(currentBook)}")
                                        }
                                        DropdownMenu(
                                            expanded = bookExpanded,
                                            onDismissRequest = { bookExpanded = false },
                                            containerColor = MenuBackgroundColor
                                        ) {
                                            availableBooks.forEach { book ->
                                                DropdownMenuItem(
                                                    text = { Text(bookName(book), color = MenuTextColor) },
                                                    onClick = {
                                                        bookExpanded = false
                                                        currentBook = book
                                                        currentChapter = 1
                                                        currentVerse = 1
                                                        loadChapter()
                                                    }
                                                )
                                            }
                                        }

                                        TextButton(enabled = availableChapters.isNotEmpty(), onClick = { chapterExpanded = true }) {
                                            Text("Ch: $currentChapter")
                                        }
                                        DropdownMenu(
                                            expanded = chapterExpanded,
                                            onDismissRequest = { chapterExpanded = false },
                                            containerColor = MenuBackgroundColor
                                        ) {
                                            availableChapters.forEach { chapter ->
                                                DropdownMenuItem(
                                                    text = { Text("Chapter $chapter", color = MenuTextColor) },
                                                    onClick = {
                                                        chapterExpanded = false
                                                        currentChapter = chapter
                                                        currentVerse = 1
                                                        loadChapter()
                                                    }
                                                )
                                            }
                                        }

                                        TextButton(enabled = availableVerses.isNotEmpty(), onClick = { verseExpanded = true }) {
                                            Text("V: $currentVerse")
                                        }
                                        DropdownMenu(
                                            expanded = verseExpanded,
                                            onDismissRequest = { verseExpanded = false },
                                            containerColor = MenuBackgroundColor
                                        ) {
                                            availableVerses.forEach { verse ->
                                                DropdownMenuItem(
                                                    text = { Text("Verse $verse", color = MenuTextColor) },
                                                    onClick = {
                                                        verseExpanded = false
                                                        currentVerse = verse
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Text(scriptureReference(), color = Color(0xFF9DB7A6))

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        state = verseListState,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        items(verses) { verse ->
                                            var verseMenuExpanded by remember(verse.number, verse.text) { mutableStateOf(false) }
                                            val verseText = "${bookName(currentBook)} $currentChapter:${verse.number} ${verse.text}"
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            color = if (verse.number == currentVerse) Color(0xFF132018) else Color.Transparent,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .pointerInput(verse.number, verse.text) {
                                                            detectTapGestures(
                                                                onTap = { currentVerse = verse.number },
                                                                onLongPress = { verseMenuExpanded = true }
                                                            )
                                                        }
                                                        .padding(horizontal = 6.dp, vertical = 8.dp)
                                                ) {
                                                    Text(
                                                        text = "${verse.number}. ${verse.text}",
                                                        color = if (verse.number == currentVerse) Color(0xFFCAE6D0) else Color(0xFFE8E6E3),
                                                        fontSize = fontSizeSp.sp,
                                                        fontFamily = selectedFontFamily()
                                                    )
                                                }
                                                DropdownMenu(
                                                    expanded = verseMenuExpanded,
                                                    onDismissRequest = { verseMenuExpanded = false },
                                                    containerColor = MenuBackgroundColor
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text("Copy verse", color = MenuTextColor) },
                                                        onClick = {
                                                            verseMenuExpanded = false
                                                            copyText(context, verseText)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Share verse", color = MenuTextColor) },
                                                        onClick = {
                                                            verseMenuExpanded = false
                                                            shareText(context, "${selectedVersion?.label ?: "Bible"} - ${bookName(currentBook)} $currentChapter:${verse.number}", verseText)
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ReaderPage.Search -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = if (selectedVersion == null) "Pick a Bible version in settings to search." else "Search the current Bible version.",
                                    color = Color(0xFF9DB7A6)
                                )

                                OutlinedTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    label = { Text("Search verses") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Button(
                                    enabled = !isBusy && selectedVersion != null && searchQuery.isNotBlank(),
                                    onClick = {
                                        scope.launch {
                                            val version = selectedVersion ?: return@launch
                                            isBusy = true
                                            val query = searchQuery.trim()
                                            val matches = withContext(Dispatchers.IO) {
                                                reader.searchVersesLike(version.file, query)
                                            }
                                            searchResults.clear()
                                            searchResults.addAll(matches)
                                            status = if (matches.isEmpty()) {
                                                "No results for \"$query\"."
                                            } else {
                                                "Found ${matches.size} result(s) for \"$query\"."
                                            }
                                            isBusy = false
                                        }
                                    }
                                ) { Text("Find") }

                                Text(status, color = Color(0xFF9DB7A6))

                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(searchResults) { result ->
                                        var resultMenuExpanded by remember(result.book, result.chapter, result.verse, result.text) { mutableStateOf(false) }
                                        val resultText = "${bookName(result.book)} ${result.chapter}:${result.verse} ${result.text}"
                                        Box(modifier = Modifier.fillMaxWidth()) {
                                            TextButton(onClick = {
                                                currentBook = result.book
                                                currentChapter = result.chapter
                                                currentVerse = result.verse
                                                searchResults.clear()
                                                currentPage = ReaderPage.Scripture
                                                loadChapter()
                                            }, modifier = Modifier
                                                .fillMaxWidth()
                                                .pointerInput(result.book, result.chapter, result.verse, result.text) {
                                                    detectTapGestures(onLongPress = { resultMenuExpanded = true })
                                                }) {
                                                val preview = result.text.take(SEARCH_RESULT_PREVIEW_LENGTH).let {
                                                    if (result.text.length > SEARCH_RESULT_PREVIEW_LENGTH) "$it…" else it
                                                }
                                                Text("${bookName(result.book)} C${result.chapter} V${result.verse} $preview")
                                            }
                                            DropdownMenu(
                                                expanded = resultMenuExpanded,
                                                onDismissRequest = { resultMenuExpanded = false },
                                                containerColor = MenuBackgroundColor
                                            ) {
                                                DropdownMenuItem(
                                                    text = { Text("Copy verse", color = MenuTextColor) },
                                                    onClick = {
                                                        resultMenuExpanded = false
                                                        copyText(context, resultText)
                                                    }
                                                )
                                                DropdownMenuItem(
                                                    text = { Text("Share verse", color = MenuTextColor) },
                                                    onClick = {
                                                        resultMenuExpanded = false
                                                        shareText(context, "${selectedVersion?.label ?: "Bible"} - ${bookName(result.book)} ${result.chapter}:${result.verse}", resultText)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        ReaderPage.Settings -> {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Bible version", style = MaterialTheme.typography.titleMedium)

                                Box {
                                    TextButton(onClick = { installedExpanded = true }) {
                                        Text(selectedVersion?.label ?: "Select offline version")
                                    }
                                    DropdownMenu(
                                        expanded = installedExpanded,
                                        onDismissRequest = { installedExpanded = false },
                                        containerColor = MenuBackgroundColor
                                    ) {
                                        installedVersions.forEach { version ->
                                            DropdownMenuItem(
                                                text = { Text(version.label, color = MenuTextColor) },
                                                onClick = {
                                                    installedExpanded = false
                                                    selectedVersion = version
                                                    scope.launch { prefs.saveVersion(version.file.absolutePath, version.label) }
                                                    loadChapter()
                                                }
                                            )
                                        }
                                    }
                                }

                                Box {
                                    Button(enabled = !isBusy, onClick = {
                                        scope.launch {
                                            isBusy = true
                                            status = "Loading version catalog..."
                                            val fetched = repository.fetchRemoteVersions()
                                            remoteVersions.clear()
                                            remoteVersions.addAll(fetched)
                                            status = if (fetched.isEmpty()) "No versions found from available sources." else "Choose a version to download."
                                            isBusy = false
                                            remoteExpanded = fetched.isNotEmpty()
                                        }
                                    }) { Text("Refresh sources") }

                                    DropdownMenu(
                                        expanded = remoteExpanded,
                                        onDismissRequest = { remoteExpanded = false },
                                        containerColor = MenuBackgroundColor
                                    ) {
                                        remoteVersions.forEach { remote ->
                                            DropdownMenuItem(
                                                text = { Text("${remote.displayName} (${remote.sizeMb} MB)", color = MenuTextColor) },
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

                                Text("Reader style", style = MaterialTheme.typography.titleMedium)

                                val previewVerse = verses.firstOrNull { it.number == currentVerse }
                                Text("Style preview", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9DB7A6))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF132018), RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = previewVerse?.let {
                                            "${bookName(currentBook)} $currentChapter:${it.number} ${it.text}"
                                        } ?: "Select a verse in Scripture to preview its styling here.",
                                        color = if (previewVerse != null) Color(0xFFCAE6D0) else Color(0xFF9DB7A6),
                                        fontSize = fontSizeSp.sp,
                                        fontFamily = selectedFontFamily()
                                    )
                                }

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

                                Text("Size: ${fontSizeSp.toInt()}sp", color = Color(0xFF9DB7A6))
                                Slider(
                                    value = fontSizeSp,
                                    onValueChange = {
                                        fontSizeSp = it
                                        scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
                                    },
                                    valueRange = 14f..34f
                                )

                                Text(status, color = Color(0xFF9DB7A6))
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Button(
                    onClick = { currentPage = ReaderPage.Search },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        1.dp,
                        if (currentPage == ReaderPage.Search) Color(0xFF82A98E) else Color(0xFF3A3F3C)
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == ReaderPage.Search) Color(0xFF18251D) else Color(0xFF121212),
                        contentColor = if (currentPage == ReaderPage.Search) Color(0xFFF2F6F4) else Color(0xFFB9C9BF)
                    )
                ) {
                    Text("Search")
                }
                Button(
                    onClick = { currentPage = ReaderPage.Scripture },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        1.dp,
                        if (currentPage == ReaderPage.Scripture) Color(0xFF82A98E) else Color(0xFF3A3F3C)
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == ReaderPage.Scripture) Color(0xFF18251D) else Color(0xFF121212),
                        contentColor = if (currentPage == ReaderPage.Scripture) Color(0xFFF2F6F4) else Color(0xFFB9C9BF)
                    )
                ) {
                    Text("Scripture")
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
data class VerseSearchHit(val book: Int, val chapter: Int, val verse: Int, val text: String)
private data class ChapterLoad(
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val books: List<Int>,
    val chapters: List<Int>,
    val verses: List<Int>,
    val chapterText: List<Verse>
)
data class ChapterLocation(val book: Int, val chapter: Int)
data class InstalledVersion(val label: String, val file: File)
data class BibleSource(val owner: String, val repo: String, val branch: String) {
    val key: String = "${owner}_${repo}_$branch"
    val label: String = "$owner/$repo"
}

data class RemoteVersion(val source: BibleSource, val branch: String, val path: String, val sizeBytes: Long) {
    val displayName: String = "${source.label}: ${displayNameFromPath(path)}"
    val sizeMb: String = String.format("%.1f", sizeBytes / 1024.0 / 1024.0)
    val downloadUrl: String = "https://raw.githubusercontent.com/${source.owner}/${source.repo}/$branch/$path"
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

    fun installedVersions(): List<InstalledVersion> = versionsDir
        .walkTopDown()
        .filter { it.isFile && it.extension.lowercase() == "json" }
        .sortedBy { it.absolutePath.lowercase() }
        .map { file ->
            val sourceLabel = sourceLabelFromPath(file)
            val displayName = displayNameFromPath(file.name)
            InstalledVersion(
                label = if (sourceLabel == null) displayName else "$sourceLabel: $displayName",
                file = file
            )
        }
        .toList()

    suspend fun fetchRemoteVersions(): List<RemoteVersion> = withContext(Dispatchers.IO) {
        val versions = mutableListOf<RemoteVersion>()
        for (source in BibleSources) {
            val fetched = runCatching { fetchTree(source, source.branch) }.getOrDefault(emptyList())
            versions.addAll(fetched)
        }
        versions.sortedBy { it.displayName.lowercase() }
    }

    suspend fun installRemoteVersion(remote: RemoteVersion): InstalledVersion = withContext(Dispatchers.IO) {
        val target = File(File(versionsDir, remote.source.key), remote.path.substringAfterLast('/'))
        target.parentFile?.mkdirs()
        if (!target.exists()) {
            downloadFile(remote.downloadUrl, target)
        }
        InstalledVersion(label = remote.displayName, file = target)
    }

    private fun fetchTree(source: BibleSource, branch: String): List<RemoteVersion> {
        val endpoint = "https://api.github.com/repos/${source.owner}/${source.repo}/git/trees/$branch?recursive=1"
        val payload = getText(endpoint)
        val root = JSONObject(payload)
        val tree = root.optJSONArray("tree") ?: return emptyList()

        val versions = mutableListOf<RemoteVersion>()
        for (i in 0 until tree.length()) {
            val node = tree.getJSONObject(i)
            if (node.optString("type") != "blob") continue
            val path = node.optString("path")
            val lower = path.lowercase()
            val isJsonVersion = lower.startsWith("json/t_") && lower.endsWith(".json")
            if (!isJsonVersion) continue
            versions += RemoteVersion(
                source = source,
                branch = branch,
                path = path,
                sizeBytes = node.optLong("size", 0)
            )
        }
        return versions.sortedBy { it.displayName.lowercase() }
    }

    private fun sourceLabelFromPath(file: File): String? {
        val sourceKey = file.parentFile?.name ?: return null
        return BibleSources.firstOrNull { it.key == sourceKey }?.label
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

private class JsonBibleReader {
    private val cache = mutableMapOf<String, JsonBibleData>()

    fun availableBooks(jsonFile: File): List<Int> = load(jsonFile).books

    fun availableChapters(jsonFile: File, book: Int): List<Int> = load(jsonFile).chaptersByBook[book].orEmpty()

    fun searchVersesLike(jsonFile: File, textQuery: String, limit: Int = 100): List<VerseSearchHit> {
        if (textQuery.isBlank()) return emptyList()
        val query = textQuery.trim().lowercase()
        return load(jsonFile).allVerses
            .asSequence()
            .filter { it.text.lowercase().contains(query) }
            .sortedWith(compareBy<JsonVerseRecord> { it.book }.thenBy { it.chapter }.thenBy { it.verse })
            .take(limit)
            .map { VerseSearchHit(it.book, it.chapter, it.verse, it.text) }
            .toList()
    }

    fun readChapter(jsonFile: File, book: Int, chapter: Int): List<Verse> {
        return load(jsonFile).versesByChapter[ChapterLocation(book, chapter)].orEmpty()
    }

    fun findAdjacent(jsonFile: File, book: Int, chapter: Int, direction: Int): ChapterLocation? {
        val refs = load(jsonFile).orderedChapters
        val index = refs.indexOfFirst { it.book == book && it.chapter == chapter }
        if (index == -1) return refs.firstOrNull()
        return refs.getOrNull(index + direction)
    }

    private fun load(jsonFile: File): JsonBibleData {
        val cacheKey = "${jsonFile.absolutePath}:${jsonFile.lastModified()}"
        return cache.getOrPut(cacheKey) { parse(jsonFile) }
    }

    private fun parse(jsonFile: File): JsonBibleData {
        val root = JSONObject(jsonFile.readText())
        val resultset = root.optJSONObject("resultset") ?: return JsonBibleData.empty()
        val rows = resultset.optJSONArray("row") ?: return JsonBibleData.empty()

        val chapterMap = mutableMapOf<ChapterLocation, MutableList<Verse>>()
        val chapterNumbersByBook = mutableMapOf<Int, MutableSet<Int>>()
        val verses = mutableListOf<JsonVerseRecord>()

        for (i in 0 until rows.length()) {
            val row = rows.optJSONObject(i) ?: continue
            val field = row.optJSONArray("field") ?: continue
            if (field.length() < 5) continue

            val book = field.optInt(1, 0)
            val chapter = field.optInt(2, 0)
            val verse = field.optInt(3, 0)
            val text = field.optString(4, "")
            if (book <= 0 || chapter <= 0 || verse <= 0) continue

            verses += JsonVerseRecord(book, chapter, verse, text)
            chapterMap.getOrPut(ChapterLocation(book, chapter)) { mutableListOf() } += Verse(verse, text)
            chapterNumbersByBook.getOrPut(book) { mutableSetOf() }.add(chapter)
        }

        val orderedChapters = chapterMap.keys.sortedWith(compareBy<ChapterLocation> { it.book }.thenBy { it.chapter })
        val versesByChapter = chapterMap.mapValues { (_, chapterVerses) -> chapterVerses.sortedBy { it.number } }
        val books = chapterNumbersByBook.keys.sorted()
        val chaptersByBook = chapterNumbersByBook.mapValues { (_, chapters) -> chapters.sorted() }

        return JsonBibleData(
            books = books,
            chaptersByBook = chaptersByBook,
            orderedChapters = orderedChapters,
            versesByChapter = versesByChapter,
            allVerses = verses
        )
    }

    private data class JsonBibleData(
        val books: List<Int>,
        val chaptersByBook: Map<Int, List<Int>>,
        val orderedChapters: List<ChapterLocation>,
        val versesByChapter: Map<ChapterLocation, List<Verse>>,
        val allVerses: List<JsonVerseRecord>
    ) {
        companion object {
            fun empty() = JsonBibleData(emptyList(), emptyMap(), emptyList(), emptyMap(), emptyList())
        }
    }

    private data class JsonVerseRecord(val book: Int, val chapter: Int, val verse: Int, val text: String)
}

fun displayNameFromPath(path: String): String {
    val fileName = path.substringAfterLast('/')
    if (fileName.lowercase().endsWith(".json") && (fileName.startsWith("t_") || fileName.startsWith("t-"))) {
        return versionLabelFromPath(fileName)
    }

    val name = fileName.substringBeforeLast('.')
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

private fun bookName(bookNumber: Int): String = BibleBookNames.getOrNull(bookNumber - 1) ?: "Book $bookNumber"

private fun versionLabelFromPath(path: String): String {
    val name = path.substringAfterLast('/').substringBeforeLast('.').lowercase()
    val versionKey = name.removePrefix("t_").removePrefix("t-")
    return when (versionKey) {
        "asv" -> "ASV"
        "bbe" -> "BBE"
        "kjv" -> "KJV"
        "web" -> "WEB"
        "ylt" -> "YLT"
        else -> versionKey
            .replace('_', ' ')
            .replace('-', ' ')
            .trim()
            .split(" ")
            .filter { it.isNotBlank() }
            .joinToString(" ") { token -> token.replaceFirstChar { c -> c.uppercase() } }
            .ifBlank { "Bible" }
    }
}

private fun copyText(context: Context, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager ?: return
    clipboard.setPrimaryClip(ClipData.newPlainText("Bible verse", text))
}

private fun shareText(context: Context, subject: String, text: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, subject)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, subject))
}
