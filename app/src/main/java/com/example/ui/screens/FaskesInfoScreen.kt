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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MedicalRecord

@Composable
fun FaskesInfoScreen(
    records: List<MedicalRecord>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val watesCount = records.count { it.faskes.contains("Wates", ignoreCase = true) }
    val plosoklatenCount = records.count { it.faskes.contains("Plosoklaten", ignoreCase = true) }
    val otherCount = records.size - watesCount - plosoklatenCount

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
                // Header Card Statistik (Compact)
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
                            text = "Rekapitulasi Kunjungan Faskes",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Total $records.size rujukan anak asuh SRT 1 Kediri",
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
                                title = "Pusk. Wates",
                                count = watesCount,
                                bgColor = Color(0xFF00695C).copy(alpha = 0.14f),
                                textColor = Color(0xFF00695C),
                                modifier = Modifier.weight(1f)
                            )
                            CompactStatBox(
                                title = "Pusk. Plosoklaten",
                                count = plosoklatenCount,
                                bgColor = Color(0xFF2E7D32).copy(alpha = 0.14f),
                                textColor = Color(0xFF2E7D32),
                                modifier = Modifier.weight(1f)
                            )
                            CompactStatBox(
                                title = "Faskes Lain",
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
                    text = "Fasilitas Kesehatan Mitra Utama",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Card Puskesmas Wates (Compact)
            item {
                CompactFaskesDetailCard(
                    name = "Puskesmas Wates",
                    subdistrict = "Kec. Wates, Kab. Kediri",
                    address = "Jl. Raya Wates No. 45, Kab. Kediri",
                    hours = "Poli: 07.30 - 14.00 WIB • UGD: 24 Jam",
                    phone = "(0354) 441012",
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0354441012"))
                        context.startActivity(intent)
                    }
                )
            }

            // Card Puskesmas Plosoklaten (Compact)
            item {
                CompactFaskesDetailCard(
                    name = "Puskesmas Plosoklaten",
                    subdistrict = "Kec. Plosoklaten, Kab. Kediri",
                    address = "Jl. Raya Plosoklaten No. 12, Kab. Kediri",
                    hours = "Poli: 07.30 - 14.00 WIB • UGD: 24 Jam",
                    phone = "(0354) 479155",
                    onCall = {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0354479155"))
                        context.startActivity(intent)
                    }
                )
            }

            // SOP Asrama (Compact)
            item {
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
                                text = "SOP Darurat Kesehatan Asrama SRT 1",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        Text(
                            text = "1. Demam > 38.5°C lebih dari 24 jam atau sesak nafas: Segera rujuk ke UGD Puskesmas Wates / Plosoklaten.",
                            fontSize = 11.sp
                        )
                        Text(
                            text = "2. Jangan memberikan obat antibiotik tanpa pemeriksaan dokter puskesmas.",
                            fontSize = 11.sp
                        )
                        Text(
                            text = "3. Selalu periksa tanggal kontrol anak agar penyakit sembuh total.",
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
                color = textColor
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
    onCall: () -> Unit
) {
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onCall,
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("btn_call_${name.replace(" ", "_")}"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = null,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Hubungi ($phone)", fontSize = 11.sp)
                }
            }
        }
    }
}
