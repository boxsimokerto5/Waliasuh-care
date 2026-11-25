package com.example.ui.dialogs

import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BibleBookSpec
import com.example.data.model.BibleBookmark
import com.example.data.model.BibleCatalog
import com.example.data.model.BibleNote
import com.example.data.model.BibleVerseItem
import com.example.util.BibleChapterLoadResult
import com.example.util.BibleContentRepository
import java.util.Locale

private enum class BibleBottomTab {
    ALKITAB,
    RENUNGAN,
    KISAH,
    CATATAN,
    BOOKMARK,
    CARI
}

@Composable
fun BibleReaderSheet(
    bookmarks: List<BibleBookmark>,
    notes: List<BibleNote>,
    initialEnglish: Boolean = false,
    useDialogWindow: Boolean = false,
    onSyncRequested: () -> Unit,
    onToggleBookmark: (
        bookId: String,
        chapter: Int,
        verse: Int,
        verseTextId: String,
        verseTextEn: String,
        highlightColor: String,
        onResult: (Boolean, String) -> Unit
    ) -> Unit,
    onDeleteBookmark: (BibleBookmark, (String) -> Unit) -> Unit,
    onSaveNote: (
        existingNote: BibleNote?,
        bookId: String,
        chapter: Int,
        verse: Int,
        title: String,
        content: String,
        categoryTag: String,
        onResult: (Boolean, String) -> Unit
    ) -> Unit,
    onDeleteNote: (BibleNote, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current

    var activeTab by remember { mutableStateOf(BibleBottomTab.ALKITAB) }
    var activeBook by remember { mutableStateOf(BibleCatalog.findBook("2TI")) }
    var activeChapter by remember { mutableIntStateOf(4) }
    var isEnglish by remember { mutableStateOf(initialEnglish) }
    var fontSizeSp by remember { mutableIntStateOf(17) }
    var useSerif by remember { mutableStateOf(true) }
    var showTypographyStrip by remember { mutableStateOf(false) }
    var showBookPicker by remember { mutableStateOf(false) }
    var searchPrefill by remember { mutableStateOf("") }
    var reloadNonce by remember { mutableIntStateOf(0) }
    var forceRefreshNext by remember { mutableStateOf(false) }

    var chapterResult by remember { mutableStateOf<BibleChapterLoadResult?>(null) }
    var isLoadingChapter by remember { mutableStateOf(true) }

    var selectedVerseForAction by remember { mutableStateOf<BibleVerseItem?>(null) }
    var editingNote by remember { mutableStateOf<BibleNote?>(null) }
    var noteAnchorVerse by remember { mutableIntStateOf(0) }
    var showNoteEditor by remember { mutableStateOf(false) }

    // TextToSpeech untuk fitur Audio Reader tombol Speaker
    var ttsReady by remember { mutableStateOf(false) }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(Unit) {
        val engine = TextToSpeech(context.applicationContext) { status ->
            ttsReady = (status == TextToSpeech.SUCCESS)
        }
        ttsEngine = engine
        onDispose {
            runCatching {
                engine.stop()
                engine.shutdown()
            }
            ttsEngine = null
        }
    }

    fun stopAudio() {
        runCatching { ttsEngine?.stop() }
        isSpeaking = false
    }

    fun speakVerses(verses: List<BibleVerseItem>, startFromVerse: Int = 1) {
        val engine = ttsEngine
        if (!ttsReady || engine == null) {
            Toast.makeText(
                context,
                if (isEnglish) "Audio voice engine is initializing..." else "Suara audio sedang disiapkan...",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val locale = if (isEnglish) Locale.US else Locale("id", "ID")
        val langResult = engine.setLanguage(locale)
        if (langResult == TextToSpeech.LANG_MISSING_DATA || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.setLanguage(Locale.ENGLISH)
        }
        val textToRead = verses
            .filter { it.verse >= startFromVerse }
            .mapNotNull { v ->
                val raw = if (isEnglish) v.textEn.ifBlank { v.textId } else v.textId
                raw.takeIf { it.isNotBlank() }?.let { "${v.verse}. $it" }
            }
            .joinToString(". ")
        if (textToRead.isBlank()) return
        engine.speak(textToRead.take(3600), TextToSpeech.QUEUE_FLUSH, null, "bible_read_${activeBook.id}_$activeChapter")
        isSpeaking = true
    }

    LaunchedEffect(Unit) {
        onSyncRequested()
    }

    LaunchedEffect(activeBook.id, activeChapter, isEnglish, reloadNonce) {
        val force = forceRefreshNext
        forceRefreshNext = false
        isLoadingChapter = true
        stopAudio()
        val loaded = BibleContentRepository.loadChapterVerses(
            context = context,
            book = activeBook,
            chapter = activeChapter,
            isEnglish = isEnglish,
            forceRefresh = force
        )
        chapterResult = loaded
        isLoadingChapter = false
        if (loaded.isFromOnlineOrCache && !Build.FINGERPRINT.equals("robolectric", ignoreCase = true)) {
            BibleContentRepository.prefetchAdjacentChapters(
                context = context,
                book = activeBook,
                chapter = activeChapter,
                isEnglish = isEnglish
            )
        }
    }

    val currentChapterBookmarks = remember(bookmarks, activeBook.id, activeChapter) {
        bookmarks
            .filter { it.bookId.equals(activeBook.id, ignoreCase = true) && it.chapter == activeChapter }
            .associateBy { it.verse }
    }

    val currentChapterNoteVerses = remember(notes, activeBook.id, activeChapter) {
        notes
            .filter { it.bookId.equals(activeBook.id, ignoreCase = true) && it.chapter == activeChapter }
            .map { it.verse }
            .toSet()
    }

    val readerBody: @Composable () -> Unit = {
        BackHandler {
            if (showBookPicker) {
                showBookPicker = false
            } else if (activeTab != BibleBottomTab.ALKITAB) {
                activeTab = BibleBottomTab.ALKITAB
            } else {
                stopAudio()
                onDismiss()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BibleParchmentPalette.BackgroundTop,
                            BibleParchmentPalette.BackgroundBottom
                        )
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                )
                .testTag("bible_reader_root")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header surface extends cleanly behind system status bar when edge-to-edge
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BibleParchmentPalette.TopBarSurface
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                            )
                    ) {
                        // Dekorasi garis titik warna-warni tipis di atas bar (nuansa perayaan Alkitab)
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .height(6.dp)
                        ) {
                            val dotColors = listOf(
                                Color(0xFF10B981),
                                Color(0xFFF59E0B),
                                Color(0xFFEF4444),
                                Color(0xFF3B82F6),
                                Color(0xFFEC4899)
                            )
                            val step = size.width / 10f
                            for (i in 0 until 10) {
                                drawCircle(
                                    color = dotColors[i % dotColors.size].copy(alpha = 0.85f),
                                    radius = 3.dp.toPx(),
                                    center = Offset(step * i + step * 0.5f, size.height / 2f)
                                )
                            }
                        }

                        // TOP BAR (Proportional: Emblem + Book Dropdown + IND|ENG + Audio + T + Close)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                    // Left Golden Cross Emblem Button
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                activeTab = BibleBottomTab.ALKITAB
                                activeBook = BibleCatalog.findBook("JHN")
                                activeChapter = 3
                            }
                            .testTag("bible_emblem_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = BibleParchmentPalette.CardSurface,
                        border = BorderStroke(1.dp, Color(0xFFD9B882)),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "✝",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    // Book & Chapter Selector Pill ("📖 Kisah... 11 v")
                    val activeBookLabel = if (isEnglish) activeBook.nameEn else activeBook.nameId
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clickable { showBookPicker = true }
                            .testTag("bible_book_dropdown_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = BibleParchmentPalette.PillSurface,
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                contentDescription = if (isEnglish) "Book selector" else "Pilih kitab",
                                tint = BibleParchmentPalette.MaroonAccent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "$activeBookLabel $activeChapter",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BibleParchmentPalette.InkPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Rounded.KeyboardArrowDown,
                                contentDescription = null,
                                tint = BibleParchmentPalette.InkSecondary,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // IND | ENG Bilingual Pill Toggle
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = BibleParchmentPalette.PillSurface,
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Row(modifier = Modifier.padding(2.dp)) {
                            BibleLangSegment(
                                text = "IND",
                                selected = !isEnglish,
                                onClick = {
                                    stopAudio()
                                    isEnglish = false
                                },
                                modifier = Modifier.testTag("bible_lang_ind")
                            )
                            BibleLangSegment(
                                text = "ENG",
                                selected = isEnglish,
                                onClick = {
                                    stopAudio()
                                    isEnglish = true
                                },
                                modifier = Modifier.testTag("bible_lang_eng")
                            )
                        }
                    }

                    // Audio TTS Speaker IconButton
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                stopAudio()
                            } else {
                                val verses = chapterResult?.verses.orEmpty()
                                if (verses.isNotEmpty()) {
                                    speakVerses(verses, 1)
                                }
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("bible_audio_button")
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                            contentDescription = if (isSpeaking) {
                                if (isEnglish) "Stop reading" else "Hentikan suara"
                            } else {
                                if (isEnglish) "Read chapter aloud" else "Dengarkan pasal"
                            },
                            tint = if (isSpeaking) BibleParchmentPalette.MaroonAccent else BibleParchmentPalette.InkPrimary,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    // Typography "T" Toggle Button
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                if (showTypographyStrip) BibleParchmentPalette.PillSurface else Color.Transparent
                            )
                            .clickable { showTypographyStrip = !showTypographyStrip }
                            .testTag("bible_typography_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "T",
                            fontSize = 17.sp,
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold,
                            color = BibleParchmentPalette.MaroonAccent
                        )
                    }

                    // Search button omitted here because Cari tab sits in bottom nav, freeing room for Book Selector
                    // Right Golden Close / Exit Button
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                stopAudio()
                                onDismiss()
                            }
                            .testTag("bible_close_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF59E0B),
                        border = BorderStroke(1.dp, Color(0xFFD97706)),
                        shadowElevation = 2.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = if (isEnglish) "Close Bible" else "Tutup Alkitab",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
                    }
                }

                // Quick Typography Panel (when "T" tapped)
                if (showTypographyStrip) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(BibleParchmentPalette.CardSoft)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (useSerif) BibleParchmentPalette.MaroonAccent else BibleParchmentPalette.CardSurface,
                                border = BorderStroke(1.dp, BibleParchmentPalette.Border),
                                modifier = Modifier.clickable { useSerif = true }
                            ) {
                                Text(
                                    text = "Serif Klasik",
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Serif,
                                    color = if (useSerif) Color.White else BibleParchmentPalette.InkPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (!useSerif) BibleParchmentPalette.MaroonAccent else BibleParchmentPalette.CardSurface,
                                border = BorderStroke(1.dp, BibleParchmentPalette.Border),
                                modifier = Modifier.clickable { useSerif = false }
                            ) {
                                Text(
                                    text = "Sans Modern",
                                    fontSize = 11.sp,
                                    color = if (!useSerif) Color.White else BibleParchmentPalette.InkPrimary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                        Text(
                            text = "${fontSizeSp}sp",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = BibleParchmentPalette.InkSecondary
                        )
                    }
                }

                HorizontalDivider(color = BibleParchmentPalette.Border)

                // MAIN BODY AREA BY ACTIVE TAB
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when (activeTab) {
                        BibleBottomTab.ALKITAB -> {
                            BiblePassageReaderContent(
                                book = activeBook,
                                chapter = activeChapter,
                                isEnglish = isEnglish,
                                fontSizeSp = fontSizeSp,
                                useSerif = useSerif,
                                isLoading = isLoadingChapter,
                                chapterResult = chapterResult,
                                bookmarksByVerse = currentChapterBookmarks,
                                notedVerses = currentChapterNoteVerses,
                                onDecreaseFont = { if (fontSizeSp > 14) fontSizeSp -= 1 },
                                onIncreaseFont = { if (fontSizeSp < 28) fontSizeSp += 1 },
                                onRetryOnline = {
                                    forceRefreshNext = true
                                    reloadNonce += 1
                                },
                                onPrevChapter = {
                                    if (activeChapter > 1) {
                                        activeChapter -= 1
                                    } else {
                                        val idx = BibleCatalog.ALL_BOOKS.indexOfFirst { it.id == activeBook.id }
                                        if (idx > 0) {
                                            val prevBook = BibleCatalog.ALL_BOOKS[idx - 1]
                                            activeBook = prevBook
                                            activeChapter = prevBook.chaptersCount
                                        }
                                    }
                                },
                                onNextChapter = {
                                    if (activeChapter < activeBook.chaptersCount) {
                                        activeChapter += 1
                                    } else {
                                        val idx = BibleCatalog.ALL_BOOKS.indexOfFirst { it.id == activeBook.id }
                                        if (idx in 0 until BibleCatalog.ALL_BOOKS.lastIndex) {
                                            val nextBook = BibleCatalog.ALL_BOOKS[idx + 1]
                                            activeBook = nextBook
                                            activeChapter = 1
                                        }
                                    }
                                },
                                onOpenPicker = { showBookPicker = true },
                                onVerseClick = { verse -> selectedVerseForAction = verse }
                            )
                        }

                        BibleBottomTab.RENUNGAN -> {
                            BibleDevotionalTabContent(
                                isEnglish = isEnglish,
                                fontSizeSp = fontSizeSp,
                                useSerif = useSerif,
                                onOpenPassage = { bookId, ch ->
                                    activeBook = BibleCatalog.findBook(bookId)
                                    activeChapter = ch.coerceIn(1, activeBook.chaptersCount)
                                    activeTab = BibleBottomTab.ALKITAB
                                }
                            )
                        }

                        BibleBottomTab.KISAH -> {
                            BibleStoriesTabContent(
                                isEnglish = isEnglish,
                                fontSizeSp = fontSizeSp,
                                useSerif = useSerif,
                                onOpenPassage = { bookId, ch ->
                                    activeBook = BibleCatalog.findBook(bookId)
                                    activeChapter = ch.coerceIn(1, activeBook.chaptersCount)
                                    activeTab = BibleBottomTab.ALKITAB
                                }
                            )
                        }

                        BibleBottomTab.CATATAN -> {
                            BibleNotesTabContent(
                                notes = notes,
                                isEnglish = isEnglish,
                                onCreateNote = {
                                    editingNote = null
                                    noteAnchorVerse = 1
                                    showNoteEditor = true
                                },
                                onEditNote = { existing ->
                                    editingNote = existing
                                    noteAnchorVerse = existing.verse
                                    showNoteEditor = true
                                },
                                onDeleteNote = { target ->
                                    onDeleteNote(target) { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onOpenPassage = { bookId, ch ->
                                    activeBook = BibleCatalog.findBook(bookId)
                                    activeChapter = ch.coerceIn(1, activeBook.chaptersCount)
                                    activeTab = BibleBottomTab.ALKITAB
                                }
                            )
                        }

                        BibleBottomTab.BOOKMARK -> {
                            BibleBookmarksTabContent(
                                bookmarks = bookmarks,
                                isEnglish = isEnglish,
                                onDeleteBookmark = { bm ->
                                    onDeleteBookmark(bm) { msg ->
                                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onOpenPassage = { bookId, ch ->
                                    activeBook = BibleCatalog.findBook(bookId)
                                    activeChapter = ch.coerceIn(1, activeBook.chaptersCount)
                                    activeTab = BibleBottomTab.ALKITAB
                                }
                            )
                        }

                        BibleBottomTab.CARI -> {
                            BibleSearchTabContent(
                                isEnglish = isEnglish,
                                initialQuery = searchPrefill,
                                onOpenBookChapter = { book, ch ->
                                    activeBook = book
                                    activeChapter = ch.coerceIn(1, book.chaptersCount)
                                    activeTab = BibleBottomTab.ALKITAB
                                }
                            )
                        }
                    }
                }

                // BOTTOM 6-TAB BAR (Surface paints behind Android nav bar; Row stays fully above it via navigationBarsPadding)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = BibleParchmentPalette.TopBarSurface,
                    shadowElevation = 6.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        HorizontalDivider(color = BibleParchmentPalette.Border)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsPadding(
                                    WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                                )
                                .imePadding()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BibleBottomNavItem(
                                label = if (isEnglish) "Bible" else "Alkitab",
                                icon = Icons.AutoMirrored.Rounded.MenuBook,
                                selected = activeTab == BibleBottomTab.ALKITAB,
                                badgeCount = 0,
                                onClick = { activeTab = BibleBottomTab.ALKITAB },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bible_nav_alkitab")
                            )
                            BibleBottomNavItem(
                                label = if (isEnglish) "Devotion" else "Renungan",
                                icon = Icons.Rounded.AutoAwesome,
                                selected = activeTab == BibleBottomTab.RENUNGAN,
                                badgeCount = 0,
                                onClick = { activeTab = BibleBottomTab.RENUNGAN },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bible_nav_renungan")
                            )
                            BibleBottomNavItem(
                                label = if (isEnglish) "Stories" else "Kisah",
                                icon = Icons.Rounded.FavoriteBorder,
                                selected = activeTab == BibleBottomTab.KISAH,
                                badgeCount = 0,
                                onClick = { activeTab = BibleBottomTab.KISAH },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bible_nav_kisah")
                            )
                            BibleBottomNavItem(
                                label = if (isEnglish) "Notes" else "Catatan",
                                icon = Icons.Rounded.Description,
                                selected = activeTab == BibleBottomTab.CATATAN,
                                badgeCount = notes.size,
                                onClick = { activeTab = BibleBottomTab.CATATAN },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bible_nav_catatan")
                            )
                            BibleBottomNavItem(
                                label = "Bookmark",
                                icon = Icons.Rounded.BookmarkBorder,
                                selected = activeTab == BibleBottomTab.BOOKMARK,
                                badgeCount = bookmarks.size,
                                onClick = { activeTab = BibleBottomTab.BOOKMARK },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bible_nav_bookmark")
                            )
                            BibleBottomNavItem(
                                label = if (isEnglish) "Search" else "Cari",
                                icon = Icons.Rounded.Search,
                                selected = activeTab == BibleBottomTab.CARI,
                                badgeCount = 0,
                                onClick = { activeTab = BibleBottomTab.CARI },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("bible_nav_cari")
                            )
                        }
                    }
                }
            }

            if (showBookPicker) {
                BibleBookPickerSheet(
                    currentBook = activeBook,
                    currentChapter = activeChapter,
                    isEnglish = isEnglish,
                    useDialogWindow = false,
                    onSelectBookChapter = { book, chapter ->
                        activeBook = book
                        activeChapter = chapter
                        activeTab = BibleBottomTab.ALKITAB
                        showBookPicker = false
                    },
                    onDismiss = { showBookPicker = false }
                )
            }

            selectedVerseForAction?.let { verse ->
                val isMarked = currentChapterBookmarks.containsKey(verse.verse)
                BibleVerseActionDialog(
                    book = activeBook,
                    chapter = activeChapter,
                    verse = verse,
                    isBookmarked = isMarked,
                    isEnglish = isEnglish,
                    onToggleBookmark = { color ->
                        selectedVerseForAction = null
                        onToggleBookmark(
                            activeBook.id,
                            activeChapter,
                            verse.verse,
                            verse.textId,
                            verse.textEn,
                            color
                        ) { _, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    onAddNote = {
                        selectedVerseForAction = null
                        editingNote = null
                        noteAnchorVerse = verse.verse
                        showNoteEditor = true
                    },
                    onSpeakFromVerse = {
                        selectedVerseForAction = null
                        speakVerses(chapterResult?.verses.orEmpty(), verse.verse)
                    },
                    onCopyVerse = {
                        selectedVerseForAction = null
                        val clip = if (isEnglish) verse.textEn else verse.textId
                        clipboard.setText(AnnotatedString(clip))
                        Toast.makeText(
                            context,
                            if (isEnglish) "Verse copied to clipboard." else "Ayat disalin ke clipboard.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    onDismiss = { selectedVerseForAction = null }
                )
            }

            if (showNoteEditor) {
                BibleNoteEditorDialog(
                    initialNote = editingNote,
                    defaultBook = activeBook,
                    defaultChapter = activeChapter,
                    defaultVerse = noteAnchorVerse,
                    isEnglish = isEnglish,
                    onSave = { title, content, tag ->
                        val targetBookId = editingNote?.bookId ?: activeBook.id
                        val targetChapter = editingNote?.chapter ?: activeChapter
                        val targetVerse = editingNote?.verse ?: noteAnchorVerse
                        onSaveNote(
                            editingNote,
                            targetBookId,
                            targetChapter,
                            targetVerse,
                            title,
                            content,
                            tag
                        ) { ok, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            if (ok) {
                                showNoteEditor = false
                                editingNote = null
                            }
                        }
                    },
                    onDismiss = {
                        showNoteEditor = false
                        editingNote = null
                    }
                )
            }
        }
    }

    if (useDialogWindow) {
        Dialog(
            onDismissRequest = {
                stopAudio()
                onDismiss()
            },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            readerBody()
        }
    } else {
        readerBody()
    }
}

@Composable
private fun BibleLangSegment(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) BibleParchmentPalette.MaroonAccent else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else BibleParchmentPalette.InkSecondary
        )
    }
}

@Composable
private fun BiblePassageReaderContent(
    book: BibleBookSpec,
    chapter: Int,
    isEnglish: Boolean,
    fontSizeSp: Int,
    useSerif: Boolean,
    isLoading: Boolean,
    chapterResult: BibleChapterLoadResult?,
    bookmarksByVerse: Map<Int, BibleBookmark>,
    notedVerses: Set<Int>,
    onDecreaseFont: () -> Unit,
    onIncreaseFont: () -> Unit,
    onRetryOnline: () -> Unit,
    onPrevChapter: () -> Unit,
    onNextChapter: () -> Unit,
    onOpenPicker: () -> Unit,
    onVerseClick: (BibleVerseItem) -> Unit
) {
    val fontFamily = if (useSerif) FontFamily.Serif else FontFamily.SansSerif
    val catLabel = (if (isEnglish) book.categoryEn else book.categoryId).uppercase()
    val bookTitle = (if (isEnglish) book.nameEn else book.nameId).uppercase()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 680.dp)
    ) {
        // Subheader Card: Row 1 shows FULL untruncated Book Title + Font Stepper; Row 2 shows Category Pill + Source Edition
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            color = BibleParchmentPalette.CardSurface,
            border = BorderStroke(1.dp, BibleParchmentPalette.Border)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = bookTitle,
                        fontSize = 18.sp,
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = BibleParchmentPalette.InkPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(onClick = onOpenPicker)
                            .testTag("bible_subheader_book_title")
                    )

                    // Font Stepper Pill [- 17 +]
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = BibleParchmentPalette.PillSurface,
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onDecreaseFont)
                                    .testTag("bible_font_minus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "−",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BibleParchmentPalette.InkPrimary
                                )
                            }
                            Text(
                                text = fontSizeSp.toString(),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BibleParchmentPalette.InkPrimary,
                                modifier = Modifier.padding(horizontal = 5.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onIncreaseFont)
                                    .testTag("bible_font_plus"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BibleParchmentPalette.InkPrimary
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = BibleParchmentPalette.CardSoft,
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border),
                        modifier = Modifier.clickable(onClick = onOpenPicker)
                    ) {
                        Text(
                            text = "$catLabel • ${book.testament.code}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BibleParchmentPalette.MaroonAccent,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }

                    chapterResult?.let { res ->
                        Text(
                            text = if (isEnglish) res.sourceLabelEn else res.sourceLabelId,
                            fontSize = 11.sp,
                            color = BibleParchmentPalette.InkMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Chapter Quick Switcher Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BibleParchmentPalette.CardSurface,
                border = BorderStroke(1.dp, BibleParchmentPalette.Border),
                modifier = Modifier
                    .clickable(onClick = onPrevChapter)
                    .testTag("bible_prev_chapter")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = if (isEnglish) "Previous chapter" else "Pasal sebelumnya",
                        tint = BibleParchmentPalette.InkSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = if (isEnglish) "Prev" else "Sblm",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BibleParchmentPalette.InkSecondary
                    )
                }
            }

            Text(
                text = if (isEnglish) {
                    "Chapter $chapter of ${book.chaptersCount}"
                } else {
                    "Pasal $chapter dari ${book.chaptersCount}"
                },
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = BibleParchmentPalette.MaroonAccent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onOpenPicker)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BibleParchmentPalette.CardSurface,
                border = BorderStroke(1.dp, BibleParchmentPalette.Border),
                modifier = Modifier
                    .clickable(onClick = onNextChapter)
                    .testTag("bible_next_chapter")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEnglish) "Next" else "Lanjut",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = BibleParchmentPalette.InkSecondary
                    )
                    Icon(
                        imageVector = Icons.Rounded.ChevronRight,
                        contentDescription = if (isEnglish) "Next chapter" else "Pasal berikutnya",
                        tint = BibleParchmentPalette.InkSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = BibleParchmentPalette.MaroonAccent)
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isEnglish) "Opening chapter..." else "Membuka pasal...",
                        fontSize = 13.sp,
                        color = BibleParchmentPalette.InkSecondary
                    )
                }
            }
        } else {
            val verses = chapterResult?.verses.orEmpty()
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (chapterResult?.isOfflineFallback == true) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            OutlinedButton(
                                onClick = onRetryOnline,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BibleParchmentPalette.AmberAccent),
                                modifier = Modifier
                                    .height(44.dp)
                                    .testTag("bible_retry_chapter_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = null,
                                    tint = BibleParchmentPalette.MaroonAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isEnglish) "Retry Loading Chapter" else "Muat Ulang Pasal Online",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BibleParchmentPalette.MaroonAccent
                                )
                            }
                        }
                    }
                }
                items(verses, key = { it.verse }) { v ->
                    val bm = bookmarksByVerse[v.verse]
                    val hasNote = notedVerses.contains(v.verse)
                    val rowBg = if (bm != null) resolveHighlightBg(bm.highlightColor) else Color.Transparent
                    val displayVerseText = if (isEnglish) {
                        v.textEn.ifBlank { v.textId }
                    } else {
                        v.textId.ifBlank { "" }
                    }
                    if (displayVerseText.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(rowBg)
                                .clickable { onVerseClick(v) }
                                .padding(horizontal = if (bm != null) 10.dp else 4.dp, vertical = 6.dp)
                                .testTag("bible_verse_row_${v.verse}"),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Surface(
                                    modifier = Modifier
                                        .padding(top = 3.dp)
                                        .size(26.dp),
                                    shape = RoundedCornerShape(7.dp),
                                    color = if (bm != null) BibleParchmentPalette.MaroonAccent else BibleParchmentPalette.CardSoft,
                                    border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = v.verse.toString(),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (bm != null) Color.White else BibleParchmentPalette.InkSecondary
                                        )
                                    }
                                }
                                if (bm != null || hasNote) {
                                    Spacer(Modifier.height(4.dp))
                                    Icon(
                                        imageVector = if (bm != null) Icons.Rounded.Bookmark else Icons.Rounded.Description,
                                        contentDescription = null,
                                        tint = BibleParchmentPalette.AmberAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Text(
                                text = displayVerseText,
                                fontSize = fontSizeSp.sp,
                                fontFamily = fontFamily,
                                lineHeight = (fontSizeSp * 1.58f).sp,
                                color = BibleParchmentPalette.InkPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
                item {
                    Text(
                        text = if (isEnglish) {
                            "Tip: Tap any verse to bookmark, highlight, listen, copy, or add a personal note."
                        } else {
                            "Tips: Sentuh ayat mana saja untuk bookmark, sorot warna, dengar audio, salin, atau buat catatan."
                        },
                        fontSize = 11.sp,
                        color = BibleParchmentPalette.InkMuted,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BibleBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badgeCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = BibleParchmentPalette.MaroonAccent
    val idleColor = BibleParchmentPalette.InkSecondary

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Active maroon top indicator matching Screenshot 2 "ALKITAB"
        Box(
            modifier = Modifier
                .width(26.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) activeColor else Color.Transparent)
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) activeColor else idleColor,
                modifier = Modifier.size(21.dp)
            )
            if (badgeCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 7.dp, y = (-4).dp),
                    shape = CircleShape,
                    color = Color(0xFFB91C1C)
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) activeColor else idleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
