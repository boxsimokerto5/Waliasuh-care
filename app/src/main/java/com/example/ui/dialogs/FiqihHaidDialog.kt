package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.util.i18n.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiqihHaidDialog(
    onDismiss: () -> Unit
) {
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = if (isEn) {
        listOf("Fiqih Haid", "Mandi Suci", "Worship & Dua", "Scan Guide")
    } else {
        listOf("Fiqih Haid", "Mandi Suci", "Amalan & Doa", "Panduan Scan")
    }

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
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFCCFBF1)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MenuBook,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isEn) "Fiqih Haid & Women's Guide" else "Panduan & Fiqih Haid Wanita",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isEn) "Islamic Shariah Rules & App Instructions" else "Kaidah Syariat Islam & Petunjuk Aplikasi",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("btn_close_fiqih_guide")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = s.nav.close,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tabs Navigation
                PrimaryTabRow(
                    selectedTabIndex = selectedTab,
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = Color.Transparent
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Box Scrollable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 240.dp, max = 460.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (selectedTab) {
                        0 -> FiqihHaidSection()
                        1 -> MandiSuciSection()
                        2 -> AmalanHaidSection()
                        3 -> PanduanScanSection()
                    }
                }
            }
        }
    }
}

@Composable
fun FiqihHaidSection() {
    val isEn = LocalAppStrings.current.isEnglish
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Banner Ringkasan
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF99F6E4))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "Haid Duration Limits (Shafi'i School)" else "Batas Waktu Haid Menurut Madzhab Syafi'i",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF115E59)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn) {
                        "Haid is natural uterine bleeding occurring in a healthy state—not caused by illness, childbirth, or injury."
                    } else {
                        "Haid adalah darah yang keluar dari rahim wanita dalam keadaan sehat, bukan karena penyakit, melahirkan, atau luka."
                    },
                    fontSize = 11.5.sp,
                    color = Color(0xFF134E4A)
                )
            }
        }

        // Kartu 1: Batas Minimal Haid
        FiqihCard(
            title = if (isEn) "1. Minimum Haid Duration" else "1. Batas Minimal Haid",
            highlight = if (isEn) "24 Hours (1 Day 1 Night)" else "24 Jam (1 Hari 1 Malam)",
            content = if (isEn) {
                "Bleeding lasting at least 24 hours, either continuously or accumulated intermittently within a 15-day window."
            } else {
                "Darah yang keluar sekurang-kurangnya selama 24 jam, baik keluar secara terus-menerus maupun terputus-putus dalam kurun waktu 15 hari asalkan totalnya mencapai 24 jam."
            },
            badgeColor = Color(0xFF0D9488)
        )

        // Kartu 2: Batas Maksimal Haid
        FiqihCard(
            title = if (isEn) "2. Maximum Haid Duration" else "2. Batas Maksimal Haid",
            highlight = if (isEn) "15 Days 15 Nights" else "15 Hari 15 Malam",
            content = if (isEn) {
                "The maximum Haid period is 15 days and 15 nights. Any bleeding continuing beyond 15 days is Istihadhah (non-menstrual bleeding); the student must perform Mandi Wajib and resume daily prayers."
            } else {
                "Maksimal masa haid adalah 15 hari 15 malam. Jika darah masih terus keluar melebihi 15 hari, maka darah selebihnya dihukumi sebagai darah ISTIHADHAH (darah penyakit). Siswi wajib mandi besar dan tetap melaksanakan sholat fardhu."
            },
            badgeColor = Color(0xFFE11D48)
        )

        // Kartu 3: Kebiasaan Umum
        FiqihCard(
            title = if (isEn) "3. Typical Cycle (Ghalib)" else "3. Kebiasaan Umum (Ghalib)",
            highlight = if (isEn) "6 or 7 Days" else "6 atau 7 Hari",
            content = if (isEn) {
                "Most women commonly experience a Haid duration of 6 or 7 days, usually recurring consistently each month."
            } else {
                "Mayoritas wanita umumnya mengalami masa haid selama 6 atau 7 hari. Siklus ini biasanya berulang secara konsisten setiap bulan."
            },
            badgeColor = Color(0xFF2563EB)
        )

        // Kartu 4: Batas Minimal Suci
        FiqihCard(
            title = if (isEn) "4. Minimum Suci Interval Between Cycles" else "4. Masa Suci Antara Dua Haid",
            highlight = if (isEn) "Min 15 Days 15 Nights" else "Minimal 15 Hari 15 Malam",
            content = if (isEn) {
                "The Suci interval between the end of one Haid and the start of the next must be at least 15 days and 15 nights. Bleeding before 15 Suci days is considered Istihadhah."
            } else {
                "Jarak suci antara akhir haid pertama dengan awal haid berikutnya sekurang-kurangnya adalah 15 hari 15 malam. Bila keluar darah sebelum genap 15 hari masa suci, darah tersebut dihukumi darah fasad/istihadhah."
            },
            badgeColor = Color(0xFF7C3AED)
        )
    }
}

@Composable
fun MandiSuciSection() {
    val isEn = LocalAppStrings.current.isEnglish
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Niat Mandi Wajib
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFDF4FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0ABFC))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "Intention (Niat) for Mandi Wajib After Haid" else "Niat Mandi Wajib Bersuci Setelah Haid",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp,
                    color = Color(0xFF86198F)
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Tulisan Arab
                Text(
                    text = "نَوَيْتُ الْغُسْلَ لِرَفْعِ حَدَثِ الْحَيْضِ فَرْضًا لِلّٰهِ تَعَالَى",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Right,
                    color = Color(0xFF701A75),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Latin
                Text(
                    text = "\"Nawaitul ghusla lirof'il hadatsil haidhi fardhol lillaahi ta'aalaa.\"",
                    fontSize = 11.5.sp,
                    fontStyle = FontStyle.Italic,
                    color = Color(0xFF86198F),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Artinya
                Text(
                    text = if (isEn) {
                        "Meaning: \"I intend to perform the purification bath (Mandi Wajib) to remove the major impurity of Haid, obligatory for the sake of Allah the Almighty.\""
                    } else {
                        "Artinya: \"Aku berniat mandi untuk menghilangkan hadats besar dari haid, fardhu karena Allah Ta'ala.\""
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF581C87)
                )
            }
        }

        // Rukun Mandi Wajib
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "2 Core Pillars of Mandi Wajib:" else "2 Rukun Pokok Mandi Wajib:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF166534)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn) {
                        "1. Intention (Niat) as water first touches the body.\n2. Washing all skin and hair evenly with pure purifying water (from roots to tips) without missing any spot."
                    } else {
                        "1. Niat saat air pertama kali menyentuh anggota tubuh.\n2. Meratakan air suci dan menyucikan ke seluruh permukaan kulit dan rambut (dari pangkal hingga ujung) tanpa ada yang terlewat."
                    },
                    fontSize = 11.5.sp,
                    color = Color(0xFF14532D)
                )
            }
        }

        // Sunnah-sunnah Mandi
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "Sunnah Practices of Mandi Wajib:" else "Sunnah-Sunnah Mandi Wajib:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEn) {
                        "• Recite Basmalah and wash both hands 3 times.\n• Clean any remaining traces before washing.\n• Perform complete Wudhu as for prayer.\n• Run wet fingers through hair roots 3 times.\n• Pour water over the head 3 times, then wash the right side of the body followed by the left."
                    } else {
                        "• Membaca Basmalah dan mencuci kedua tangan 3 kali.\n• Menghilangkan kotoran atau sisa darah pada kemaluan.\n• Berwudhu sempurna seperti wudhu hendak sholat.\n• Menyela pangkal rambut dengan jari basah 3 kali.\n• Mengguyur kepala 3 kali lalu mengguyur seluruh badan dimulai dari sisi kanan kemudian sisi kiri."
                    },
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AmalanHaidSection() {
    val isEn = LocalAppStrings.current.isEnglish
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Larangan saat haid
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "Prohibitions During Haid" else "Larangan Bagi Wanita yang Sedang Haid",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF991B1B)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn) {
                        "❌ Prayers (obligatory or sunnah)\n❌ Puasa (obligatory days must be Qadha later)\n❌ Touching or carrying the Qur'an Mushaf\n❌ Performing Tawaf around the Ka'bah\n❌ Staying inside a mosque (I'tikaf)"
                    } else {
                        "❌ Sholat (fardhu maupun sunnah)\n❌ Puasa (wajib diqadha di kemudian hari)\n❌ Memegang & membawa mushaf Al-Qur'an\n❌ Melakukan thawaf di Ka'bah\n❌ Berdiam diri (i'tikaf) di dalam masjid"
                    },
                    fontSize = 11.5.sp,
                    color = Color(0xFF7F1D1D)
                )
            }
        }

        // Amalan yang tetap berpahala
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "Rewarded Deeds Still Encouraged" else "Amalan Berpahala yang Tetap Dianjurkan",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF166534)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isEn) {
                        "✅ Increase morning & evening Dhikr, Tasbih, Tahmid, Tahlil\n✅ Recite Istighfar and Shalawat upon Prophet Muhammad SAW\n✅ Listen to Qur'an recitations\n✅ Study Islamic knowledge, Fiqih, Tafsir, and Hadith\n✅ Recite daily prayers and bedtime Dhikr\n✅ Give charity and help fellow Asrama friends"
                    } else {
                        "✅ Memperbanyak dzikir pagi & petang, tasbih, tahmid, tahlil\n✅ Membaca istighfar dan shalawat atas Nabi Muhammad SAW\n✅ Mendengarkan murottal & lantunan ayat suci Al-Qur'an\n✅ Mempelajari ilmu agama, fiqih, tafsir, dan hadits (Thalabul 'Ilmi)\n✅ Membaca doa harian dan dzikir sebelum tidur\n✅ Bersedekah dan membantu teman asrama yang membutuhkan"
                    },
                    fontSize = 11.5.sp,
                    color = Color(0xFF14532D)
                )
            }
        }
    }
}

@Composable
fun PanduanScanSection() {
    val isEn = LocalAppStrings.current.isEnglish
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isEn) "How to Scan Student Card Barcode" else "Cara Scan Barcode Kartu Siswa",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF0369A1)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isEn) {
                        "1. Open 'Input Haid' on the dashboard.\n2. Tap 'Scan Student Card' (or 'Select Student').\n3. If the student is still in Daftar Suci (< 15 days), an alert appears: confirm genuine Haid via tissue sample test before moving her to Daftar Haid.\n4. Pick 'Haid Day...' (1 to 15) and add notes if needed, then tap 'Save'.\n5. Timelines in Daftar Haid & Daftar Suci update automatically each day."
                    } else {
                        "1. Buka menu 'Input Haid' di layar dashboard.\n2. Klik 'Scan Kartu Siswa' (atau 'Pilih Siswi').\n3. Jika siswi masih di Daftar Suci (< 15 hari), muncul pop-up peringatan: pastikan siswi benar-benar haid dengan tes sampel darah menggunakan tisu sebelum memindahkannya ke Daftar Haid.\n4. Pilih 'Haid Hari ke-...' (1 s.d. 15) dan tambahkan catatan bila perlu, lalu tekan 'Simpan'.\n5. Timeline di Daftar Haid & Daftar Suci akan berjalan otomatis setiap hari."
                    },
                    fontSize = 11.5.sp,
                    color = Color(0xFF0C4A6E)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (isEn) "💡 Flexible Rules for Caregivers & Wali Asuh:" else "💡 Aturan Fleksibel untuk Pengasuh & Wali Asuh:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = Color(0xFFB45309)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isEn) {
                        "• If Haid ends earlier (e.g. day 6 or 8), tap 'Mandi Suci' in Daftar Haid to move immediately to Daftar Suci.\n• If it passes the 15-day maximum, the system moves the student automatically to Daftar Suci.\n• In Daftar Suci, the Suci Day timeline keeps counting so caregivers spot reports that are too early."
                    } else {
                        "• Jika haid sudah selesai lebih cepat (misal hari ke-6 atau ke-8), tekan tombol 'Mandi Suci' di Daftar Haid agar langsung pindah ke Daftar Suci.\n• Jika melewati batas maksimal 15 hari haid, sistem memindahkan siswi secara otomatis ke Daftar Suci.\n• Di Daftar Suci, timeline Hari Suci ke-... terus berjalan agar pengasuh langsung tahu bila siswi melapor haid terlalu cepat."
                    },
                    fontSize = 11.sp,
                    color = Color(0xFF92400E)
                )
            }
        }
    }
}

@Composable
fun FiqihCard(
    title: String,
    highlight: String,
    content: String,
    badgeColor: Color
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, badgeColor.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp)
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = highlight,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
