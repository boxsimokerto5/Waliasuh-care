package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranBookmark
import com.example.data.model.QuranCatalog
import com.example.data.model.QuranLastRead
import com.example.data.model.QuranNote
import com.example.data.model.QuranSurahSpec
import com.example.util.QuranContentRepository
import com.example.util.QuranOfflineData
import com.example.util.QuranSearchHit

@Composable
internal fun QuranSurahIndexTabContent(
    lastRead: QuranLastRead,
    palette: QuranPalette,
    arabicFontFamily: FontFamily,
    isEnglish: Boolean,
    onSelectSurah: (surahNumber: Int, targetAyah: Int) -> Unit
) {
    var filterMode by remember { mutableStateOf("ALL") } // ALL | JUZ30 | FEATURED
    var searchQuery by remember { mutableStateOf("") }

    val filteredSurahs = remember(filterMode, searchQuery) {
        val q = searchQuery.trim().lowercase()
        QuranCatalog.ALL_SURAHS.filter { s ->
            val modeOk = when (filterMode) {
                "JUZ30" -> s.number >= 78
                "FEATURED" -> s.number in QuranCatalog.FEATURED_SURAH_NUMBERS
                else -> true
            }
            val queryOk = q.isBlank() ||
                s.nameLatin.lowercase().contains(q) ||
                s.meaningId.lowercase().contains(q) ||
                s.meaningEn.lowercase().contains(q) ||
                s.number.toString() == q
            modeOk && queryOk
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_surah_index_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Kartu Terakhir Baca
        item(key = "quran_last_read_card") {
            val lastSpec = remember(lastRead.surahNumber) { QuranCatalog.findSurah(lastRead.surahNumber) }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSurah(lastSpec.number, lastRead.ayahNumber) }
                    .testTag("quran_last_read_banner"),
                shape = RoundedCornerShape(16.dp),
                color = palette.cardSoft,
                border = BorderStroke(1.2.dp, palette.goldPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEnglish) "CONTINUE RECITING" else "TERAKHIR DIBACA",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.goldDeep,
                            letterSpacing = 0.7.sp
                        )
                        Text(
                            text = "${lastSpec.number}. ${lastSpec.nameLatin} • ${if (isEnglish) "Ayah" else "Ayat"} ${lastRead.ayahNumber}",
                            fontSize = 14.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.inkArabic
                        )
                        Text(
                            text = if (isEnglish) "Tap to resume inside the Mushaf frame" else "Ketuk untuk lanjut membaca di bingkai Mushaf",
                            fontSize = 11.sp,
                            color = palette.inkSecondary
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = lastSpec.nameArabic,
                        fontFamily = arabicFontFamily,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldDeep
                    )
                }
            }
        }

        // Kolom Cari Surah
        item(key = "quran_surah_search_box") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isEnglish) "Search surah name or number (e.g., Yasin, 67)..." else "Cari nama / nomor surah (misal: Yasin, 67)...",
                        fontSize = 12.5.sp,
                        color = palette.inkMuted
                    )
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.goldPrimary)
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = palette.cardSurface,
                    unfocusedContainerColor = palette.cardSurface,
                    focusedBorderColor = palette.goldPrimary,
                    unfocusedBorderColor = palette.border,
                    focusedTextColor = palette.inkArabic,
                    unfocusedTextColor = palette.inkArabic
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_surah_search_input")
            )
        }

        // Filter Chips
        item(key = "quran_surah_filters") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "ALL" to (if (isEnglish) "All 114 Surahs" else "Semua 114 Surah"),
                    "JUZ30" to "Juz 'Amma (30)",
                    "FEATURED" to (if (isEnglish) "Daily Surahs" else "Surah Pilihan")
                ).forEach { (key, label) ->
                    val selected = filterMode == key
                    Surface(
                        onClick = { filterMode = key },
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) palette.goldPrimary else palette.cardSurface,
                        border = BorderStroke(1.dp, if (selected) palette.goldDeep else palette.border)
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else palette.inkSecondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        items(filteredSurahs, key = { "surah_idx_${it.number}" }) { spec ->
            val hasBuiltInOffline = QuranOfflineData.hasOfflineSurah(spec.number)
            val meaning = if (isEnglish) spec.meaningEn else spec.meaningId
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSurah(spec.number, 1) }
                    .testTag("quran_surah_row_${spec.number}"),
                shape = RoundedCornerShape(14.dp),
                color = palette.cardSurface,
                border = BorderStroke(1.dp, palette.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ArabicAyahRosetteBadge(
                        ayahNumber = spec.number,
                        isPlaying = false,
                        isBookmarked = false,
                        palette = palette,
                        onClick = { onSelectSurah(spec.number, 1) }
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = spec.nameLatin,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.inkArabic
                            )
                            if (hasBuiltInOffline) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = palette.emeraldAccent.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = "Offline",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = palette.emeraldAccent,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "$meaning • ${spec.revelationType} • ${spec.versesCount} ${if (isEnglish) "Ayahs" else "Ayat"}",
                            fontSize = 11.sp,
                            color = palette.inkSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = spec.nameArabic,
                        fontFamily = arabicFontFamily,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        color = palette.goldDeep
                    )
                }
            }
        }
    }
}

@Composable
internal fun QuranBookmarksTabContent(
    bookmarks: List<QuranBookmark>,
    palette: QuranPalette,
    arabicFontFamily: FontFamily,
    isEnglish: Boolean,
    onDeleteBookmark: (QuranBookmark) -> Unit,
    onOpenBookmark: (surahNumber: Int, ayahNumber: Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_bookmarks_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (bookmarks.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Bookmark,
                            contentDescription = null,
                            tint = palette.goldPrimary,
                            modifier = Modifier.size(34.dp)
                        )
                        Text(
                            text = if (isEnglish) "No Bookmarked Ayahs Yet" else "Belum Ada Ayat Ditandai",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkArabic
                        )
                        Text(
                            text = if (isEnglish) {
                                "Tap any ayah or its golden rosette badge while reading to save bookmarks."
                            } else {
                                "Ketuk ayat atau medali nomor ayat saat membaca untuk menyimpan penanda ayat favorit."
                            },
                            fontSize = 12.sp,
                            color = palette.inkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(bookmarks, key = { "qbm_${it.id}" }) { bm ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenBookmark(bm.surahNumber, bm.ayahNumber) }
                        .testTag("quran_bookmark_card_${bm.id}"),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = palette.cardSoft
                            ) {
                                Text(
                                    text = "QS. ${bm.surahNameLatin} (${bm.surahNumber}) : ${bm.ayahNumber}",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette.goldDeep,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteBookmark(bm) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = if (isEnglish) "Delete Bookmark" else "Hapus Bookmark",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                        if (bm.arabicPreview.isNotBlank()) {
                            Text(
                                text = bm.arabicPreview,
                                fontFamily = arabicFontFamily,
                                fontSize = 21.sp,
                                lineHeight = 36.sp,
                                color = palette.inkArabic,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (bm.translationId.isNotBlank()) {
                            Text(
                                text = bm.translationId,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = palette.inkSecondary,
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun QuranNotesTabContent(
    notes: List<QuranNote>,
    palette: QuranPalette,
    isEnglish: Boolean,
    onCreateNote: () -> Unit,
    onEditNote: (QuranNote) -> Unit,
    onDeleteNote: (QuranNote) -> Unit,
    onOpenPassage: (surahNumber: Int, ayahNumber: Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_notes_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Button(
                onClick = onCreateNote,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.goldPrimary, contentColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("btn_quran_add_note")
            ) {
                Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(
                    text = if (isEnglish) "Add Memorization / Tajwid Note" else "Tambah Catatan Hafalan / Tajwid",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (notes.isEmpty()) {
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(Icons.Rounded.Description, contentDescription = null, tint = palette.goldPrimary, modifier = Modifier.size(34.dp))
                        Text(
                            text = if (isEnglish) "No Memorization Notes Yet" else "Belum Ada Catatan Hafalan",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = palette.inkArabic
                        )
                        Text(
                            text = if (isEnglish) {
                                "Record target surahs, tajwid reminders, or reflection notes."
                            } else {
                                "Catat target muroja'ah, hukum tajwid penting, atau tadabbur ayat di sini."
                            },
                            fontSize = 12.sp,
                            color = palette.inkSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(notes, key = { "qnote_${it.id}" }) { note ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPassage(note.surahNumber, note.ayahNumber) },
                    shape = RoundedCornerShape(16.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Column(
                        modifier = Modifier.padding(13.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(shape = RoundedCornerShape(6.dp), color = palette.cardSoft) {
                                    Text(
                                        text = note.categoryTag,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.goldDeep,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                                Text(
                                    text = "${note.surahNameLatin} : ${note.ayahNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.inkMuted
                                )
                            }
                            Row {
                                IconButton(onClick = { onEditNote(note) }, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Rounded.Edit, contentDescription = null, tint = palette.goldPrimary, modifier = Modifier.size(18.dp))
                                }
                                IconButton(onClick = { onDeleteNote(note) }, modifier = Modifier.size(34.dp)) {
                                    Icon(Icons.Rounded.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                        Text(
                            text = note.title,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.inkArabic
                        )
                        if (note.content.isNotBlank()) {
                            Text(
                                text = note.content,
                                fontSize = 12.sp,
                                lineHeight = 17.sp,
                                color = palette.inkSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun QuranDuaAndToolsTabContent(
    palette: QuranPalette,
    arabicFontFamily: FontFamily,
    isEnglish: Boolean,
    isDownloadingOffline: Boolean,
    downloadProgress: Pair<Int, Int>,
    downloadStatusLabel: String,
    onStartOfflineDownload: () -> Unit,
    onOpenSurahAyah: (surahNumber: Int, ayahNumber: Int) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var searchHits by remember { mutableStateOf<List<QuranSearchHit>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    LaunchedEffect(searchQuery, isEnglish) {
        if (searchQuery.trim().length < 2) {
            searchHits = emptyList()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        searchHits = QuranContentRepository.searchVersesAndSurahs(context, searchQuery, isEnglish)
        isSearching = false
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_dua_tools_list"),
        contentPadding = PaddingValues(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Pencarian Ayat Global
        item(key = "quran_global_search_box") {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isEnglish) "Search ayah keywords or surah..." else "Cari kata terjemahan ayat atau surah...",
                        fontSize = 12.5.sp,
                        color = palette.inkMuted
                    )
                },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = palette.goldPrimary) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = palette.cardSurface,
                    unfocusedContainerColor = palette.cardSurface,
                    focusedBorderColor = palette.goldPrimary,
                    unfocusedBorderColor = palette.border,
                    focusedTextColor = palette.inkArabic,
                    unfocusedTextColor = palette.inkArabic
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quran_global_search_input")
            )
        }

        if (isSearching) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = palette.goldPrimary, strokeWidth = 2.dp)
                    Spacer(Modifier.width(8.dp))
                    Text(if (isEnglish) "Searching..." else "Mencari ayat...", fontSize = 12.sp, color = palette.inkSecondary)
                }
            }
        } else if (searchHits.isNotEmpty()) {
            items(searchHits, key = { "qhit_${it.surah.number}_${it.ayahNumber}_${it.snippet.hashCode()}" }) { hit ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSurahAyah(hit.surah.number, hit.ayahNumber) },
                    shape = RoundedCornerShape(12.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.55f))
                ) {
                    Column(modifier = Modifier.padding(11.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "${hit.surah.nameLatin} (${hit.surah.number}) : ${hit.ayahNumber}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.goldDeep
                        )
                        Text(
                            text = hit.arabicPreview,
                            fontFamily = arabicFontFamily,
                            fontSize = 18.sp,
                            color = palette.inkArabic,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = hit.snippet,
                            fontSize = 11.5.sp,
                            color = palette.inkSecondary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Paket Offline Juz Amma & Surah Pilihan
        item(key = "quran_offline_pack_card") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = palette.cardSoft,
                border = BorderStroke(1.dp, palette.goldPrimary)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.CloudDownload, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(20.dp))
                        Text(
                            text = if (isEnglish) "Offline Mushaf Pack (Juz 'Amma + Key Surahs)" else "Paket Baca Offline (Juz 'Amma + Surah Pilihan)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = palette.inkArabic
                        )
                    }
                    Text(
                        text = if (isEnglish) {
                            "Al-Fatihah & Surahs 93-114 are always built-in. Tap below to cache all of Juz 30 + Al-Kahfi, Yasin, Ar-Rahman & Al-Mulk."
                        } else {
                            "Al-Fatihah & Surah 93–114 sudah tersedia bawaan. Ketuk tombol untuk menyimpan seluruh Juz 30 + Al-Kahfi, Yasin, Ar-Rahman & Al-Mulk agar siap dibaca tanpa sinyal."
                        },
                        fontSize = 11.5.sp,
                        lineHeight = 16.sp,
                        color = palette.inkSecondary
                    )
                    if (isDownloadingOffline) {
                        val (done, total) = downloadProgress
                        LinearProgressIndicator(
                            progress = { if (total > 0) done.toFloat() / total.toFloat() else 0f },
                            modifier = Modifier.fillMaxWidth(),
                            color = palette.goldPrimary,
                            trackColor = palette.cardSurface
                        )
                        Text(downloadStatusLabel, fontSize = 11.sp, color = palette.goldDeep, fontWeight = FontWeight.Bold)
                    } else {
                        OutlinedButton(
                            onClick = onStartOfflineDownload,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, palette.goldPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 44.dp)
                                .testTag("btn_quran_download_offline_pack")
                        ) {
                            Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (downloadStatusLabel.isNotBlank()) downloadStatusLabel
                                else if (isEnglish) "Download & Save Juz 'Amma Offline" else "Simpan Juz 'Amma & Surah Pilihan Offline",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = palette.goldDeep
                            )
                        }
                    }
                }
            }
        }

        // Doa Mengaji & Adab Tilawah
        item(key = "quran_duas_header") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = palette.goldPrimary, modifier = Modifier.size(18.dp))
                Text(
                    text = if (isEnglish) "Prayers Before & After Reciting" else "Doa Adab Sebelum & Sesudah Mengaji",
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.inkArabic
                )
            }
        }

        items(QuranCatalog.CURATED_DUAS, key = { it.id }) { dua ->
            MushafIlluminatedPageFrame(palette = palette) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEnglish) dua.titleEn else dua.titleId,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.goldDeep
                    )
                    Text(
                        text = if (isEnglish) dua.occasionEn else dua.occasionId,
                        fontSize = 11.sp,
                        color = palette.inkMuted
                    )
                    Text(
                        text = dua.arabicText,
                        fontFamily = arabicFontFamily,
                        fontSize = 23.sp,
                        lineHeight = 41.sp,
                        color = palette.inkArabic,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = dua.latinText,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = palette.goldDeep
                    )
                    Text(
                        text = if (isEnglish) dua.translationEn else dua.translationId,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = palette.inkSecondary
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Surface(
                            onClick = { onOpenSurahAyah(dua.linkedSurah, dua.linkedAyah) },
                            shape = RoundedCornerShape(999.dp),
                            color = palette.cardSoft,
                            border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Rounded.MenuBook, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(14.dp))
                                Text(
                                    text = if (isEnglish) "Open Surah ${dua.linkedSurah}" else "Buka Surah ${dua.linkedSurah}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.goldDeep
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
