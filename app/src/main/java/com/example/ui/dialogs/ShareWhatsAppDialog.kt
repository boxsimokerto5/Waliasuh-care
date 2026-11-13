package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareWhatsAppDialog(
    record: MedicalRecord,
    schoolProfile: SchoolProfile = SchoolProfile(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val treatmentDateStr = record.treatmentDate?.let { DateUtils.formatFullDate(it) }
        ?: DateUtils.formatFullDate(record.planDate)

    val controlInfo = if (record.needsControl) {
        "🗓️ *Wajib Kontrol:* ${record.controlDate?.let { DateUtils.formatFullDate(it) } ?: "Sesuai anjuran dokter"}"
    } else {
        "✅ *Kontrol:* Tidak perlu kontrol (Tuntas)"
    }

    val activeSchoolTitle = schoolProfile.schoolName.trim().ifBlank { "Wali Asuh Care" }
    val activeShortTitle = schoolProfile.shortName.trim().ifBlank { activeSchoolTitle }

    val reportText = """
🏥 *LAPORAN PENGOBATAN ANAK ASUH*
*$activeSchoolTitle*
----------------------------------------
👦 *Nama:* ${record.studentName}
🏠 *Asrama:* ${record.studentDormitory}
📍 *Faskes:* ${record.faskes}
📅 *Waktu:* $treatmentDateStr

⚠️ *Keluhan:* ${record.complaint}
🩺 *Diagnosa:* ${record.diagnosis.ifBlank { "-" }}
💉 *Tindakan:* ${record.actionsTaken.ifBlank { "Pemeriksaan fisik" }}
💊 *Obat:* ${record.prescription.ifBlank { "-" }}
$controlInfo
👤 *Pendamping:* ${record.escortName.ifBlank { "Pengasuh $activeShortTitle" }}
${if (record.notes.isNotBlank()) "📝 *Catatan:* ${record.notes}" else ""}
----------------------------------------
_SIK Anak Asuh ${activeSchoolTitle}_
""".trimIndent()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Laporan WhatsApp",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Format pesan siap dikirim ke grup WhatsApp pengasuh / kepala asrama:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(
                        text = reportText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Laporan Pengobatan Anak Asuh", reportText)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Salin", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                        ) {
                            Text("Tutup", fontSize = 11.sp)
                        }
                    }

                    Button(
                        onClick = {
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                putExtra(Intent.EXTRA_TEXT, reportText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, "Kirim Laporan Pengobatan")
                            context.startActivity(shareIntent)
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("share_whatsapp_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kirim / Bagikan", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}
