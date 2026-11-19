package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BibleBookSpec
import com.example.data.model.BibleBookmark
import com.example.data.model.BibleCatalog
import com.example.data.model.BibleNote
import com.example.data.model.BibleVerseItem
import com.example.util.BibleContentRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

internal object BibleParchmentPalette {
    val BackgroundTop = Color(0xFFFAF3E6)
    val BackgroundBottom = Color(0xFFEEDFC3)
    val TopBarSurface = Color(0xFFF3E7D0)
    val PillSurface = Color(0xFFE9D8B8)
    val CardSurface = Color(0xFFFDF8EE)
    val CardSoft = Color(0xFFF5EAD4)
    val Border = Color(0xFFDFCCA8)
    val InkPrimary = Color(0xFF331E12)
    val InkSecondary = Color(0xFF6E4E39)
    val InkMuted = Color(0xFF8B6F56)
    val MaroonAccent = Color(0xFF8C2D1E)
    val AmberAccent = Color(0xFFD97706)
    val HighlightGold = Color(0xFFFFF3C4)
    val HighlightAmber = Color(0xFFFFE4BA)
    val HighlightSage = Color(0xFFE2F2E3)
    val HighlightSky = Color(0xFFE0EEFA)
}

internal fun resolveHighlightBg(code: String): Color = when (code.uppercase()) {
    "AMBER" -> BibleParchmentPalette.HighlightAmber
    "SAGE" -> BibleParchmentPalette.HighlightSage
    "BLUE" -> BibleParchmentPalette.HighlightSky
    else -> BibleParchmentPalette.HighlightGold
}

@Composable
internal fun BibleDevotionalTabContent(
    isEnglish: Boolean,
    fontSizeSp: Int,
    useSerif: Boolean,
    onOpenPassage: (String, Int) -> Unit
) {
    val fontFamily = if (useSerif) FontFamily.Serif else FontFamily.SansSerif
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (isEnglish) "Daily Devotionals" else "Renungan Harian & Penguatan Iman",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BibleParchmentPalette.InkPrimary,
                fontFamily = fontFamily
            )
            Text(
                text = if (isEnglish) {
                    "Short Scripture reflections for guardians and boarding mentors."
                } else {
                    "Renungan singkat Alkitab untuk penguatan pelayanan Wali Asuh & Admin."
                },
                fontSize = 12.sp,
                color = BibleParchmentPalette.InkSecondary
            )
        }
        items(BibleContentRepository.DEVOTIONALS, key = { it.id }) { item ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bible_devotional_card_${item.id}"),
                shape = RoundedCornerShape(18.dp),
                color = BibleParchmentPalette.CardSurface,
                border = BorderStroke(1.dp, BibleParchmentPalette.Border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = BibleParchmentPalette.PillSurface
                    ) {
                        Text(
                            text = (if (isEnglish) item.themeEn else item.themeId).uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BibleParchmentPalette.MaroonAccent,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isEnglish) item.titleEn else item.titleId,
                        fontSize = (fontSizeSp - 2).coerceIn(16, 22).sp,
                        fontWeight = FontWeight.Bold,
                        color = BibleParchmentPalette.InkPrimary,
                        fontFamily = fontFamily
                    )
                    Spacer(Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BibleParchmentPalette.CardSoft,
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Text(
                            text = if (isEnglish) item.keyVerseEn else item.keyVerseId,
                            fontSize = (fontSizeSp - 5).coerceIn(13, 18).sp,
                            fontFamily = FontFamily.Serif,
                            color = BibleParchmentPalette.MaroonAccent,
                            lineHeight = 22.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isEnglish) item.reflectionEn else item.reflectionId,
                        fontSize = (fontSizeSp - 5).coerceIn(14, 19).sp,
                        lineHeight = 23.sp,
                        color = BibleParchmentPalette.InkPrimary,
                        fontFamily = fontFamily
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = if (isEnglish) "Prayer: ${item.prayerEn}" else "Doa: ${item.prayerId}",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = BibleParchmentPalette.InkSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onOpenPassage(item.bookId, item.chapter) },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BibleParchmentPalette.AmberAccent),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = BibleParchmentPalette.MaroonAccent),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = if (isEnglish) "Read Chapter in Bible" else "Buka Pasal di Alkitab",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BibleStoriesTabContent(
    isEnglish: Boolean,
    fontSizeSp: Int,
    useSerif: Boolean,
    onOpenPassage: (String, Int) -> Unit
) {
    val fontFamily = if (useSerif) FontFamily.Serif else FontFamily.SansSerif
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = if (isEnglish) "Inspiring Bible Stories" else "Kisah Teladan Alkitab",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BibleParchmentPalette.InkPrimary,
                fontFamily = fontFamily
            )
            Text(
                text = if (isEnglish) {
                    "Key biblical narratives with faith lessons for character building."
                } else {
                    "Ringkasan kisah Alkitab beserta pesan iman untuk pembinaan karakter."
                },
                fontSize = 12.sp,
                color = BibleParchmentPalette.InkSecondary
            )
        }
        items(BibleContentRepository.STORIES, key = { it.id }) { story ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("bible_story_card_${story.id}"),
                shape = RoundedCornerShape(18.dp),
                color = BibleParchmentPalette.CardSurface,
                border = BorderStroke(1.dp, BibleParchmentPalette.Border)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = (if (isEnglish) story.eraEn else story.eraId).uppercase(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BibleParchmentPalette.AmberAccent
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = if (isEnglish) story.titleEn else story.titleId,
                        fontSize = (fontSizeSp - 2).coerceIn(16, 22).sp,
                        fontWeight = FontWeight.Bold,
                        color = BibleParchmentPalette.InkPrimary,
                        fontFamily = fontFamily
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = if (isEnglish) story.summaryEn else story.summaryId,
                        fontSize = (fontSizeSp - 5).coerceIn(14, 19).sp,
                        lineHeight = 23.sp,
                        color = BibleParchmentPalette.InkPrimary,
                        fontFamily = fontFamily
                    )
                    Spacer(Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = BibleParchmentPalette.HighlightGold
                    ) {
                        Text(
                            text = if (isEnglish) {
                                "Faith Lesson: ${story.lessonEn}"
                            } else {
                                "Nilai Iman: ${story.lessonId}"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = BibleParchmentPalette.InkPrimary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onOpenPassage(story.bookId, story.chapter) },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BibleParchmentPalette.MaroonAccent,
                            contentColor = Color.White
                        ),
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text(
                            text = if (isEnglish) "Read Scripture Chapter" else "Baca Pasal Terkait",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BibleNotesTabContent(
    notes: List<BibleNote>,
    isEnglish: Boolean,
    onCreateNote: () -> Unit,
    onEditNote: (BibleNote) -> Unit,
    onDeleteNote: (BibleNote) -> Unit,
    onOpenPassage: (String, Int) -> Unit
) {
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEnglish) "Personal Notes" else "Catatan Pribadi",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BibleParchmentPalette.InkPrimary
                    )
                    Text(
                        text = if (isEnglish) {
                            "Synced privately to your account"
                        } else {
                            "Tersinkron otomatis & pribadi per akun"
                        },
                        fontSize = 12.sp,
                        color = BibleParchmentPalette.InkSecondary
                    )
                }
                Button(
                    onClick = onCreateNote,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BibleParchmentPalette.MaroonAccent,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(44.dp)
                        .testTag("bible_add_note_button")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(if (isEnglish) "New Note" else "Catatan Baru", fontSize = 13.sp)
                }
            }
        }
        if (notes.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = BibleParchmentPalette.CardSurface,
                    border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.EditNote,
                            contentDescription = null,
                            tint = BibleParchmentPalette.AmberAccent,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isEnglish) "No notes yet" else "Belum ada catatan pribadi",
                            fontWeight = FontWeight.Bold,
                            color = BibleParchmentPalette.InkPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) {
                                "Tap any verse while reading or tap 'New Note' to save your personal reflection."
                            } else {
                                "Sentuh ayat saat membaca atau tekan 'Catatan Baru' untuk menyimpan renungan pribadi Anda."
                            },
                            fontSize = 12.sp,
                            color = BibleParchmentPalette.InkSecondary
                        )
                    }
                }
            }
        } else {
            items(notes, key = { it.id }) { note ->
                val ref = BibleCatalog.formatReference(
                    note.bookId,
                    note.chapter,
                    note.verse.takeIf { it > 0 },
                    isEnglish
                )
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bible_note_card_${note.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = BibleParchmentPalette.CardSurface,
                    border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = BibleParchmentPalette.PillSurface,
                                    modifier = Modifier.clickable {
                                        onOpenPassage(note.bookId, note.chapter)
                                    }
                                ) {
                                    Text(
                                        text = ref,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BibleParchmentPalette.MaroonAccent,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(999.dp),
                                    color = BibleParchmentPalette.HighlightGold
                                ) {
                                    Text(
                                        text = note.categoryTag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BibleParchmentPalette.AmberAccent,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Row {
                                IconButton(
                                    onClick = { onEditNote(note) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = if (isEnglish) "Edit note" else "Edit catatan",
                                        tint = BibleParchmentPalette.InkSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onDeleteNote(note) },
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.DeleteOutline,
                                        contentDescription = if (isEnglish) "Delete note" else "Hapus catatan",
                                        tint = BibleParchmentPalette.MaroonAccent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                        if (note.title.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = note.title,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BibleParchmentPalette.InkPrimary
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = note.content,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = BibleParchmentPalette.InkPrimary
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = dateFmt.format(Date(note.updatedAt)),
                            fontSize = 11.sp,
                            color = BibleParchmentPalette.InkMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BibleBookmarksTabContent(
    bookmarks: List<BibleBookmark>,
    isEnglish: Boolean,
    onDeleteBookmark: (BibleBookmark) -> Unit,
    onOpenPassage: (String, Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isEnglish) "Bookmarked Verses" else "Penanda & Sorotan Ayat",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BibleParchmentPalette.InkPrimary
            )
            Text(
                text = if (isEnglish) {
                    "Tap a bookmark to jump straight to that chapter"
                } else {
                    "Sentuh penanda untuk langsung membuka pasalnya (tersinkron)"
                },
                fontSize = 12.sp,
                color = BibleParchmentPalette.InkSecondary
            )
        }
        if (bookmarks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    color = BibleParchmentPalette.CardSurface,
                    border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.BookmarkBorder,
                            contentDescription = null,
                            tint = BibleParchmentPalette.AmberAccent,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (isEnglish) "No bookmarks yet" else "Belum ada ayat yang ditandai",
                            fontWeight = FontWeight.Bold,
                            color = BibleParchmentPalette.InkPrimary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = if (isEnglish) {
                                "Tap any verse while reading Alkitab to bookmark and highlight it."
                            } else {
                                "Sentuh salah satu ayat saat membaca Alkitab untuk menandai & menyimpannya."
                            },
                            fontSize = 12.sp,
                            color = BibleParchmentPalette.InkSecondary
                        )
                    }
                }
            }
        } else {
            items(bookmarks, key = { it.id }) { bm ->
                val ref = BibleCatalog.formatReference(bm.bookId, bm.chapter, bm.verse, isEnglish)
                val preview = if (isEnglish) {
                    bm.verseTextEn.ifBlank { bm.verseTextId }
                } else {
                    bm.verseTextId.ifBlank { bm.verseTextEn }
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPassage(bm.bookId, bm.chapter) }
                        .testTag("bible_bookmark_card_${bm.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = resolveHighlightBg(bm.highlightColor),
                    border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Bookmark,
                                    contentDescription = null,
                                    tint = BibleParchmentPalette.MaroonAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = ref,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BibleParchmentPalette.MaroonAccent
                                )
                            }
                            IconButton(
                                onClick = { onDeleteBookmark(bm) },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = if (isEnglish) "Remove bookmark" else "Hapus bookmark",
                                    tint = BibleParchmentPalette.InkSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        if (preview.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = preview,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 21.sp,
                                color = BibleParchmentPalette.InkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BibleSearchTabContent(
    isEnglish: Boolean,
    initialQuery: String,
    onOpenBookChapter: (BibleBookSpec, Int) -> Unit
) {
    val context = LocalContext.current
    var query by remember(initialQuery) { mutableStateOf(initialQuery) }

    val matchingBooks = remember(query) {
        val q = query.trim().lowercase()
        if (q.length < 2) emptyList()
        else BibleCatalog.ALL_BOOKS.filter {
            it.nameId.lowercase().contains(q) ||
                it.nameEn.lowercase().contains(q) ||
                it.id.lowercase().contains(q)
        }.take(6)
    }

    val verseHits = remember(query, isEnglish) {
        BibleContentRepository.searchVerses(context, query, isEnglish)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = if (isEnglish) "Search Scripture & Books" else "Cari Ayat & Kitab Alkitab",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = BibleParchmentPalette.InkPrimary
            )
            Spacer(Modifier.height(8.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = BibleParchmentPalette.CardSurface,
                border = BorderStroke(1.dp, BibleParchmentPalette.Border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        tint = BibleParchmentPalette.InkSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        if (query.isEmpty()) {
                            Text(
                                text = if (isEnglish) {
                                    "Search word (e.g., peace, love, shepherd, Yohanes)"
                                } else {
                                    "Ketik kata kunci (cth: damai, kasih, gembala, Mazmur)"
                                },
                                color = BibleParchmentPalette.InkMuted,
                                fontSize = 13.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = BibleParchmentPalette.InkPrimary,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("bible_search_verse_input")
                        )
                    }
                    if (query.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = if (isEnglish) "Clear" else "Hapus",
                            tint = BibleParchmentPalette.InkSecondary,
                            modifier = Modifier
                                .size(20.dp)
                                .clickable { query = "" }
                        )
                    }
                }
            }
        }

        if (query.trim().length < 2) {
            item {
                val quickKeywords = if (isEnglish) {
                    listOf("peace", "love", "shepherd", "hope", "light", "faith")
                } else {
                    listOf("damai", "kasih", "gembala", "terang", "iman", "pengharapan")
                }
                Text(
                    text = if (isEnglish) "Quick Topic Keywords" else "Kata Kunci Populer",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BibleParchmentPalette.InkSecondary
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickKeywords.take(3).forEach { kw ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = BibleParchmentPalette.PillSurface,
                            modifier = Modifier.clickable { query = kw }
                        ) {
                            Text(
                                text = kw,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BibleParchmentPalette.MaroonAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickKeywords.drop(3).forEach { kw ->
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = BibleParchmentPalette.PillSurface,
                            modifier = Modifier.clickable { query = kw }
                        ) {
                            Text(
                                text = kw,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BibleParchmentPalette.MaroonAccent,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        } else {
            if (matchingBooks.isNotEmpty()) {
                item {
                    Text(
                        text = if (isEnglish) "Matching Books" else "Kitab yang Cocok",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BibleParchmentPalette.InkSecondary
                    )
                }
                items(matchingBooks, key = { "bk_${it.id}" }) { book ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenBookChapter(book, 1) },
                        shape = RoundedCornerShape(14.dp),
                        color = BibleParchmentPalette.CardSoft,
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isEnglish) book.nameEn else book.nameId,
                                fontWeight = FontWeight.Bold,
                                color = BibleParchmentPalette.MaroonAccent
                            )
                            Text(
                                text = if (isEnglish) "${book.chaptersCount} Ch" else "${book.chaptersCount} Pasal",
                                fontSize = 12.sp,
                                color = BibleParchmentPalette.InkSecondary
                            )
                        }
                    }
                }
            }

            if (verseHits.isEmpty() && matchingBooks.isEmpty()) {
                item {
                    Text(
                        text = if (isEnglish) {
                            "No matching verses found in offline/cached chapters."
                        } else {
                            "Belum ditemukan ayat yang cocok pada koleksi offline/cache."
                        },
                        fontSize = 13.sp,
                        color = BibleParchmentPalette.InkSecondary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            } else {
                items(verseHits, key = { "${it.book.id}_${it.chapter}_${it.verse}" }) { hit ->
                    val ref = BibleCatalog.formatReference(hit.book.id, hit.chapter, hit.verse, isEnglish)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenBookChapter(hit.book, hit.chapter) },
                        shape = RoundedCornerShape(16.dp),
                        color = BibleParchmentPalette.CardSurface,
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = ref,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BibleParchmentPalette.MaroonAccent
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (isEnglish) hit.textEn else hit.textId,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 21.sp,
                                color = BibleParchmentPalette.InkPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun BibleVerseActionDialog(
    book: BibleBookSpec,
    chapter: Int,
    verse: BibleVerseItem,
    isBookmarked: Boolean,
    isEnglish: Boolean,
    onToggleBookmark: (String) -> Unit,
    onAddNote: () -> Unit,
    onSpeakFromVerse: () -> Unit,
    onCopyVerse: () -> Unit,
    onDismiss: () -> Unit
) {
    val ref = BibleCatalog.formatReference(book.id, chapter, verse.verse, isEnglish)
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BibleParchmentPalette.CardSurface,
        title = {
            Text(
                text = ref,
                fontWeight = FontWeight.Bold,
                color = BibleParchmentPalette.MaroonAccent
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = if (isEnglish) verse.textEn else verse.textId,
                    fontFamily = FontFamily.Serif,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = BibleParchmentPalette.InkPrimary,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                HorizontalDivider(color = BibleParchmentPalette.Border)
                Text(
                    text = if (isEnglish) "Highlight & Bookmark Color:" else "Pilih Warna Sorotan & Bookmark:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BibleParchmentPalette.InkSecondary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        "GOLD" to BibleParchmentPalette.HighlightGold,
                        "AMBER" to BibleParchmentPalette.HighlightAmber,
                        "SAGE" to BibleParchmentPalette.HighlightSage,
                        "BLUE" to BibleParchmentPalette.HighlightSky
                    ).forEach { (code, color) ->
                        Surface(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { onToggleBookmark(code) },
                            shape = CircleShape,
                            color = color,
                            border = BorderStroke(1.5.dp, BibleParchmentPalette.AmberAccent)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                                    contentDescription = code,
                                    tint = BibleParchmentPalette.MaroonAccent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onAddNote,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isEnglish) "Note" else "Catatan", fontSize = 12.sp)
                    }
                    OutlinedButton(
                        onClick = onSpeakFromVerse,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                    ) {
                        Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isEnglish) "Audio" else "Dengar", fontSize = 12.sp)
                    }
                }
                OutlinedButton(
                    onClick = onCopyVerse,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, BibleParchmentPalette.Border)
                ) {
                    Icon(Icons.Rounded.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (isEnglish) "Copy Verse Text" else "Salin Teks Ayat", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEnglish) "Close" else "Tutup", color = BibleParchmentPalette.MaroonAccent)
            }
        }
    )
}

@Composable
internal fun BibleNoteEditorDialog(
    initialNote: BibleNote?,
    defaultBook: BibleBookSpec,
    defaultChapter: Int,
    defaultVerse: Int,
    isEnglish: Boolean,
    onSave: (title: String, content: String, tag: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember(initialNote?.id) {
        mutableStateOf(
            initialNote?.title ?: BibleCatalog.formatReference(
                defaultBook.id,
                defaultChapter,
                defaultVerse.takeIf { it > 0 },
                isEnglish
            )
        )
    }
    var content by remember(initialNote?.id) { mutableStateOf(initialNote?.content.orEmpty()) }
    var selectedTag by remember(initialNote?.id) { mutableStateOf(initialNote?.categoryTag ?: "RENUNGAN") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = BibleParchmentPalette.CardSurface,
        title = {
            Text(
                text = if (initialNote == null) {
                    if (isEnglish) "New Bible Note" else "Catatan Pribadi Baru"
                } else {
                    if (isEnglish) "Edit Bible Note" else "Ubah Catatan Pribadi"
                },
                fontWeight = FontWeight.Bold,
                color = BibleParchmentPalette.InkPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("RENUNGAN", "DOA", "KHOTBAH", "BIMBINGAN").forEach { tag ->
                        val active = selectedTag == tag
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (active) BibleParchmentPalette.MaroonAccent else BibleParchmentPalette.PillSurface,
                            modifier = Modifier.clickable { selectedTag = tag }
                        ) {
                            Text(
                                text = tag,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (active) Color.White else BibleParchmentPalette.InkSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(if (isEnglish) "Title / Reference" else "Judul / Referensi Ayat") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BibleParchmentPalette.MaroonAccent,
                        unfocusedBorderColor = BibleParchmentPalette.Border
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text(if (isEnglish) "Personal Reflection" else "Isi Catatan / Renungan") },
                    minLines = 4,
                    maxLines = 7,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BibleParchmentPalette.MaroonAccent,
                        unfocusedBorderColor = BibleParchmentPalette.Border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("bible_note_content_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, content, selectedTag) },
                enabled = content.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BibleParchmentPalette.MaroonAccent,
                    contentColor = Color.White
                ),
                modifier = Modifier.testTag("bible_note_save_button")
            ) {
                Text(if (isEnglish) "Save" else "Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isEnglish) "Cancel" else "Batal", color = BibleParchmentPalette.InkSecondary)
            }
        }
    )
}
