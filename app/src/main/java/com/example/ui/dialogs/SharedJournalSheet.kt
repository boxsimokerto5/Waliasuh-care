package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.CheckBox
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.JournalChecklistItem
import com.example.data.model.JournalImageAttachment
import com.example.data.model.SharedJournalHelper
import com.example.data.model.SharedJournalNote
import com.example.ui.components.ConfigureEdgeToEdgeDialogWindow
import com.example.util.i18n.LocalAppStrings
import kotlinx.coroutines.launch
import java.io.File

private enum class JournalCategoryFilter {
    ALL, JURNAL, ATURAN
}

@Composable
fun SharedJournalSheet(
    notes: List<SharedJournalNote>,
    currentUsername: String?,
    currentWaliName: String?,
    isAdmin: Boolean,
    isAnakSuperAdmin: Boolean,
    onPrepareImageUri: suspend (Uri) -> JournalImageAttachment? = { null },
    onSaveNote: (
        existingNote: SharedJournalNote?,
        category: String,
        title: String,
        content: String,
        checklistJson: String,
        imageUrlsJson: String,
        isPinned: Boolean,
        silent: Boolean,
        onResult: (Boolean, String) -> Unit
    ) -> Unit,
    onTogglePin: (SharedJournalNote, (Boolean, String) -> Unit) -> Unit,
    onDeleteNote: (SharedJournalNote, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit,
    onShowMessage: (String) -> Unit = {}
) {
    val isEn = LocalAppStrings.current.isEnglish
    var filter by remember { mutableStateOf(JournalCategoryFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var activeNoteId by remember { mutableStateOf<Long?>(null) }
    var isCreatingNew by remember { mutableStateOf(false) }

    val activeNote = remember(notes, activeNoteId) {
        activeNoteId?.let { id -> notes.firstOrNull { it.id == id } }
    }

    val isEditorOpen = isCreatingNew || activeNote != null

    val isLightDialogBars = MaterialTheme.colorScheme.surface.luminance() > 0.5f

    Dialog(
        onDismissRequest = {
            if (!isEditorOpen) onDismiss()
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        ConfigureEdgeToEdgeDialogWindow(
            lightStatusBars = isLightDialogBars,
            lightNavigationBars = isLightDialogBars,
            adjustSoftInput = true
        )
        BackHandler(enabled = !isEditorOpen) {
            onDismiss()
        }

        if (isEditorOpen) {
            val defaultCategory = if (filter == JournalCategoryFilter.ATURAN) {
                SharedJournalHelper.CATEGORY_ATURAN
            } else {
                SharedJournalHelper.CATEGORY_JURNAL
            }
            SharedJournalEditorScreen(
                existingNote = activeNote,
                defaultCategory = defaultCategory,
                currentUsername = currentUsername,
                currentWaliName = currentWaliName,
                isAdmin = isAdmin,
                isAnakSuperAdmin = isAnakSuperAdmin,
                isEn = isEn,
                onPrepareImageUri = onPrepareImageUri,
                onSaveNote = onSaveNote,
                onTogglePin = onTogglePin,
                onDeleteNote = onDeleteNote,
                onShowMessage = onShowMessage,
                onBackToList = {
                    isCreatingNew = false
                    activeNoteId = null
                }
            )
        } else {
            SharedJournalListScreen(
                notes = notes,
                filter = filter,
                onFilterChange = { filter = it },
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                isEn = isEn,
                onOpenNote = { note ->
                    isCreatingNew = false
                    activeNoteId = note.id
                },
                onCreateNew = {
                    activeNoteId = null
                    isCreatingNew = true
                },
                onDismiss = onDismiss
            )
        }
    }
}

@Composable
private fun SharedJournalListScreen(
    notes: List<SharedJournalNote>,
    filter: JournalCategoryFilter,
    onFilterChange: (JournalCategoryFilter) -> Unit,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    isEn: Boolean,
    onOpenNote: (SharedJournalNote) -> Unit,
    onCreateNew: () -> Unit,
    onDismiss: () -> Unit
) {
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val pageBg = if (isLight) Color(0xFFF6F7F9) else MaterialTheme.colorScheme.background
    val cardBg = if (isLight) Color.White else MaterialTheme.colorScheme.surface
    val searchBg = if (isLight) Color(0xFFECEEF2) else MaterialTheme.colorScheme.surfaceVariant

    val jurnalCount = remember(notes) {
        notes.count { SharedJournalHelper.normalizeCategory(it.category) == SharedJournalHelper.CATEGORY_JURNAL }
    }
    val aturanCount = remember(notes) {
        notes.count { SharedJournalHelper.normalizeCategory(it.category) == SharedJournalHelper.CATEGORY_ATURAN }
    }

    val filteredNotes = remember(notes, filter, searchQuery) {
        val q = searchQuery.trim().lowercase()
        notes.filter { item ->
            val catMatch = when (filter) {
                JournalCategoryFilter.ALL -> true
                JournalCategoryFilter.JURNAL ->
                    SharedJournalHelper.normalizeCategory(item.category) == SharedJournalHelper.CATEGORY_JURNAL
                JournalCategoryFilter.ATURAN ->
                    SharedJournalHelper.normalizeCategory(item.category) == SharedJournalHelper.CATEGORY_ATURAN
            }
            val checklistText = SharedJournalHelper.parseChecklist(item.checklistJson)
                .joinToString(" ") { it.text }
                .lowercase()
            val searchMatch = q.isBlank() ||
                item.title.lowercase().contains(q) ||
                item.content.lowercase().contains(q) ||
                item.authorName.lowercase().contains(q) ||
                checklistText.contains(q)
            catMatch && searchMatch
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_shared_journal_list"),
        containerColor = pageBg,
        contentWindowInsets = WindowInsets.safeDrawing,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateNew,
                shape = CircleShape,
                containerColor = Color(0xFFF59E0B),
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .size(58.dp)
                    .testTag("btn_add_shared_journal")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = if (isEn) "Add journal note or rule" else "Tambah catatan jurnal atau aturan",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                // Header bersih gaya MIUI Notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 6.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_shared_journal")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEn) "Back" else "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Journal & Rules" else "Catatan Jurnal & Aturan",
                            fontSize = 19.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isEn) {
                                "$jurnalCount Journals • $aturanCount Joint Rules"
                            } else {
                                "$jurnalCount Catatan Jurnal • $aturanCount Aturan Bersama"
                            },
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEDD5),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_menu_jurnal_aturan),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                // Pill Search Bar gaya MIUI Notes
                Surface(
                    shape = RoundedCornerShape(26.dp),
                    color = searchBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .heightIn(min = 48.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Box(modifier = Modifier.weight(1f)) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = if (isEn) "Search notes or rules..." else "Cari catatan jurnal atau aturan",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.78f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = onSearchChange,
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                cursorBrush = SolidColor(Color(0xFFF59E0B)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("input_search_shared_journal")
                            )
                        }
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { onSearchChange("") },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isEn) "Clear search" else "Hapus pencarian",
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Category Filter Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JournalCategoryFilterPill(
                        label = if (isEn) "All (${notes.size})" else "Semua (${notes.size})",
                        selected = filter == JournalCategoryFilter.ALL,
                        activeColor = Color(0xFF1E293B),
                        onClick = { onFilterChange(JournalCategoryFilter.ALL) },
                        testTag = "chip_filter_all",
                        modifier = Modifier.weight(0.9f)
                    )
                    JournalCategoryFilterPill(
                        label = if (isEn) "Journal ($jurnalCount)" else "Jurnal ($jurnalCount)",
                        selected = filter == JournalCategoryFilter.JURNAL,
                        activeColor = Color(0xFFD97706),
                        onClick = { onFilterChange(JournalCategoryFilter.JURNAL) },
                        testTag = "chip_filter_jurnal",
                        modifier = Modifier.weight(1f)
                    )
                    JournalCategoryFilterPill(
                        label = if (isEn) "Rules ($aturanCount)" else "Aturan ($aturanCount)",
                        selected = filter == JournalCategoryFilter.ATURAN,
                        activeColor = Color(0xFF0F766E),
                        onClick = { onFilterChange(JournalCategoryFilter.ATURAN) },
                        testTag = "chip_filter_aturan",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Daftar Kartu Putih Rapat & Bersih
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFEF3C7),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (notes.isEmpty()) {
                                    if (isEn) "No notes or rules yet" else "Belum ada catatan jurnal & aturan"
                                } else {
                                    if (isEn) "No matching entries found" else "Catatan tidak ditemukan"
                                },
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) {
                                    "Tap the + button below to create a journal note with checklists or photos."
                                } else {
                                    "Ketuk tombol + di kanan bawah untuk menulis catatan, daftar centang, atau unggah foto."
                                },
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .testTag("list_shared_journal_cards"),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 92.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = filteredNotes,
                            key = { it.id }
                        ) { note ->
                            SharedJournalMiuiCard(
                                note = note,
                                cardBg = cardBg,
                                isEn = isEn,
                                onClick = { onOpenNote(note) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JournalCategoryFilterPill(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = if (selected) activeColor else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SharedJournalMiuiCard(
    note: SharedJournalNote,
    cardBg: Color,
    isEn: Boolean,
    onClick: () -> Unit
) {
    val isAturan = SharedJournalHelper.normalizeCategory(note.category) == SharedJournalHelper.CATEGORY_ATURAN
    val badgeBg = if (isAturan) Color(0xFFCCFBF1) else Color(0xFFFEF3C7)
    val badgeFg = if (isAturan) Color(0xFF0F766E) else Color(0xFFB45309)

    val checklist = remember(note.checklistJson) {
        SharedJournalHelper.parseChecklist(note.checklistJson)
    }
    val images = remember(note.imageUrlsJson) {
        SharedJournalHelper.parseImages(note.imageUrlsJson)
    }
    val preview = remember(note.content, checklist, isEn) {
        SharedJournalHelper.buildPreviewSummary(note.content, checklist, isEn)
    }
    val displayTitle = note.title.trim().ifBlank {
        if (isEn) "Untitled Note" else "Catatan Tanpa Judul"
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        shadowElevation = 0.5.dp,
        border = if (note.isPinned) {
            BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f))
        } else {
            BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .testTag("card_shared_journal_${note.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Text(
                    text = displayTitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = preview,
                    fontSize = 13.5.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = SharedJournalHelper.formatCompactDate(note.updatedAt, isEn),
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg
                    ) {
                        Text(
                            text = if (isAturan) {
                                if (isEn) "Rule" else "Aturan"
                            } else {
                                if (isEn) "Journal" else "Jurnal"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = if (isEn) "Pinned" else "Disematkan",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                    }

                    Text(
                        text = "• ${note.authorName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (images.isNotEmpty()) {
                val thumbSrc = images.first().preferredDisplaySource()
                val thumbModel: Any = if (thumbSrc.startsWith("/")) File(thumbSrc) else thumbSrc
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    AsyncImage(
                        model = thumbModel,
                        contentDescription = if (isEn) "Note photo" else "Foto catatan",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (images.size > 1) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.62f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                        ) {
                            Text(
                                text = "+${images.size - 1}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedJournalEditorScreen(
    existingNote: SharedJournalNote?,
    defaultCategory: String,
    currentUsername: String?,
    currentWaliName: String?,
    isAdmin: Boolean,
    isAnakSuperAdmin: Boolean,
    isEn: Boolean,
    onPrepareImageUri: suspend (Uri) -> JournalImageAttachment?,
    onSaveNote: (
        existingNote: SharedJournalNote?,
        category: String,
        title: String,
        content: String,
        checklistJson: String,
        imageUrlsJson: String,
        isPinned: Boolean,
        silent: Boolean,
        onResult: (Boolean, String) -> Unit
    ) -> Unit,
    onTogglePin: (SharedJournalNote, (Boolean, String) -> Unit) -> Unit,
    onDeleteNote: (SharedJournalNote, (Boolean, String) -> Unit) -> Unit,
    onShowMessage: (String) -> Unit,
    onBackToList: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val canManage = remember(existingNote, currentUsername, currentWaliName, isAdmin, isAnakSuperAdmin) {
        existingNote == null || SharedJournalHelper.canManageNote(
            note = existingNote,
            currentUsername = currentUsername,
            currentWaliName = currentWaliName,
            isAdmin = isAdmin,
            isAnakSuperAdmin = isAnakSuperAdmin
        )
    }

    var category by remember(existingNote?.id) {
        mutableStateOf(
            existingNote?.let { SharedJournalHelper.normalizeCategory(it.category) } ?: defaultCategory
        )
    }
    var title by remember(existingNote?.id) {
        mutableStateOf(existingNote?.title.orEmpty())
    }
    var content by remember(existingNote?.id) {
        mutableStateOf(existingNote?.content.orEmpty())
    }
    var checklist by remember(existingNote?.id) {
        mutableStateOf(SharedJournalHelper.parseChecklist(existingNote?.checklistJson))
    }
    var images by remember(existingNote?.id) {
        mutableStateOf(SharedJournalHelper.parseImages(existingNote?.imageUrlsJson))
    }
    var isPinned by remember(existingNote?.id) {
        mutableStateOf(existingNote?.isPinned ?: false)
    }
    var isProcessingImage by remember { mutableStateOf(false) }
    var isSaving by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var previewImage by remember { mutableStateOf<JournalImageAttachment?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                isProcessingImage = true
                val prepared = onPrepareImageUri(uri)
                isProcessingImage = false
                if (prepared != null) {
                    if (images.size >= 8) {
                        onShowMessage(if (isEn) "Maximum 8 photos per note." else "Maksimal 8 gambar per catatan.")
                    } else {
                        images = images + prepared
                    }
                } else {
                    onShowMessage(if (isEn) "Failed to load selected photo." else "Gagal memproses gambar yang dipilih.")
                }
            }
        }
    }

    val serializedChecklist = SharedJournalHelper.serializeChecklist(checklist.filter { it.text.isNotBlank() })
    val serializedImages = SharedJournalHelper.serializeImages(images)
    val hasAnyContent = title.isNotBlank() ||
        content.isNotBlank() ||
        checklist.any { it.text.isNotBlank() } ||
        images.isNotEmpty()

    val isDirty = remember(existingNote, category, title, content, serializedChecklist, serializedImages, isPinned) {
        if (existingNote == null) {
            hasAnyContent
        } else {
            SharedJournalHelper.normalizeCategory(existingNote.category) != category ||
                existingNote.title.trim() != title.trim() ||
                existingNote.content.trim() != content.trim() ||
                SharedJournalHelper.serializeChecklist(SharedJournalHelper.parseChecklist(existingNote.checklistJson).filter { it.text.isNotBlank() }) != serializedChecklist ||
                SharedJournalHelper.serializeImages(SharedJournalHelper.parseImages(existingNote.imageUrlsJson)) != serializedImages ||
                existingNote.isPinned != isPinned
        }
    }

    val triggerSave: (Boolean, () -> Unit) -> Unit = { silent, afterSuccess ->
        if (!canManage || isSaving) {
            afterSuccess()
        } else {
            isSaving = true
            onSaveNote(
                existingNote,
                category,
                title,
                content,
                serializedChecklist,
                serializedImages,
                isPinned,
                silent
            ) { ok, msg ->
                isSaving = false
                if (msg.isNotBlank()) onShowMessage(msg)
                if (ok) afterSuccess()
            }
        }
    }

    val handleBack: () -> Unit = {
        if (previewImage != null) {
            previewImage = null
        } else if (canManage && isDirty && hasAnyContent) {
            triggerSave(true) { onBackToList() }
        } else {
            onBackToList()
        }
    }

    BackHandler { handleBack() }

    val displayTimestamp = existingNote?.updatedAt ?: System.currentTimeMillis()
    val charCount = SharedJournalHelper.countCharacters(title, content, checklist)
    val authorLabel = existingNote?.authorName ?: (currentWaliName?.ifBlank { "Wali Asuh" } ?: "Wali Asuh")
    val isAturan = category == SharedJournalHelper.CATEGORY_ATURAN
    val accentColor = if (isAturan) Color(0xFF0F766E) else Color(0xFFD97706)

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_shared_journal_editor"),
        containerColor = MaterialTheme.colorScheme.surface,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (canManage) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(
                                WindowInsets.navigationBars.only(
                                    WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal
                                )
                            )
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(
                                    onClick = {
                                        checklist = checklist + JournalChecklistItem()
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_toolbar_checklist")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.CheckBox,
                                            contentDescription = if (isEn) "Add checklist item" else "Tambah item centang",
                                            tint = Color(0xFFD97706),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = if (isEn) "Checklist" else "Centang",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }

                                Surface(
                                    onClick = {
                                        if (!isProcessingImage) {
                                            imagePickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_toolbar_image")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (isProcessingImage) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Outlined.Image,
                                                contentDescription = if (isEn) "Upload photo" else "Upload gambar",
                                                tint = Color(0xFF0F766E),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Text(
                                            text = if (isEn) "Photo" else "Gambar",
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    triggerSave(false) { onBackToList() }
                                },
                                enabled = !isSaving,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentColor,
                                    contentColor = Color.White
                                ),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_save_shared_journal")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Save" else "Simpan",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
            ) {
                // Top Bar minimalis gaya MIUI Notes
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = handleBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_back_shared_journal_editor")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isEn) "Back" else "Kembali",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        // Share button
                        IconButton(
                            onClick = {
                                val previewNote = SharedJournalNote(
                                    id = existingNote?.id ?: 0L,
                                    category = category,
                                    title = title.ifBlank { if (isEn) "Note" else "Catatan" },
                                    content = content,
                                    checklistJson = serializedChecklist,
                                    imageUrlsJson = serializedImages,
                                    isPinned = isPinned,
                                    authorName = authorLabel,
                                    updatedAt = displayTimestamp
                                )
                                val shareText = SharedJournalHelper.buildShareText(previewNote, isEn)
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareText)
                                }
                                runCatching {
                                    context.startActivity(
                                        Intent.createChooser(
                                            sendIntent,
                                            if (isEn) "Share note" else "Bagikan catatan"
                                        )
                                    )
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_share_shared_journal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = if (isEn) "Share" else "Bagikan",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        if (canManage) {
                            IconButton(
                                onClick = {
                                    isPinned = !isPinned
                                    if (existingNote != null && !isDirty) {
                                        onTogglePin(existingNote) { _, msg ->
                                            if (msg.isNotBlank()) onShowMessage(msg)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_pin_shared_journal")
                            ) {
                                Icon(
                                    imageVector = if (isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                    contentDescription = if (isPinned) {
                                        if (isEn) "Unpin" else "Lepas semat"
                                    } else {
                                        if (isEn) "Pin" else "Sematkan"
                                    },
                                    tint = if (isPinned) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                                )
                            }

                            if (existingNote != null) {
                                IconButton(
                                    onClick = { showDeleteConfirm = true },
                                    modifier = Modifier
                                        .size(48.dp)
                                        .testTag("btn_delete_shared_journal")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DeleteOutline,
                                        contentDescription = if (isEn) "Delete" else "Hapus",
                                        tint = Color(0xFFDC2626)
                                    )
                                }
                            }
                        }
                    }
                }

                // Isi editor scrollable full-canvas
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Pill kategori atau badge Mode Baca
                    if (canManage) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            EditorCategoryPill(
                                label = if (isEn) "Journal Note" else "Catatan Jurnal",
                                icon = Icons.Default.MenuBook,
                                selected = category == SharedJournalHelper.CATEGORY_JURNAL,
                                activeColor = Color(0xFFD97706),
                                onClick = { category = SharedJournalHelper.CATEGORY_JURNAL },
                                testTag = "btn_category_jurnal"
                            )
                            EditorCategoryPill(
                                label = if (isEn) "Joint Rule" else "Aturan Bersama",
                                icon = Icons.Default.Gavel,
                                selected = category == SharedJournalHelper.CATEGORY_ATURAN,
                                activeColor = Color(0xFF0F766E),
                                onClick = { category = SharedJournalHelper.CATEGORY_ATURAN },
                                testTag = "btn_category_aturan"
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                            modifier = Modifier.testTag("badge_journal_read_only")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isEn) {
                                        "Read-only • By $authorLabel"
                                    } else {
                                        "Mode Baca • Oleh $authorLabel"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Input Judul tanpa kotak (gaya MIUI Notes)
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (title.isEmpty()) {
                            Text(
                                text = if (isEn) "Title" else "Judul",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                            )
                        }
                        BasicTextField(
                            value = title,
                            onValueChange = { if (canManage) title = it },
                            readOnly = !canManage,
                            textStyle = TextStyle(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(accentColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_shared_journal_title")
                        )
                    }

                    // Metadata Timestamp | Jumlah Karakter • Penulis
                    Text(
                        text = if (isEn) {
                            "${SharedJournalHelper.formatEditorTimestamp(displayTimestamp, true)}  |  $charCount characters  •  $authorLabel"
                        } else {
                            "${SharedJournalHelper.formatEditorTimestamp(displayTimestamp, false)}  |  $charCount karakter  •  $authorLabel"
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                    )

                    // Input Body Bebas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 72.dp)
                    ) {
                        if (content.isEmpty()) {
                            Text(
                                text = if (isEn) {
                                    "Start typing your journal note or shared rules..."
                                } else {
                                    "Mulai menulis catatan jurnal atau poin aturan bersama..."
                                },
                                fontSize = 15.5.sp,
                                lineHeight = 23.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.48f)
                            )
                        }
                        BasicTextField(
                            value = content,
                            onValueChange = { if (canManage) content = it },
                            readOnly = !canManage,
                            textStyle = TextStyle(
                                fontSize = 15.5.sp,
                                lineHeight = 23.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            ),
                            cursorBrush = SolidColor(accentColor),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_shared_journal_content")
                        )
                    }

                    // Daftar Centang Interaktif (Checklist)
                    if (checklist.isNotEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            checklist.forEachIndexed { idx, item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("checklist_row_$idx"),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = item.isChecked,
                                        onCheckedChange = { checked ->
                                            if (canManage) {
                                                checklist = checklist.toMutableList().also {
                                                    it[idx] = item.copy(isChecked = checked)
                                                }
                                            }
                                        },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = accentColor,
                                            uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        modifier = Modifier
                                            .size(48.dp)
                                            .testTag("checklist_checkbox_$idx")
                                    )

                                    Box(modifier = Modifier.weight(1f)) {
                                        if (item.text.isEmpty()) {
                                            Text(
                                                text = if (isEn) "List item" else "Item daftar",
                                                fontSize = 15.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f)
                                            )
                                        }
                                        BasicTextField(
                                            value = item.text,
                                            onValueChange = { nextText ->
                                                if (canManage) {
                                                    checklist = checklist.toMutableList().also {
                                                        it[idx] = item.copy(text = nextText)
                                                    }
                                                }
                                            },
                                            readOnly = !canManage,
                                            textStyle = TextStyle(
                                                fontSize = 15.sp,
                                                lineHeight = 21.sp,
                                                color = if (item.isChecked) {
                                                    MaterialTheme.colorScheme.onSurfaceVariant
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                textDecoration = if (item.isChecked) {
                                                    TextDecoration.LineThrough
                                                } else {
                                                    TextDecoration.None
                                                }
                                            ),
                                            cursorBrush = SolidColor(accentColor),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp)
                                                .testTag("checklist_input_$idx")
                                        )
                                    }

                                    if (canManage) {
                                        IconButton(
                                            onClick = {
                                                checklist = checklist.filterNot { it.id == item.id }
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("checklist_remove_$idx")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = if (isEn) "Remove item" else "Hapus item",
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            if (canManage) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { checklist = checklist + JournalChecklistItem() }
                                        .heightIn(min = 48.dp)
                                        .padding(horizontal = 12.dp)
                                        .testTag("btn_add_checklist_row"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Add checklist item" else "Tambah item centang",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }

                    // Galeri Gambar Terlampir
                    if (images.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            images.forEachIndexed { idx, img ->
                                val src = img.preferredDisplaySource()
                                val model: Any = if (src.startsWith("/")) File(src) else src
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                        .clickable { previewImage = img }
                                        .testTag("journal_image_card_$idx")
                                ) {
                                    AsyncImage(
                                        model = model,
                                        contentDescription = if (isEn) "Attached photo ${idx + 1}" else "Gambar lampiran ${idx + 1}",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(min = 160.dp, max = 260.dp)
                                    )
                                    if (canManage) {
                                        Surface(
                                            onClick = {
                                                images = images.filterNot { it.id == img.id }
                                            },
                                            shape = CircleShape,
                                            color = Color.Black.copy(alpha = 0.62f),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(8.dp)
                                                .size(48.dp)
                                                .testTag("btn_remove_journal_img_$idx")
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Close,
                                                    contentDescription = if (isEn) "Remove photo" else "Hapus gambar",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }

    // Dialog Konfirmasi Hapus
    if (showDeleteConfirm && existingNote != null) {
        val displayTitle = existingNote.title.ifBlank {
            if (isEn) "this note" else "catatan ini"
        }
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = {
                Text(
                    text = if (isEn) "Delete Note?" else "Hapus Catatan Ini?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "\"$displayTitle\" will be deleted for all Wali Asuh in this school."
                    } else {
                        "\"$displayTitle\" akan dihapus dari daftar catatan jurnal & aturan bersama."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteNote(existingNote) { ok, msg ->
                            if (msg.isNotBlank()) onShowMessage(msg)
                            if (ok) onBackToList()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_confirm_delete_shared_journal")
                ) {
                    Text(if (isEn) "Delete" else "Hapus", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirm = false },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    // Full-Screen Zoomable Photo Viewer
    previewImage?.let { targetImg ->
        val src = targetImg.preferredDisplaySource()
        val model: Any = if (src.startsWith("/")) File(src) else src
        var scale by remember { mutableFloatStateOf(1f) }
        var offset by remember { mutableStateOf(Offset.Zero) }

        Dialog(
            onDismissRequest = { previewImage = null },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            ConfigureEdgeToEdgeDialogWindow(
                lightStatusBars = false,
                lightNavigationBars = false
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = model,
                    contentDescription = if (isEn) "Zoomable attachment" else "Pratinjau gambar catatan",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            translationX = offset.x
                            translationY = offset.y
                        }
                        .clickable {
                            if (scale > 1f) {
                                scale = 1f
                                offset = Offset.Zero
                            } else {
                                previewImage = null
                            }
                        }
                        .then(
                            Modifier.pointerTransformZoom(
                                onTransform = { zoomChange, panChange ->
                                    val nextScale = (scale * zoomChange).coerceIn(1f, 4f)
                                    scale = nextScale
                                    offset = if (nextScale > 1f) offset + panChange else Offset.Zero
                                }
                            )
                        )
                )

                IconButton(
                    onClick = { previewImage = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                            )
                        )
                        .padding(16.dp)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isEn) "Close photo" else "Tutup gambar",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

private fun Modifier.pointerTransformZoom(
    onTransform: (zoomChange: Float, panChange: Offset) -> Unit
): Modifier = pointerInput(Unit) {
    detectTransformGestures { _, pan, zoom, _ ->
        onTransform(zoom, pan)
    }
}

@Composable
private fun EditorCategoryPill(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) activeColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) activeColor else Color.Transparent
        ),
        modifier = Modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 12.5.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
