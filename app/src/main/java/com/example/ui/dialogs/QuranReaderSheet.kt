package com.example.ui.dialogs

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LibraryBooks
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SkipNext
import androidx.compose.material.icons.rounded.SkipPrevious
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.R
import com.example.data.model.QuranArabicFontChoice
import com.example.data.model.QuranAyahItem
import com.example.data.model.QuranCatalog
import com.example.data.model.QuranNote
import com.example.data.model.QuranReaderTheme
import com.example.data.model.QuranSurahSpec
import com.example.util.QuranContentRepository
import com.example.util.QuranSurahLoadResult
import com.example.util.QuranUserPrefsStore
import kotlinx.coroutines.launch

private enum class QuranBottomTab {
    MUSHAF,
    SURAH,
    BOOKMARK,
    CATATAN,
    DOA_CARI
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranReaderSheet(
    initialEnglish: Boolean = false,
    onRecordToMengaji: ((surah: QuranSurahSpec, ayahNumber: Int) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var prefsState by remember { mutableStateOf(QuranUserPrefsStore.load(context)) }
    val palette = remember(prefsState.theme) { resolveQuranPalette(prefsState.theme) }
    val arabicFontFamily = remember(prefsState.fontChoice) { resolveArabicFontFamily(prefsState.fontChoice) }
    val activeQari = remember(prefsState.selectedQariId) { QuranCatalog.findQari(prefsState.selectedQariId) }

    var activeTab by remember { mutableStateOf(QuranBottomTab.MUSHAF) }
    var activeSurahNumber by remember { mutableIntStateOf(prefsState.lastRead.surahNumber.coerceIn(1, 114)) }
    var targetScrollAyah by remember { mutableIntStateOf(prefsState.lastRead.ayahNumber.coerceAtLeast(1)) }
    val activeSurah = remember(activeSurahNumber) { QuranCatalog.findSurah(activeSurahNumber) }

    var showSettingsStrip by remember { mutableStateOf(false) }
    var reloadNonce by remember { mutableIntStateOf(0) }
    var forceRefreshNext by remember { mutableStateOf(false) }

    var surahResult by remember { mutableStateOf<QuranSurahLoadResult?>(null) }
    var isLoadingSurah by remember { mutableStateOf(true) }

    var selectedAyahForAction by remember { mutableStateOf<QuranAyahItem?>(null) }
    var showNoteEditor by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<QuranNote?>(null) }
    var noteAnchorAyah by remember { mutableIntStateOf(1) }

    var isDownloadingOffline by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0 to 0) }
    var downloadStatusLabel by remember { mutableStateOf("") }

    // Audio Murottal state (ExoPlayer guarded cleanly for Robolectric)
    val isRobolectric = remember { Build.FINGERPRINT.equals("robolectric", ignoreCase = true) }
    val exoPlayer = remember(context) {
        if (isRobolectric) null else runCatching { ExoPlayer.Builder(context).build() }.getOrNull()
    }
    var playingAyahNumber by remember { mutableIntStateOf(0) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var continuousAutoNext by remember { mutableStateOf(true) }
    var activeAudioPrimary by remember { mutableStateOf("") }
    var activeAudioFallback by remember { mutableStateOf("") }
    var triedFallbackAudio by remember { mutableStateOf(false) }

    val mushafListState = rememberLazyListState()

    fun stopMurottal() {
        runCatching { exoPlayer?.stop() }
        playingAyahNumber = 0
        isAudioPlaying = false
    }

    fun playAyahAudio(ayahNum: Int) {
        val loadedAyahs = surahResult?.ayahs.orEmpty()
        val item = loadedAyahs.firstOrNull { it.numberInSurah == ayahNum }
        val primary = item?.audioUrlPrimary?.takeIf { it.isNotBlank() }
            ?: QuranContentRepository.buildAyahAudioUrl(activeSurahNumber, ayahNum, activeQari)
        val fallback = item?.audioUrlFallback?.takeIf { it.isNotBlank() }
            ?: QuranContentRepository.buildEquranAyahAudioFallback(activeSurahNumber, ayahNum, activeQari)

        playingAyahNumber = ayahNum
        activeAudioPrimary = primary
        activeAudioFallback = fallback
        triedFallbackAudio = false

        if (exoPlayer == null) {
            isAudioPlaying = true
            Toast.makeText(
                context,
                "${activeQari.nameId} • ${activeSurah.nameLatin} Ayat $ayahNum",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        runCatching {
            exoPlayer.setMediaItem(MediaItem.fromUri(primary))
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }.onFailure {
            isAudioPlaying = false
        }
    }

    if (exoPlayer != null) {
        DisposableEffect(exoPlayer) {
            val listener = object : Player.Listener {
                override fun onIsPlayingChanged(playingNow: Boolean) {
                    isAudioPlaying = playingNow
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_ENDED && playingAyahNumber > 0) {
                        val nextAyah = playingAyahNumber + 1
                        val total = surahResult?.ayahs?.size ?: activeSurah.versesCount
                        if (continuousAutoNext && nextAyah <= total) {
                            playAyahAudio(nextAyah)
                        } else {
                            playingAyahNumber = 0
                            isAudioPlaying = false
                        }
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    if (!triedFallbackAudio && activeAudioFallback.isNotBlank()) {
                        triedFallbackAudio = true
                        runCatching {
                            exoPlayer.setMediaItem(MediaItem.fromUri(activeAudioFallback))
                            exoPlayer.prepare()
                            exoPlayer.playWhenReady = true
                        }
                    } else {
                        isAudioPlaying = false
                        Toast.makeText(
                            context,
                            if (initialEnglish) "Audio requires internet connection." else "Pemutaran audio memerlukan koneksi internet.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
            exoPlayer.addListener(listener)
            onDispose {
                exoPlayer.removeListener(listener)
                runCatching { exoPlayer.release() }
            }
        }
    }

    LaunchedEffect(activeSurahNumber, initialEnglish, reloadNonce) {
        val force = forceRefreshNext
        forceRefreshNext = false
        isLoadingSurah = true
        stopMurottal()
        surahResult = QuranContentRepository.loadSurah(
            context = context,
            surahNumber = activeSurahNumber,
            isEnglish = initialEnglish,
            forceRefresh = force
        )
        isLoadingSurah = false
        val updatedLast = QuranUserPrefsStore.saveLastRead(context, activeSurahNumber, targetScrollAyah.coerceIn(1, activeSurah.versesCount))
        prefsState = prefsState.copy(lastRead = updatedLast)
        val scrollIdx = if (targetScrollAyah <= 1) 0 else targetScrollAyah
        runCatching { mushafListState.scrollToItem(scrollIdx) }
    }

    LaunchedEffect(playingAyahNumber, activeTab) {
        if (playingAyahNumber > 0 && activeTab == QuranBottomTab.MUSHAF) {
            runCatching { mushafListState.animateScrollToItem(playingAyahNumber) }
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = {
            stopMurottal()
            onDismiss()
        },
        sheetState = sheetState,
        dragHandle = null,
        containerColor = palette.paperTop,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        BackHandler {
            if (activeTab != QuranBottomTab.MUSHAF) {
                activeTab = QuranBottomTab.MUSHAF
            } else {
                stopMurottal()
                onDismiss()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.95f)
                .background(Brush.verticalGradient(listOf(palette.paperTop, palette.paperBottom)))
                .navigationBarsPadding()
                .imePadding()
                .testTag("quran_reader_sheet")
        ) {
            // Drag Handle Pill Emas Halus
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(palette.topBarSurface)
                    .padding(top = 8.dp, bottom = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(42.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(palette.goldPrimary.copy(alpha = 0.5f))
                )
            }

            // HEADER BAR
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = palette.topBarSurface
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_menu_alquran),
                            contentDescription = "Al-Qur'an Digital",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .clickable { activeTab = QuranBottomTab.SURAH }
                        )

                        // Pill Pemilih Surah
                        Surface(
                            onClick = { activeTab = QuranBottomTab.SURAH },
                            shape = RoundedCornerShape(12.dp),
                            color = palette.cardSurface,
                            border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_quran_surah_picker")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${activeSurah.number}. ${activeSurah.nameLatin}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = palette.inkArabic,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${if (initialEnglish) activeSurah.meaningEn else activeSurah.meaningId} • ${activeSurah.versesCount} ${if (initialEnglish) "Ayat" else "Ayat"}",
                                        fontSize = 10.sp,
                                        color = palette.inkMuted,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = palette.goldDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Audio Play / Stop
                        Surface(
                            onClick = {
                                if (isAudioPlaying || playingAyahNumber > 0) {
                                    stopMurottal()
                                } else {
                                    playAyahAudio(1)
                                }
                            },
                            shape = CircleShape,
                            color = if (isAudioPlaying) palette.goldPrimary else palette.cardSurface,
                            border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_quran_audio_toggle")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isAudioPlaying) Icons.Rounded.Stop else Icons.AutoMirrored.Rounded.VolumeUp,
                                    contentDescription = if (initialEnglish) "Audio Recitation" else "Putar Murottal",
                                    tint = if (isAudioPlaying) Color.White else palette.goldDeep,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // Typography & Tema
                        Surface(
                            onClick = { showSettingsStrip = !showSettingsStrip },
                            shape = CircleShape,
                            color = if (showSettingsStrip) palette.goldPrimary else palette.cardSurface,
                            border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_quran_typography")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.FormatSize,
                                    contentDescription = if (initialEnglish) "Font & Theme" else "Atur Huruf & Tema",
                                    tint = if (showSettingsStrip) Color.White else palette.goldDeep,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }

                        // Tutup
                        Surface(
                            onClick = {
                                stopMurottal()
                                onDismiss()
                            },
                            shape = CircleShape,
                            color = palette.cardSurface,
                            border = BorderStroke(1.dp, palette.border),
                            modifier = Modifier
                                .size(38.dp)
                                .testTag("btn_quran_close")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = if (initialEnglish) "Close Al-Qur'an" else "Tutup Al-Qur'an",
                                    tint = palette.inkSecondary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Panel Pengaturan Cepat (Ukuran, Font Arab, Tema Kertas, Latin/Terjemahan, Qari)
                    AnimatedVisibility(visible = showSettingsStrip) {
                        QuranSettingsQuickStrip(
                            state = prefsState,
                            palette = palette,
                            isEnglish = initialEnglish,
                            onUpdateState = { next ->
                                prefsState = next
                                QuranUserPrefsStore.saveSettings(context, next)
                            }
                        )
                    }
                    HorizontalDivider(color = palette.goldPrimary.copy(alpha = 0.35f))
                }
            }

            // KONTEN UTAMA
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (activeTab) {
                    QuranBottomTab.MUSHAF -> {
                        val loaded = surahResult
                        val bookmarkedMap = remember(prefsState.bookmarks, activeSurahNumber) {
                            prefsState.bookmarks
                                .filter { it.surahNumber == activeSurahNumber }
                                .associateBy { it.ayahNumber }
                        }
                        if (isLoadingSurah && loaded == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    CircularProgressIndicator(color = palette.goldPrimary)
                                    Text(
                                        text = if (initialEnglish) "Opening Mushaf ${activeSurah.nameLatin}..." else "Membuka Mushaf ${activeSurah.nameLatin}...",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = palette.goldDeep
                                    )
                                }
                            }
                        } else {
                            val ayahs = loaded?.ayahs.orEmpty()
                            LazyColumn(
                                state = mushafListState,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .testTag("quran_mushaf_scroll"),
                                contentPadding = PaddingValues(12.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                item(key = "quran_mushaf_top") {
                                    MushafTopCalligraphyCap(palette = palette) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            SurahCalligraphyCartouche(
                                                surah = activeSurah,
                                                palette = palette,
                                                arabicFontFamily = arabicFontFamily,
                                                isEnglish = initialEnglish
                                            )

                                            if (activeSurah.number != 1 && activeSurah.number != 9) {
                                                BismillahOrnamentBanner(
                                                    palette = palette,
                                                    arabicFontFamily = arabicFontFamily
                                                )
                                            }

                                            // Status Sumber + Tombol Mode Mushaf Fokus / Belajar
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = if (initialEnglish) loaded?.sourceLabelEn.orEmpty() else loaded?.sourceLabelId.orEmpty(),
                                                    fontSize = 10.sp,
                                                    color = palette.inkMuted,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(Modifier.width(6.dp))
                                                Surface(
                                                    onClick = {
                                                        val next = prefsState.copy(isMushafFocusMode = !prefsState.isMushafFocusMode)
                                                        prefsState = next
                                                        QuranUserPrefsStore.saveSettings(context, next)
                                                    },
                                                    shape = RoundedCornerShape(999.dp),
                                                    color = if (prefsState.isMushafFocusMode) palette.goldPrimary else palette.cardSoft,
                                                    border = BorderStroke(0.8.dp, palette.goldPrimary.copy(alpha = 0.65f)),
                                                    modifier = Modifier.testTag("btn_quran_mushaf_focus_toggle")
                                                ) {
                                                    Text(
                                                        text = if (prefsState.isMushafFocusMode) {
                                                            if (initialEnglish) "Mushaf View" else "Mode Mushaf"
                                                        } else {
                                                            if (initialEnglish) "Study View" else "Mode Belajar"
                                                        },
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (prefsState.isMushafFocusMode) Color.White else palette.goldDeep,
                                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                                    )
                                                }
                                            }

                                            if (loaded?.isOfflinePlaceholder == true) {
                                                OutlinedButton(
                                                    onClick = {
                                                        forceRefreshNext = true
                                                        reloadNonce++
                                                    },
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, palette.goldPrimary),
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Icon(Icons.Rounded.Refresh, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(16.dp))
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = if (initialEnglish) "Reload Full Surah Online" else "Muat Surah Lengkap Online",
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = palette.goldDeep
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                itemsIndexed(
                                    items = ayahs,
                                    key = { _, ayah -> "ayah_${activeSurahNumber}_${ayah.numberInSurah}" }
                                ) { idx, ayah ->
                                    val isPlayingThis = playingAyahNumber == ayah.numberInSurah
                                    val isBookmarked = bookmarkedMap.containsKey(ayah.numberInSurah)
                                    MushafAyahSideFrame(palette = palette) {
                                        if (prefsState.isMushafFocusMode && loaded?.isOfflinePlaceholder != true) {
                                            Surface(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable { selectedAyahForAction = ayah }
                                                    .testTag("quran_ayah_mushaf_${ayah.numberInSurah}"),
                                                shape = RoundedCornerShape(12.dp),
                                                color = when {
                                                    isPlayingThis -> palette.playingBg
                                                    isBookmarked -> palette.goldGlow.copy(alpha = 0.25f)
                                                    else -> Color.Transparent
                                                }
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(horizontal = 6.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    ArabicAyahRosetteBadge(
                                                        ayahNumber = ayah.numberInSurah,
                                                        isPlaying = isPlayingThis,
                                                        isBookmarked = isBookmarked,
                                                        palette = palette,
                                                        onClick = { selectedAyahForAction = ayah }
                                                    )
                                                    Spacer(Modifier.width(10.dp))
                                                    Text(
                                                        text = "${ayah.arabicText} \u06DD${QuranCatalog.toArabicIndicDigits(ayah.numberInSurah)}",
                                                        fontFamily = arabicFontFamily,
                                                        fontSize = prefsState.arabicFontSizeSp.sp,
                                                        lineHeight = (prefsState.arabicFontSizeSp * 1.92f).sp,
                                                        color = palette.inkArabic,
                                                        textAlign = TextAlign.End,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                }
                                            }
                                        } else {
                                            Column(modifier = Modifier.fillMaxWidth()) {
                                                if (idx > 0) {
                                                    HorizontalDivider(
                                                        color = palette.border.copy(alpha = 0.55f),
                                                        modifier = Modifier.padding(vertical = 2.dp)
                                                    )
                                                }
                                                Surface(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable { selectedAyahForAction = ayah }
                                                        .testTag("quran_ayah_row_${ayah.numberInSurah}"),
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = when {
                                                        isPlayingThis -> palette.playingBg
                                                        isBookmarked -> palette.goldGlow.copy(alpha = 0.28f)
                                                        else -> Color.Transparent
                                                    }
                                                ) {
                                                    Column(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(horizontal = 6.dp, vertical = 8.dp),
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Row(
                                                            modifier = Modifier.fillMaxWidth(),
                                                            verticalAlignment = Alignment.Top
                                                        ) {
                                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                                ArabicAyahRosetteBadge(
                                                                    ayahNumber = ayah.numberInSurah,
                                                                    isPlaying = isPlayingThis,
                                                                    isBookmarked = isBookmarked,
                                                                    palette = palette,
                                                                    onClick = { selectedAyahForAction = ayah }
                                                                )
                                                                if (isBookmarked) {
                                                                    Spacer(Modifier.height(3.dp))
                                                                    Icon(
                                                                        Icons.Rounded.Bookmark,
                                                                        contentDescription = null,
                                                                        tint = palette.goldPrimary,
                                                                        modifier = Modifier.size(14.dp)
                                                                    )
                                                                }
                                                            }
                                                            Spacer(Modifier.width(10.dp))
                                                            Text(
                                                                text = ayah.arabicText,
                                                                fontFamily = arabicFontFamily,
                                                                fontSize = prefsState.arabicFontSizeSp.sp,
                                                                lineHeight = (prefsState.arabicFontSizeSp * 1.92f).sp,
                                                                color = palette.inkArabic,
                                                                textAlign = TextAlign.End,
                                                                modifier = Modifier.weight(1f)
                                                            )
                                                        }
                                                        if (prefsState.showLatin && ayah.latinText.isNotBlank()) {
                                                            Text(
                                                                text = ayah.latinText,
                                                                fontSize = (prefsState.translationFontSizeSp - 0.5f).sp,
                                                                fontStyle = FontStyle.Italic,
                                                                color = palette.goldDeep,
                                                                lineHeight = (prefsState.translationFontSizeSp * 1.4f).sp
                                                            )
                                                        }
                                                        if (prefsState.showTranslation) {
                                                            val trans = if (initialEnglish && ayah.translationEn.isNotBlank()) {
                                                                ayah.translationEn
                                                            } else {
                                                                ayah.translationId.ifBlank { ayah.translationEn }
                                                            }
                                                            if (trans.isNotBlank()) {
                                                                Text(
                                                                    text = trans,
                                                                    fontSize = prefsState.translationFontSizeSp.sp,
                                                                    color = palette.inkSecondary,
                                                                    lineHeight = (prefsState.translationFontSizeSp * 1.52f).sp
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                item(key = "quran_mushaf_bottom") {
                                    MushafBottomCalligraphyCap(palette = palette) {
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            HorizontalDivider(color = palette.goldPrimary.copy(alpha = 0.45f))
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                OutlinedButton(
                                                    onClick = {
                                                        if (activeSurahNumber > 1) {
                                                            targetScrollAyah = 1
                                                            activeSurahNumber--
                                                        }
                                                    },
                                                    enabled = activeSurahNumber > 1,
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.7f)),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Rounded.ChevronLeft, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(18.dp))
                                                    Text(if (initialEnglish) "Prev" else "Sblmnya", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = palette.goldDeep)
                                                }

                                                if (onRecordToMengaji != null) {
                                                    Button(
                                                        onClick = {
                                                            stopMurottal()
                                                            onRecordToMengaji(activeSurah, playingAyahNumber.takeIf { it > 0 } ?: 1)
                                                        },
                                                        shape = RoundedCornerShape(12.dp),
                                                        colors = ButtonDefaults.buttonColors(containerColor = palette.emeraldAccent, contentColor = Color.White),
                                                        modifier = Modifier
                                                            .weight(1.25f)
                                                            .testTag("btn_quran_record_mengaji")
                                                    ) {
                                                        Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(16.dp))
                                                        Spacer(Modifier.width(4.dp))
                                                        Text(
                                                            text = if (initialEnglish) "Log Mengaji" else "Catat Mengaji",
                                                            fontSize = 11.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            maxLines = 1
                                                        )
                                                    }
                                                }

                                                OutlinedButton(
                                                    onClick = {
                                                        if (activeSurahNumber < 114) {
                                                            targetScrollAyah = 1
                                                            activeSurahNumber++
                                                        }
                                                    },
                                                    enabled = activeSurahNumber < 114,
                                                    shape = RoundedCornerShape(12.dp),
                                                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.7f)),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Text(if (initialEnglish) "Next" else "Brktnya", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = palette.goldDeep)
                                                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    QuranBottomTab.SURAH -> {
                        QuranSurahIndexTabContent(
                            lastRead = prefsState.lastRead,
                            palette = palette,
                            arabicFontFamily = arabicFontFamily,
                            isEnglish = initialEnglish,
                            onSelectSurah = { surahNum, ayahNum ->
                                targetScrollAyah = ayahNum
                                activeSurahNumber = surahNum
                                activeTab = QuranBottomTab.MUSHAF
                            }
                        )
                    }

                    QuranBottomTab.BOOKMARK -> {
                        QuranBookmarksTabContent(
                            bookmarks = prefsState.bookmarks,
                            palette = palette,
                            arabicFontFamily = arabicFontFamily,
                            isEnglish = initialEnglish,
                            onDeleteBookmark = { bm ->
                                val nextList = QuranUserPrefsStore.deleteBookmark(context, prefsState.bookmarks, bm)
                                prefsState = prefsState.copy(bookmarks = nextList)
                            },
                            onOpenBookmark = { sNum, aNum ->
                                targetScrollAyah = aNum
                                activeSurahNumber = sNum
                                activeTab = QuranBottomTab.MUSHAF
                            }
                        )
                    }

                    QuranBottomTab.CATATAN -> {
                        QuranNotesTabContent(
                            notes = prefsState.notes,
                            palette = palette,
                            isEnglish = initialEnglish,
                            onCreateNote = {
                                editingNote = null
                                noteAnchorAyah = 1
                                showNoteEditor = true
                            },
                            onEditNote = { n ->
                                editingNote = n
                                noteAnchorAyah = n.ayahNumber
                                showNoteEditor = true
                            },
                            onDeleteNote = { n ->
                                val nextNotes = QuranUserPrefsStore.deleteNote(context, prefsState.notes, n)
                                prefsState = prefsState.copy(notes = nextNotes)
                            },
                            onOpenPassage = { sNum, aNum ->
                                targetScrollAyah = aNum
                                activeSurahNumber = sNum
                                activeTab = QuranBottomTab.MUSHAF
                            }
                        )
                    }

                    QuranBottomTab.DOA_CARI -> {
                        QuranDuaAndToolsTabContent(
                            palette = palette,
                            arabicFontFamily = arabicFontFamily,
                            isEnglish = initialEnglish,
                            isDownloadingOffline = isDownloadingOffline,
                            downloadProgress = downloadProgress,
                            downloadStatusLabel = downloadStatusLabel,
                            onStartOfflineDownload = {
                                if (!isDownloadingOffline) {
                                    isDownloadingOffline = true
                                    scope.launch {
                                        val count = QuranContentRepository.downloadEssentialOfflinePack(context) { done, total, name ->
                                            downloadProgress = done to total
                                            downloadStatusLabel = if (initialEnglish) {
                                                "Caching ($done/$total): $name"
                                            } else {
                                                "Menyimpan ($done/$total): $name"
                                            }
                                        }
                                        isDownloadingOffline = false
                                        downloadStatusLabel = if (initialEnglish) {
                                            "$count Surahs ready offline!"
                                        } else {
                                            "$count Surah siap dibaca offline!"
                                        }
                                        Toast.makeText(context, downloadStatusLabel, Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onOpenSurahAyah = { sNum, aNum ->
                                targetScrollAyah = aNum
                                activeSurahNumber = sNum
                                activeTab = QuranBottomTab.MUSHAF
                            }
                        )
                    }
                }
            }

            // MINI AUDIO MUROTTAL BAR (jika sedang memutar audio)
            AnimatedVisibility(visible = playingAyahNumber > 0) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = palette.cardSoft,
                    border = BorderStroke(1.dp, palette.goldPrimary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${activeSurah.nameLatin} : $playingAyahNumber",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = palette.goldDeep
                            )
                            Text(
                                text = activeQari.nameId,
                                fontSize = 10.sp,
                                color = palette.inkSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = {
                                val prev = (playingAyahNumber - 1).coerceAtLeast(1)
                                playAyahAudio(prev)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.SkipPrevious, contentDescription = null, tint = palette.goldDeep)
                        }
                        IconButton(
                            onClick = {
                                if (exoPlayer == null) {
                                    isAudioPlaying = !isAudioPlaying
                                } else if (isAudioPlaying) {
                                    exoPlayer.pause()
                                } else {
                                    exoPlayer.play()
                                }
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isAudioPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = palette.goldDeep
                            )
                        }
                        IconButton(
                            onClick = {
                                val total = surahResult?.ayahs?.size ?: activeSurah.versesCount
                                val next = (playingAyahNumber + 1).coerceAtMost(total)
                                playAyahAudio(next)
                            },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.SkipNext, contentDescription = null, tint = palette.goldDeep)
                        }
                        IconButton(
                            onClick = { stopMurottal() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(Icons.Rounded.Stop, contentDescription = null, tint = Color(0xFFDC2626))
                        }
                    }
                }
            }

            // BOTTOM 5-TAB NAVIGATION BAR
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = palette.topBarSurface,
                shadowElevation = 6.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    HorizontalDivider(color = palette.goldPrimary.copy(alpha = 0.35f))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        QuranBottomNavItem(
                            label = "Mushaf",
                            icon = Icons.AutoMirrored.Rounded.MenuBook,
                            selected = activeTab == QuranBottomTab.MUSHAF,
                            badgeCount = 0,
                            palette = palette,
                            onClick = { activeTab = QuranBottomTab.MUSHAF },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quran_nav_mushaf")
                        )
                        QuranBottomNavItem(
                            label = if (initialEnglish) "Surahs" else "114 Surah",
                            icon = Icons.Rounded.LibraryBooks,
                            selected = activeTab == QuranBottomTab.SURAH,
                            badgeCount = 0,
                            palette = palette,
                            onClick = { activeTab = QuranBottomTab.SURAH },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quran_nav_surah")
                        )
                        QuranBottomNavItem(
                            label = "Bookmark",
                            icon = Icons.Rounded.BookmarkBorder,
                            selected = activeTab == QuranBottomTab.BOOKMARK,
                            badgeCount = prefsState.bookmarks.size,
                            palette = palette,
                            onClick = { activeTab = QuranBottomTab.BOOKMARK },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quran_nav_bookmark")
                        )
                        QuranBottomNavItem(
                            label = if (initialEnglish) "Notes" else "Hafalan",
                            icon = Icons.Rounded.Description,
                            selected = activeTab == QuranBottomTab.CATATAN,
                            badgeCount = prefsState.notes.size,
                            palette = palette,
                            onClick = { activeTab = QuranBottomTab.CATATAN },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quran_nav_catatan")
                        )
                        QuranBottomNavItem(
                            label = if (initialEnglish) "Dua/Find" else "Doa & Cari",
                            icon = Icons.Rounded.AutoAwesome,
                            selected = activeTab == QuranBottomTab.DOA_CARI,
                            badgeCount = 0,
                            palette = palette,
                            onClick = { activeTab = QuranBottomTab.DOA_CARI },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("quran_nav_doa_cari")
                        )
                    }
                }
            }
        }
    }

    // POPUP AKSI AYAT (SLIDER DARI BAWAH)
    selectedAyahForAction?.let { ayah ->
        val isBookmarked = prefsState.bookmarks.any {
            it.surahNumber == activeSurahNumber && it.ayahNumber == ayah.numberInSurah
        }
        val ayahSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedAyahForAction = null },
            sheetState = ayahSheetState,
            containerColor = palette.cardSurface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "QS. ${activeSurah.nameLatin} (${activeSurah.number}) : ${ayah.numberInSurah}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = palette.goldDeep
                )
                Text(
                    text = ayah.arabicText,
                    fontFamily = arabicFontFamily,
                    fontSize = 22.sp,
                    lineHeight = 38.sp,
                    color = palette.inkArabic,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                val transPreview = if (initialEnglish && ayah.translationEn.isNotBlank()) ayah.translationEn else ayah.translationId
                if (transPreview.isNotBlank()) {
                    Text(
                        text = transPreview,
                        fontSize = 12.sp,
                        color = palette.inkSecondary,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider(color = palette.border)
                Button(
                    onClick = {
                        playAyahAudio(ayah.numberInSurah)
                        selectedAyahForAction = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = palette.goldPrimary, contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 46.dp)
                ) {
                    Icon(Icons.AutoMirrored.Rounded.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (initialEnglish) "Play Audio Recitation" else "Putar Audio Murottal Ayat Ini", fontWeight = FontWeight.Bold)
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            val (nextList, added) = QuranUserPrefsStore.toggleBookmark(
                                context = context,
                                currentList = prefsState.bookmarks,
                                surahNumber = activeSurahNumber,
                                ayahNumber = ayah.numberInSurah,
                                arabicPreview = ayah.arabicText,
                                translationId = ayah.translationId
                            )
                            prefsState = prefsState.copy(bookmarks = nextList)
                            Toast.makeText(
                                context,
                                if (added) "Ayat disimpan ke Bookmark" else "Bookmark dihapus",
                                Toast.LENGTH_SHORT
                            ).show()
                            selectedAyahForAction = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.goldPrimary),
                        modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                    ) {
                        Icon(if (isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (isBookmarked) "Hapus" else "Bookmark", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.goldDeep)
                    }
                    OutlinedButton(
                        onClick = {
                            val updatedLast = QuranUserPrefsStore.saveLastRead(context, activeSurahNumber, ayah.numberInSurah)
                            prefsState = prefsState.copy(lastRead = updatedLast)
                            Toast.makeText(
                                context,
                                "Terakhir dibaca ditandai di ${activeSurah.nameLatin} : ${ayah.numberInSurah}",
                                Toast.LENGTH_SHORT
                            ).show()
                            selectedAyahForAction = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.goldPrimary),
                        modifier = Modifier.weight(1f).heightIn(min = 46.dp)
                    ) {
                        Text(if (initialEnglish) "Set Last Read" else "Tandai Batas", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = palette.goldDeep)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            editingNote = null
                            noteAnchorAyah = ayah.numberInSurah
                            selectedAyahForAction = null
                            showNoteEditor = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                    ) {
                        Icon(Icons.Rounded.EditNote, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (initialEnglish) "Add Note" else "Catatan Hafalan", fontSize = 11.5.sp, color = palette.inkArabic)
                    }
                    OutlinedButton(
                        onClick = {
                            val copied = "${ayah.arabicText}\n\n${ayah.translationId}\n(QS. ${activeSurah.nameLatin}: ${ayah.numberInSurah})"
                            clipboard.setText(AnnotatedString(copied))
                            Toast.makeText(context, if (initialEnglish) "Ayah copied" else "Ayat berhasil disalin", Toast.LENGTH_SHORT).show()
                            selectedAyahForAction = null
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.border),
                        modifier = Modifier.weight(1f).heightIn(min = 44.dp)
                    ) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = null, tint = palette.goldDeep, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(if (initialEnglish) "Copy" else "Salin", fontSize = 11.5.sp, color = palette.inkArabic)
                    }
                }

                if (onRecordToMengaji != null) {
                    Button(
                        onClick = {
                            selectedAyahForAction = null
                            stopMurottal()
                            onRecordToMengaji(activeSurah, ayah.numberInSurah)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = palette.emeraldAccent, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().heightIn(min = 44.dp)
                    ) {
                        Text(if (initialEnglish) "Record This Ayah to Mengaji Log" else "Catat Surah & Ayat Ini ke Setoran Mengaji", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
    }

    // POPUP EDITOR CATATAN HAFALAN (SLIDER DARI BAWAH)
    if (showNoteEditor) {
        QuranNoteEditorSheet(
            surah = activeSurah,
            ayahNumber = noteAnchorAyah,
            existingNote = editingNote,
            palette = palette,
            isEnglish = initialEnglish,
            onSave = { title, content, tag ->
                val nextNotes = QuranUserPrefsStore.saveNote(
                    context = context,
                    currentList = prefsState.notes,
                    existingId = editingNote?.id,
                    surahNumber = activeSurah.number,
                    ayahNumber = noteAnchorAyah,
                    title = title,
                    content = content,
                    categoryTag = tag
                )
                prefsState = prefsState.copy(notes = nextNotes)
                showNoteEditor = false
            },
            onDismiss = { showNoteEditor = false }
        )
    }
}

@Composable
private fun QuranSettingsQuickStrip(
    state: com.example.util.QuranReaderPrefsState,
    palette: QuranPalette,
    isEnglish: Boolean,
    onUpdateState: (com.example.util.QuranReaderPrefsState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.cardSoft.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Baris 1: Ukuran Huruf Arab + Toggle Latin & Terjemahan
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = { onUpdateState(state.copy(arabicFontSizeSp = (state.arabicFontSizeSp - 2).coerceAtLeast(20))) },
                    shape = RoundedCornerShape(8.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.6f))
                ) {
                    Text("A-", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = palette.goldDeep, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
                Text("${state.arabicFontSizeSp}sp", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = palette.inkArabic)
                Surface(
                    onClick = { onUpdateState(state.copy(arabicFontSizeSp = (state.arabicFontSizeSp + 2).coerceAtMost(42))) },
                    shape = RoundedCornerShape(8.dp),
                    color = palette.cardSurface,
                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.6f))
                ) {
                    Text("A+", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = palette.goldDeep, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    onClick = { onUpdateState(state.copy(showLatin = !state.showLatin)) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (state.showLatin) palette.goldPrimary else palette.cardSurface,
                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.6f))
                ) {
                    Text("Latin", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (state.showLatin) Color.White else palette.goldDeep, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                }
                Surface(
                    onClick = { onUpdateState(state.copy(showTranslation = !state.showTranslation)) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (state.showTranslation) palette.goldPrimary else palette.cardSurface,
                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.6f))
                ) {
                    Text(if (isEnglish) "Trans" else "Arti", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (state.showTranslation) Color.White else palette.goldDeep, modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp))
                }
            }
        }

        // Baris 2: Pilihan Tema Kertas Nyaman Mata
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuranReaderTheme.entries.forEach { t ->
                val selected = state.theme == t
                Surface(
                    onClick = { onUpdateState(state.copy(theme = t)) },
                    shape = RoundedCornerShape(999.dp),
                    color = if (selected) palette.goldPrimary else palette.cardSurface,
                    border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.6f))
                ) {
                    Text(
                        text = if (isEnglish) t.labelEn else t.labelId,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else palette.goldDeep,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }
        }

        // Baris 3: Pilihan Font Arab + Pilihan Qari
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            QuranArabicFontChoice.entries.forEach { f ->
                val selected = state.fontChoice == f
                Surface(
                    onClick = { onUpdateState(state.copy(fontChoice = f)) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) palette.goldDeep else palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Text(
                        text = f.labelId,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Color.White else palette.inkSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            QuranCatalog.QARIS.forEach { q ->
                val selected = state.selectedQariId == q.id
                Surface(
                    onClick = { onUpdateState(state.copy(selectedQariId = q.id)) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selected) palette.emeraldAccent else palette.cardSurface,
                    border = BorderStroke(1.dp, palette.border)
                ) {
                    Text(
                        text = q.nameId,
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (selected) Color.White else palette.inkSecondary,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuranBottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    badgeCount: Int,
    palette: QuranPalette,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeColor = palette.goldDeep
    val idleColor = palette.inkMuted

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(if (selected) palette.goldPrimary else Color.Transparent)
        )
        Spacer(Modifier.height(4.dp))
        Box {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) activeColor else idleColor,
                modifier = Modifier.size(20.dp)
            )
            if (badgeCount > 0) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 7.dp, y = (-4).dp),
                    shape = CircleShape,
                    color = palette.goldPrimary
                ) {
                    Text(
                        text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                        fontSize = 8.5.sp,
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
            fontSize = 9.5.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) activeColor else idleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuranNoteEditorSheet(
    surah: QuranSurahSpec,
    ayahNumber: Int,
    existingNote: QuranNote?,
    palette: QuranPalette,
    isEnglish: Boolean,
    onSave: (title: String, content: String, tag: String) -> Unit,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf(existingNote?.title ?: "${surah.nameLatin} : $ayahNumber") }
    var content by remember { mutableStateOf(existingNote?.content ?: "") }
    var tag by remember { mutableStateOf(existingNote?.categoryTag ?: "HAFALAN") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = palette.cardSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isEnglish) "Memorization & Tajwid Note" else "Catatan Hafalan & Tajwid",
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = palette.goldDeep
            )
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("HAFALAN", "TAJWID", "TADABBUR", "SETORAN").forEach { itemTag ->
                    val selected = tag == itemTag
                    Surface(
                        onClick = { tag = itemTag },
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) palette.goldPrimary else palette.cardSoft,
                        border = BorderStroke(1.dp, palette.goldPrimary.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = itemTag,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else palette.goldDeep,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(if (isEnglish) "Title" else "Judul Catatan") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.goldPrimary,
                    unfocusedBorderColor = palette.border,
                    focusedTextColor = palette.inkArabic,
                    unfocusedTextColor = palette.inkArabic
                ),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                label = { Text(if (isEnglish) "Notes / Tajwid reminder..." else "Isi catatan hafalan / tajwid...") },
                minLines = 3,
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = palette.goldPrimary,
                    unfocusedBorderColor = palette.border,
                    focusedTextColor = palette.inkArabic,
                    unfocusedTextColor = palette.inkArabic
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = { onSave(title, content, tag) },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = palette.goldPrimary, contentColor = Color.White),
                modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)
            ) {
                Text(if (isEnglish) "Save Note" else "Simpan Catatan", fontWeight = FontWeight.Bold)
            }
        }
    }
}
