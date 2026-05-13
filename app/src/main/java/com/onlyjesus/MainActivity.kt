package com.onlyjesus

import android.app.Activity
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
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
import android.graphics.Typeface

private const val SEARCH_RESULT_PREVIEW_LENGTH = 100
private const val CONTENT_BOTTOM_PADDING = 96
private val MenuBackgroundColor = Color(0xFF111111)
private val MenuTextColor = Color(0xFFE8E6E3)
private val ThemeAccentOptions = listOf(
    Color(0xFF82A98E),
    Color(0xFF74A7A0),
    Color(0xFF7397C7),
    Color(0xFF7B87C8),
    Color(0xFFA17FC2),
    Color(0xFFC07A9A),
    Color(0xFFC77E72),
    Color(0xFFC2A364),
    Color(0xFF95A76C),
    Color(0xFF7E93A6)
)
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

private enum class ReferencePickerStage {
    Book,
    Chapter
}

@Composable
private fun androidPrimaryThemeColor(context: Context): Color {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        androidx.compose.material3.dynamicDarkColorScheme(context).primary
    } else {
        Color(0xFF0A84FF)
    }
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
    var fontFamilyExpanded by remember { mutableStateOf(false) }
    var fontSizeSp by remember { mutableStateOf(20f) }
    var themeColorIndex by remember { mutableStateOf(0) }
    var useAndroidPrimaryTheme by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("No offline Bible selected.") }
    var isBusy by remember { mutableStateOf(false) }
    var installedExpanded by remember { mutableStateOf(false) }
    var remoteExpanded by remember { mutableStateOf(false) }
    var referencePickerExpanded by remember { mutableStateOf(false) }
    var referencePickerStage by remember { mutableStateOf(ReferencePickerStage.Book) }
    var searchQuery by remember { mutableStateOf("") }
    val verses = remember { mutableStateListOf<Verse>() }
    val verseListState = rememberLazyListState()
    val availableBooks = remember { mutableStateListOf<Int>() }
    val availableChapters = remember { mutableStateListOf<Int>() }
    val availableVerses = remember { mutableStateListOf<Int>() }
    val searchResults = remember { mutableStateListOf<VerseSearchHit>() }
    val installedVersions = remember { mutableStateListOf<InstalledVersion>() }
    val remoteVersions = remember { mutableStateListOf<RemoteVersion>() }
    val fontOptions = remember { mutableStateListOf<FontOption>() }
    val settingsScrollState = rememberScrollState()

    fun refreshFonts() {
        fontOptions.clear()
        fontOptions.addAll(repository.installedFonts())
    }

    val importFontLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            isBusy = true
            val importedFont = withContext(Dispatchers.IO) { repository.importFont(uri) }
            refreshFonts()
            fontFamilyKey = importedFont.key
            scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
            isBusy = false
        }
    }

    fun selectedFontFamily(): FontFamily = fontOptions.firstOrNull { it.key == fontFamilyKey }?.family ?: FontFamily.Serif

    @Composable
    fun selectedThemeColor(): Color {
        return if (useAndroidPrimaryTheme) {
            androidPrimaryThemeColor(context)
        } else {
            ThemeAccentOptions[themeColorIndex.coerceIn(0, ThemeAccentOptions.lastIndex)]
        }
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
        themeColorIndex = saved.themeColorIndex.coerceIn(0, ThemeAccentOptions.lastIndex)
        useAndroidPrimaryTheme = saved.useAndroidPrimaryTheme

        refreshInstalled()
        refreshFonts()

        if (fontOptions.none { it.key == fontFamilyKey }) {
            fontFamilyKey = fontOptions.firstOrNull()?.key ?: "serif"
        }

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

    val themeAccent = selectedThemeColor()
    val themeHighlight = themeAccent.copy(alpha = 0.14f)
    val themeBorder = themeAccent.copy(alpha = 0.72f)
    val themeMuted = themeAccent.copy(alpha = 0.5f)

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
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                    .padding(bottom = CONTENT_BOTTOM_PADDING.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                color = themeAccent.copy(alpha = 0.75f),
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
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box {
                                        TextButton(
                                            enabled = !isBusy && selectedVersion != null,
                                            onClick = {
                                                referencePickerStage = ReferencePickerStage.Book
                                                referencePickerExpanded = !referencePickerExpanded
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                                                .background(themeHighlight, RoundedCornerShape(12.dp))
                                        ) {
                                            Column(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalArrangement = Arrangement.spacedBy(2.dp),
                                                horizontalAlignment = Alignment.Start
                                            ) {
                                                Text(
                                                    text = scriptureReference(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = themeAccent
                                                )
                                                Text(
                                                    text = if (referencePickerExpanded) "Pick a book, then a chapter" else "Tap to change book or chapter",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = themeAccent.copy(alpha = 0.78f)
                                                )
                                            }
                                        }

                                        if (referencePickerExpanded) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 56.dp)
                                                    .border(1.dp, themeBorder.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                                                    .background(themeHighlight.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                                    .padding(10.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                            Row(
                                                                modifier = Modifier.fillMaxWidth(),
                                                                horizontalArrangement = Arrangement.End,
                                                                verticalAlignment = Alignment.CenterVertically
                                                            ) {
                                                                TextButton(onClick = {
                                                                    if (referencePickerStage == ReferencePickerStage.Chapter) {
                                                                        referencePickerStage = ReferencePickerStage.Book
                                                                    } else {
                                                                        referencePickerExpanded = false
                                                                    }
                                                                }) {
                                                                    Text(
                                                                        text = if (referencePickerStage == ReferencePickerStage.Chapter) "Back" else "Close",
                                                                        color = themeAccent
                                                                    )
                                                                }
                                                    }

                                                    if (referencePickerStage == ReferencePickerStage.Book) {
                                                        Text(
                                                            text = "Choose a book",
                                                            color = themeAccent.copy(alpha = 0.78f),
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        LazyVerticalGrid(
                                                            columns = GridCells.Fixed(3),
                                                            modifier = Modifier.height(280.dp),
                                                            contentPadding = PaddingValues(4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            items(availableBooks.size) { index ->
                                                                val book = availableBooks[index]
                                                                val selected = book == currentBook
                                                                TextButton(
                                                                    onClick = {
                                                                        currentBook = book
                                                                        currentChapter = 1
                                                                        currentVerse = 1
                                                                        referencePickerStage = ReferencePickerStage.Chapter
                                                                        loadChapter()
                                                                    },
                                                                    colors = ButtonDefaults.textButtonColors(
                                                                        contentColor = if (selected) themeAccent else themeAccent.copy(alpha = 0.78f)
                                                                    ),
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .background(
                                                                            if (selected) themeAccentBackground(themeAccent) else Color.Transparent,
                                                                            RoundedCornerShape(10.dp)
                                                                        )
                                                                        .border(
                                                                            1.dp,
                                                                            if (selected) themeBorder else Color.Transparent,
                                                                            RoundedCornerShape(10.dp)
                                                                        )
                                                                ) {
                                                                    Text(bookName(book), style = MaterialTheme.typography.bodyMedium)
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        Text(
                                                            text = bookName(currentBook),
                                                            color = themeAccent,
                                                            style = MaterialTheme.typography.bodyMedium
                                                        )
                                                        Text(
                                                            text = "Choose a chapter",
                                                            color = themeAccent.copy(alpha = 0.78f),
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                        LazyVerticalGrid(
                                                            columns = GridCells.Fixed(6),
                                                            modifier = Modifier.height(220.dp),
                                                            contentPadding = PaddingValues(4.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                                        ) {
                                                            items(availableChapters.size) { index ->
                                                                val chapter = availableChapters[index]
                                                                val selected = chapter == currentChapter
                                                                TextButton(
                                                                    onClick = {
                                                                        currentChapter = chapter
                                                                        currentVerse = 1
                                                                        referencePickerExpanded = false
                                                                        loadChapter()
                                                                    },
                                                                    colors = ButtonDefaults.textButtonColors(
                                                                        contentColor = if (selected) themeAccent else themeAccent.copy(alpha = 0.78f)
                                                                    ),
                                                                    modifier = Modifier
                                                                        .fillMaxWidth()
                                                                        .background(
                                                                            if (selected) themeAccentBackground(themeAccent) else Color.Transparent,
                                                                            RoundedCornerShape(8.dp)
                                                                        )
                                                                        .border(
                                                                            1.dp,
                                                                            if (selected) themeBorder else Color.Transparent,
                                                                            RoundedCornerShape(8.dp)
                                                                        )
                                                                ) {
                                                                    Text(chapter.toString(), style = MaterialTheme.typography.bodyMedium)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    LazyColumn(
                                        modifier = Modifier.fillMaxSize(),
                                        state = verseListState,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(verses) { verse ->
                                            var verseMenuExpanded by remember(verse.number, verse.text) { mutableStateOf(false) }
                                            val verseText = "${bookName(currentBook)} $currentChapter:${verse.number} ${verse.text}"
                                            val verseDisplay = buildAnnotatedString {
                                                withStyle(SpanStyle(color = if (verse.number == currentVerse) themeMuted else Color(0xFFE8E6E3).copy(alpha = 0.25f))) {
                                                    append("${verse.number}.")
                                                }
                                                append(" ")
                                                withStyle(SpanStyle(color = if (verse.number == currentVerse) themeAccent else Color(0xFFE8E6E3))) {
                                                    append(verse.text)
                                                }
                                            }
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            color = if (verse.number == currentVerse) themeHighlight else Color.Transparent,
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
                                                        text = verseDisplay,
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
                                    color = themeAccent.copy(alpha = 0.78f)
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
                                ,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeHighlight,
                                        contentColor = themeAccent
                                    ),
                                    border = BorderStroke(1.dp, themeBorder)
                                ) { Text("Find") }

                                Text(status, color = themeAccent.copy(alpha = 0.78f))

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
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(settingsScrollState)
                                    .padding(bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Text("Bible version", style = MaterialTheme.typography.titleMedium)

                                Box {
                                    TextButton(
                                        onClick = { installedExpanded = true },
                                        modifier = Modifier.border(1.dp, themeBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        Text(selectedVersion?.label ?: "Select offline version", color = themeAccent)
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
                                    Button(
                                        enabled = !isBusy,
                                        onClick = {
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
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeHighlight,
                                            contentColor = themeAccent
                                        ),
                                        border = BorderStroke(1.dp, themeBorder)
                                    ) { Text("Refresh sources") }

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

                                Text("Theme", style = MaterialTheme.typography.titleMedium, color = themeAccent)

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Use Android primary theme color", color = themeAccent)
                                            Switch(
                                                checked = useAndroidPrimaryTheme,
                                                onCheckedChange = {
                                                    useAndroidPrimaryTheme = it
                                                    scope.launch { prefs.saveTheme(themeColorIndex, useAndroidPrimaryTheme) }
                                                }
                                            )
                                        }

                                        Text(
                                            text = if (useAndroidPrimaryTheme) "Android primary is active. Pick a swatch to keep a fallback when it is off." else "Choose one of 10 subtle theme colors.",
                                            color = themeAccent.copy(alpha = 0.78f)
                                        )

                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            ThemeAccentOptions.chunked(5).forEachIndexed { rowIndex, row ->
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    row.forEachIndexed { columnIndex, swatch ->
                                                        val swatchIndex = rowIndex * 5 + columnIndex
                                                        val selected = swatchIndex == themeColorIndex
                                                        Box(
                                                            modifier = Modifier
                                                                .weight(1f)
                                                                .height(42.dp)
                                                                .background(swatch, RoundedCornerShape(12.dp))
                                                                .border(
                                                                    1.dp,
                                                                    if (selected) themeBorder else Color(0xFF2C3130),
                                                                    RoundedCornerShape(12.dp)
                                                                )
                                                                .pointerInput(swatchIndex, useAndroidPrimaryTheme) {
                                                                    detectTapGestures(onTap = {
                                                                        themeColorIndex = swatchIndex
                                                                        scope.launch { prefs.saveTheme(themeColorIndex, useAndroidPrimaryTheme) }
                                                                    })
                                                                },
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = if (selected) "✓" else "",
                                                                color = Color.White.copy(alpha = if (selected) 0.95f else 0f)
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        Text("Reader style", style = MaterialTheme.typography.titleMedium, color = themeAccent)

                                        Text("Font family", color = themeAccent.copy(alpha = 0.78f))
                                        Box {
                                            TextButton(
                                                onClick = { fontFamilyExpanded = true },
                                                modifier = Modifier
                                                    .border(1.dp, themeBorder, RoundedCornerShape(8.dp))
                                                    .background(themeHighlight, RoundedCornerShape(8.dp))
                                            ) {
                                                Text("${fontFamilyLabel(fontFamilyKey, fontOptions)} ▼", color = themeAccent)
                                            }
                                            DropdownMenu(
                                                expanded = fontFamilyExpanded,
                                                onDismissRequest = { fontFamilyExpanded = false },
                                                containerColor = MenuBackgroundColor
                                            ) {
                                                fontOptions.forEach { option ->
                                                    DropdownMenuItem(
                                                        text = { Text(option.label, color = MenuTextColor) },
                                                        onClick = {
                                                            fontFamilyExpanded = false
                                                            fontFamilyKey = option.key
                                                            scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
                                                        }
                                                    )
                                                }
                                                DropdownMenuItem(
                                                    text = { Text("Add font from file…", color = MenuTextColor) },
                                                    onClick = {
                                                        fontFamilyExpanded = false
                                                        importFontLauncher.launch(
                                                            arrayOf(
                                                                "font/*",
                                                                "application/x-font-ttf",
                                                                "application/x-font-otf",
                                                                "application/octet-stream",
                                                                "*/*"
                                                            )
                                                        )
                                                    }
                                                )
                                            }
                                        }

                                val previewVerse = verses.firstOrNull { it.number == currentVerse }
                                Text("Style preview", style = MaterialTheme.typography.bodyMedium, color = themeAccent.copy(alpha = 0.78f))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(themeHighlight, RoundedCornerShape(8.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = previewVerse?.let {
                                            "${bookName(currentBook)} $currentChapter:${it.number} ${it.text}"
                                        } ?: "Select a verse in Scripture to preview its styling here.",
                                        color = if (previewVerse != null) themeAccent else themeAccent.copy(alpha = 0.78f),
                                        fontSize = fontSizeSp.sp,
                                        fontFamily = selectedFontFamily()
                                    )
                                }

                                Text("Size: ${fontSizeSp.toInt()}sp", color = themeAccent.copy(alpha = 0.78f))
                                Slider(
                                    value = fontSizeSp,
                                    onValueChange = {
                                        fontSizeSp = it
                                        scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
                                    },
                                    valueRange = 14f..34f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeAccent,
                                        activeTrackColor = themeAccent,
                                        activeTickColor = themeAccent,
                                        inactiveTrackColor = themeAccent.copy(alpha = 0.28f),
                                        inactiveTickColor = themeAccent.copy(alpha = 0.28f)
                                    )
                                )

                                Text(status, color = themeAccent.copy(alpha = 0.78f))
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
                        if (currentPage == ReaderPage.Search) themeBorder else Color(0xFF3A3F3C)
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == ReaderPage.Search) themeHighlight else Color(0xFF121212),
                        contentColor = if (currentPage == ReaderPage.Search) Color(0xFFF2F6F4) else themeAccent.copy(alpha = 0.72f)
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
                        if (currentPage == ReaderPage.Scripture) themeBorder else Color(0xFF3A3F3C)
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == ReaderPage.Scripture) themeHighlight else Color(0xFF121212),
                        contentColor = if (currentPage == ReaderPage.Scripture) Color(0xFFF2F6F4) else themeAccent.copy(alpha = 0.72f)
                    )
                ) {
                    Text("Scripture")
                }
            }
        }
    }
}

private fun themeAccentBackground(accent: Color): Color = accent.copy(alpha = 0.12f)

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
data class FontOption(val key: String, val label: String, val family: FontFamily)
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
    val fontSize: Float,
    val themeColorIndex: Int,
    val useAndroidPrimaryTheme: Boolean
)

private class ReaderPreferencesStore(private val context: Context) {
    private val versionPathKey = stringPreferencesKey("version_path")
    private val versionNameKey = stringPreferencesKey("version_name")
    private val bookKey = intPreferencesKey("book")
    private val chapterKey = intPreferencesKey("chapter")
    private val fontFamilyKey = stringPreferencesKey("font_family")
    private val fontSizeKey = floatPreferencesKey("font_size")
    private val themeColorIndexKey = intPreferencesKey("theme_color_index")
    private val useAndroidPrimaryThemeKey = booleanPreferencesKey("use_android_primary_theme")

    suspend fun load(): ReaderSettings {
        val prefs = context.dataStore.data.first()
        return ReaderSettings(
            versionPath = prefs[versionPathKey] ?: "",
            book = prefs[bookKey] ?: 1,
            chapter = prefs[chapterKey] ?: 1,
            fontFamily = prefs[fontFamilyKey] ?: "serif",
            fontSize = prefs[fontSizeKey] ?: 20f,
            themeColorIndex = prefs[themeColorIndexKey] ?: 0,
            useAndroidPrimaryTheme = prefs[useAndroidPrimaryThemeKey] ?: false
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

    suspend fun saveTheme(themeColorIndex: Int, useAndroidPrimaryTheme: Boolean) {
        context.dataStore.edit {
            it[themeColorIndexKey] = themeColorIndex
            it[useAndroidPrimaryThemeKey] = useAndroidPrimaryTheme
        }
    }
}

private class BibleRepository(private val context: Context) {
    private val versionsDir: File = File(context.filesDir, "versions").apply { mkdirs() }
    private val fontsDir: File = File(context.filesDir, "fonts").apply { mkdirs() }

    fun installedFonts(): List<FontOption> {
        val builtIns = listOf(
            FontOption("serif", "Serif", FontFamily.Serif),
            FontOption("sans", "Sans", FontFamily.SansSerif),
            FontOption("mono", "Mono", FontFamily.Monospace)
        )

        val imported = fontsDir.listFiles()
            ?.filter { it.isFile && isFontFile(it) }
            ?.sortedBy { it.name.lowercase() }
            ?.map { file ->
                FontOption(
                    key = customFontKey(file.name),
                    label = fontLabelFromFileName(file.name),
                    family = FontFamily(Typeface.createFromFile(file))
                )
            }
            .orEmpty()

        return builtIns + imported
    }

    suspend fun importFont(uri: Uri): FontOption = withContext(Dispatchers.IO) {
        val displayName = queryDisplayName(uri) ?: "custom-font"
        val targetName = normalizeFontFileName(displayName)
        val target = File(fontsDir, targetName)
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { output -> input.copyTo(output) }
        } ?: error("Unable to open font file")

        FontOption(
            key = customFontKey(target.name),
            label = fontLabelFromFileName(target.name),
            family = FontFamily(Typeface.createFromFile(target))
        )
    }

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

    private fun queryDisplayName(uri: Uri): String? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0 && cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }

    private fun isFontFile(file: File): Boolean {
        val extension = file.extension.lowercase()
        return extension == "ttf" || extension == "otf" || extension == "ttc"
    }

    private fun customFontKey(fileName: String): String = "custom:$fileName"

    private fun normalizeFontFileName(fileName: String): String {
        val sanitized = fileName
            .trim()
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .ifBlank { "custom-font" }
        return if (sanitized.contains('.')) sanitized else "$sanitized.ttf"
    }

    private fun fontLabelFromFileName(fileName: String): String = fileName
        .substringBeforeLast('.')
        .replace('_', ' ')
        .replace('-', ' ')
        .trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .joinToString(" ") { token -> token.replaceFirstChar { c -> c.uppercase() } }
        .ifBlank { "Custom Font" }

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

private fun fontFamilyLabel(key: String, options: List<FontOption> = emptyList()): String = when (key) {
    "sans" -> "Sans"
    "mono" -> "Mono"
    "serif" -> "Serif"
    else -> options.firstOrNull { it.key == key }?.label ?: key.removePrefix("custom:").substringBeforeLast('.').ifBlank { "Custom Font" }
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
