package com.onlyjesus

import android.app.Activity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.FormatListBulleted
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatListNumbered
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Stop
import android.content.Context
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.Bundle
import android.os.Looper
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.net.Uri
import android.provider.OpenableColumns
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.material3.lightColorScheme
import androidx.compose.animation.animateContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextRange
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextDecoration
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
import org.json.JSONArray
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import android.graphics.Typeface
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.OutlinedRichTextEditor

private const val SEARCH_RESULT_PREVIEW_LENGTH = 100
private const val CONTENT_BOTTOM_PADDING = 96
private const val API_BIBLE_BASE_URL = "https://rest.api.bible/v1"
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
        branch = "2024",
        pathPrefix = "json/t_",
        pathSuffix = ".json"
    ),
    BibleSource(
        owner = "Beblia",
        repo = "Bundled-XML",
        branch = "local",
        bundledAssetDir = "bibles/beblia"
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
    Settings,
    Library
}

private enum class LibraryTopSection {
    Search,
    Timeline,
    Plans
}

private enum class ThemeModePreference {
    Light,
    System,
    Dark
}

private enum class ReferencePickerStage {
    Book,
    Chapter
}

private fun isPublicDomainCopyright(copyright: String?): Boolean {
    if (copyright.isNullOrBlank()) return false
    val normalized = copyright.lowercase()
    return normalized.contains("public domain") ||
        normalized.contains("unlicense") ||
        normalized.contains("cc0") ||
        normalized.contains("creativecommons.org/publicdomain")
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

@OptIn(ExperimentalRichTextApi::class, ExperimentalMaterial3Api::class)
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
    val libraryStore = remember { VerseLibraryStore(context) }
    val readingPlanStore = remember { ReadingPlanStore(context) }
    val mainHandler = remember { Handler(Looper.getMainLooper()) }
    val appContext = context.applicationContext

    var currentPage by remember { mutableStateOf(ReaderPage.Scripture) }
    var selectedVersion by remember { mutableStateOf<InstalledVersion?>(null) }
    var currentBook by remember { mutableStateOf(1) }
    var currentChapter by remember { mutableStateOf(1) }
    var currentVerse by remember { mutableStateOf(1) }
    var fontFamilyKey by remember { mutableStateOf("serif") }
    var fontFamilyExpanded by remember { mutableStateOf(false) }
    var fontSizeSp by remember { mutableStateOf(20f) }
    var themeColorIndex by remember { mutableStateOf(0) }
    var themeModePreference by remember { mutableStateOf(ThemeModePreference.System) }
    var useAndroidPrimaryTheme by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("No offline Bible selected.") }
    var isBusy by remember { mutableStateOf(false) }
    var installedExpanded by remember { mutableStateOf(false) }
    var remoteExpanded by remember { mutableStateOf(false) }
    var referencePickerExpanded by remember { mutableStateOf(false) }
    var referencePickerStage by remember { mutableStateOf(ReferencePickerStage.Book) }
    var searchQuery by remember { mutableStateOf("") }
    var licenseExpanded by remember { mutableStateOf(false) }
    var licenseTapStreak by remember { mutableStateOf(0) }
    var ttsInitialized by remember { mutableStateOf(false) }
    var ttsReady by remember { mutableStateOf(false) }
    var ttsStatus by remember { mutableStateOf("Text-to-speech is loading.") }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsSpeakingVerseNumber by remember { mutableStateOf<Int?>(null) }
    var ttsQueuedVerseNumbers by remember { mutableStateOf(emptyList<Int>()) }
    var ttsLanguageCode by remember { mutableStateOf("") }
    var ttsVoiceName by remember { mutableStateOf("") }
    var ttsSpeechRate by remember { mutableFloatStateOf(1f) }
    var ttsPitch by remember { mutableFloatStateOf(1f) }
    var ttsVoiceExpanded by remember { mutableStateOf(false) }
    var ttsLanguageExpanded by remember { mutableStateOf(false) }
    var showNonPublicDomainVersions by remember { mutableStateOf(false) }
    var pendingScrollVerse by remember(currentBook, currentChapter, selectedVersion?.file?.absolutePath) { mutableStateOf<Int?>(null) }
    val verses = remember { mutableStateListOf<Verse>() }
    val verseListState = rememberLazyListState()
    val highlightedVerseNumber by remember(verses, currentPage) {
        derivedStateOf {
            if (ttsSpeakingVerseNumber != null) {
                return@derivedStateOf ttsSpeakingVerseNumber!!
            }
            if (currentPage != ReaderPage.Scripture || verses.isEmpty()) {
                currentVerse
            } else {
                val secondVisibleIndex = (verseListState.firstVisibleItemIndex + 1).coerceAtMost(verses.lastIndex)
                verses.getOrNull(secondVisibleIndex)?.number ?: verses.first().number
            }
        }
    }
    val availableBooks = remember { mutableStateListOf<Int>() }
    val availableChapters = remember { mutableStateListOf<Int>() }
    val availableVerses = remember { mutableStateListOf<Int>() }
    val searchResults = remember { mutableStateListOf<VerseSearchHit>() }
    val installedVersions = remember { mutableStateListOf<InstalledVersion>() }
    val remoteVersions = remember { mutableStateListOf<RemoteVersion>() }
    val fontOptions = remember { mutableStateListOf<FontOption>() }
    val settingsScrollState = rememberScrollState()
    var previousChapterPreview by remember { mutableStateOf<ChapterLoad?>(null) }
    var nextChapterPreview by remember { mutableStateOf<ChapterLoad?>(null) }
    var swipeOffsetPx by remember { mutableFloatStateOf(0f) }
    var swipeSettledOffsetPx by remember { mutableFloatStateOf(0f) }
    var isDraggingChapter by remember { mutableStateOf(false) }
    val verseAnnotations = remember { mutableStateListOf<VerseAnnotation>() }
    val readingHistory = remember { mutableStateListOf<ReadingHistoryEntry>() }
    var selectedLibraryVerse by remember { mutableStateOf<VerseReference?>(null) }
    val noteEditorState = rememberRichTextState()
    var libraryTopSection by remember { mutableStateOf(LibraryTopSection.Search) }
    var librarySection by remember { mutableStateOf(LibrarySection.Bookmarks) }
    val searchScrollState = rememberScrollState()
    val readingPlans = remember { mutableStateListOf<ReadingPlan>() }
    var readingPlansStatus by remember { mutableStateOf("No reading plans yet.") }
    var readingPlansLoading by remember { mutableStateOf(true) }
    var selectedPlanTemplateIndex by remember { mutableStateOf(0) }
    var newPlanTitle by remember { mutableStateOf("") }
    var expandedPlanId by remember { mutableStateOf<String?>(null) }
    val availableTtsVoices = remember { mutableStateListOf<Voice>() }
    val availableTtsLanguages = remember { mutableStateListOf<String>() }
    var ttsPlaybackVerses by remember { mutableStateOf<List<Verse>>(emptyList()) }
    var ttsPlaybackLabel by remember { mutableStateOf("") }
    var ttsPlaybackIndex by remember { mutableStateOf(0) }
    var mediaSession by remember { mutableStateOf<MediaSession?>(null) }
    var speechAudioFocusRequest by remember { mutableStateOf<AudioFocusRequest?>(null) }
    val textToSpeech = remember(appContext) {
        TextToSpeech(appContext) { status ->
            mainHandler.post {
                ttsInitialized = status == TextToSpeech.SUCCESS
                if (status != TextToSpeech.SUCCESS) {
                    ttsReady = false
                    ttsStatus = "Text-to-speech could not start on this device."
                }
            }
        }
    }

    fun updateMediaPlaybackState() {
        val session = mediaSession ?: return
        val actions = PlaybackState.ACTION_PLAY or
            PlaybackState.ACTION_PAUSE or
            PlaybackState.ACTION_STOP or
            PlaybackState.ACTION_SKIP_TO_NEXT or
            PlaybackState.ACTION_SKIP_TO_PREVIOUS
        val state = when {
            isSpeaking -> PlaybackState.STATE_PLAYING
            ttsPlaybackVerses.isNotEmpty() -> PlaybackState.STATE_PAUSED
            else -> PlaybackState.STATE_STOPPED
        }
        session.setPlaybackState(
            PlaybackState.Builder()
                .setActions(actions)
                .setState(state, PlaybackState.PLAYBACK_POSITION_UNKNOWN, 1f)
                .build()
        )
        session.isActive = isSpeaking || ttsPlaybackVerses.isNotEmpty()
    }

    fun isSupportedTtsVoice(voice: Voice): Boolean {
        return voice.locale.language == "en" && voice.locale.country == "US"
    }

    fun ttsLanguageLabel(languageCode: String): String {
        return if (languageCode == "en") {
            "English (United States)"
        } else {
            Locale(languageCode).getDisplayLanguage(Locale.getDefault())
        }
    }

    fun requestSpeechAudioFocus(): Boolean {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return true
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (speechAudioFocusRequest == null) {
                speechAudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build()
                    )
                    .setOnAudioFocusChangeListener { focusChange ->
                        if (focusChange <= AudioManager.AUDIOFOCUS_LOSS) {
                            mainHandler.post {
                                textToSpeech.stop()
                                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
                                if (audioManager != null) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                        val request = speechAudioFocusRequest
                                        if (request != null) {
                                            audioManager.abandonAudioFocusRequest(request)
                                        }
                                    } else {
                                        @Suppress("DEPRECATION")
                                        audioManager.abandonAudioFocus(null)
                                    }
                                }
                                isSpeaking = false
                                ttsSpeakingVerseNumber = null
                                ttsQueuedVerseNumbers = emptyList()
                                ttsPlaybackVerses = emptyList()
                                ttsPlaybackLabel = ""
                                ttsPlaybackIndex = 0
                                ttsStatus = "Playback paused."
                                updateMediaPlaybackState()
                            }
                        }
                    }
                    .build()
            }
            audioManager.requestAudioFocus(speechAudioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    fun abandonSpeechAudioFocus() {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val request = speechAudioFocusRequest ?: return
            audioManager.abandonAudioFocusRequest(request)
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    fun stopReading() {
        textToSpeech.stop()
        isSpeaking = false
        ttsSpeakingVerseNumber = null
        ttsQueuedVerseNumbers = emptyList()
        ttsPlaybackVerses = emptyList()
        ttsPlaybackLabel = ""
        ttsPlaybackIndex = 0
        ttsStatus = "Playback stopped."
        abandonSpeechAudioFocus()
        updateMediaPlaybackState()
    }

    fun speakVerses(referenceLabel: String, versesToRead: List<Verse>) {
        val sanitizedVerses = versesToRead.mapNotNull { verse ->
            val text = verse.text.replace(Regex("\\s+"), " ").trim()
            if (text.isBlank()) null else verse.copy(text = text)
        }
        if (sanitizedVerses.isEmpty()) {
            ttsStatus = "Nothing to read."
            return
        }
        if (!ttsReady) {
            ttsStatus = if (ttsInitialized) {
                "Text-to-speech is not available on this device."
            } else {
                "Text-to-speech is still loading."
            }
            return
        }
        ttsQueuedVerseNumbers = sanitizedVerses.map { it.number }
        if (ttsLanguageCode.isNotBlank()) {
            textToSpeech.language = Locale(ttsLanguageCode)
        }
        textToSpeech.setSpeechRate(ttsSpeechRate.coerceIn(0.5f, 2.0f))
        textToSpeech.setPitch(ttsPitch.coerceIn(0.5f, 2.0f))
        val selectedVoice = availableTtsVoices.firstOrNull {
            it.name == ttsVoiceName && isSupportedTtsVoice(it)
        }
        if (selectedVoice != null) {
            textToSpeech.voice = selectedVoice
        }
        ttsPlaybackVerses = sanitizedVerses
        ttsPlaybackLabel = referenceLabel
        if (ttsPlaybackIndex !in sanitizedVerses.indices) {
            ttsPlaybackIndex = 0
        }
        val firstResult = textToSpeech.speak(sanitizedVerses.first().text, TextToSpeech.QUEUE_FLUSH, null, "${referenceLabel}:${sanitizedVerses.first().number}")
        if (firstResult != TextToSpeech.SUCCESS) {
            ttsQueuedVerseNumbers = emptyList()
            ttsPlaybackVerses = emptyList()
            ttsPlaybackLabel = ""
            ttsPlaybackIndex = 0
            ttsStatus = "Text-to-speech could not start."
            abandonSpeechAudioFocus()
            updateMediaPlaybackState()
            return
        }
        for (verse in sanitizedVerses.drop(1)) {
            val queuedResult = textToSpeech.speak(verse.text, TextToSpeech.QUEUE_ADD, null, "${referenceLabel}:${verse.number}")
            if (queuedResult != TextToSpeech.SUCCESS) {
                ttsQueuedVerseNumbers = emptyList()
                ttsPlaybackVerses = emptyList()
                ttsPlaybackLabel = ""
                ttsPlaybackIndex = 0
                ttsStatus = "Text-to-speech could not queue all verses."
                abandonSpeechAudioFocus()
                updateMediaPlaybackState()
                return
            }
        }
        isSpeaking = true
        ttsSpeakingVerseNumber = sanitizedVerses.first().number
        ttsStatus = "Reading $referenceLabel."
        updateMediaPlaybackState()
    }

    fun pauseReading() {
        textToSpeech.stop()
        abandonSpeechAudioFocus()
        isSpeaking = false
        ttsSpeakingVerseNumber = null
        ttsQueuedVerseNumbers = emptyList()
        ttsStatus = "Playback paused."
        updateMediaPlaybackState()
    }

    fun resumeReading() {
        if (ttsPlaybackVerses.isEmpty()) return
        if (!requestSpeechAudioFocus()) {
            ttsStatus = "Unable to pause other audio for playback."
            return
        }
        speakVerses(ttsPlaybackLabel, ttsPlaybackVerses.drop(ttsPlaybackIndex))
    }

    mediaSession = remember(appContext) {
        MediaSession(appContext, "OnlyJesusTts").apply {
            setPlaybackToLocal(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
            isActive = false
            setCallback(object : MediaSession.Callback() {
                override fun onPlay() {
                    mainHandler.post { resumeReading() }
                }

                override fun onPause() {
                    mainHandler.post { pauseReading() }
                }

                override fun onStop() {
                    mainHandler.post { stopReading() }
                }

                override fun onSkipToNext() {
                    mainHandler.post {
                        if (ttsPlaybackVerses.isEmpty()) return@post
                        ttsPlaybackIndex = (ttsPlaybackIndex + 1).coerceAtMost(ttsPlaybackVerses.lastIndex)
                        pauseReading()
                        resumeReading()
                    }
                }

                override fun onSkipToPrevious() {
                    mainHandler.post {
                        if (ttsPlaybackVerses.isEmpty()) return@post
                        ttsPlaybackIndex = (ttsPlaybackIndex - 1).coerceAtLeast(0)
                        pauseReading()
                        resumeReading()
                    }
                }
            })
        }
    }

    DisposableEffect(textToSpeech) {
        onDispose {
            textToSpeech.stop()
            textToSpeech.shutdown()
            mediaSession?.release()
        }
    }

    LaunchedEffect(ttsInitialized) {
        if (!ttsInitialized) return@LaunchedEffect
        val primaryLanguageResult = textToSpeech.setLanguage(Locale.US)
        val ready = if (
            primaryLanguageResult == TextToSpeech.LANG_MISSING_DATA ||
            primaryLanguageResult == TextToSpeech.LANG_NOT_SUPPORTED
        ) {
            val fallbackLanguageResult = textToSpeech.setLanguage(Locale.getDefault())
            fallbackLanguageResult != TextToSpeech.LANG_MISSING_DATA &&
                fallbackLanguageResult != TextToSpeech.LANG_NOT_SUPPORTED
        } else {
            true
        }
        ttsReady = ready
        ttsStatus = if (ready) {
            "Text-to-speech ready."
        } else {
            "Install a speech engine or language pack to enable TTS."
        }
    }

    LaunchedEffect(ttsReady) {
        if (!ttsReady) return@LaunchedEffect
        availableTtsVoices.clear()
        availableTtsVoices.addAll(
            textToSpeech.voices
                .orEmpty()
                .filter(::isSupportedTtsVoice)
                .sortedWith(compareBy<Voice> { it.name })
        )
        availableTtsLanguages.clear()
        availableTtsLanguages.add("en")
        if (ttsLanguageCode.isNotBlank() && availableTtsLanguages.none { it == ttsLanguageCode }) {
            ttsLanguageCode = ""
        }
        if (ttsLanguageCode.isNotBlank() && availableTtsVoices.none { it.name == ttsVoiceName && isSupportedTtsVoice(it) }) {
            ttsVoiceName = ""
        }
        if (ttsVoiceName.isNotBlank() && availableTtsVoices.none { it.name == ttsVoiceName }) {
            ttsVoiceName = ""
        }
    }

    LaunchedEffect(textToSpeech) {
        textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                val verseNumber = utteranceId?.substringAfterLast(':')?.toIntOrNull()
                mainHandler.post {
                    if (verseNumber != null) {
                        ttsSpeakingVerseNumber = verseNumber
                        ttsPlaybackIndex = ttsPlaybackVerses.indexOfFirst { it.number == verseNumber }.takeIf { it >= 0 } ?: ttsPlaybackIndex
                    }
                    isSpeaking = true
                    updateMediaPlaybackState()
                }
            }

            override fun onDone(utteranceId: String?) {
                val verseNumber = utteranceId?.substringAfterLast(':')?.toIntOrNull()
                mainHandler.post {
                    val lastQueuedVerse = ttsQueuedVerseNumbers.lastOrNull()
                    if (verseNumber != null && verseNumber == lastQueuedVerse) {
                        isSpeaking = false
                        ttsSpeakingVerseNumber = null
                        ttsQueuedVerseNumbers = emptyList()
                        ttsPlaybackVerses = emptyList()
                        ttsPlaybackLabel = ""
                        ttsPlaybackIndex = 0
                        ttsStatus = "Playback finished."
                        updateMediaPlaybackState()
                    }
                }
            }

            override fun onError(utteranceId: String?) {
                mainHandler.post {
                    isSpeaking = false
                    ttsSpeakingVerseNumber = null
                    ttsQueuedVerseNumbers = emptyList()
                    ttsPlaybackVerses = emptyList()
                    ttsPlaybackLabel = ""
                    ttsPlaybackIndex = 0
                    ttsStatus = "Text-to-speech stopped with an error."
                    updateMediaPlaybackState()
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                mainHandler.post {
                    isSpeaking = false
                    ttsSpeakingVerseNumber = null
                    ttsQueuedVerseNumbers = emptyList()
                    ttsPlaybackVerses = emptyList()
                    ttsPlaybackLabel = ""
                    ttsPlaybackIndex = 0
                    ttsStatus = "Text-to-speech stopped with an error."
                    updateMediaPlaybackState()
                }
            }
        })
    }

    fun speakVerse(referenceLabel: String, verseText: String, verseNumber: Int) {
        speakVerses(referenceLabel, listOf(Verse(verseNumber, verseText)))
    }

    fun persistLibraryState(
        topSection: LibraryTopSection = libraryTopSection,
        section: LibrarySection = librarySection,
        reference: VerseReference? = selectedLibraryVerse
    ) {
        scope.launch {
            prefs.saveLibraryState(topSection, section, reference)
        }
    }

    suspend fun loadChapterData(book: Int, chapter: Int, verse: Int = 1): ChapterLoad? = withContext(Dispatchers.IO) {
        val version = selectedVersion ?: return@withContext null
        val books = reader.availableBooks(version.file)
        var normalizedBook = books.firstOrNull()
        for (candidate in books) {
            if (candidate == book) {
                normalizedBook = candidate
                break
            }
        }
        if (normalizedBook == null) return@withContext null
        val chapters = reader.availableChapters(version.file, normalizedBook)
        var normalizedChapter = chapters.firstOrNull()
        for (candidate in chapters) {
            if (candidate == chapter) {
                normalizedChapter = candidate
                break
            }
        }
        if (normalizedChapter == null) return@withContext null
        val chapterText = reader.readChapter(version.file, normalizedBook, normalizedChapter)
        val verseNumbers = buildList {
            for (verseEntry in chapterText) {
                add(verseEntry.number)
            }
        }
        var normalizedVerse = verseNumbers.firstOrNull() ?: 1
        for (candidate in verseNumbers) {
            if (candidate == verse) {
                normalizedVerse = candidate
                break
            }
        }
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

    fun refreshFonts() {
        fontOptions.clear()
        fontOptions.addAll(repository.installedFonts())
    }

    suspend fun orderedChaptersForSelectedVersion(): List<ChapterLocation> = withContext(Dispatchers.IO) {
        val version = selectedVersion ?: return@withContext emptyList()
        val books = reader.availableBooks(version.file)
        buildList {
            for (book in books) {
                val chapters = reader.availableChapters(version.file, book)
                for (chapter in chapters) {
                    add(ChapterLocation(book, chapter))
                }
            }
        }
    }

    fun saveReadingPlans() {
        scope.launch {
            readingPlanStore.savePlans(readingPlans.toList())
        }
    }

    fun reloadReadingPlans() {
        scope.launch {
            readingPlansLoading = true
            val loaded = readingPlanStore.loadPlans()
            readingPlans.clear()
            readingPlans.addAll(loaded)
            readingPlansStatus = if (loaded.isEmpty()) {
                "No reading plans yet."
            } else {
                "Loaded ${loaded.size} reading plan(s)."
            }
            readingPlansLoading = false
        }
    }

    fun createReadingPlan(template: ReadingPlanTemplate) {
        scope.launch {
            val version = selectedVersion
            if (version == null) {
                readingPlansStatus = "Choose a Bible version first."
                return@launch
            }
            val orderedChapters = orderedChaptersForSelectedVersion()
            if (orderedChapters.isEmpty()) {
                readingPlansStatus = "No chapters found in the current version."
                return@launch
            }
            val plan = generateReadingPlan(
                template = template,
                versionPath = version.file.absolutePath,
                versionLabel = version.label,
                orderedChapters = orderedChapters,
                titleOverride = newPlanTitle.trim().ifBlank { null }
            )
            readingPlans.add(0, plan)
            expandedPlanId = plan.id
            readingPlansStatus = "Created ${plan.title}."
            newPlanTitle = ""
            saveReadingPlans()
        }
    }

    fun updateReadingPlan(updatedPlan: ReadingPlan) {
        val index = readingPlans.indexOfFirst { it.id == updatedPlan.id }
        if (index >= 0) {
            readingPlans[index] = updatedPlan
            saveReadingPlans()
        }
    }

    fun togglePlanChapterCompletion(plan: ReadingPlan, dayIndex: Int, chapter: ReadingPlanChapterRef) {
        updateReadingPlan(toggleReadingPlanChapterCompletion(plan, dayIndex, chapter))
    }

    fun deleteReadingPlan(plan: ReadingPlan) {
        val updatedPlans = removeReadingPlan(readingPlans.toList(), plan.id)
        if (updatedPlans.size != readingPlans.size) {
            readingPlans.clear()
            readingPlans.addAll(updatedPlans)
            if (expandedPlanId == plan.id) {
                expandedPlanId = null
            }
            readingPlansStatus = if (readingPlans.isEmpty()) {
                "No reading plans yet."
            } else {
                "Deleted ${plan.title}."
            }
            saveReadingPlans()
        }
    }

    fun openReadingPlanVerse(plan: ReadingPlan, reference: ReadingPlanChapterRef) {
        val matchingVersion = installedVersions.firstOrNull { it.file.absolutePath == plan.versionPath }
        if (matchingVersion != null) {
            selectedVersion = matchingVersion
        }
        currentBook = reference.book
        currentChapter = reference.chapter
        currentVerse = 1
        currentPage = ReaderPage.Scripture
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

    fun selectedFontOption(): FontOption? {
        return fontOptions.firstOrNull { it.key == fontFamilyKey }
    }

    fun selectedFontFamily(): FontFamily {
        return selectedFontOption()?.family ?: FontFamily.Serif
    }

    @Composable
    fun selectedThemeColor(): Color {
        return if (useAndroidPrimaryTheme) {
            androidPrimaryThemeColor(context)
        } else {
            ThemeAccentOptions[themeColorIndex.coerceIn(0, ThemeAccentOptions.lastIndex)]
        }
    }

    fun scriptureReference(): String = "${bookName(currentBook)} $currentChapter:$currentVerse"

    fun currentVersionPath(): String = selectedVersion?.file?.absolutePath.orEmpty()

    fun annotationFor(book: Int = currentBook, chapter: Int = currentChapter, verse: Int = currentVerse): VerseAnnotation? {
        val versionPath = currentVersionPath()
        for (annotation in verseAnnotations) {
            if (annotation.versionPath == versionPath && annotation.book == book && annotation.chapter == chapter && annotation.verse == verse) {
                return annotation
            }
        }
        return null
    }

    fun currentAnnotation(): VerseAnnotation? = annotationFor()

    fun refreshNoteEditor(reference: VerseReference) {
        selectedLibraryVerse = reference
        persistLibraryState()
    }

    fun updateAnnotation(book: Int, chapter: Int, verse: Int, transform: (VerseAnnotation) -> VerseAnnotation) {
        val versionPath = currentVersionPath()
        if (versionPath.isBlank()) return
        var existingIndex = -1
        for (index in verseAnnotations.indices) {
            val annotation = verseAnnotations[index]
            if (annotation.versionPath == versionPath && annotation.book == book && annotation.chapter == chapter && annotation.verse == verse) {
                existingIndex = index
                break
            }
        }
        val existing = if (existingIndex >= 0) verseAnnotations[existingIndex] else VerseAnnotation(versionPath, book, chapter, verse)
        val updated = transform(existing).copy(versionPath = versionPath, book = book, chapter = chapter, verse = verse, updatedAt = System.currentTimeMillis())
        if (existingIndex >= 0) {
            verseAnnotations[existingIndex] = updated
        } else {
            verseAnnotations.add(updated)
        }
        scope.launch {
            libraryStore.saveAnnotations(verseAnnotations.toList())
        }
    }

    fun setVerseBookmark(book: Int, chapter: Int, verse: Int, bookmarked: Boolean) {
        updateAnnotation(book, chapter, verse) { it.copy(bookmarked = bookmarked) }
    }

    fun setVerseHighlight(book: Int, chapter: Int, verse: Int, highlighted: Boolean) {
        updateAnnotation(book, chapter, verse) { it.copy(highlighted = highlighted) }
    }

    fun saveSelectedNote() {
        val reference = selectedLibraryVerse ?: VerseReference(currentBook, currentChapter, currentVerse)
        val text = noteEditorState.toMarkdown()
        updateAnnotation(reference.book, reference.chapter, reference.verse) {
            it.copy(noteMarkdown = text)
        }
    }

    fun shareSelectedMarkdown() {
        val reference = selectedLibraryVerse ?: VerseReference(currentBook, currentChapter, currentVerse)
        val markdown = buildString {
            appendLine("# ${bookName(reference.book)} ${reference.chapter}:${reference.verse}")
            appendLine()
            appendLine(noteEditorState.toMarkdown())
        }.trim()
        shareText(context, "${bookName(reference.book)} ${reference.chapter}:${reference.verse}", markdown)
    }

    fun recordHistory(book: Int, chapter: Int, verse: Int) {
        val versionPath = currentVersionPath()
        val versionLabel = selectedVersion?.label.orEmpty()
        if (versionPath.isBlank() || versionLabel.isBlank()) return
        val entry = ReadingHistoryEntry(versionPath, versionLabel, book, chapter, verse, System.currentTimeMillis())
        var existingIndex = -1
        for (index in readingHistory.indices) {
            if (readingHistory[index].matches(entry)) {
                existingIndex = index
                break
            }
        }
        if (existingIndex >= 0) {
            readingHistory.removeAt(existingIndex)
        }
        readingHistory.add(0, entry)
        while (readingHistory.size > 100) {
            readingHistory.removeAt(readingHistory.lastIndex)
        }
        scope.launch {
            libraryStore.saveHistory(readingHistory.toList())
        }
    }

    fun bookmarkedAnnotations(): List<VerseAnnotation> = verseAnnotations
        .filter {
            it.versionPath == currentVersionPath() && it.bookmarked
        }
        .sortedWith(compareBy<VerseAnnotation> { annotation -> annotation.book }
            .thenBy { annotation -> annotation.chapter }
            .thenBy { annotation -> annotation.verse })

    fun refreshInstalled() {
        installedVersions.clear()
        installedVersions.addAll(repository.installedVersions())
        if (!showNonPublicDomainVersions && selectedVersion?.isPublicDomain == false) {
            val publicDomainFallback = installedVersions.firstOrNull { it.isPublicDomain }
            selectedVersion = publicDomainFallback
            if (publicDomainFallback != null) {
                scope.launch { prefs.saveVersion(publicDomainFallback.file.absolutePath, publicDomainFallback.label) }
            }
        }
    }

    fun fallbackVersion(): InstalledVersion? {
        return installedVersions.firstOrNull { version ->
            val name = version.file.name.lowercase()
            name.contains("kjv") || version.label.lowercase().contains("kjv")
        } ?: installedVersions.firstOrNull()
    }

    suspend fun applyChapterLoad(chapterLoad: ChapterLoad) {
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
        pendingScrollVerse = chapterLoad.verse
        status = if (chapterLoad.chapterText.isEmpty()) {
            "No text found."
        } else {
            ""
        }
        prefs.savePosition(currentBook, currentChapter, currentVerse)
        recordHistory(currentBook, currentChapter, currentVerse)
    }

    suspend fun updateChapterPreviews(version: InstalledVersion) = withContext(Dispatchers.IO) {
        val previousLocation = reader.findAdjacent(version.file, currentBook, currentChapter, -1)
        val previous = if (previousLocation != null) {
            loadChapterData(previousLocation.book, previousLocation.chapter, 1)
        } else {
            null
        }
        val nextLocation = reader.findAdjacent(version.file, currentBook, currentChapter, 1)
        val next = if (nextLocation != null) {
            loadChapterData(nextLocation.book, nextLocation.chapter, 1)
        } else {
            null
        }
        previousChapterPreview = previous
        nextChapterPreview = next
    }

    fun loadChapter() {
        scope.launch {
            isBusy = true
            val chapterLoad = runCatching { loadChapterData(currentBook, currentChapter, currentVerse) }.getOrNull()
            if (chapterLoad == null) {
                val fallback = fallbackVersion()
                if (fallback != null && selectedVersion?.file?.absolutePath != fallback.file.absolutePath) {
                    selectedVersion = fallback
                    prefs.saveVersion(fallback.file.absolutePath, fallback.label)
                    status = "That version could not load. Falling back to KJV."
                    val fallbackLoad = runCatching { loadChapterData(currentBook, currentChapter, currentVerse) }.getOrNull()
                    if (fallbackLoad != null) {
                        applyChapterLoad(fallbackLoad)
                        updateChapterPreviews(fallback)
                    } else {
                        status = "KJV also could not load."
                    }
                } else {
                    status = "That version could not load."
                }
                isBusy = false
                return@launch
            }
            applyChapterLoad(chapterLoad)
            val version = selectedVersion
            if (version != null) {
                updateChapterPreviews(version)
            }
            isBusy = false
        }
    }

    fun navigateChapter(direction: Int) {
        val version = selectedVersion ?: return
        scope.launch {
            val adjacent = withContext(Dispatchers.IO) {
                reader.findAdjacent(version.file, currentBook, currentChapter, direction)
            }
            if (adjacent != null) {
                currentBook = adjacent.book
                currentChapter = adjacent.chapter
                currentVerse = 1
                loadChapter()
            }
        }
    }

    fun loadRandomChapter() {
        val version = selectedVersion ?: return
        scope.launch {
            isBusy = true
            val randomLocation = withContext(Dispatchers.IO) {
                val books = reader.availableBooks(version.file)
                if (books.isEmpty()) return@withContext null
                val book = books.random()
                val chapters = reader.availableChapters(version.file, book)
                if (chapters.isEmpty()) return@withContext null
                val chapter = chapters.random()
                ChapterLocation(book, chapter)
            } ?: run {
                isBusy = false
                return@launch
            }

            currentBook = randomLocation.book
            currentChapter = randomLocation.chapter
            currentVerse = 1
            currentPage = ReaderPage.Scripture
            loadChapter()
        }
    }

    LaunchedEffect(Unit) {
        val saved = prefs.load()
        currentBook = saved.book
        currentChapter = saved.chapter
        currentVerse = saved.verse
        fontFamilyKey = saved.fontFamily
        fontSizeSp = saved.fontSize
        themeColorIndex = saved.themeColorIndex.coerceIn(0, ThemeAccentOptions.lastIndex)
        themeModePreference = saved.themeMode
        useAndroidPrimaryTheme = saved.useAndroidPrimaryTheme
        ttsLanguageCode = saved.ttsLanguageCode
        ttsVoiceName = saved.ttsVoiceName
        ttsSpeechRate = saved.ttsSpeechRate
        ttsPitch = saved.ttsPitch
        libraryTopSection = saved.libraryTopSection
        librarySection = saved.librarySection
        selectedLibraryVerse = if (saved.libraryBook > 0 && saved.libraryChapter > 0 && saved.libraryVerse > 0) {
            VerseReference(saved.libraryBook, saved.libraryChapter, saved.libraryVerse)
        } else {
            null
        }

        verseAnnotations.clear()
        verseAnnotations.addAll(libraryStore.loadAnnotations())
        readingHistory.clear()
        readingHistory.addAll(libraryStore.loadHistory())
        readingPlans.clear()
        readingPlans.addAll(readingPlanStore.loadPlans())
        readingPlansStatus = if (readingPlans.isEmpty()) {
            "No reading plans yet."
        } else {
            "Loaded ${readingPlans.size} reading plan(s)."
        }
        readingPlansLoading = false

        refreshInstalled()
        refreshFonts()

        var hasFontFamily = false
        for (option in fontOptions) {
            if (option.key == fontFamilyKey) {
                hasFontFamily = true
                break
            }
        }
        if (!hasFontFamily) {
            fontFamilyKey = fontOptions.firstOrNull()?.key ?: "serif"
        }

        var restoredVersion: InstalledVersion? = null
        for (version in installedVersions) {
            if (version.file.absolutePath == saved.versionPath) {
                restoredVersion = version
                break
            }
        }
        val preferredVersion = restoredVersion ?: installedVersions.firstOrNull()
        selectedVersion = if (showNonPublicDomainVersions || preferredVersion?.isPublicDomain != false) {
            preferredVersion
        } else {
            installedVersions.firstOrNull { it.isPublicDomain }
        }

        val restoredSelectedVersion = selectedVersion
        if (restoredSelectedVersion != null) {
            prefs.saveVersion(restoredSelectedVersion.file.absolutePath, restoredSelectedVersion.label)
            loadChapter()
        }

        persistLibraryState()
    }

    LaunchedEffect(currentPage, currentBook, currentChapter, currentVerse, selectedVersion) {
        if (currentPage == ReaderPage.Scripture && selectedVersion != null) {
            prefs.savePosition(currentBook, currentChapter, currentVerse)
        }
    }

    LaunchedEffect(currentPage, selectedVersion, currentBook, currentChapter) {
        if (currentPage != ReaderPage.Scripture || selectedVersion == null) {
            previousChapterPreview = null
            nextChapterPreview = null
            return@LaunchedEffect
        }
        val version = selectedVersion ?: return@LaunchedEffect
        val previousPreview = withContext(Dispatchers.IO) {
            val previousChapter = reader.findAdjacent(version.file, currentBook, currentChapter, -1)
            if (previousChapter != null) {
                loadChapterData(previousChapter.book, previousChapter.chapter, 1)
            } else {
                null
            }
        }
        val nextPreview = withContext(Dispatchers.IO) {
            val nextChapter = reader.findAdjacent(version.file, currentBook, currentChapter, 1)
            if (nextChapter != null) {
                loadChapterData(nextChapter.book, nextChapter.chapter, 1)
            } else {
                null
            }
        }
        previousChapterPreview = previousPreview
        nextChapterPreview = nextPreview
    }

    LaunchedEffect(currentPage, pendingScrollVerse, verses) {
        val targetVerse = pendingScrollVerse ?: return@LaunchedEffect
        if (currentPage != ReaderPage.Scripture || verses.isEmpty()) return@LaunchedEffect
        var targetIndex = 0
        for (index in verses.indices) {
            if (verses[index].number == targetVerse) {
                targetIndex = index
                break
            }
        }
        verseListState.animateScrollToItem(targetIndex)
        pendingScrollVerse = null
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

    BackHandler(enabled = currentPage == ReaderPage.Settings) {
        currentPage = ReaderPage.Scripture
    }

    val followsSystem = themeModePreference == ThemeModePreference.System
    val isDarkTheme = when (themeModePreference) {
        ThemeModePreference.Light -> false
        ThemeModePreference.System -> isSystemInDarkTheme()
        ThemeModePreference.Dark -> true
    }
    val backgroundColor = if (isDarkTheme) Color(0xFF0B0E0D) else Color(0xFFF4F0E8)
    val surfaceColor = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFFFCF8)
    val panelColor = if (isDarkTheme) Color(0xFF161A18) else Color(0xFFFFFFFF)
    val borderNeutral = if (isDarkTheme) Color(0xFF3A3F3C) else Color(0xFFD8D0C3)
    val contentPrimary = if (isDarkTheme) Color(0xFFE8E6E3) else Color(0xFF171717)
    val contentSecondary = if (isDarkTheme) contentPrimary.copy(alpha = 0.78f) else Color(0xFF404040).copy(alpha = 0.78f)
    val contentOnAccent = if (isDarkTheme) Color(0xFFF2F6F4) else Color(0xFF132117)
    val contentOnAccentMuted = if (isDarkTheme) contentOnAccent.copy(alpha = 0.72f) else Color(0xFF203125).copy(alpha = 0.82f)
    val navInactiveContainer = if (isDarkTheme) Color(0xFF121212) else Color(0xFFF7F3EC)
    val navInactiveBorder = if (isDarkTheme) Color(0xFF3A3F3C) else Color(0xFFD8D0C3)
    val MenuBackgroundColor = if (isDarkTheme) Color(0xFF111111) else Color(0xFFFFFCF8)
    val MenuTextColor = contentPrimary
    val searchSectionBackground = if (isDarkTheme) Color(0xFF121212) else Color(0xFFFFFFFF)

    val themeAccent = selectedThemeColor()
    val themeHighlight = themeAccent.copy(alpha = 0.14f)
    val themeBorder = themeAccent.copy(alpha = 0.72f)
    val themeMuted = themeAccent.copy(alpha = 0.5f)

    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = themeAccent,
            secondary = themeAccent,
            tertiary = themeAccent,
            background = backgroundColor,
            surface = surfaceColor,
            onBackground = contentPrimary,
            onSurface = contentPrimary,
        )
    } else {
        lightColorScheme(
            primary = themeAccent,
            secondary = themeAccent,
            tertiary = themeAccent,
            background = backgroundColor,
            surface = surfaceColor,
            onBackground = contentPrimary,
            onSurface = contentPrimary,
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            color = backgroundColor,
            contentColor = contentPrimary
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
                                        onClick = {
                                            currentPage = if (currentPage == ReaderPage.Settings) {
                                                ReaderPage.Scripture
                                            } else {
                                                ReaderPage.Settings
                                            }
                                        },
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
                            BoxWithConstraints(
                                modifier = Modifier
                                    .fillMaxSize()
                            ) {
                                val density = androidx.compose.ui.platform.LocalDensity.current
                                val widthPx = with(density) { maxWidth.toPx() }
                                val previewAlpha = if (widthPx <= 0f) 0f else (abs(swipeOffsetPx) / widthPx).coerceIn(0f, 1f)

                                val displayedOffsetPx = if (isDraggingChapter) swipeOffsetPx else androidx.compose.animation.core.animateFloatAsState(
                                    targetValue = swipeSettledOffsetPx,
                                    animationSpec = androidx.compose.animation.core.spring(stiffness = androidx.compose.animation.core.Spring.StiffnessLow),
                                    label = "chapterSwipeSettled"
                                ).value

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .pointerInput(currentPage, selectedVersion, currentBook, currentChapter, isBusy, widthPx) {
                                            detectHorizontalDragGestures(
                                                onHorizontalDrag = { change, dragAmount ->
                                                    change.consume()
                                                    if (widthPx <= 0f) return@detectHorizontalDragGestures
                                                    isDraggingChapter = true
                                                    swipeOffsetPx = (swipeOffsetPx + dragAmount).coerceIn(-widthPx, widthPx)
                                                },
                                                onDragEnd = {
                                                    val threshold = widthPx * 0.22f
                                                    val direction = when {
                                                        swipeOffsetPx <= -threshold -> 1
                                                        swipeOffsetPx >= threshold -> -1
                                                        else -> 0
                                                    }
                                                    if (!isBusy && selectedVersion != null && direction != 0) {
                                                        swipeSettledOffsetPx = widthPx * direction
                                                        scope.launch {
                                                            kotlinx.coroutines.delay(180)
                                                            navigateChapter(direction)
                                                            swipeOffsetPx = 0f
                                                            swipeSettledOffsetPx = 0f
                                                        }
                                                    } else {
                                                        swipeSettledOffsetPx = 0f
                                                        swipeOffsetPx = 0f
                                                    }
                                                    isDraggingChapter = false
                                                },
                                                onDragCancel = {
                                                    swipeSettledOffsetPx = 0f
                                                    swipeOffsetPx = 0f
                                                    isDraggingChapter = false
                                                }
                                            )
                                        }
                                        .graphicsLayer {
                                            clip = false
                                        }
                                ) {
                                    previousChapterPreview?.let { preview ->
                                        ChapterPreviewPane(
                                            chapterLoad = preview,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    translationX = displayedOffsetPx - widthPx
                                                    alpha = if (displayedOffsetPx > 0f) previewAlpha else 0f
                                                },
                                            label = "Previous chapter",
                                            themeAccent = themeAccent,
                                            themeMuted = themeMuted,
                                            fontFamily = selectedFontFamily(),
                                            fontSizeSp = fontSizeSp
                                        )
                                    }

                                    nextChapterPreview?.let { preview ->
                                        ChapterPreviewPane(
                                            chapterLoad = preview,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .graphicsLayer {
                                                    translationX = displayedOffsetPx + widthPx
                                                    alpha = if (displayedOffsetPx < 0f) previewAlpha else 0f
                                                },
                                            label = "Next chapter",
                                            themeAccent = themeAccent,
                                            themeMuted = themeMuted,
                                            fontFamily = selectedFontFamily(),
                                            fontSizeSp = fontSizeSp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .graphicsLayer {
                                                translationX = displayedOffsetPx
                                            }
                                    ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Button(
                                                onClick = {
                                                    speakVerses("${bookName(currentBook)} $currentChapter", verses)
                                                },
                                                enabled = verses.isNotEmpty() && selectedVersion != null && ttsReady,
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = themeHighlight,
                                                    contentColor = themeAccent
                                                ),
                                                border = BorderStroke(1.dp, themeBorder)
                                            ) {
                                                Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                                                Text("Read chapter")
                                            }
                                            TextButton(
                                                onClick = { stopReading() },
                                                enabled = isSpeaking
                                            ) {
                                                Icon(Icons.Outlined.Stop, contentDescription = null, tint = themeAccent)
                                                Text("Stop", color = themeAccent)
                                            }
                                        }

                                        Text(
                                            text = ttsStatus,
                                            color = themeAccent.copy(alpha = 0.72f),
                                            style = MaterialTheme.typography.bodySmall
                                        )

                                        if (referencePickerExpanded) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(1.dp, themeBorder.copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                                                    .background(themeHighlight.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                                                    .padding(6.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                    if (referencePickerStage == ReferencePickerStage.Chapter) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            horizontalArrangement = Arrangement.End,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            TextButton(onClick = {
                                                                referencePickerStage = ReferencePickerStage.Book
                                                            }) {
                                                                Text(
                                                                    text = "Back",
                                                                    color = themeAccent
                                                                )
                                                            }
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
                                                            contentPadding = PaddingValues(2.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                                                            contentPadding = PaddingValues(2.dp),
                                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                            verticalArrangement = Arrangement.spacedBy(4.dp)
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
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        items(verses) { verse ->
                                            var verseMenuExpanded by remember(verse.number, verse.text) { mutableStateOf(false) }
                                            val annotation = annotationFor(verse = verse.number)
                                            val verseText = "${bookName(currentBook)} $currentChapter:${verse.number} ${verse.text}"
                                            val verseDisplay = buildAnnotatedString {
                                                withStyle(SpanStyle(color = if (verse.number == highlightedVerseNumber) themeMuted else contentSecondary.copy(alpha = 0.55f))) {
                                                    append("${verse.number}.")
                                                }
                                                append(" ")
                                                withStyle(SpanStyle(color = if (verse.number == highlightedVerseNumber) themeAccent else contentPrimary)) {
                                                    append(verse.text)
                                                }
                                            }
                                            Box(modifier = Modifier.fillMaxWidth()) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .background(
                                                            color = when {
                                                                verse.number == highlightedVerseNumber && annotation?.highlighted == true -> themeAccent.copy(alpha = 0.24f)
                                                                verse.number == highlightedVerseNumber -> themeHighlight
                                                                annotation?.highlighted == true -> themeAccent.copy(alpha = 0.14f)
                                                                annotation?.bookmarked == true || !annotation?.noteMarkdown.isNullOrBlank() -> themeAccent.copy(alpha = 0.08f)
                                                                else -> Color.Transparent
                                                            },
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .pointerInput(verse.number, verse.text) {
                                                            detectTapGestures(
                                                                onTap = { currentVerse = verse.number },
                                                                onLongPress = { verseMenuExpanded = true }
                                                            )
                                                        }
                                                        .padding(horizontal = 4.dp, vertical = 4.dp)
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
                                                    DropdownMenuItem(
                                                        text = { Text("Read aloud", color = MenuTextColor) },
                                                        onClick = {
                                                            verseMenuExpanded = false
                                                            speakVerse("${bookName(currentBook)} $currentChapter:${verse.number}", verse.text, verse.number)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text(if (annotation?.bookmarked == true) "Remove bookmark" else "Bookmark verse", color = MenuTextColor) },
                                                        onClick = {
                                                            verseMenuExpanded = false
                                                            setVerseBookmark(currentBook, currentChapter, verse.number, annotation?.bookmarked != true)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text(if (annotation?.highlighted == true) "Remove highlight" else "Highlight verse", color = MenuTextColor) },
                                                        onClick = {
                                                            verseMenuExpanded = false
                                                            setVerseHighlight(currentBook, currentChapter, verse.number, annotation?.highlighted != true)
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Edit note", color = MenuTextColor) },
                                                        onClick = {
                                                            verseMenuExpanded = false
                                                            refreshNoteEditor(VerseReference(currentBook, currentChapter, verse.number))
                                                            librarySection = LibrarySection.Notes
                                                            currentPage = ReaderPage.Search
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text("Open in BibleGateway", color = MenuTextColor) },
                                                        onClick = {
                                                            verseMenuExpanded = false
                                                            openBibleGateway(
                                                                context = context,
                                                                reference = "${bookName(currentBook)} $currentChapter:${verse.number}",
                                                                version = selectedVersion
                                                            )
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                                    }
                                }
                            }
                        }

                        ReaderPage.Library -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(searchScrollState)
                                    .padding(bottom = CONTENT_BOTTOM_PADDING.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                                ) {
                                    listOf(
                                        LibraryTopSection.Search to "Search",
                                        LibraryTopSection.Timeline to "Timeline",
                                        LibraryTopSection.Plans to "Plans"
                                    ).forEach { (section, label) ->
                                        val selected = libraryTopSection == section
                                        Button(
                                            onClick = {
                                                libraryTopSection = section
                                                persistLibraryState(topSection = section)
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .fillMaxWidth(),
                                            shape = RoundedCornerShape(0.dp),
                                            border = BorderStroke(
                                                1.dp,
                                                if (selected) themeBorder else navInactiveBorder
                                            ),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (selected) themeHighlight else navInactiveContainer,
                                                contentColor = if (selected) contentOnAccent else themeAccent.copy(alpha = 0.72f)
                                            )
                                        ) {
                                            Text(label)
                                        }
                                    }
                                }

                                when (libraryTopSection) {
                                    LibraryTopSection.Search -> {
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
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = themeHighlight,
                                                contentColor = themeAccent
                                            ),
                                            border = BorderStroke(1.dp, themeBorder)
                                        ) { Text("Find") }

                                        Text(status, color = themeAccent.copy(alpha = 0.78f))

                                        LazyColumn(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(320.dp),
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
                                                        DropdownMenuItem(
                                                            text = { Text("Open in BibleGateway", color = MenuTextColor) },
                                                            onClick = {
                                                                resultMenuExpanded = false
                                                                openBibleGateway(
                                                                    context = context,
                                                                    reference = "${bookName(result.book)} ${result.chapter}:${result.verse}",
                                                                    version = selectedVersion
                                                                )
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                                                .background(themeHighlight.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                                                .padding(12.dp)
                                        ) {
                                            Button(
                                                enabled = !isBusy && selectedVersion != null,
                                                onClick = { loadRandomChapter() },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = themeAccent,
                                                    contentColor = contentOnAccent
                                                ),
                                                border = BorderStroke(1.dp, themeBorder)
                                            ) {
                                                Text("Random")
                                            }
                                        }
                                    }

                                    LibraryTopSection.Timeline -> {
                                        val activeReference = selectedLibraryVerse ?: VerseReference(currentBook, currentChapter, currentVerse)
                                        val activeAnnotation = annotationFor(activeReference.book, activeReference.chapter, activeReference.verse)
                                        val bookmarks = bookmarkedAnnotations()
                                        val highlightedItems = verseAnnotations
                                            .filter { it.versionPath == currentVersionPath() && it.highlighted }
                                        val recentHistory = readingHistory.take(20)

                                        SearchLibraryContent(
                                            context = context,
                                            librarySection = librarySection,
                                            bookmarks = bookmarks,
                                            highlights = highlightedItems,
                                            recentHistory = recentHistory,
                                            activeReference = activeReference,
                                            activeAnnotation = activeAnnotation,
                                            themeAccent = themeAccent,
                                            themeBorder = themeBorder,
                                            themeHighlight = themeHighlight,
                                            borderNeutral = borderNeutral,
                                            panelColor = panelColor,
                                            contentSecondary = contentSecondary,
                                            onOpenVerse = { book, chapter, verse ->
                                                currentBook = book
                                                currentChapter = chapter
                                                currentVerse = verse
                                                currentPage = ReaderPage.Scripture
                                                loadChapter()
                                            },
                                            onEditVerse = { reference ->
                                                refreshNoteEditor(reference)
                                                librarySection = LibrarySection.Notes
                                            },
                                            onSwitchSection = { section ->
                                                librarySection = section
                                                persistLibraryState(section = section)
                                            },
                                            notesContent = {
                                                Column(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        IconButton(onClick = { noteEditorState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }) {
                                                            Icon(Icons.Outlined.FormatBold, contentDescription = "Bold", tint = themeAccent)
                                                        }
                                                        IconButton(onClick = { noteEditorState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }) {
                                                            Icon(Icons.Outlined.FormatItalic, contentDescription = "Italic", tint = themeAccent)
                                                        }
                                                        IconButton(onClick = { noteEditorState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }) {
                                                            Icon(Icons.Outlined.FormatUnderlined, contentDescription = "Underline", tint = themeAccent)
                                                        }
                                                        IconButton(onClick = { noteEditorState.toggleCodeSpan() }) {
                                                            Icon(Icons.Outlined.Code, contentDescription = "Code", tint = themeAccent)
                                                        }
                                                        IconButton(onClick = { noteEditorState.toggleUnorderedList() }) {
                                                            Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "Bulleted list", tint = themeAccent)
                                                        }
                                                        IconButton(onClick = { noteEditorState.toggleOrderedList() }) {
                                                            Icon(Icons.Outlined.FormatListNumbered, contentDescription = "Numbered list", tint = themeAccent)
                                                        }
                                                    }

                                                    OutlinedRichTextEditor(
                                                        state = noteEditorState,
                                                        modifier = Modifier.fillMaxWidth(),
                                                        minLines = 8,
                                                        textStyle = androidx.compose.ui.text.TextStyle(
                                                            fontFamily = selectedFontFamily(),
                                                            fontSize = fontSizeSp.sp,
                                                            color = contentPrimary
                                                        ),
                                                        label = { Text("Rich markdown note", color = contentSecondary) },
                                                        placeholder = { Text("Write a formatted note", color = contentSecondary) }
                                                    )

                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                    ) {
                                                        Button(
                                                            onClick = { saveSelectedNote() },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = themeAccent,
                                                                contentColor = contentOnAccent
                                                            )
                                                        ) { Text("Save note") }
                                                        Button(
                                                            onClick = {
                                                                setVerseBookmark(activeReference.book, activeReference.chapter, activeReference.verse, activeAnnotation?.bookmarked != true)
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = themeHighlight,
                                                                contentColor = contentOnAccent
                                                            )
                                                        ) { Text(if (activeAnnotation?.bookmarked == true) "Unbookmark" else "Bookmark") }
                                                        Button(
                                                            onClick = {
                                                                setVerseHighlight(activeReference.book, activeReference.chapter, activeReference.verse, activeAnnotation?.highlighted != true)
                                                            },
                                                            colors = ButtonDefaults.buttonColors(
                                                                containerColor = themeHighlight,
                                                                contentColor = contentOnAccent
                                                            )
                                                        ) { Text(if (activeAnnotation?.highlighted == true) "Unhighlight" else "Highlight") }
                                                        IconButton(onClick = { shareSelectedMarkdown() }) {
                                                            Icon(Icons.Outlined.Share, contentDescription = "Share note", tint = themeAccent)
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    LibraryTopSection.Plans -> {
                                        Column(
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text("Reading plans", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                                            Text(
                                                text = if (selectedVersion == null) {
                                                    "Select a Bible version in Settings before creating a plan."
                                                } else {
                                                    "Create a schedule from the current Bible version and track chapter completion."
                                                },
                                                color = contentSecondary
                                            )

                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                                                    .background(themeHighlight.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                                                    .padding(12.dp)
                                            ) {
                                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    Text("Create plan", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                                                    OutlinedTextField(
                                                        value = newPlanTitle,
                                                        onValueChange = { newPlanTitle = it },
                                                        label = { Text("Optional title") },
                                                        modifier = Modifier.fillMaxWidth(),
                                                        singleLine = true
                                                    )
                                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                        BuiltInReadingPlanTemplates.forEachIndexed { index, template ->
                                                            TextButton(
                                                                onClick = { selectedPlanTemplateIndex = index },
                                                                modifier = Modifier
                                                                    .border(
                                                                        1.dp,
                                                                        if (selectedPlanTemplateIndex == index) themeBorder else borderNeutral,
                                                                        RoundedCornerShape(999.dp)
                                                                    )
                                                                    .background(
                                                                        if (selectedPlanTemplateIndex == index) themeHighlight else panelColor,
                                                                        RoundedCornerShape(999.dp)
                                                                    )
                                                            ) {
                                                                Text(template.title, color = if (selectedPlanTemplateIndex == index) themeAccent else contentSecondary)
                                                            }
                                                        }
                                                    }
                                                    Text(
                                                        text = BuiltInReadingPlanTemplates[selectedPlanTemplateIndex].description,
                                                        color = contentSecondary
                                                    )
                                                    Button(
                                                        enabled = !readingPlansLoading && selectedVersion != null,
                                                        onClick = {
                                                            createReadingPlan(BuiltInReadingPlanTemplates[selectedPlanTemplateIndex])
                                                        },
                                                        colors = ButtonDefaults.buttonColors(
                                                            containerColor = themeAccent,
                                                            contentColor = contentOnAccent
                                                        ),
                                                        border = BorderStroke(1.dp, themeBorder)
                                                    ) {
                                                        Text("Create from current version")
                                                    }
                                                }
                                            }

                                            Text(readingPlansStatus, color = themeAccent.copy(alpha = 0.78f))

                                            if (readingPlansLoading) {
                                                Text("Loading plans...", color = contentSecondary)
                                            } else if (readingPlans.isEmpty()) {
                                                Text("No plans saved yet.", color = contentSecondary)
                                            } else {
                                                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                    readingPlans.forEach { plan ->
                                                        val expanded = expandedPlanId == plan.id
                                                        Box(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                                                                .background(panelColor, RoundedCornerShape(12.dp))
                                                                .padding(12.dp)
                                                        ) {
                                                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                                                Row(
                                                                    modifier = Modifier.fillMaxWidth(),
                                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                                    verticalAlignment = Alignment.CenterVertically
                                                                ) {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                                        Text(plan.title, style = MaterialTheme.typography.titleMedium, color = themeAccent)
                                                                        Text(plan.description, color = contentSecondary)
                                                                        Text(
                                                                            text = "${plan.progressPercent()}% complete • ${plan.completedChapters()}/${plan.totalChapters()} chapters",
                                                                            color = contentSecondary
                                                                        )
                                                                    }
                                                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                                        TextButton(onClick = {
                                                                            expandedPlanId = if (expanded) null else plan.id
                                                                        }) {
                                                                            Text(if (expanded) "Hide" else "Open", color = themeAccent)
                                                                        }
                                                                        TextButton(onClick = {
                                                                            deleteReadingPlan(plan)
                                                                        }) {
                                                                            Text("Delete", color = contentSecondary)
                                                                        }
                                                                    }
                                                                }

                                                                if (expanded) {
                                                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                        plan.days.forEach { day ->
                                                                            Box(
                                                                                modifier = Modifier
                                                                                    .fillMaxWidth()
                                                                                    .border(1.dp, borderNeutral, RoundedCornerShape(10.dp))
                                                                                    .background(themeHighlight.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                                                                    .padding(10.dp)
                                                                            ) {
                                                                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                                                                    Text(
                                                                                        text = "Day ${day.dayIndex} • ${day.completedCount()}/${day.chapters.size} complete",
                                                                                        color = themeAccent
                                                                                    )
                                                                                    if (day.chapters.isEmpty()) {
                                                                                        Text("No chapters assigned.", color = contentSecondary)
                                                                                    } else {
                                                                                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                                                                            day.chapters.forEach { chapter ->
                                                                                                val done = day.completedChapters.any { it.key() == chapter.key() }
                                                                                                Row(
                                                                                                    modifier = Modifier.fillMaxWidth(),
                                                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                                                ) {
                                                                                                    Button(
                                                                                                        onClick = {
                                                                                                            openReadingPlanVerse(plan, chapter)
                                                                                                            loadChapter()
                                                                                                        },
                                                                                                        modifier = Modifier.weight(1f)
                                                                                                    ) {
                                                                                                        Text("${bookName(chapter.book)} ${chapter.chapter}")
                                                                                                    }
                                                                                                    TextButton(onClick = {
                                                                                                        togglePlanChapterCompletion(plan, day.dayIndex, chapter)
                                                                                                    }) {
                                                                                                        Text(if (done) "Done" else "Mark")
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                          }

                        ReaderPage.Search -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(searchScrollState)
                                    .padding(bottom = CONTENT_BOTTOM_PADDING.dp),
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
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(320.dp),
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
                                                DropdownMenuItem(
                                                    text = { Text("Open in BibleGateway", color = MenuTextColor) },
                                                    onClick = {
                                                        resultMenuExpanded = false
                                                        openBibleGateway(
                                                            context = context,
                                                            reference = "${bookName(result.book)} ${result.chapter}:${result.verse}",
                                                            version = selectedVersion
                                                        )
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                val activeReference = selectedLibraryVerse ?: VerseReference(currentBook, currentChapter, currentVerse)
                                val activeAnnotation = annotationFor(activeReference.book, activeReference.chapter, activeReference.verse)
                                val bookmarks = bookmarkedAnnotations()
                                val recentHistory = readingHistory.take(20)

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                                        .background(themeHighlight.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Button(
                                        enabled = !isBusy && selectedVersion != null,
                                        onClick = { loadRandomChapter() },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeAccent,
                                            contentColor = contentOnAccent
                                        ),
                                        border = BorderStroke(1.dp, themeBorder)
                                    ) {
                                        Text("Random")
                                    }
                                }

                                SearchLibraryContent(
                                    context = context,
                                    librarySection = librarySection,
                                    bookmarks = bookmarks,
                                    highlights = verseAnnotations.filter { it.versionPath == currentVersionPath() && it.highlighted },
                                    recentHistory = recentHistory,
                                    activeReference = activeReference,
                                    activeAnnotation = activeAnnotation,
                                    themeAccent = themeAccent,
                                    themeBorder = themeBorder,
                                    themeHighlight = themeHighlight,
                                    borderNeutral = borderNeutral,
                                    panelColor = panelColor,
                                    contentSecondary = contentSecondary,
                                    onOpenVerse = { book, chapter, verse ->
                                        currentBook = book
                                        currentChapter = chapter
                                        currentVerse = verse
                                        currentPage = ReaderPage.Scripture
                                        loadChapter()
                                    },
                                    onEditVerse = { reference ->
                                        refreshNoteEditor(reference)
                                        librarySection = LibrarySection.Notes
                                    },
                                    onSwitchSection = { section ->
                                        librarySection = section
                                        persistLibraryState(section = section)
                                    },
                                    notesContent = {
                                        Column(
                                            modifier = Modifier.fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                IconButton(onClick = { noteEditorState.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) }) {
                                                    Icon(Icons.Outlined.FormatBold, contentDescription = "Bold", tint = themeAccent)
                                                }
                                                IconButton(onClick = { noteEditorState.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) }) {
                                                    Icon(Icons.Outlined.FormatItalic, contentDescription = "Italic", tint = themeAccent)
                                                }
                                                IconButton(onClick = { noteEditorState.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) }) {
                                                    Icon(Icons.Outlined.FormatUnderlined, contentDescription = "Underline", tint = themeAccent)
                                                }
                                                IconButton(onClick = { noteEditorState.toggleCodeSpan() }) {
                                                    Icon(Icons.Outlined.Code, contentDescription = "Code", tint = themeAccent)
                                                }
                                                IconButton(onClick = { noteEditorState.toggleUnorderedList() }) {
                                                    Icon(Icons.AutoMirrored.Outlined.FormatListBulleted, contentDescription = "Bulleted list", tint = themeAccent)
                                                }
                                                IconButton(onClick = { noteEditorState.toggleOrderedList() }) {
                                                    Icon(Icons.Outlined.FormatListNumbered, contentDescription = "Numbered list", tint = themeAccent)
                                                }
                                            }

                                            OutlinedRichTextEditor(
                                                state = noteEditorState,
                                                modifier = Modifier.fillMaxWidth(),
                                                minLines = 8,
                                                textStyle = androidx.compose.ui.text.TextStyle(
                                                    fontFamily = selectedFontFamily(),
                                                    fontSize = fontSizeSp.sp,
                                                    color = contentPrimary
                                                ),
                                                label = { Text("Rich markdown note", color = contentSecondary) },
                                                placeholder = { Text("Write a formatted note", color = contentSecondary) }
                                            )

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                Button(
                                                    onClick = { saveSelectedNote() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = themeAccent,
                                                        contentColor = contentOnAccent
                                                    )
                                                ) { Text("Save note") }
                                                Button(
                                                    onClick = {
                                                        setVerseBookmark(activeReference.book, activeReference.chapter, activeReference.verse, activeAnnotation?.bookmarked != true)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = themeHighlight,
                                                        contentColor = contentOnAccent
                                                    )
                                                ) { Text(if (activeAnnotation?.bookmarked == true) "Unbookmark" else "Bookmark") }
                                                Button(
                                                    onClick = {
                                                        setVerseHighlight(activeReference.book, activeReference.chapter, activeReference.verse, activeAnnotation?.highlighted != true)
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = themeHighlight,
                                                        contentColor = contentOnAccent
                                                    )
                                                ) { Text(if (activeAnnotation?.highlighted == true) "Unhighlight" else "Highlight") }
                                                IconButton(onClick = { shareSelectedMarkdown() }) {
                                                    Icon(Icons.Outlined.Share, contentDescription = "Share note", tint = themeAccent)
                                                }
                                            }
                                        }
                                    }
                                )
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
                                        val visibleInstalled = if (showNonPublicDomainVersions) {
                                            installedVersions
                                        } else {
                                            installedVersions.filter { it.isPublicDomain }
                                        }
                                        if (visibleInstalled.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No public-domain versions installed", color = MenuTextColor.copy(alpha = 0.7f)) },
                                                onClick = { installedExpanded = false }
                                            )
                                        } else {
                                            visibleInstalled.forEach { version ->
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
                                }

                                selectedVersion?.copyright?.let { copyrightText ->
                                    Text(
                                        text = copyrightText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = contentSecondary,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
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
                                                val visibleRemoteCount = if (showNonPublicDomainVersions) {
                                                    fetched.size
                                                } else {
                                                    fetched.count { it.isPublicDomain }
                                                }
                                                status = if (visibleRemoteCount == 0) {
                                                    "No public-domain versions found from available sources."
                                                } else {
                                                    "Choose a version to download."
                                                }
                                                isBusy = false
                                                remoteExpanded = visibleRemoteCount > 0
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = themeHighlight,
                                            contentColor = themeAccent
                                        ),
                                        border = BorderStroke(1.dp, themeBorder)
                                    ) {
                                        Text("Refresh sources")
                                    }

                                    DropdownMenu(
                                        expanded = remoteExpanded,
                                        onDismissRequest = { remoteExpanded = false },
                                        containerColor = MenuBackgroundColor
                                    ) {
                                        val visibleRemote = if (showNonPublicDomainVersions) {
                                            remoteVersions
                                        } else {
                                            remoteVersions.filter { it.isPublicDomain }
                                        }
                                        if (visibleRemote.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No public-domain versions available", color = MenuTextColor.copy(alpha = 0.7f)) },
                                                onClick = { remoteExpanded = false }
                                            )
                                        } else {
                                            visibleRemote.forEach { remote ->
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
                                }

                                Text("Appearance", style = MaterialTheme.typography.titleMedium, color = themeAccent)

                                Text("Theme mode", color = contentSecondary)
                                Slider(
                                    value = themeModePreference.ordinal.toFloat(),
                                    onValueChange = { value ->
                                        themeModePreference = ThemeModePreference.entries[value.roundToInt().coerceIn(0, ThemeModePreference.entries.lastIndex)]
                                        scope.launch { prefs.saveThemeMode(themeModePreference) }
                                    },
                                    valueRange = 0f..2f,
                                    steps = 1,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeAccent,
                                        activeTrackColor = themeAccent,
                                        activeTickColor = themeAccent,
                                        inactiveTrackColor = themeAccent.copy(alpha = 0.28f),
                                        inactiveTickColor = themeAccent.copy(alpha = 0.28f)
                                    )
                                )
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    listOf("Light", "System", "Dark").forEachIndexed { index, label ->
                                        Text(
                                            text = label,
                                            color = if (themeModePreference.ordinal == index) themeAccent else contentSecondary
                                        )
                                    }
                                }

                                Text(
                                    text = if (followsSystem) "Following your device appearance. Drag the slider to force light or dark." else "Follow system to match the device theme automatically.",
                                    color = contentSecondary
                                )

                                Text("Accent", style = MaterialTheme.typography.bodyMedium, color = themeAccent)

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Use Android primary accent", color = themeAccent)
                                    Switch(
                                        checked = useAndroidPrimaryTheme,
                                        onCheckedChange = {
                                            useAndroidPrimaryTheme = it
                                            scope.launch { prefs.saveTheme(themeColorIndex, useAndroidPrimaryTheme) }
                                        }
                                    )
                                }

                                Text(
                                    text = if (useAndroidPrimaryTheme) "Android primary is active. Pick a swatch to keep a fallback when it is off." else "Choose an accent color.",
                                    color = contentSecondary
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
                                                            if (selected) themeBorder else borderNeutral,
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
                                                        color = contentOnAccent.copy(alpha = if (selected) 0.95f else 0f)
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
                                                Text("${selectedFontOption()?.label ?: fontFamilyLabel(fontFamilyKey, fontOptions)} ▼", color = themeAccent)
                                            }
                                            DropdownMenu(
                                                expanded = fontFamilyExpanded,
                                                onDismissRequest = { fontFamilyExpanded = false },
                                                containerColor = MenuBackgroundColor
                                            ) {
                                                val builtInFonts = fontOptions.filterNot { it.key.startsWith("custom:") }
                                                val customFonts = fontOptions.filter { it.key.startsWith("custom:") }
                                                Text(
                                                    text = "Built-in fonts",
                                                    color = MenuTextColor.copy(alpha = 0.65f),
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                                builtInFonts.forEach { option ->
                                                    val selected = option.key == fontFamilyKey
                                                    DropdownMenuItem(
                                                        text = {
                                                            Column {
                                                                Text(option.label, color = MenuTextColor)
                                                                Text(
                                                                    "The quick brown fox jumps over the lazy dog.",
                                                                    color = MenuTextColor.copy(alpha = 0.7f),
                                                                    fontFamily = option.family,
                                                                    fontSize = 12.sp,
                                                                    maxLines = 1
                                                                )
                                                            }
                                                        },
                                                        trailingIcon = {
                                                            Text(if (selected) "✓" else "", color = themeAccent)
                                                        },
                                                        onClick = {
                                                            fontFamilyExpanded = false
                                                            fontFamilyKey = option.key
                                                            scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
                                                        }
                                                    )
                                                }
                                                if (customFonts.isNotEmpty()) {
                                                    Text(
                                                        text = "Imported fonts",
                                                        color = MenuTextColor.copy(alpha = 0.65f),
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                    )
                                                    customFonts.forEach { option ->
                                                        val selected = option.key == fontFamilyKey
                                                        DropdownMenuItem(
                                                            text = {
                                                                Column {
                                                                    Text(option.label, color = MenuTextColor)
                                                                    Text(
                                                                        "The quick brown fox jumps over the lazy dog.",
                                                                        color = MenuTextColor.copy(alpha = 0.7f),
                                                                        fontFamily = option.family,
                                                                        fontSize = 12.sp,
                                                                        maxLines = 1
                                                                    )
                                                                }
                                                            },
                                                            trailingIcon = {
                                                                Text(if (selected) "✓" else "", color = themeAccent)
                                                            },
                                                            onClick = {
                                                                fontFamilyExpanded = false
                                                                fontFamilyKey = option.key
                                                                scope.launch { prefs.saveFont(fontFamilyKey, fontSizeSp) }
                                                            }
                                                        )
                                                    }
                                                }
                                                HorizontalDivider(color = themeAccent.copy(alpha = 0.18f))
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

                                Text(
                                    text = "Selected font: ${selectedFontOption()?.label ?: fontFamilyLabel(fontFamilyKey, fontOptions)}",
                                    color = themeAccent.copy(alpha = 0.72f),
                                    fontSize = 13.sp
                                )

                                Text("Size: ${fontSizeSp.toInt()}px", color = themeAccent.copy(alpha = 0.78f))
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

                                Text("Voice options", style = MaterialTheme.typography.titleMedium, color = themeAccent)

                                Text("Language", color = themeAccent.copy(alpha = 0.78f))
                                Box {
                                    TextButton(
                                        onClick = { ttsLanguageExpanded = true },
                                        modifier = Modifier.border(1.dp, themeBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        val selectedLanguageLabel = if (ttsLanguageCode.isBlank()) {
                                            "System default"
                                        } else {
                                                ttsLanguageLabel(ttsLanguageCode)
                                        }
                                        Text("$selectedLanguageLabel ▼", color = themeAccent)
                                    }
                                    DropdownMenu(
                                        expanded = ttsLanguageExpanded,
                                        onDismissRequest = { ttsLanguageExpanded = false },
                                        containerColor = MenuBackgroundColor
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("System default", color = MenuTextColor) },
                                            trailingIcon = { Text(if (ttsLanguageCode.isBlank()) "✓" else "", color = themeAccent) },
                                            onClick = {
                                                ttsLanguageExpanded = false
                                                ttsLanguageCode = ""
                                                ttsVoiceName = ""
                                                scope.launch { prefs.saveTtsSettings(ttsLanguageCode, ttsVoiceName, ttsSpeechRate, ttsPitch) }
                                            }
                                        )
                                        if (availableTtsLanguages.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No English (United States) voices available", color = MenuTextColor.copy(alpha = 0.7f)) },
                                                onClick = { ttsLanguageExpanded = false }
                                            )
                                        } else {
                                            availableTtsLanguages.forEach { languageCode ->
                                                val selected = languageCode == ttsLanguageCode
                                                DropdownMenuItem(
                                                    text = { Text(ttsLanguageLabel(languageCode), color = MenuTextColor) },
                                                    trailingIcon = { Text(if (selected) "✓" else "", color = themeAccent) },
                                                    onClick = {
                                                        ttsLanguageExpanded = false
                                                        ttsLanguageCode = languageCode
                                                        ttsVoiceName = ""
                                                        scope.launch { prefs.saveTtsSettings(ttsLanguageCode, ttsVoiceName, ttsSpeechRate, ttsPitch) }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Text("Voice", color = themeAccent.copy(alpha = 0.78f))
                                Box {
                                    TextButton(
                                        onClick = { ttsVoiceExpanded = true },
                                        modifier = Modifier.border(1.dp, themeBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        val selectedVoiceLabel = availableTtsVoices.firstOrNull { it.name == ttsVoiceName }
                                            ?.let { voice -> "${voice.name} (English (United States))" }
                                            ?: "System default"
                                        Text("$selectedVoiceLabel ▼", color = themeAccent)
                                    }
                                    DropdownMenu(
                                        expanded = ttsVoiceExpanded,
                                        onDismissRequest = { ttsVoiceExpanded = false },
                                        containerColor = MenuBackgroundColor
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("System default", color = MenuTextColor) },
                                            trailingIcon = { Text(if (ttsVoiceName.isBlank()) "✓" else "", color = themeAccent) },
                                            onClick = {
                                                ttsVoiceExpanded = false
                                                ttsVoiceName = ""
                                                scope.launch { prefs.saveTtsSettings(ttsLanguageCode, ttsVoiceName, ttsSpeechRate, ttsPitch) }
                                            }
                                        )
                                        val filteredVoices = availableTtsVoices.filter {
                                            isSupportedTtsVoice(it)
                                        }
                                        if (filteredVoices.isEmpty()) {
                                            DropdownMenuItem(
                                                text = { Text("No English (United States) voices available", color = MenuTextColor.copy(alpha = 0.7f)) },
                                                onClick = { ttsVoiceExpanded = false }
                                            )
                                        } else {
                                            filteredVoices.forEach { voice ->
                                                val selected = voice.name == ttsVoiceName
                                                DropdownMenuItem(
                                                    text = {
                                                        Column {
                                                            Text(voice.name, color = MenuTextColor)
                                                            Text(
                                                                "English (United States) · ${voice.features.size} features",
                                                                color = MenuTextColor.copy(alpha = 0.7f),
                                                                fontSize = 12.sp,
                                                                maxLines = 1
                                                            )
                                                        }
                                                    },
                                                    trailingIcon = { Text(if (selected) "✓" else "", color = themeAccent) },
                                                    onClick = {
                                                        ttsVoiceExpanded = false
                                                        ttsVoiceName = voice.name
                                                        scope.launch { prefs.saveTtsSettings(ttsLanguageCode, ttsVoiceName, ttsSpeechRate, ttsPitch) }
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }

                                Text("Speech rate", color = themeAccent.copy(alpha = 0.78f))
                                Slider(
                                    value = ttsSpeechRate,
                                    onValueChange = {
                                        ttsSpeechRate = it
                                        scope.launch { prefs.saveTtsSettings(ttsLanguageCode, ttsVoiceName, ttsSpeechRate, ttsPitch) }
                                    },
                                    valueRange = 0.5f..2f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeAccent,
                                        activeTrackColor = themeAccent,
                                        activeTickColor = themeAccent,
                                        inactiveTrackColor = themeAccent.copy(alpha = 0.28f),
                                        inactiveTickColor = themeAccent.copy(alpha = 0.28f)
                                    )
                                )
                                Text("${ttsSpeechRate.formatTtsValue()}x", color = themeAccent.copy(alpha = 0.72f))

                                Text("Pitch", color = themeAccent.copy(alpha = 0.78f))
                                Slider(
                                    value = ttsPitch,
                                    onValueChange = {
                                        ttsPitch = it
                                        scope.launch { prefs.saveTtsSettings(ttsLanguageCode, ttsVoiceName, ttsSpeechRate, ttsPitch) }
                                    },
                                    valueRange = 0.5f..2f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = themeAccent,
                                        activeTrackColor = themeAccent,
                                        activeTickColor = themeAccent,
                                        inactiveTrackColor = themeAccent.copy(alpha = 0.28f),
                                        inactiveTickColor = themeAccent.copy(alpha = 0.28f)
                                    )
                                )
                                Text(ttsPitch.formatTtsValue(), color = themeAccent.copy(alpha = 0.72f))

                                Text("Test current verse", style = MaterialTheme.typography.titleSmall, color = themeAccent)
                                Button(
                                    onClick = {
                                        val previewVerse = verses.firstOrNull { it.number == currentVerse }
                                        if (previewVerse != null) {
                                            speakVerse("${bookName(currentBook)} $currentChapter:$currentVerse", previewVerse.text, previewVerse.number)
                                        } else {
                                            ttsStatus = "No current verse is loaded to test."
                                        }
                                    },
                                    enabled = ttsReady && verses.any { it.number == currentVerse },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = themeHighlight,
                                        contentColor = themeAccent
                                    ),
                                    border = BorderStroke(1.dp, themeBorder)
                                ) {
                                    Icon(Icons.Outlined.PlayArrow, contentDescription = null)
                                    Text("Test current verse")
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .animateContentSize()
                                        .border(1.dp, themeBorder, RoundedCornerShape(12.dp))
                                        .background(themeHighlight.copy(alpha = 0.18f), RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("License", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                                            TextButton(onClick = {
                                                licenseExpanded = !licenseExpanded
                                                if (!showNonPublicDomainVersions) {
                                                    licenseTapStreak += 1
                                                    if (licenseTapStreak >= 10) {
                                                        showNonPublicDomainVersions = true
                                                        status = "Non-public-domain versions unlocked for this session."
                                                    }
                                                }
                                            }) {
                                                Text(if (licenseExpanded) "Hide" else "Show", color = themeAccent)
                                            }
                                        }

                                        if (licenseExpanded) {
                                            Text(
                                                text = "Released into the public domain under the Unlicense. There is no warranty.",
                                                color = themeAccent.copy(alpha = 0.78f)
                                            )
                                            Button(
                                                onClick = {
                                                    context.startActivity(
                                                        Intent(
                                                            Intent.ACTION_VIEW,
                                                            Uri.parse("https://github.com/lucasburlingham/OnlyJesus/")
                                                        )
                                                    )
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = themeHighlight,
                                                    contentColor = themeAccent
                                                ),
                                                border = BorderStroke(1.dp, themeBorder)
                                            ) {
                                                Text("Open GitHub repo")
                                            }
                                        }
                                    }
                                }

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
                    onClick = {
                        currentPage = ReaderPage.Search
                    },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        1.dp,
                        if (currentPage == ReaderPage.Search) themeBorder else navInactiveBorder
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == ReaderPage.Search) themeHighlight else navInactiveContainer,
                        contentColor = if (currentPage == ReaderPage.Search) contentOnAccent else themeAccent.copy(alpha = 0.72f)
                    )
                ) {
                    Text("Search")
                }
                Button(
                    onClick = { currentPage = ReaderPage.Library },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        1.dp,
                        if (currentPage == ReaderPage.Library) themeBorder else navInactiveBorder
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == ReaderPage.Library) themeHighlight else navInactiveContainer,
                        contentColor = if (currentPage == ReaderPage.Library) contentOnAccent else themeAccent.copy(alpha = 0.72f)
                    )
                ) {
                    Text("Library")
                }
                Button(
                    onClick = { currentPage = ReaderPage.Scripture },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    border = BorderStroke(
                        1.dp,
                        if (currentPage == ReaderPage.Scripture) themeBorder else navInactiveBorder
                    ),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentPage == ReaderPage.Scripture) themeHighlight else navInactiveContainer,
                        contentColor = if (currentPage == ReaderPage.Scripture) contentOnAccent else themeAccent.copy(alpha = 0.72f)
                    )
                ) {
                    Text("Scripture")
                }
            }
        }
    }
}

}

private fun themeAccentBackground(accent: Color): Color = accent.copy(alpha = 0.12f)

@Composable
private fun SearchLibraryContent(
    context: Context,
    librarySection: LibrarySection,
    bookmarks: List<VerseAnnotation>,
    highlights: List<VerseAnnotation>,
    recentHistory: List<ReadingHistoryEntry>,
    activeReference: VerseReference,
    activeAnnotation: VerseAnnotation?,
    themeAccent: Color,
    themeBorder: Color,
    themeHighlight: Color,
    borderNeutral: Color,
    panelColor: Color,
    contentSecondary: Color,
    onOpenVerse: (Int, Int, Int) -> Unit,
    onEditVerse: (VerseReference) -> Unit,
    onSwitchSection: (LibrarySection) -> Unit,
    notesContent: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                LibrarySection.Bookmarks to "Bookmarks",
                LibrarySection.History to "History",
                LibrarySection.Notes to "Notes",
                LibrarySection.Highlights to "Highlights"
            ).forEach { (section, label) ->
                val selected = librarySection == section
                TextButton(
                    onClick = { onSwitchSection(section) },
                    modifier = Modifier
                        .border(1.dp, if (selected) themeBorder else borderNeutral, RoundedCornerShape(999.dp))
                        .background(if (selected) themeHighlight else panelColor, RoundedCornerShape(999.dp))
                ) {
                    Text(label, color = if (selected) themeAccent else contentSecondary)
                }
            }
        }

        when (librarySection) {
            LibrarySection.Bookmarks -> {
                Text("Bookmarks", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                if (bookmarks.isEmpty()) {
                    Text("No bookmarks or notes yet.", color = contentSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        bookmarks.forEach { item ->
                            val preview = item.noteMarkdown.take(120).replace('\n', ' ')
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, themeBorder, RoundedCornerShape(10.dp))
                                    .background(themeHighlight.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${bookName(item.book)} ${item.chapter}:${item.verse}", color = themeAccent)
                                    if (preview.isNotBlank()) {
                                        Text(preview, color = contentSecondary)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onOpenVerse(item.book, item.chapter, item.verse) }) { Text("Open") }
                                        Button(onClick = { onEditVerse(VerseReference(item.book, item.chapter, item.verse)) }) { Text("Edit") }
                                        Button(onClick = {
                                            shareText(context, "${bookName(item.book)} ${item.chapter}:${item.verse}", item.noteMarkdown.ifBlank { "Bookmark from OnlyJesus" })
                                        }) { Text("Share") }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LibrarySection.History -> {
                Text("Reading history", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                if (recentHistory.isEmpty()) {
                    Text("Your recent chapters will show up here.", color = contentSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recentHistory.forEach { item ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, borderNeutral, RoundedCornerShape(10.dp))
                                    .background(panelColor, RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                TextButton(onClick = { onOpenVerse(item.book, item.chapter, item.verse) }) {
                                    Text("${bookName(item.book)} ${item.chapter}:${item.verse}")
                                }
                            }
                        }
                    }
                }
            }

            LibrarySection.Notes -> {
                Text("Notes", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                Text("${bookName(activeReference.book)} ${activeReference.chapter}:${activeReference.verse}", color = contentSecondary)
                notesContent()
            }

            LibrarySection.Highlights -> {
                Text("Highlights", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                if (highlights.isEmpty()) {
                    Text("No highlighted verses yet.", color = contentSecondary)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        highlights.forEach { item ->
                            val preview = item.noteMarkdown.take(120).replace('\n', ' ')
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, themeBorder, RoundedCornerShape(10.dp))
                                    .background(themeHighlight.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
                                    .padding(10.dp)
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text("${bookName(item.book)} ${item.chapter}:${item.verse}", color = themeAccent)
                                    if (preview.isNotBlank()) {
                                        Text(preview, color = contentSecondary)
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(onClick = { onOpenVerse(item.book, item.chapter, item.verse) }) { Text("Open") }
                                        Button(onClick = { onEditVerse(VerseReference(item.book, item.chapter, item.verse)) }) { Text("Edit") }
                                        Button(onClick = {
                                            shareText(context, "${bookName(item.book)} ${item.chapter}:${item.verse}", item.noteMarkdown.ifBlank { "Highlight from OnlyJesus" })
                                        }) { Text("Share") }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LibrarySection.Plans -> {
                Text("Reading plans", style = MaterialTheme.typography.titleMedium, color = themeAccent)
                Text("Reading plan management is available in the plans screen.", color = contentSecondary)
            }
        }
    }
}

@Composable
private fun ChapterPreviewPane(
    chapterLoad: ChapterLoad,
    modifier: Modifier = Modifier,
    label: String,
    themeAccent: Color,
    themeMuted: Color,
    fontFamily: FontFamily,
    fontSizeSp: Float
) {
    Box(
        modifier = modifier.background(Color.Black.copy(alpha = 0.36f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(label, color = themeAccent.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
            Text(
                text = "${bookName(chapterLoad.book)} ${chapterLoad.chapter}",
                color = themeAccent,
                style = MaterialTheme.typography.titleMedium
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                chapterLoad.chapterText.take(8).forEach { verse ->
                    val verseDisplay = buildAnnotatedString {
                        withStyle(SpanStyle(color = themeMuted.copy(alpha = 0.55f))) {
                            append("${verse.number}.")
                        }
                        append(" ")
                        withStyle(SpanStyle(color = themeAccent.copy(alpha = 0.72f))) {
                            append(verse.text)
                        }
                    }
                    Text(
                        text = verseDisplay,
                        fontSize = (fontSizeSp * 0.92f).sp,
                        fontFamily = fontFamily
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
data class InstalledVersion(
    val label: String,
    val file: File,
    val copyright: String? = null,
    val isPublicDomain: Boolean = false
)
data class FontOption(val key: String, val label: String, val family: FontFamily)
data class BibleSource(
    val owner: String,
    val repo: String,
    val branch: String,
    val pathPrefix: String? = null,
    val pathSuffix: String? = null,
    val bundledAssetDir: String? = null
) {
    val key: String = "${owner}_${repo}_$branch"
    val label: String = "$owner/$repo"
}

data class RemoteVersion(
    val source: BibleSource,
    val branch: String,
    val path: String,
    val sizeBytes: Long,
    val displayNameOverride: String? = null,
    val copyright: String? = null,
    val isPublicDomain: Boolean = false
) {
    val displayName: String = displayNameOverride ?: "${source.label}: ${displayNameFromPath(path)}"
    val sizeMb: String = String.format("%.1f", sizeBytes / 1024.0 / 1024.0)
    val downloadUrl: String = "https://raw.githubusercontent.com/${source.owner}/${source.repo}/$branch/$path"
}

private data class ReaderSettings(
    val versionPath: String,
    val book: Int,
    val chapter: Int,
    val verse: Int,
    val fontFamily: String,
    val fontSize: Float,
    val themeColorIndex: Int,
    val themeMode: ThemeModePreference,
    val useAndroidPrimaryTheme: Boolean,
    val ttsLanguageCode: String,
    val ttsVoiceName: String,
    val ttsSpeechRate: Float,
    val ttsPitch: Float,
    val libraryTopSection: LibraryTopSection,
    val librarySection: LibrarySection,
    val libraryBook: Int,
    val libraryChapter: Int,
    val libraryVerse: Int
)

private class ReaderPreferencesStore(private val context: Context) {
    private val versionPathKey = stringPreferencesKey("version_path")
    private val versionNameKey = stringPreferencesKey("version_name")
    private val bookKey = intPreferencesKey("book")
    private val chapterKey = intPreferencesKey("chapter")
    private val verseKey = intPreferencesKey("verse")
    private val fontFamilyKey = stringPreferencesKey("font_family")
    private val fontSizeKey = floatPreferencesKey("font_size")
    private val themeColorIndexKey = intPreferencesKey("theme_color_index")
    private val themeModeKey = stringPreferencesKey("theme_mode")
    private val useAndroidPrimaryThemeKey = booleanPreferencesKey("use_android_primary_theme")
    private val ttsLanguageCodeKey = stringPreferencesKey("tts_language_code")
    private val ttsVoiceNameKey = stringPreferencesKey("tts_voice_name")
    private val ttsSpeechRateKey = floatPreferencesKey("tts_speech_rate")
    private val ttsPitchKey = floatPreferencesKey("tts_pitch")
    private val libraryTopSectionKey = stringPreferencesKey("library_top_section")
    private val librarySectionKey = stringPreferencesKey("library_section")
    private val libraryBookKey = intPreferencesKey("library_book")
    private val libraryChapterKey = intPreferencesKey("library_chapter")
    private val libraryVerseKey = intPreferencesKey("library_verse")

    suspend fun load(): ReaderSettings {
        val prefs = context.dataStore.data.first()
        return ReaderSettings(
            versionPath = prefs[versionPathKey] ?: "",
            book = prefs[bookKey] ?: 1,
            chapter = prefs[chapterKey] ?: 1,
            verse = prefs[verseKey] ?: 1,
            fontFamily = prefs[fontFamilyKey] ?: "serif",
            fontSize = prefs[fontSizeKey] ?: 20f,
            themeColorIndex = prefs[themeColorIndexKey] ?: 0,
            themeMode = prefs[themeModeKey]?.let { raw ->
                runCatching { ThemeModePreference.valueOf(raw) }.getOrNull()
            } ?: ThemeModePreference.System,
            useAndroidPrimaryTheme = prefs[useAndroidPrimaryThemeKey] ?: false,
            ttsLanguageCode = prefs[ttsLanguageCodeKey] ?: "",
            ttsVoiceName = prefs[ttsVoiceNameKey] ?: "",
            ttsSpeechRate = prefs[ttsSpeechRateKey] ?: 1f,
            ttsPitch = prefs[ttsPitchKey] ?: 1f,
            libraryTopSection = prefs[libraryTopSectionKey]?.let { raw ->
                runCatching { LibraryTopSection.valueOf(raw) }.getOrNull()
            } ?: LibraryTopSection.Search,
            librarySection = prefs[librarySectionKey]?.let { raw ->
                runCatching { LibrarySection.valueOf(raw) }.getOrNull()
            } ?: LibrarySection.Bookmarks,
            libraryBook = prefs[libraryBookKey] ?: 0,
            libraryChapter = prefs[libraryChapterKey] ?: 0,
            libraryVerse = prefs[libraryVerseKey] ?: 0
        )
    }

    suspend fun saveVersion(path: String, name: String) {
        context.dataStore.edit {
            it[versionPathKey] = path
            it[versionNameKey] = name
        }
    }

    suspend fun savePosition(book: Int, chapter: Int, verse: Int) {
        context.dataStore.edit {
            it[bookKey] = book
            it[chapterKey] = chapter
            it[verseKey] = verse
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

    suspend fun saveThemeMode(themeMode: ThemeModePreference) {
        context.dataStore.edit {
            it[themeModeKey] = themeMode.name
        }
    }

    suspend fun saveTtsSettings(languageCode: String, voiceName: String, speechRate: Float, pitch: Float) {
        context.dataStore.edit {
            it[ttsLanguageCodeKey] = languageCode
            it[ttsVoiceNameKey] = voiceName
            it[ttsSpeechRateKey] = speechRate
            it[ttsPitchKey] = pitch
        }
    }

    suspend fun saveLibraryState(topSection: LibraryTopSection, section: LibrarySection, reference: VerseReference?) {
        context.dataStore.edit {
            it[libraryTopSectionKey] = topSection.name
            it[librarySectionKey] = section.name
            it[libraryBookKey] = reference?.book ?: 0
            it[libraryChapterKey] = reference?.chapter ?: 0
            it[libraryVerseKey] = reference?.verse ?: 0
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
        .filter { it.isFile && (it.extension.lowercase() == "json" || it.extension.lowercase() == "xml") }
        .groupBy { file ->
            (file.parentFile?.name.orEmpty()) to file.nameWithoutExtension.lowercase()
        }
        .values
        .mapNotNull { files ->
            files.sortedWith(compareBy<File> { it.extension.lowercase() != "xml" }.thenBy { it.absolutePath.lowercase() }).firstOrNull()
        }
        .sortedBy { it.absolutePath.lowercase() }
        .map { file ->
            val displayName = xmlBibleNameFromFile(file) ?: displayNameFromPath(file.name)
            val copyright = xmlBibleCopyrightFromFile(file)
            InstalledVersion(
                label = displayName,
                file = file,
                copyright = copyright,
                isPublicDomain = isPublicDomainCopyright(copyright)
            )
        }
        .toList()

    suspend fun fetchRemoteVersions(): List<RemoteVersion> = withContext(Dispatchers.IO) {
        val versions = mutableListOf<RemoteVersion>()
        for (source in BibleSources) {
            if (source.bundledAssetDir != null) {
                val assetPaths = context.assets.list(source.bundledAssetDir).orEmpty()
                assetPaths
                    .filter { it.lowercase().endsWith(".xml") }
                    .forEach { assetName ->
                        val assetPath = "${source.bundledAssetDir}/$assetName"
                        val copyright = xmlBibleCopyrightFromAsset(assetPath)
                        versions += RemoteVersion(
                            source = source,
                            branch = source.branch,
                            path = assetPath,
                            sizeBytes = bundledAssetSize(assetPath),
                            displayNameOverride = xmlBibleNameFromAsset(assetPath),
                            copyright = copyright,
                            isPublicDomain = isPublicDomainCopyright(copyright)
                        )
                    }
            } else {
                val fetched = runCatching { fetchTree(source, source.branch) }.getOrDefault(emptyList())
                versions.addAll(fetched)
            }
        }
        versions.sortedBy { it.displayName.lowercase() }
    }

    suspend fun installRemoteVersion(remote: RemoteVersion): InstalledVersion = withContext(Dispatchers.IO) {
        val targetName = remote.path.substringAfterLast('/')
        val target = File(File(versionsDir, remote.source.key), targetName)
        target.parentFile?.mkdirs()
        if (!target.exists()) {
            if (remote.path.lowercase().endsWith(".xml")) {
                if (remote.source.bundledAssetDir != null) {
                    context.assets.open(remote.path).use { input ->
                        target.outputStream().use { output -> input.copyTo(output) }
                    }
                } else {
                    downloadFile(remote.downloadUrl, target)
                }
                val legacyJson = File(target.parentFile, target.nameWithoutExtension + ".json")
                if (legacyJson.exists()) {
                    legacyJson.delete()
                }
            } else {
                downloadFile(remote.downloadUrl, target)
            }
        }
        val resolvedCopyright = remote.copyright ?: xmlBibleCopyrightFromFile(target)
        InstalledVersion(
            label = remote.displayName,
            file = target,
            copyright = resolvedCopyright,
            isPublicDomain = isPublicDomainCopyright(resolvedCopyright)
        )
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
            val matchesPrefix = source.pathPrefix?.let { path.startsWith(it) } ?: true
            val matchesSuffix = source.pathSuffix?.let { lower.endsWith(it.lowercase()) } ?: true
            if (!matchesPrefix || !matchesSuffix) continue
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

    fun xmlBibleCopyrightFromFile(file: File): String? {
        return try {
            file.inputStream().use { input ->
                xmlBibleCopyrightFromInput(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun xmlBibleNameFromAsset(assetPath: String): String? {
        return try {
            context.assets.open(assetPath).use { input ->
                xmlBibleNameFromInput(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun xmlBibleCopyrightFromAsset(assetPath: String): String? {
        return try {
            context.assets.open(assetPath).use { input ->
                xmlBibleCopyrightFromInput(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun xmlBibleNameFromFile(file: File): String? {
        return try {
            file.inputStream().use { input ->
                xmlBibleNameFromInput(input)
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun xmlBibleCopyrightFromInput(input: java.io.InputStream): String? {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(InputStreamReader(input, Charsets.UTF_8))
        var event = parser.eventType
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
            event = parser.next()
        }
        if (event != XmlPullParser.START_TAG) return null
        if (parser.name != "bible") return null
        return parser.getAttributeValue(null, "copyright")
            ?: parser.getAttributeValue(null, "status")
            ?: parser.getAttributeValue(null, "info")
    }

    private fun xmlBibleNameFromInput(input: java.io.InputStream): String? {
        val parser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(InputStreamReader(input, Charsets.UTF_8))
        var event = parser.eventType
        while (event != XmlPullParser.START_TAG && event != XmlPullParser.END_DOCUMENT) {
            event = parser.next()
        }
        if (event != XmlPullParser.START_TAG) return null
        if (parser.name != "bible") return null
        return parser.getAttributeValue(null, "translation") ?: parser.getAttributeValue(null, "name")
    }

    private fun bundledAssetSize(assetPath: String): Long {
        return context.assets.open(assetPath).use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            var total = 0L
            while (true) {
                val read = input.read(buffer)
                if (read <= 0) break
                total += read
            }
            total
        }
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

private fun parseBibleJson(jsonFile: File): JsonBibleData {
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

private fun parseBibleXmlIndex(xmlFile: File): XmlBibleIndex {
    val factory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = false
    }
    val parser = factory.newPullParser()

    val chapterRefs = linkedSetOf<ChapterLocation>()
    val chapterNumbersByBook = mutableMapOf<Int, MutableSet<Int>>()

    var currentBook = 0
    var currentChapter = 0

    xmlFile.inputStream().use { input ->
        InputStreamReader(input, Charsets.UTF_8).use { reader ->
            parser.setInput(reader)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "book" -> currentBook = parser.getAttributeValue(null, "number")?.toIntOrNull() ?: currentBook
                        "chapter" -> {
                            currentChapter = parser.getAttributeValue(null, "number")?.toIntOrNull() ?: currentChapter
                            if (currentBook > 0 && currentChapter > 0) {
                                chapterRefs += ChapterLocation(currentBook, currentChapter)
                                chapterNumbersByBook.getOrPut(currentBook) { mutableSetOf() }.add(currentChapter)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
        }
    }

    val orderedChapters = chapterRefs.sortedWith(compareBy<ChapterLocation> { it.book }.thenBy { it.chapter })
    val books = chapterNumbersByBook.keys.sorted()
    val chaptersByBook = chapterNumbersByBook.mapValues { (_, chapters) -> chapters.sorted() }

    return XmlBibleIndex(
        books = books,
        chaptersByBook = chaptersByBook,
        orderedChapters = orderedChapters
    )
}

private fun readBibleXmlChapter(xmlFile: File, book: Int, chapter: Int): List<Verse> {
    val factory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = false
    }
    val parser = factory.newPullParser()

    val verses = mutableListOf<Verse>()
    var currentBook = 0
    var currentChapter = 0
    var currentVerse = 0
    var verseText = StringBuilder()
    var inVerse = false

    xmlFile.inputStream().use { input ->
        InputStreamReader(input, Charsets.UTF_8).use { reader ->
            parser.setInput(reader)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "book" -> currentBook = parser.getAttributeValue(null, "number")?.toIntOrNull() ?: currentBook
                        "chapter" -> currentChapter = parser.getAttributeValue(null, "number")?.toIntOrNull() ?: currentChapter
                        "verse" -> {
                            currentVerse = parser.getAttributeValue(null, "number")?.toIntOrNull()
                                ?: parser.getAttributeValue(null, "bnumber")?.toIntOrNull()
                                ?: 0
                            verseText = StringBuilder()
                            inVerse = true
                        }
                    }

                    XmlPullParser.TEXT -> if (inVerse && currentBook == book && currentChapter == chapter) {
                        verseText.append(parser.text)
                    }

                    XmlPullParser.END_TAG -> if (parser.name == "verse") {
                        val text = verseText.toString().trim()
                        if (currentBook == book && currentChapter == chapter && currentVerse > 0 && text.isNotBlank()) {
                            verses += Verse(currentVerse, text)
                        }
                        inVerse = false
                    }
                }
                eventType = parser.next()
            }
        }
    }

    return verses.sortedBy { it.number }
}

private fun searchBibleXmlVerses(xmlFile: File, textQuery: String, limit: Int): List<VerseSearchHit> {
    val query = textQuery.trim().lowercase()
    if (query.isBlank()) return emptyList()

    val factory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = false
    }
    val parser = factory.newPullParser()

    val hits = mutableListOf<VerseSearchHit>()
    var currentBook = 0
    var currentChapter = 0
    var currentVerse = 0
    var verseText = StringBuilder()
    var inVerse = false

    xmlFile.inputStream().use { input ->
        InputStreamReader(input, Charsets.UTF_8).use { reader ->
            parser.setInput(reader)

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT && hits.size < limit) {
                when (eventType) {
                    XmlPullParser.START_TAG -> when (parser.name) {
                        "book" -> currentBook = parser.getAttributeValue(null, "number")?.toIntOrNull() ?: currentBook
                        "chapter" -> currentChapter = parser.getAttributeValue(null, "number")?.toIntOrNull() ?: currentChapter
                        "verse" -> {
                            currentVerse = parser.getAttributeValue(null, "number")?.toIntOrNull()
                                ?: parser.getAttributeValue(null, "bnumber")?.toIntOrNull()
                                ?: 0
                            verseText = StringBuilder()
                            inVerse = true
                        }
                    }

                    XmlPullParser.TEXT -> if (inVerse) {
                        verseText.append(parser.text)
                    }

                    XmlPullParser.END_TAG -> if (parser.name == "verse") {
                        val text = verseText.toString().trim()
                        if (currentBook > 0 && currentChapter > 0 && currentVerse > 0 && text.lowercase().contains(query)) {
                            hits += VerseSearchHit(currentBook, currentChapter, currentVerse, text)
                        }
                        inVerse = false
                    }
                }
                eventType = parser.next()
            }
        }
    }

    return hits.sortedWith(compareBy<VerseSearchHit> { it.book }.thenBy { it.chapter }.thenBy { it.verse })
}

private sealed interface BibleCache {
    val books: List<Int>
    val chaptersByBook: Map<Int, List<Int>>
    val orderedChapters: List<ChapterLocation>
}

private data class JsonBibleData(
    override val books: List<Int>,
    override val chaptersByBook: Map<Int, List<Int>>,
    override val orderedChapters: List<ChapterLocation>,
    val versesByChapter: Map<ChapterLocation, List<Verse>>,
    val allVerses: List<JsonVerseRecord>
) : BibleCache {
    companion object {
        fun empty() = JsonBibleData(emptyList(), emptyMap(), emptyList(), emptyMap(), emptyList())
    }
}

private data class XmlBibleIndex(
    override val books: List<Int>,
    override val chaptersByBook: Map<Int, List<Int>>,
    override val orderedChapters: List<ChapterLocation>
) : BibleCache

private data class JsonVerseRecord(val book: Int, val chapter: Int, val verse: Int, val text: String)

private class JsonBibleReader {
    private val cache = mutableMapOf<String, BibleCache>()

    fun availableBooks(jsonFile: File): List<Int> = load(jsonFile).books

    fun availableChapters(jsonFile: File, book: Int): List<Int> = load(jsonFile).chaptersByBook[book].orEmpty()

    fun searchVersesLike(jsonFile: File, textQuery: String, limit: Int = 100): List<VerseSearchHit> {
        return when (val data = load(jsonFile)) {
            is JsonBibleData -> {
                if (textQuery.isBlank()) return emptyList()
                val query = textQuery.trim().lowercase()
                data.allVerses
                    .asSequence()
                    .filter { it.text.lowercase().contains(query) }
                    .sortedWith(compareBy<JsonVerseRecord> { it.book }.thenBy { it.chapter }.thenBy { it.verse })
                    .take(limit)
                    .map { VerseSearchHit(it.book, it.chapter, it.verse, it.text) }
                    .toList()
            }
            is XmlBibleIndex -> searchBibleXmlVerses(jsonFile, textQuery, limit)
        }
    }

    fun readChapter(jsonFile: File, book: Int, chapter: Int): List<Verse> {
        return when (val data = load(jsonFile)) {
            is JsonBibleData -> data.versesByChapter[ChapterLocation(book, chapter)].orEmpty()
            is XmlBibleIndex -> readBibleXmlChapter(jsonFile, book, chapter)
        }
    }

    fun findAdjacent(jsonFile: File, book: Int, chapter: Int, direction: Int): ChapterLocation? {
        val refs = load(jsonFile).orderedChapters
        val index = refs.indexOfFirst { it.book == book && it.chapter == chapter }
        if (index == -1) return refs.firstOrNull()
        return refs.getOrNull(index + direction)
    }

    private fun load(jsonFile: File): BibleCache {
        val cacheKey = "${jsonFile.absolutePath}:${jsonFile.lastModified()}"
        return cache.getOrPut(cacheKey) {
            if (jsonFile.extension.lowercase() == "xml") {
                parseBibleXmlIndex(jsonFile)
            } else {
                parseBibleJson(jsonFile)
            }
        }
    }
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

private fun openBibleGateway(context: Context, reference: String, version: InstalledVersion?) {
    val versionCode = bibleGatewayVersionCode(version)
    val url = "https://www.biblegateway.com/passage/?search=${Uri.encode(reference)}&version=${Uri.encode(versionCode)}"
    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
}

private fun bibleGatewayVersionCode(version: InstalledVersion?): String {
    val label = version?.label.orEmpty()
    val extracted = label.substringAfterLast(':').trim().ifBlank { version?.file?.nameWithoutExtension.orEmpty() }
    val compact = extracted
        .removePrefix("t_")
        .removePrefix("t-")
        .replace(Regex("[^A-Za-z0-9]+"), "")
        .uppercase()
    return when (compact) {
        "ASV" -> "ASV"
        "BBE" -> "BBE"
        "ESV" -> "ESV"
        "KJV" -> "KJV"
        "NIV" -> "NIV"
        "NKJV" -> "NKJV"
        "WEB" -> "WEB"
        "YLT" -> "YLT"
        else -> compact.ifBlank { "NIV" }
    }
}

private fun Float.formatTtsValue(): String {
    return String.format(Locale.US, "%.2f", this)
}

@Suppress("DEPRECATION")
private fun appVersionName(context: Context): String {
    return runCatching {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    }.getOrNull().orEmpty().ifBlank { "Unknown" }
}
