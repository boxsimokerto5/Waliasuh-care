package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.pocketbase.PocketBaseClient
import com.example.data.remote.pocketbase.PocketBaseConfig
import com.example.data.remote.pocketbase.PocketBaseSchemaProvider
import com.example.ui.HealthViewModel
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PocketBaseConfigDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val appStrings = LocalAppStrings.current
    val isEn = appStrings.isEnglish

    var urlInput by remember { mutableStateOf(PocketBaseConfig.getBaseUrl(context)) }

    val isConfigured by viewModel.isCloudConfigured.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val activeSchoolId = schoolProfile.tenantCode.ifBlank { "SRT1-KDR" }
    val activeSchoolStudentsCount = remember(students, activeSchoolId) {
        students.count { it.schoolId.equals(activeSchoolId, ignoreCase = true) }
    }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessStatus by remember { mutableStateOf(true) }
    var showSchemaInfo by remember { mutableStateOf(false) }

    val lastSync = PocketBaseConfig.getLastSyncTime(context)
    val schemaJsonText = PocketBaseSchemaProvider.POCKETBASE_COLLECTIONS_JSON

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
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
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isEn) "PocketBase Server Connection" else "Koneksi Server PocketBase",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isEn) {
                                "Cloud Sync & Online Checklists across Wali Asuh"
                            } else {
                                "Sinkronisasi Cloud & Checklist Online antar Wali Asuh"
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isConfigured) Color(0xFF10B981).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isConfigured) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (isConfigured) Color(0xFF10B981) else Color.Gray,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isConfigured) {
                                    if (isEn) "Connected" else "Terkoneksi"
                                } else {
                                    if (isEn) "Not Configured" else "Belum Diatur"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isConfigured) Color(0xFF065F46) else Color.Gray
                            )
                        }
                    }
                }

                HorizontalDivider()

                if (statusMessage != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSuccessStatus) Color(0xFF10B981).copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = statusMessage ?: "",
                            fontSize = 12.sp,
                            color = if (isSuccessStatus) Color(0xFF047857) else MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text(if (isEn) "PocketBase Server URL" else "URL Server PocketBase") },
                    placeholder = { Text(PocketBaseConfig.EMBEDDED_URL) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            PocketBaseConfig.saveBaseUrl(context, urlInput)
                            PocketBaseClient.invalidateCache()
                            viewModel.testCloudConnection { success, msg ->
                                isSuccessStatus = success
                                statusMessage = msg
                            }
                        },
                        enabled = !isSyncing && urlInput.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (isEn) "Checking..." else "Memeriksa...")
                        } else {
                            Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isEn) "Save & Test Connection" else "Simpan & Uji Koneksi")
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            PocketBaseConfig.resetToDefaultUrl(context)
                            urlInput = PocketBaseConfig.EMBEDDED_URL
                            PocketBaseClient.invalidateCache()
                            Toast.makeText(
                                context,
                                if (isEn) "URL restored to default server." else "URL dikembalikan ke server default.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Restore,
                            contentDescription = if (isEn) "Reset URL" else "Reset URL",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (lastSync > 0) {
                    Text(
                        text = if (isEn) {
                            "Last synced: ${DateUtils.formatDateTime(lastSync, appStrings.language)}"
                        } else {
                            "Terakhir disinkronkan: ${DateUtils.formatDateTime(lastSync, appStrings.language)}"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                OutlinedButton(
                    onClick = {
                        viewModel.syncAllWithCloud { success, msg ->
                            isSuccessStatus = success
                            statusMessage = msg
                        }
                    },
                    enabled = !isSyncing && isConfigured,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        if (isEn) {
                            "Sync All School Data ($activeSchoolId)"
                        } else {
                            "Sinkronkan Semua Data Sekolah ($activeSchoolId)"
                        }
                    )
                }

                OutlinedButton(
                    onClick = {
                        viewModel.uploadAllStudentsToCloud { success, msg ->
                            isSuccessStatus = success
                            statusMessage = msg
                        }
                    },
                    enabled = !isSyncing && isConfigured,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    val targetSchool = schoolProfile.shortName.ifBlank { activeSchoolId }
                    Text(
                        if (isEn) {
                            "Upload $activeSchoolStudentsCount Students ($targetSchool)"
                        } else {
                            "Unggah $activeSchoolStudentsCount Data Siswa $targetSchool"
                        }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isEn) {
                                        "PocketBase Collections Schema (${PocketBaseSchemaProvider.COLLECTIONS.size} Tables)"
                                    } else {
                                        "Skema Koleksi PocketBase (${PocketBaseSchemaProvider.COLLECTIONS.size} Tabel)"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = PocketBaseSchemaProvider.SCHEMA_VERSION,
                                    fontSize = 10.5.sp,
                                    color = WaliPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("PocketBase Collections JSON", schemaJsonText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(
                                        context,
                                        if (isEn) {
                                            "9 PocketBase collections schema JSON copied!"
                                        } else {
                                            "JSON skema 9 koleksi PocketBase berhasil disalin!"
                                        },
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = if (isEn) "Copy Schema JSON" else "Salin JSON Skema",
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = { showSchemaInfo = !showSchemaInfo },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showSchemaInfo) {
                                    if (isEn) "Hide Collections JSON" else "Sembunyikan JSON Koleksi"
                                } else {
                                    if (isEn) "View 9 Collections Schema JSON" else "Lihat JSON Skema 9 Koleksi"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showSchemaInfo) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                                    .horizontalScroll(rememberScrollState())
                            ) {
                                SelectionContainer {
                                    Text(
                                        text = schemaJsonText,
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isEn) "Done" else "Selesai")
                    }
                }
            }
        }
    }
}
