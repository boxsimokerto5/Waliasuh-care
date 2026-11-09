package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
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
import com.example.util.DateUtils

@Composable
fun ShareWhatsAppDialog(
    record: MedicalRecord,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val treatmentDateStr = record.treatmentDate?.let { DateUtils.formatFullDate(it) }
        ?: DateUtils.formatFullDate(record.planDate)

    val controlInfo = if (record.needsControl) {
        "🗓️ *Wajib Kontrol:* ${record.controlDate?.let { DateUtils.formatFullDate(it) } ?: "Sesuai anjuran dokter"}"
    } else {
        "✅ *Kontrol:* Tidak perlu kontrol (Tuntas)"
    }

    val reportText = """
🏥 *LAPORAN PENGOBATAN ANAK ASUH*
*Sekolah Rakyat SRT 1 Kab. Kediri*
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
👤 *Pendamping:* ${record.escortName.ifBlank { "Pengasuh SRT 1" }}
${if (record.notes.isNotBlank()) "📝 *Catatan:* ${record.notes}" else ""}
----------------------------------------
_SIK Anak Asuh SRT 1 Kediri_
""".trimIndent()

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(max = 500.dp),
        title = {
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Format pesan siap dikirim ke grup WhatsApp pengasuh / kepala asrama:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = reportText,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, reportText)
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "Kirim Laporan Pengobatan")
                    context.startActivity(shareIntent)
                },
                modifier = Modifier.testTag("share_whatsapp_button"),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp)
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Kirim / Bagikan", fontSize = 11.sp)
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Laporan Pengobatan Anak Asuh", reportText)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                    },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("Salin", fontSize = 11.sp)
                }
                OutlinedButton(
                    onClick = onDismiss,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Text("Tutup", fontSize = 11.sp)
                }
            }
        }
    )
}
