package com.example.ui.dialogs

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChildStudent
import com.example.data.model.SchoolProfile
import com.example.data.model.TabunganHelper
import com.example.data.model.TabunganLog
import com.example.data.model.TabunganTransaction
import com.example.util.DateUtils
import com.example.util.PdfReportGenerator
import com.example.util.i18n.LocalAppStrings
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TabunganHistoryLogsSheet(
    transactions: List<TabunganTransaction>,
    logs: List<TabunganLog>,
    studentsForSummary: List<ChildStudent>,
    waliName: String,
    schoolProfile: SchoolProfile,
    initialTab: Int = 0,
    onOpenStudentById: (Long) -> Unit,
    onEditTransaction: (TabunganTransaction) -> Unit = {},
    onCancelTransaction: (TabunganTransaction, String) -> Unit,
    onDeleteTransaction: (TabunganTransaction) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish

    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var searchQuery by remember { mutableStateOf("") }
    var txFilter by remember { mutableIntStateOf(0) } // 0=All, 1=IN, 2=OUT, 3=CANCELLED
    var logFilter by remember { mutableStateOf("ALL") }

    var txToCancel by remember { mutableStateOf<TabunganTransaction?>(null) }
    var cancelReason by remember { mutableStateOf("") }
    var txToDelete by remember { mutableStateOf<TabunganTransaction?>(null) }
    var readyPdfFile by remember { mutableStateOf<File?>(null) }

    val filteredTransactions = remember(transactions, searchQuery, txFilter) {
        val q = searchQuery.trim().lowercase()
        transactions
            .filter { tx ->
                val matchFilter = when (txFilter) {
                    1 -> TabunganHelper.isIncome(tx.transactionType) && TabunganHelper.isActive(tx.status)
                    2 -> !TabunganHelper.isIncome(tx.transactionType) && TabunganHelper.isActive(tx.status)
                    3 -> !TabunganHelper.isActive(tx.status)
                    else -> true
                }
                val matchSearch = q.isEmpty() ||
                    tx.studentName.lowercase().contains(q) ||
                    tx.title.lowercase().contains(q) ||
                    tx.notes.lowercase().contains(q) ||
                    tx.ownerWaliName.lowercase().contains(q)
                matchFilter && matchSearch
            }
            .sortedWith(compareByDescending<TabunganTransaction> { it.transactionDate }.thenByDescending { it.createdAt })
    }

    val filteredLogs = remember(logs, searchQuery, logFilter) {
        val q = searchQuery.trim().lowercase()
        logs
            .filter { log ->
                val matchFilter = logFilter == "ALL" || log.actionType.equals(logFilter, ignoreCase = true)
                val matchSearch = q.isEmpty() ||
                    log.studentName.lowercase().contains(q) ||
                    log.summary.lowercase().contains(q) ||
                    log.actorName.lowercase().contains(q)
                matchFilter && matchSearch
            }
            .sortedByDescending { it.createdAt }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 2.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(4.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "Savings History & Audit Log" else "Riwayat & Log Tabungan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(12.dp))
                            Text(
                                text = if (isEn) "Private audit trail of all entries, cancellations & deletions" else "Catatan pribadi seluruh mutasi, pembatalan & penghapusan",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            val pdf = PdfReportGenerator.generateTabunganSummaryPdf(
                                context = context,
                                students = studentsForSummary,
                                transactions = transactions,
                                waliName = waliName,
                                schoolProfile = schoolProfile
                            )
                            if (pdf != null) {
                                readyPdfFile = pdf
                            } else {
                                Toast.makeText(
                                    context,
                                    if (isEn) "Failed to generate recap PDF." else "Gagal membuat PDF rekap tabungan.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("btn_tabungan_history_rekap_pdf")
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFF0F766E), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("PDF", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F766E))
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_tabungan_history")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = s.nav.close)
                    }
                }

                TabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("tab_tabungan_history_tx"),
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ReceiptLong, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isEn) "Transactions (${transactions.size})" else "Mutasi (${transactions.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .testTag("tab_tabungan_history_logs"),
                        content = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isEn) "Audit Log (${logs.size})" else "Log Audit (${logs.size})",
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                }

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(if (isEn) "Search student, description, or notes..." else "Cari nama siswa, keterangan, atau catatan...", fontSize = 12.5.sp)
                    },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_tabungan_history")
                )

                if (selectedTab == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(selected = txFilter == 0, onClick = { txFilter = 0 }, label = { Text(if (isEn) "All" else "Semua", fontSize = 11.sp) })
                        FilterChip(selected = txFilter == 1, onClick = { txFilter = 1 }, label = { Text(if (isEn) "In (+)" else "Masuk (+)", fontSize = 11.sp) })
                        FilterChip(selected = txFilter == 2, onClick = { txFilter = 2 }, label = { Text(if (isEn) "Out (-)" else "Keluar (-)", fontSize = 11.sp) })
                        FilterChip(selected = txFilter == 3, onClick = { txFilter = 3 }, label = { Text(if (isEn) "Cancelled" else "Dibatalkan", fontSize = 11.sp) })
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FilterChip(selected = logFilter == "ALL", onClick = { logFilter = "ALL" }, label = { Text(if (isEn) "All" else "Semua", fontSize = 11.sp) })
                        FilterChip(selected = logFilter == TabunganHelper.LOG_INPUT_IN, onClick = { logFilter = TabunganHelper.LOG_INPUT_IN }, label = { Text(if (isEn) "Deposit" else "Masuk", fontSize = 11.sp) })
                        FilterChip(selected = logFilter == TabunganHelper.LOG_INPUT_OUT, onClick = { logFilter = TabunganHelper.LOG_INPUT_OUT }, label = { Text(if (isEn) "Expense" else "Keluar", fontSize = 11.sp) })
                        FilterChip(selected = logFilter == TabunganHelper.LOG_EDIT, onClick = { logFilter = TabunganHelper.LOG_EDIT }, label = { Text(if (isEn) "Edited" else "Ubah", fontSize = 11.sp) })
                        FilterChip(selected = logFilter == TabunganHelper.LOG_CANCEL, onClick = { logFilter = TabunganHelper.LOG_CANCEL }, label = { Text(if (isEn) "Cancelled" else "Batal", fontSize = 11.sp) })
                        FilterChip(selected = logFilter == TabunganHelper.LOG_DELETE, onClick = { logFilter = TabunganHelper.LOG_DELETE }, label = { Text(if (isEn) "Deleted" else "Hapus", fontSize = 11.sp) })
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                        .testTag("list_tabungan_history_content"),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedTab == 0) {
                        if (filteredTransactions.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "No matching transactions found." else "Belum ada riwayat transaksi yang cocok.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            items(filteredTransactions, key = { "hist_tx_${it.id}" }) { tx ->
                                val isActive = TabunganHelper.isActive(tx.status)
                                val isIn = TabunganHelper.isIncome(tx.transactionType)
                                val sign = if (isIn) "+" else "-"
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onOpenStudentById(tx.studentId) }
                                        .testTag("card_tabungan_history_tx_${tx.id}"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isActive) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                                    ),
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            !isActive -> MaterialTheme.colorScheme.outlineVariant
                                            isIn -> Color(0xFFBBF7D0)
                                            else -> Color(0xFFFECACA)
                                        }
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(10.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = tx.studentName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "$sign${TabunganHelper.formatRupiah(tx.amount)}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                textDecoration = if (isActive) TextDecoration.None else TextDecoration.LineThrough,
                                                color = when {
                                                    !isActive -> MaterialTheme.colorScheme.outline
                                                    isIn -> Color(0xFF15803D)
                                                    else -> Color(0xFFDC2626)
                                                }
                                            )
                                        }
                                        Text(
                                            text = buildString {
                                                append(tx.title.ifBlank { if (isIn) "Titip Masuk" else "Pengeluaran" })
                                                if (tx.notes.isNotBlank()) append(" • ${tx.notes}")
                                            },
                                            fontSize = 11.5.sp,
                                            textDecoration = if (isActive) TextDecoration.None else TextDecoration.LineThrough,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        if (!isActive) {
                                            Text(
                                                text = if (isEn) "Cancelled: ${tx.cancelledReason.ifBlank { "-" }}" else "Dibatalkan: ${tx.cancelledReason.ifBlank { "-" }}",
                                                fontSize = 10.5.sp,
                                                color = Color(0xFFB45309),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "${DateUtils.formatDateTime(tx.transactionDate)} • ${tx.ownerWaliName}",
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.weight(1f),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                                if (isActive) {
                                                    TextButton(
                                                        onClick = { onEditTransaction(tx) },
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier
                                                            .heightIn(min = 48.dp)
                                                            .testTag("btn_edit_tabungan_history_tx_${tx.id}")
                                                    ) {
                                                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color(0xFF4338CA), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(if (isEn) "Edit" else "Ubah", fontSize = 11.sp, color = Color(0xFF4338CA))
                                                    }
                                                    TextButton(
                                                        onClick = {
                                                            cancelReason = ""
                                                            txToCancel = tx
                                                        },
                                                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                        modifier = Modifier.heightIn(min = 48.dp)
                                                    ) {
                                                        Icon(Icons.Default.Cancel, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(14.dp))
                                                        Spacer(modifier = Modifier.width(3.dp))
                                                        Text(if (isEn) "Cancel" else "Batalkan", fontSize = 11.sp, color = Color(0xFFB45309))
                                                    }
                                                }
                                                TextButton(
                                                    onClick = { txToDelete = tx },
                                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier.heightIn(min = 48.dp)
                                                ) {
                                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFDC2626), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(if (isEn) "Delete" else "Hapus", fontSize = 11.sp, color = Color(0xFFDC2626))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        if (filteredLogs.isEmpty()) {
                            item {
                                Text(
                                    text = if (isEn) "No audit logs recorded yet." else "Belum ada log audit tabungan tercatat.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            items(filteredLogs, key = { "hist_log_${it.id}" }) { log ->
                                TabunganLogCard(
                                    log = log,
                                    isEn = isEn,
                                    onStudentClick = { onOpenStudentById(log.studentId) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    txToCancel?.let { targetTx ->
        AlertDialog(
            onDismissRequest = { txToCancel = null },
            title = { Text(if (isEn) "Cancel Transaction?" else "Batalkan Transaksi?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isEn) {
                            "Cancel ${targetTx.studentName}'s transaction (${TabunganHelper.formatRupiah(targetTx.amount)})?"
                        } else {
                            "Batalkan transaksi ${targetTx.studentName} senilai ${TabunganHelper.formatRupiah(targetTx.amount)}?"
                        },
                        fontSize = 12.5.sp
                    )
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text(if (isEn) "Reason (Optional)" else "Alasan (Opsional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onCancelTransaction(targetTx, cancelReason)
                        txToCancel = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309))
                ) {
                    Text(if (isEn) "Yes, Cancel" else "Ya, Batalkan")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToCancel = null }) {
                    Text(if (isEn) "Back" else "Kembali")
                }
            }
        )
    }

    txToDelete?.let { targetTx ->
        AlertDialog(
            onDismissRequest = { txToDelete = null },
            title = { Text(if (isEn) "Delete Transaction Permanently?" else "Hapus Transaksi Permanen?") },
            text = {
                Text(
                    text = if (isEn) {
                        "Delete ${targetTx.studentName}'s transaction (${TabunganHelper.formatRupiah(targetTx.amount)})? Logged privately in Tabungan Audit Log."
                    } else {
                        "Hapus transaksi ${targetTx.studentName} (${TabunganHelper.formatRupiah(targetTx.amount)})? Jejak tetap tercatat di Log Pribadi Tabungan."
                    },
                    fontSize = 12.5.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteTransaction(targetTx)
                        txToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isEn) "Delete Permanently" else "Hapus Permanen")
                }
            },
            dismissButton = {
                TextButton(onClick = { txToDelete = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }

    readyPdfFile?.let { file ->
        AlertDialog(
            onDismissRequest = { readyPdfFile = null },
            title = { Text(if (isEn) "Savings Recap PDF Ready" else "PDF Rekap Tabungan Siap") },
            text = {
                Text(
                    if (isEn) "Choose to open the private savings recap PDF or share it."
                    else "Pilih untuk membuka atau membagikan dokumen PDF rekap tabungan pribadi."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.openPdfFile(context, file)
                    }
                ) {
                    Text(if (isEn) "Open PDF" else "Buka PDF")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        readyPdfFile = null
                        PdfReportGenerator.shareFastingPdf(
                            context = context,
                            pdfFile = file,
                            subject = if (isEn) "Private Savings Recap" else "Rekap Tabungan Siswa",
                            text = if (isEn) "Attached is the private savings balance recap PDF." else "Terlampir rekap saldo tabungan anak binaan."
                        )
                    }
                ) {
                    Text(if (isEn) "Share PDF" else "Bagikan")
                }
            }
        )
    }
}
