package com.example.ui.dialogs

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.filled.Code
import androidx.compose.material3.TextButton
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.remote.supabase.SupabaseClient
import com.example.data.remote.supabase.SupabaseConfig
import com.example.data.remote.supabase.SupabaseSchemaProvider
import com.example.ui.HealthViewModel
import com.example.ui.theme.WaliPrimary
import com.example.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupabaseConfigDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var urlInput by remember { mutableStateOf(SupabaseConfig.getSupabaseUrl(context)) }
    var keyInput by remember { mutableStateOf(SupabaseConfig.getSupabaseKey(context)) }
    var showKey by remember { mutableStateOf(false) }

    val isConfigured by viewModel.isSupabaseConfigured.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val students by viewModel.students.collectAsStateWithLifecycle()
    val schoolProfile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val activeSchoolId = schoolProfile.tenantCode.ifBlank { "SRT1-KDR" }
    val activeSchoolStudentsCount = remember(students, activeSchoolId) {
        students.count { it.schoolId.equals(activeSchoolId, ignoreCase = true) }
    }

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessStatus by remember { mutableStateOf(true) }
    var showSqlSchema by remember { mutableStateOf(false) }

    val lastSync = SupabaseConfig.getLastSyncTime(context)
    val sqlSchemaText = SupabaseSchemaProvider.FULL_SQL_SCHEMA

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Koneksi Database Supabase",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sinkronisasi Cloud PostgreSQL antar Wali Asuh",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Connection Status Badge
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
                                text = if (isConfigured) "Terkoneksi" else "Belum Diatur",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isConfigured) Color(0xFF065F46) else Color.Gray
                            )
                        }
                    }
                }

                HorizontalDivider()

                // Status Message Banner (if any)
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

                // Project URL Input
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    label = { Text("Project URL Supabase") },
                    placeholder = { Text("https://xyzproject.supabase.co") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Anon Key Input
                OutlinedTextField(
                    value = keyInput,
                    onValueChange = { keyInput = it },
                    label = { Text("Anon / Public API Key") },
                    placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                    singleLine = true,
                    visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKey = !showKey }) {
                            Icon(
                                imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Tampilkan Kunci"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                // Save & Test Button
                Button(
                    onClick = {
                        SupabaseConfig.saveCredentials(context, urlInput, keyInput)
                        SupabaseClient.invalidateCache()
                        viewModel.testSupabaseConnection { success, msg ->
                            isSuccessStatus = success
                            statusMessage = msg
                        }
                    },
                    enabled = !isSyncing && urlInput.isNotBlank() && keyInput.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Memeriksa Koneksi...")
                    } else {
                        Icon(imageVector = Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan & Uji Koneksi Supabase")
                    }
                }

                if (lastSync > 0) {
                    Text(
                        text = "Terakhir disinkronkan: ${DateUtils.formatDateTime(lastSync)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Action 1: Two-way full multi-tenant sync
                OutlinedButton(
                    onClick = {
                        viewModel.syncAllWithSupabase { success, msg ->
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
                    Text("Sinkronkan Semua Data Sekolah ($activeSchoolId)")
                }

                // Action 2: Upload active school's students & wali to Supabase
                OutlinedButton(
                    onClick = {
                        viewModel.uploadAllStudentsToSupabase { success, msg ->
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
                    Text("Unggah $activeSchoolStudentsCount Data Siswa ${schoolProfile.shortName.ifBlank { activeSchoolId }} ke Supabase")
                }

                Spacer(modifier = Modifier.height(4.dp))

                // SQL Script Section (Synced with SupabaseSchemaProvider)
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
                                    text = "Skrip SQL Editor Supabase (${SupabaseSchemaProvider.TABLES.size} Tabel)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Skema ${SupabaseSchemaProvider.SCHEMA_VERSION}",
                                    fontSize = 10.5.sp,
                                    color = WaliPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("SQL Schema", sqlSchemaText)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Skrip SQL lengkap berhasil disalin!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Salin Skrip SQL",
                                    tint = WaliPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = "Salin dan tempel di menu SQL Editor Supabase untuk membuat/memperbarui tabel child_students, medical_records, haid_records, wali_accounts, shift_messages, & school_registrations.",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        TextButton(
                            onClick = { showSqlSchema = !showSqlSchema },
                            modifier = Modifier.padding(0.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Code, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (showSqlSchema) "Sembunyikan Skrip SQL" else "Lihat Pratinjau Skrip SQL",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showSqlSchema) {
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
                                        text = sqlSchemaText,
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
                        Text("Selesai")
                    }
                }
            }
        }
    }
}
