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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ChildStudent
import com.example.data.model.VisitHelper
import com.example.data.model.VisitLaunchStage
import com.example.data.model.VisitRecord
import com.example.data.model.VisitSession
import com.example.data.model.VisitSessionSwap
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun VisitSessionSwapDialog(
    student: ChildStudent,
    currentSession: VisitSession,
    allSessions: List<VisitSession>,
    priorRecordInCurrentSession: VisitRecord? = null,
    existingIncomingSwap: VisitSessionSwap? = null,
    onConfirmSwap: (targetSession: VisitSession, reason: String, clearPriorCheckIn: Boolean) -> Unit,
    onRevertSwap: ((VisitSessionSwap) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val locale = s.language.locale
    val dateFmt = remember(locale) { SimpleDateFormat("EEE, d MMM yyyy", locale) }
    val timeFmt = remember(locale) { SimpleDateFormat("HH:mm", locale) }

    val primaryIndigo = Color(0xFF1D4ED8)
    val tealPrimary = Color(0xFF0F766E)

    val candidateTargets = remember(allSessions, currentSession.id) {
        allSessions
            .filter { it.isActive && it.id != currentSession.id }
            .sortedWith(
                compareBy<VisitSession> { it.getVisitDatesList().firstOrNull() ?: Long.MAX_VALUE }
                    .thenByDescending { it.monthYear }
                    .thenBy { it.title }
            )
    }

    var selectedTargetId by remember(candidateTargets) {
        mutableStateOf(candidateTargets.firstOrNull()?.id)
    }
    var reasonInput by remember(existingIncomingSwap?.reason) {
        mutableStateOf(existingIncomingSwap?.reason.orEmpty())
    }
    var clearPriorCheckIn by remember(priorRecordInCurrentSession?.id) {
        mutableStateOf(true)
    }

    val selectedTarget = candidateTargets.firstOrNull { it.id == selectedTargetId }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        ),
        modifier = Modifier
            .fillMaxWidth(0.94f)
            .systemBarsPadding()
            .imePadding()
            .padding(vertical = 12.dp)
            .testTag("dialog_visit_session_swap"),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = primaryIndigo,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isEn) "Move Visit Session" else "Tukar Sesi Kunjungan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = student.name,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primaryIndigo,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("btn_close_swap_dialog")
                ) {
                    Icon(Icons.Default.Close, contentDescription = s.nav.close)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Info anak asuh & sesi saat ini
                val distLabel = VisitHelper.displayDistrict(VisitHelper.resolveStudentDistrictKey(student), isEn)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "$distLabel • ${student.classGrade.ifBlank { "-" }} • ${student.dormitory.ifBlank { "-" }}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val curDates = currentSession.getVisitDatesList().joinToString(", ") { dateFmt.format(Date(it)) }.ifBlank { "-" }
                        Text(
                            text = if (isEn) {
                                "Current Session: ${currentSession.title} ($curDates)"
                            } else {
                                "Sesi Saat Ini: ${currentSession.title} ($curDates)"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Peringatan lembut bila orang tua sudah terlanjur check-in (tetap diperbolehkan pindah!)
                if (priorRecordInCurrentSession != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("banner_swap_prior_checkin_warning"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                        border = BorderStroke(1.dp, Color(0xFFFCD34D))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isEn) {
                                        "Already checked in (${timeFmt.format(Date(priorRecordInCurrentSession.checkedInAt))}) — move is still allowed"
                                    } else {
                                        "Orang tua terlanjur check-in (${timeFmt.format(Date(priorRecordInCurrentSession.checkedInAt))}) — tetap boleh pindah"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { clearPriorCheckIn = !clearPriorCheckIn }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = clearPriorCheckIn,
                                    onCheckedChange = { clearPriorCheckIn = it },
                                    colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD97706)),
                                    modifier = Modifier.testTag("checkbox_clear_prior_checkin")
                                )
                                Text(
                                    text = if (isEn) {
                                        "Clear mistaken check-in in origin session so parent can check in cleanly on new date"
                                    } else {
                                        "Hapus catatan check-in di sesi asal agar bisa check-in ulang di tanggal baru"
                                    },
                                    fontSize = 11.sp,
                                    color = Color(0xFF78350F),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Opsi kembalikan ke sesi asal jika saat ini adalah hasil pindahan
                if (existingIncomingSwap != null && onRevertSwap != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
                        border = BorderStroke(1.dp, Color(0xFF99F6E4))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isEn) {
                                    "Moved from: ${existingIncomingSwap.fromSessionTitle.ifBlank { "Origin session" }}"
                                } else {
                                    "Pindahan dari: ${existingIncomingSwap.fromSessionTitle.ifBlank { "Sesi asal" }}"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = tealPrimary
                            )
                            OutlinedButton(
                                onClick = { onRevertSwap(existingIncomingSwap) },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 48.dp)
                                    .testTag("btn_revert_visit_swap")
                            ) {
                                Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp), tint = tealPrimary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isEn) "Restore to Original Session" else "Kembalikan ke Sesi Asal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = tealPrimary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = if (isEn) "Select Destination Session:" else "Pilih Sesi Kunjungan Tujuan:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (candidateTargets.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                    ) {
                        Text(
                            text = if (isEn) {
                                "No other active visit sessions are available yet. Ask School Admin to create another session first."
                            } else {
                                "Belum ada sesi kunjungan aktif lainnya. Hubungi Admin Sekolah untuk membuat sesi alternatif terlebih dahulu."
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    candidateTargets.forEach { target ->
                        val isSelected = selectedTargetId == target.id
                        val datesText = target.getVisitDatesList()
                            .joinToString(", ") { dateFmt.format(Date(it)) }
                            .ifBlank { "-" }
                        val stage = target.launchStage()
                        val stageBadge = when (stage) {
                            VisitLaunchStage.PARENT_LIVE -> if (isEn) "Live Ortu" else "Live Ortu"
                            VisitLaunchStage.WALI_PREVIEW -> if (isEn) "Wali Preview" else "Preview Wali"
                            VisitLaunchStage.DRAFT -> if (isEn) "Draft" else "Draf Admin"
                            VisitLaunchStage.CLOSED -> if (isEn) "Closed" else "Selesai"
                        }
                        Card(
                            onClick = { selectedTargetId = target.id },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("option_swap_target_${target.id}"),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) primaryIndigo else MaterialTheme.colorScheme.outlineVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTargetId = target.id },
                                    colors = RadioButtonDefaults.colors(selectedColor = primaryIndigo)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = target.title,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (stage == VisitLaunchStage.PARENT_LIVE) Color(0xFFDCFCE7) else Color(0xFFDBEAFE)
                                        ) {
                                            Text(
                                                text = stageBadge,
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (stage == VisitLaunchStage.PARENT_LIVE) Color(0xFF15803D) else primaryIndigo,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = primaryIndigo, modifier = Modifier.size(13.dp))
                                        Text(
                                            text = "$datesText • ${target.displayTimeRange()}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = primaryIndigo
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reasonInput,
                        onValueChange = { reasonInput = it },
                        label = { Text(if (isEn) "Reason (optional)" else "Alasan Pindah Jadwal (opsional)", fontSize = 12.sp) },
                        placeholder = {
                            Text(
                                if (isEn) "e.g., Parents available on date 25th" else "Mis. Orang tua hanya bisa hadir sesi tgl 25",
                                fontSize = 11.5.sp
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_swap_reason")
                    )
                }
            }
        },
        confirmButton = {
            if (selectedTarget != null) {
                Button(
                    onClick = { onConfirmSwap(selectedTarget, reasonInput, clearPriorCheckIn) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryIndigo),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("btn_confirm_visit_swap")
                ) {
                    Text(
                        text = if (isEn) "Move Session" else "Simpan Pindah Sesi",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("btn_cancel_visit_swap")
            ) {
                Text(s.cancel)
            }
        }
    )
}
