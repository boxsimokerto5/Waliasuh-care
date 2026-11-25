package com.example.ui.dialogs

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.BibleBookSpec
import com.example.data.model.BibleCatalog
import com.example.data.model.BibleTestament

private sealed class PickerRowItem {
    data class Header(val title: String) : PickerRowItem()
    data class BookRow(val book: BibleBookSpec) : PickerRowItem()
}

@Composable
fun BibleBookPickerSheet(
    currentBook: BibleBookSpec,
    currentChapter: Int,
    isEnglish: Boolean,
    useDialogWindow: Boolean = false,
    onSelectBookChapter: (BibleBookSpec, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTestament by remember(currentBook.id) { mutableStateOf(currentBook.testament) }
    var previewBook by remember(currentBook.id) { mutableStateOf(currentBook) }
    var searchQuery by remember { mutableStateOf("") }

    val booksForTab = remember(selectedTestament, searchQuery, isEnglish) {
        val clean = searchQuery.trim().lowercase()
        val base = if (clean.isNotBlank()) {
            BibleCatalog.ALL_BOOKS.filter {
                it.nameId.lowercase().contains(clean) ||
                    it.nameEn.lowercase().contains(clean) ||
                    it.id.lowercase().contains(clean) ||
                    it.categoryId.lowercase().contains(clean) ||
                    it.categoryEn.lowercase().contains(clean)
            }
        } else {
            BibleCatalog.booksByTestament(selectedTestament)
        }
        val grouped = LinkedHashMap<String, MutableList<BibleBookSpec>>()
        base.forEach { b ->
            val cat = (if (isEnglish) b.categoryEn else b.categoryId).uppercase()
            grouped.getOrPut(cat) { mutableListOf() }.add(b)
        }
        buildList {
            grouped.forEach { (category, books) ->
                add(PickerRowItem.Header(category))
                books.forEach { add(PickerRowItem.BookRow(it)) }
            }
        }
    }

    val previewChapterList = remember(previewBook.id) {
        (1..previewBook.chaptersCount).toList()
    }

    val sheetContent: @Composable () -> Unit = {
        BackHandler { onDismiss() }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x8824140B))
                .windowInsetsPadding(
                    WindowInsets.safeDrawing.only(WindowInsetsSides.Top + WindowInsetsSides.Horizontal)
                )
                .padding(top = 14.dp)
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .clickable(enabled = false) {},
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                        )
                        .imePadding()
                ) {
                    // Header: Icon + "Pilih Kitab & Pasal" + active subtitle + Close X
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 10.dp, top = 16.dp, bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(42.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                                    contentDescription = if (isEnglish) "Book icon" else "Ikon kitab",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isEnglish) "Choose Book & Chapter" else "Pilih Kitab & Pasal",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1F2937)
                            )
                            val bookDisplay = if (isEnglish) previewBook.nameEn else previewBook.nameId
                            val chapWord = if (isEnglish) "Chapters" else "Pasal"
                            Text(
                                text = "$bookDisplay (${previewBook.chaptersCount} $chapWord)",
                                fontSize = 13.sp,
                                color = Color(0xFF6B7280)
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("bible_picker_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = if (isEnglish) "Close picker" else "Tutup pilihan kitab",
                                tint = Color(0xFF6B7280)
                            )
                        }
                    }

                    // Search bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF3F4F6),
                        border = BorderStroke(1.dp, Color(0xFFE5E7EB))
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
                                tint = Color(0xFF9CA3AF),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f)) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = if (isEnglish) {
                                            "Type book name (e.g., John, Psalms, Romans)"
                                        } else {
                                            "Ketik nama kitab (cth: Yohanes, Mazmur, Roma)"
                                        },
                                        color = Color(0xFF9CA3AF),
                                        fontSize = 14.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color(0xFF1F2937),
                                        fontSize = 14.sp
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("bible_picker_search_input")
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Rounded.Close,
                                    contentDescription = if (isEnglish) "Clear search" else "Bersihkan pencarian",
                                    tint = Color(0xFF6B7280),
                                    modifier = Modifier
                                        .size(20.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Two Testament Tabs: Perjanjian Baru (27) | Perjanjian Lama (39)
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TestamentTabButton(
                            label = if (isEnglish) "New Testament (27)" else "Perjanjian Baru (27)",
                            selected = selectedTestament == BibleTestament.PB,
                            onClick = {
                                selectedTestament = BibleTestament.PB
                                val firstPb = BibleCatalog.booksByTestament(BibleTestament.PB).first()
                                if (previewBook.testament != BibleTestament.PB) {
                                    previewBook = firstPb
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bible_tab_pb")
                        )
                        TestamentTabButton(
                            label = if (isEnglish) "Old Testament (39)" else "Perjanjian Lama (39)",
                            selected = selectedTestament == BibleTestament.PL,
                            onClick = {
                                selectedTestament = BibleTestament.PL
                                val firstPl = BibleCatalog.booksByTestament(BibleTestament.PL).first()
                                if (previewBook.testament != BibleTestament.PL) {
                                    previewBook = firstPl
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("bible_tab_pl")
                        )
                    }
                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // 2-Column Split Pane: Left Books List | Right Chapter Numbers Grid
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .weight(0.44f)
                                .fillMaxHeight()
                                .background(Color(0xFFFCFCFD)),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (booksForTab.isEmpty()) {
                                item {
                                    Text(
                                        text = if (isEnglish) "No matching book" else "Kitab tidak ditemukan",
                                        fontSize = 12.sp,
                                        color = Color(0xFF9CA3AF),
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            } else {
                                items(
                                    items = booksForTab,
                                    key = {
                                        when (it) {
                                            is PickerRowItem.Header -> "hdr_${it.title}"
                                            is PickerRowItem.BookRow -> "bk_${it.book.id}"
                                        }
                                    }
                                ) { row ->
                                    when (row) {
                                        is PickerRowItem.Header -> {
                                            Text(
                                                text = row.title,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.6.sp,
                                                color = Color(0xFF9CA3AF),
                                                modifier = Modifier.padding(
                                                    start = 8.dp,
                                                    top = 10.dp,
                                                    bottom = 4.dp
                                                )
                                            )
                                        }
                                        is PickerRowItem.BookRow -> {
                                            val isSelected = row.book.id == previewBook.id
                                            val displayName = if (isEnglish) row.book.nameEn else row.book.nameId
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .then(
                                                        if (isSelected) {
                                                            Modifier.background(
                                                                Brush.horizontalGradient(
                                                                    listOf(
                                                                        Color(0xFFF59E0B),
                                                                        Color(0xFFEA580C)
                                                                    )
                                                                )
                                                            )
                                                        } else {
                                                            Modifier.background(Color.Transparent)
                                                        }
                                                    )
                                                    .clickable { previewBook = row.book }
                                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                                                    .testTag("bible_book_item_${row.book.id}")
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(
                                                        text = displayName,
                                                        fontSize = 14.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else Color(0xFF374151),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis,
                                                        modifier = Modifier.weight(1f)
                                                    )
                                                    Spacer(Modifier.width(6.dp))
                                                    Text(
                                                        text = row.book.chaptersCount.toString(),
                                                        fontSize = 12.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = if (isSelected) Color(0xFFFFEDD5) else Color(0xFF9CA3AF)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        VerticalDivider(color = Color(0xFFE5E7EB))

                        // Right Column: Selected Book Header + 4-Column Chapter Grid
                        Column(
                            modifier = Modifier
                                .weight(0.56f)
                                .fillMaxHeight()
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isEnglish) previewBook.nameEn else previewBook.nameId,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1F2937),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (isEnglish) {
                                            "Choose chapter (1 - ${previewBook.chaptersCount})"
                                        } else {
                                            "Pilih nomor pasal (1 - ${previewBook.chaptersCount})"
                                        },
                                        fontSize = 12.sp,
                                        color = Color(0xFF6B7280)
                                    )
                                }
                                Spacer(Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF3F4F6)
                                ) {
                                    Text(
                                        text = if (isEnglish) previewBook.categoryEn else previewBook.categoryId,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF6B7280),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.height(14.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(4),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(previewChapterList, key = { it }) { ch ->
                                    val isActive = previewBook.id == currentBook.id && ch == currentChapter
                                    Surface(
                                        modifier = Modifier
                                            .height(48.dp)
                                            .clickable {
                                                onSelectBookChapter(previewBook, ch)
                                            }
                                            .testTag("bible_chapter_btn_${previewBook.id}_$ch"),
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isActive) Color(0xFFFFFBEB) else Color.White,
                                        border = BorderStroke(
                                            width = if (isActive) 1.5.dp else 1.dp,
                                            color = if (isActive) Color(0xFFD97706) else Color(0xFFE5E7EB)
                                        )
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = ch.toString(),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isActive) Color(0xFFB45309) else Color(0xFF374151)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFE5E7EB))

                    // Bottom Helper + Tutup Button
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFAFAFA))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isEnglish) {
                                "Tap any chapter number to open verses immediately"
                            } else {
                                "Sentuh angka pasal untuk langsung membuka ayat"
                            },
                            fontSize = 12.sp,
                            color = Color(0xFF6B7280),
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .clickable(onClick = onDismiss)
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("bible_picker_footer_close")
                        ) {
                            Text(
                                text = if (isEnglish) "Close" else "Tutup",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                        }
                    }
                }
            }
        }
    }

    if (useDialogWindow) {
        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            sheetContent()
        }
    } else {
        sheetContent()
    }
}

@Composable
private fun TestamentTabButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(top = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color(0xFFD97706) else Color(0xFF6B7280),
            modifier = Modifier.padding(bottom = 10.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.5.dp)
                .background(if (selected) Color(0xFFD97706) else Color.Transparent)
        )
    }
}
