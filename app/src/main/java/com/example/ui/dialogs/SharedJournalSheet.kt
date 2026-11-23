package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.model.SharedJournalHelper
import com.example.data.model.SharedJournalNote
import com.example.ui.theme.WaliPrimary
import com.example.util.i18n.LocalAppStrings

private enum class JournalCategoryFilter {
    ALL, JURNAL, ATURAN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedJournalSheet(
    notes: List<SharedJournalNote>,
    currentUsername: String?,
    currentWaliName: String?,
    isAdmin: Boolean,
    isAnakSuperAdmin: Boolean,
    onSaveNote: (
        existingNote: SharedJournalNote?,
        category: String,
        title: String,
        content: String,
        isPinned: Boolean,
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
    var selectedDetailNoteId by remember { mutableStateOf<Long?>(null) }
    var showFormDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<SharedJournalNote?>(null) }
    var pendingDeleteNote by remember { mutableStateOf<SharedJournalNote?>(null) }

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
            val searchMatch = q.isBlank() ||
                item.title.lowercase().contains(q) ||
                item.content.lowercase().contains(q) ||
                item.authorName.lowercase().contains(q)
            catMatch && searchMatch
        }
    }

    val selectedDetailNote = remember(notes, selectedDetailNoteId) {
        selectedDetailNoteId?.let { id -> notes.firstOrNull { it.id == id } }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .heightIn(min = 420.dp, max = 720.dp)
                    .testTag("sheet_shared_journal")
            ) {
                // Header Rapat & Menarik
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFFF7ED),
                                    Color(0xFFF0FDFA)
                                )
                            )
                        )
                        .padding(start = 14.dp, end = 6.dp, top = 10.dp, bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFEDD5),
                            border = BorderStroke(1.dp, Color(0xFFFED7AA)),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_menu_jurnal_aturan),
                                    contentDescription = if (isEn) "Journal & Joint Rules" else "Catatan Jurnal & Aturan Bersama",
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Journal & Joint Rules" else "Catatan Jurnal & Aturan Bersama",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "$jurnalCount Journals • $aturanCount Joint Rules"
                                } else {
                                    "$jurnalCount Jurnal • $aturanCount Aturan Bersama"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = {
                                editingNote = null
                                showFormDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD97706),
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 11.dp, vertical = 0.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_add_shared_journal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = if (isEn) "Add journal or rule" else "Tambah jurnal atau aturan",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isEn) "Add" else "Tambah",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_shared_journal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = if (isEn) "Close" else "Tutup",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                // Kontrol Filter Kategori + Pencarian Rapat
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        JournalFilterChip(
                            label = if (isEn) "All (${notes.size})" else "Semua (${notes.size})",
                            selected = filter == JournalCategoryFilter.ALL,
                            activeColor = WaliPrimary,
                            onClick = { filter = JournalCategoryFilter.ALL },
                            testTag = "chip_filter_all",
                            modifier = Modifier.weight(0.95f)
                        )
                        JournalFilterChip(
                            label = if (isEn) "Journals ($jurnalCount)" else "Jurnal ($jurnalCount)",
                            selected = filter == JournalCategoryFilter.JURNAL,
                            activeColor = Color(0xFFD97706),
                            onClick = { filter = JournalCategoryFilter.JURNAL },
                            testTag = "chip_filter_jurnal",
                            modifier = Modifier.weight(1f)
                        )
                        JournalFilterChip(
                            label = if (isEn) "Rules ($aturanCount)" else "Aturan ($aturanCount)",
                            selected = filter == JournalCategoryFilter.ATURAN,
                            activeColor = Color(0xFF0F766E),
                            onClick = { filter = JournalCategoryFilter.ATURAN },
                            testTag = "chip_filter_aturan",
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = if (isEn) "Clear search" else "Hapus pencarian",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                text = if (isEn) "Search title, content, or author..." else "Cari judul, isi, atau penulis...",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_search_shared_journal")
                    )
                }

                // List Kartu Catatan Jurnal & Aturan Bersama
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFFFEDD5),
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = Color(0xFFD97706),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (notes.isEmpty()) {
                                    if (isEn) "No journal notes or joint rules yet" else "Belum ada catatan jurnal atau aturan bersama"
                                } else {
                                    if (isEn) "No matching entries found" else "Tidak ada catatan yang cocok"
                                },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) {
                                    "Tap + to add caregiving notes or agreed rules for all Wali Asuh."
                                } else {
                                    "Ketuk tombol + untuk mencatat jurnal harian atau kesepakatan aturan bersama."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Button(
                                onClick = {
                                    editingNote = null
                                    showFormDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_empty_add_shared_journal")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Write First Entry" else "Buat Catatan Pertama",
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = true)
                            .testTag("list_shared_journal_cards"),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 2.dp, bottom = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        items(
                            items = filteredNotes,
                            key = { it.id }
                        ) { note ->
                            SharedJournalCard(
                                note = note,
                                isEn = isEn,
                                onClick = { selectedDetailNoteId = note.id }
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail Dialog saat kartu diklik
    if (selectedDetailNote != null) {
        val canManage = SharedJournalHelper.canManageNote(
            note = selectedDetailNote,
            currentUsername = currentUsername,
            currentWaliName = currentWaliName,
            isAdmin = isAdmin,
            isAnakSuperAdmin = isAnakSuperAdmin
        )
        SharedJournalDetailDialog(
            note = selectedDetailNote,
            isEn = isEn,
            canManage = canManage,
            onTogglePin = {
                onTogglePin(selectedDetailNote) { _, msg ->
                    onShowMessage(msg)
                }
            },
            onEdit = {
                editingNote = selectedDetailNote
                selectedDetailNoteId = null
                showFormDialog = true
            },
            onDelete = {
                pendingDeleteNote = selectedDetailNote
            },
            onDismiss = { selectedDetailNoteId = null }
        )
    }

    // Form Dialog Tambah / Edit Jurnal & Aturan
    if (showFormDialog) {
        SharedJournalFormDialog(
            existingNote = editingNote,
            isEn = isEn,
            onDismiss = {
                showFormDialog = false
                editingNote = null
            },
            onSave = { category, title, content, isPinned ->
                onSaveNote(editingNote, category, title, content, isPinned) { ok, msg ->
                    onShowMessage(msg)
                    if (ok) {
                        showFormDialog = false
                        editingNote = null
                    }
                }
            }
        )
    }

    // Konfirmasi Hapus
    pendingDeleteNote?.let { target ->
        AlertDialog(
            onDismissRequest = { pendingDeleteNote = null },
            title = {
                Text(
                    text = if (isEn) "Delete Entry?" else "Hapus Catatan Ini?",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (isEn) {
                        "\"${target.title}\" will be removed from the shared journal list."
                    } else {
                        "\"${target.title}\" akan dihapus dari daftar catatan jurnal & aturan bersama."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteNote = null
                        selectedDetailNoteId = null
                        onDeleteNote(target) { _, msg ->
                            onShowMessage(msg)
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
                    onClick = { pendingDeleteNote = null },
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}

@Composable
private fun JournalFilterChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) activeColor else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) activeColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SharedJournalCard(
    note: SharedJournalNote,
    isEn: Boolean,
    onClick: () -> Unit
) {
    val isAturan = SharedJournalHelper.normalizeCategory(note.category) == SharedJournalHelper.CATEGORY_ATURAN
    val accentColor = if (isAturan) Color(0xFF0F766E) else Color(0xFFD97706)
    val badgeBg = if (isAturan) Color(0xFFCCFBF1) else Color(0xFFFEF3C7)
    val badgeFg = if (isAturan) Color(0xFF0F766E) else Color(0xFFB45309)

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 48.dp)
            .testTag("card_shared_journal_${note.id}"),
        shape = RoundedCornerShape(13.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (note.isPinned) {
                accentColor.copy(alpha = 0.05f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Transparent)
        ) {
            // Aksen garis warna kiri rapat & elegan
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(88.dp)
                    .background(accentColor)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = badgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAturan) Icons.Default.Gavel else Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = badgeFg,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = SharedJournalHelper.categoryLabel(note.category, isEn),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeFg,
                                    maxLines = 1
                                )
                            }
                        }

                        if (note.isPinned) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEDE9FE)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = if (isEn) "Pinned" else "Disematkan",
                                        tint = Color(0xFF6D28D9),
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Pin" else "Semat",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        text = SharedJournalHelper.formatCompactDate(note.updatedAt, isEn),
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }

                Text(
                    text = note.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = note.content.replace("\n", " "),
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = note.authorName,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(1.dp)
                    ) {
                        Text(
                            text = if (isEn) "Open detail" else "Lihat detail",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = accentColor
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedJournalDetailDialog(
    note: SharedJournalNote,
    isEn: Boolean,
    canManage: Boolean,
    onTogglePin: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    val isAturan = SharedJournalHelper.normalizeCategory(note.category) == SharedJournalHelper.CATEGORY_ATURAN
    val accentColor = if (isAturan) Color(0xFF0F766E) else Color(0xFFD97706)
    val badgeBg = if (isAturan) Color(0xFFCCFBF1) else Color(0xFFFEF3C7)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .widthIn(max = 560.dp)
                .testTag("dialog_shared_journal_detail"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = badgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isAturan) Icons.Default.Gavel else Icons.Default.MenuBook,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = SharedJournalHelper.categoryLabel(note.category, isEn),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = accentColor
                                )
                            }
                        }
                        if (note.isPinned) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFEDE9FE)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PushPin,
                                        contentDescription = null,
                                        tint = Color(0xFF6D28D9),
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isEn) "Pinned" else "Disematkan",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF6D28D9)
                                    )
                                }
                            }
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_detail_shared_journal")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Close detail" else "Tutup detail"
                        )
                    }
                }

                Text(
                    text = note.title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = note.authorName,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "• ${SharedJournalHelper.formatCompactDate(note.updatedAt, isEn)}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 90.dp, max = 340.dp)
                        .verticalScroll(rememberScrollState())
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f))
                        .padding(12.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = note.content,
                            fontSize = 13.5.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                if (canManage) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onTogglePin,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_pin_shared_journal")
                        ) {
                            Icon(
                                imageVector = if (note.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                                contentDescription = if (note.isPinned) {
                                    if (isEn) "Unpin" else "Lepas semat"
                                } else {
                                    if (isEn) "Pin" else "Sematkan"
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (note.isPinned) {
                                    if (isEn) "Unpin" else "Lepas"
                                } else {
                                    if (isEn) "Pin" else "Semat"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = onDelete,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.55f)),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("btn_delete_shared_journal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isEn) "Delete" else "Hapus",
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Button(
                            onClick = onEdit,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = accentColor),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .testTag("btn_edit_shared_journal")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isEn) "Edit" else "Ubah",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SharedJournalFormDialog(
    existingNote: SharedJournalNote?,
    isEn: Boolean,
    onDismiss: () -> Unit,
    onSave: (category: String, title: String, content: String, isPinned: Boolean) -> Unit
) {
    var category by remember(existingNote) {
        mutableStateOf(
            SharedJournalHelper.normalizeCategory(existingNote?.category ?: SharedJournalHelper.CATEGORY_JURNAL)
        )
    }
    var title by remember(existingNote) { mutableStateOf(existingNote?.title.orEmpty()) }
    var content by remember(existingNote) { mutableStateOf(existingNote?.content.orEmpty()) }
    var isPinned by remember(existingNote) { mutableStateOf(existingNote?.isPinned ?: false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .widthIn(max = 560.dp)
                .imePadding()
                .testTag("dialog_shared_journal_form"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (existingNote != null) {
                            if (isEn) "Edit Note / Rule" else "Ubah Catatan / Aturan"
                        } else {
                            if (isEn) "New Journal / Joint Rule" else "Tambah Jurnal / Aturan Bersama"
                        },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = if (isEn) "Cancel" else "Tutup"
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val isJurnal = category == SharedJournalHelper.CATEGORY_JURNAL
                    Surface(
                        onClick = { category = SharedJournalHelper.CATEGORY_JURNAL },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isJurnal) Color(0xFFD97706) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, if (isJurnal) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_category_jurnal")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = if (isJurnal) Color.White else Color(0xFFD97706),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Journal Note" else "Catatan Jurnal",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isJurnal) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    val isAturan = category == SharedJournalHelper.CATEGORY_ATURAN
                    Surface(
                        onClick = { category = SharedJournalHelper.CATEGORY_ATURAN },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isAturan) Color(0xFF0F766E) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        border = BorderStroke(1.dp, if (isAturan) Color(0xFF0F766E) else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_category_aturan")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = if (isAturan) Color.White else Color(0xFF0F766E),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Joint Rule" else "Aturan Bersama",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isAturan) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Text(if (isEn) "Title" else "Judul Catatan / Aturan")
                    },
                    placeholder = {
                        Text(
                            if (category == SharedJournalHelper.CATEGORY_ATURAN) {
                                if (isEn) "e.g., Dorm Lights-Out Policy" else "Contoh: Aturan Jam Malam & HP Asrama"
                            } else {
                                if (isEn) "e.g., Weekly Dorm Evaluation" else "Contoh: Evaluasi Piket Asrama Pekan Ini"
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shared_journal_title")
                )

                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    minLines = 4,
                    maxLines = 9,
                    shape = RoundedCornerShape(12.dp),
                    label = {
                        Text(if (isEn) "Details / Content" else "Isi Lengkap Catatan atau Aturan")
                    },
                    placeholder = {
                        Text(
                            if (isEn) "Write details clearly so all caregivers stay aligned..." else "Tulis poin lengkap agar seluruh wali asuh selaras..."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shared_journal_content")
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEn) "Pin to top" else "Sematkan di paling atas",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = if (isEn) "Important rules stay visible first" else "Aturan/jurnal penting tampil paling atas",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isPinned,
                            onCheckedChange = { isPinned = it },
                            modifier = Modifier.testTag("switch_shared_journal_pin")
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                    ) {
                        Text(if (isEn) "Cancel" else "Batal")
                    }
                    Button(
                        onClick = { onSave(category, title, content, isPinned) },
                        enabled = title.trim().isNotEmpty() && content.trim().isNotEmpty(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (category == SharedJournalHelper.CATEGORY_ATURAN) Color(0xFF0F766E) else Color(0xFFD97706)
                        ),
                        modifier = Modifier
                            .weight(1.3f)
                            .heightIn(min = 48.dp)
                            .testTag("btn_save_shared_journal")
                    ) {
                        Text(
                            text = if (isEn) "Save" else "Simpan",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
