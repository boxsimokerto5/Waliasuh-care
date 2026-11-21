package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.PdfReportGenerator
import java.io.File

data class ParentReadyPdfDialogData(
    val file: File,
    val title: String,
    val description: String,
    val shareSubject: String,
    val shareText: String
)

@Composable
fun ParentPdfReadyDialog(
    data: ParentReadyPdfDialogData,
    isEn: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("dialog_parent_pdf_ready"),
        title = {
            Text(
                text = data.title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(text = data.description)
        },
        confirmButton = {
            Button(
                onClick = {
                    PdfReportGenerator.openPdfFile(context, data.file)
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_parent_pdf_open")
            ) {
                Text(if (isEn) "Open PDF" else "Buka PDF")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = {
                        PdfReportGenerator.shareDocumentPdf(
                            context = context,
                            pdfFile = data.file,
                            subject = data.shareSubject,
                            text = data.shareText
                        )
                        onDismiss()
                    },
                    modifier = Modifier.testTag("btn_parent_pdf_share")
                ) {
                    Text(if (isEn) "Share" else "Bagikan")
                }
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("btn_parent_pdf_close")
                ) {
                    Text(if (isEn) "Close" else "Tutup")
                }
            }
        }
    )
}

@Composable
fun ParentMenuPdfHeaderCard(
    title: String,
    subtitle: String,
    accentColor: Color,
    containerColor: Color,
    borderColor: Color,
    isEn: Boolean,
    onDownloadPdf: () -> Unit,
    pdfButtonTestTag: String,
    buttonLabel: String? = null,
    selectedMonthOnly: Boolean? = null,
    onToggleMonthOnly: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedButton(
                    onClick = onDownloadPdf,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, accentColor),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag(pdfButtonTestTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = if (isEn) "Download PDF report" else "Unduh laporan PDF",
                        tint = accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = buttonLabel ?: if (isEn) "PDF Report" else "Unduh PDF",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                }
            }

            if (selectedMonthOnly != null && onToggleMonthOnly != null) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedMonthOnly,
                        onClick = { onToggleMonthOnly(true) },
                        label = {
                            Text(
                                text = if (isEn) "This Month" else "Bulan Ini",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.14f),
                            selectedLabelColor = accentColor
                        ),
                        modifier = Modifier.testTag("${pdfButtonTestTag}_filter_month")
                    )
                    FilterChip(
                        selected = !selectedMonthOnly,
                        onClick = { onToggleMonthOnly(false) },
                        label = {
                            Text(
                                text = if (isEn) "All History" else "Semua Riwayat",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.14f),
                            selectedLabelColor = accentColor
                        ),
                        modifier = Modifier.testTag("${pdfButtonTestTag}_filter_all")
                    )
                }
            }
        }
    }
}
