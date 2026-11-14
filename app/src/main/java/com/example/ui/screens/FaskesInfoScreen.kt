package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord
import com.example.data.model.SchoolProfile
import com.example.util.i18n.LocalAppStrings

@Composable
fun FaskesInfoScreen(
    records: List<MedicalRecord>,
    schoolProfile: SchoolProfile = SchoolProfile(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isEn = LocalAppStrings.current.isEnglish
    val allFaskesDetails = remember(schoolProfile) { schoolProfile.getAllFaskesWithDetails() }
    val referralFaskesNames = remember(schoolProfile) { schoolProfile.getAllFaskes().drop(1) }

    val statFirst = referralFaskesNames.getOrNull(0) ?: schoolProfile.primaryFaskesName.ifBlank { "UKS Sekolah" }
    val statSecond = referralFaskesNames.getOrNull(1)
    val countFirst = records.count { it.faskes.contains(statFirst, ignoreCase = true) }
    val countSecond = if (statSecond != null) {
        records.count { it.faskes.contains(statSecond, ignoreCase = true) }
    } else {
        0
    }
    val otherCount = (records.size - countFirst - countSecond).coerceAtLeast(0)
    val schoolLabel = schoolProfile.shortName.ifBlank { schoolProfile.schoolName }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 680.dp),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isEn) "Faskes Visit Recap" else "Rekapitulasi Kunjungan Faskes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (isEn) "Total ${records.size} referrals for $schoolLabel students" else "Total ${records.size} rujukan anak asuh $schoolLabel",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CompactStatBox(
                                title = statFirst,
                                count = countFirst,
                                bgColor = Color(0xFF00695C).copy(alpha = 0.14f),
                                textColor = Color(0xFF00695C),
                                modifier = Modifier.weight(1f)
                            )
                            if (statSecond != null) {
                                CompactStatBox(
                                    title = statSecond,
                                    count = countSecond,
                                    bgColor = Color(0xFF2E7D32).copy(alpha = 0.14f),
                                    textColor = Color(0xFF2E7D32),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            CompactStatBox(
                                title = if (isEn) "Other Faskes" else "Faskes Lain",
                                count = otherCount,
                                bgColor = Color(0xFF512DA8).copy(alpha = 0.14f),
                                textColor = Color(0xFF512DA8),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = if (isEn) "Partner Health Facilities ($schoolLabel)" else "Fasilitas Kesehatan Mitra ($schoolLabel)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            itemsIndexed(allFaskesDetails) { index, info ->
                val isPrimary = index == 0
                val displayAddress = info.address.ifBlank {
                    if (schoolProfile.city.isNotBlank()) {
                        if (isEn) "${schoolProfile.city} Area" else "Wilayah ${schoolProfile.city}"
                    } else {
                        if (isEn) "Address not configured by School Admin" else "Alamat belum diisi Admin Sekolah"
                    }
                }
                val displaySubdistrict = if (isPrimary) {
                    if (isEn) "Primary Facility (Internal)" else "Faskes Utama (Internal Sekolah)"
                } else {
                    if (isEn) "Referral Faskes #$index • ${schoolProfile.city.ifBlank { schoolLabel }}" else "Faskes Rujukan #$index • ${schoolProfile.city.ifBlank { schoolLabel }}"
                }
                val displayHours = if (isPrimary) {
                    if (isEn) "Internal Asrama / School Service" else "Layanan Internal Asrama / Sekolah"
                } else {
                    if (isEn) "Per facility clinic / ER operating hours" else "Jadwal sesuai jam layanan / UGD faskes"
                }

                CompactFaskesDetailCard(
                    name = info.name,
                    subdistrict = displaySubdistrict,
                    address = displayAddress,
                    hours = displayHours,
                    phone = info.phone,
                    onCall = if (info.phone.isNotBlank()) {
                        {
                            val tel = info.phone.filter { it.isDigit() || it == '+' }
                            if (tel.isNotBlank()) {
                                runCatching {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                                    context.startActivity(intent)
                                }
                            }
                        }
                    } else null
                )
            }

            item {
                val referralText = if (referralFaskesNames.isNotEmpty()) {
                    referralFaskesNames.joinToString(" / ")
                } else {
                    schoolProfile.primaryFaskesName.ifBlank { if (isEn) "Referral Faskes" else "Faskes Rujukan" }
                }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Emergency,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isEn) "Emergency Health SOP — $schoolLabel" else "SOP Darurat Kesehatan $schoolLabel",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = if (isEn) {
                                "1. Fever > 38.5°C for over 24 hours or breathing difficulty: Refer immediately to $referralText ER."
                            } else {
                                "1. Demam > 38.5°C lebih dari 24 jam atau sesak nafas: Segera rujuk ke UGD $referralText."
                            },
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isEn) {
                                "2. Never administer antibiotics without a doctor's examination at the Faskes."
                            } else {
                                "2. Jangan memberikan obat antibiotik tanpa pemeriksaan dokter faskes."
                            },
                            fontSize = 11.sp
                        )
                        Text(
                            text = if (isEn) {
                                "3. Always check follow-up dates so students recover completely."
                            } else {
                                "3. Selalu periksa tanggal kontrol anak agar penyakit sembuh total."
                            },
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CompactStatBox(
    title: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .padding(vertical = 7.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$count",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = textColor
            )
            Text(
                text = title,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CompactFaskesDetailCard(
    name: String,
    subdistrict: String,
    address: String,
    hours: String,
    phone: String,
    onCall: (() -> Unit)? = null
) {
    val isEn = LocalAppStrings.current.isEnglish
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = subdistrict,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = address, fontSize = 11.sp)
            }

            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(text = hours, fontSize = 11.sp)
            }

            if (phone.isNotBlank() && onCall != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(
                        onClick = onCall,
                        modifier = Modifier
                            .heightIn(min = 40.dp)
                            .testTag("btn_call_${name.replace(" ", "_")}"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = if (isEn) "Call $name" else "Hubungi $name",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(if (isEn) "Call ($phone)" else "Hubungi ($phone)", fontSize = 11.sp)
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isEn) "Phone number not configured by School Admin" else "Nomor telepon belum diisi Admin Sekolah",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
