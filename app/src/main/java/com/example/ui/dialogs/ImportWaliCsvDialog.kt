package com.example.ui.dialogs

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
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.WaliAccount
import com.example.ui.theme.WaliPrimary
import com.example.util.PasswordGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportWaliCsvDialog(
    onDismiss: () -> Unit,
    onImportConfirm: (accountList: List<WaliAccount>, replace: Boolean) -> Unit
) {
    var csvText by remember { mutableStateOf("") }
    var replaceExisting by remember { mutableStateOf(false) }
    var parsedAccounts by remember { mutableStateOf<List<WaliAccount>>(emptyList()) }
    var parseError by remember { mutableStateOf<String?>(null) }

    fun parseCsv(text: String): List<WaliAccount> {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return emptyList()

        val result = mutableListOf<WaliAccount>()
        val startIdx = if (lines.first().contains("Nama", ignoreCase = true) || lines.first().contains("Username", ignoreCase = true)) 1 else 0

        for (i in startIdx until lines.size) {
            val line = lines[i]
            val tokens = line.split(",").map { it.trim().removeSurrounding("\"") }
            if (tokens.isNotEmpty()) {
                val name = tokens.getOrNull(0) ?: ""
                if (name.isBlank() || name.equals("Nama", ignoreCase = true)) continue

                val dorm = tokens.getOrNull(1)?.ifBlank { "Asrama Putra" } ?: "Asrama Putra"
                val phone = tokens.getOrNull(2) ?: ""
                val customUser = tokens.getOrNull(3)
                val customPass = tokens.getOrNull(4)

                val username = if (!customUser.isNullOrBlank()) {
                    customUser
                } else {
                    name.lowercase().replace(" ", "").filter { it.isLetterOrDigit() }.ifBlank { "wali${i + 1}" }
                }

                val password = if (!customPass.isNullOrBlank()) {
                    customPass
                } else {
                    PasswordGenerator.generateRandomPassword()
                }

                result.add(
                    WaliAccount(
                        name = name,
                        username = username,
                        password = password,
                        dormitory = dorm,
                        phone = phone
                    )
                )
            }
        }
        return result
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 640.dp)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.GroupAdd,
                        contentDescription = null,
                        tint = WaliPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Impor Akun Wali Asuh (CSV)",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Format: Nama, Asrama, No WA, Username (opsional), Password (opsional)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedTextField(
                    value = csvText,
                    onValueChange = {
                        csvText = it
                        try {
                            parsedAccounts = parseCsv(it)
                            parseError = null
                        } catch (e: Exception) {
                            parseError = e.message
                        }
                    },
                    label = { Text("Isi Data CSV Akun Wali") },
                    placeholder = { Text("Ahmad Fauzi, Asrama Putra, 081234567890\nSari Devi, Asrama Putri, 081234567891") },
                    minLines = 4,
                    maxLines = 8,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("csv_wali_input")
                )

                if (parsedAccounts.isNotEmpty()) {
                    Text(
                        text = "Terdeteksi ${parsedAccounts.size} akun wali asuh siap diimpor.",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = WaliPrimary
                    )
                }

                if (parseError != null) {
                    Text(
                        text = "Error parsing: $parseError",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = replaceExisting,
                        onCheckedChange = { replaceExisting = it }
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Gantikan (hapus) seluruh akun wali lama sebelum impor",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Tutup")
                    }

                    Button(
                        onClick = {
                            if (parsedAccounts.isNotEmpty()) {
                                onImportConfirm(parsedAccounts, replaceExisting)
                            }
                        },
                        enabled = parsedAccounts.isNotEmpty(),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("confirm_import_wali_btn")
                    ) {
                        Text("Impor (${parsedAccounts.size})")
                    }
                }
            }
        }
    }
}
