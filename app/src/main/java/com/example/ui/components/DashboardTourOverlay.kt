package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateRectAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.DashboardIconKey
import kotlin.math.roundToInt

enum class TourTargetId {
    HERO_CARE,
    ICON_KLAIM_ANAK_ASUH,
    ICON_BEROBAT,
    ICON_KUNJUNGAN,
    ICON_ORANG_TUA,
    ICON_ANAK_ASUH,
    ICON_PUASA,
    ICON_HAID,
    ICON_PESAN,
    ICON_MENGAJI,
    ICON_SHOLAT,
    ICON_TABUNGAN,
    ICON_KONSELING,
    ICON_JADWAL_WALI,
    ICON_INFO_PENTING,
    ICON_BIBLE,
    ICON_KRISTEN,
    TOUR_REPLAY_BUTTON
}

fun DashboardIconKey.toTourTargetId(): TourTargetId = when (this) {
    DashboardIconKey.BEROBAT -> TourTargetId.ICON_BEROBAT
    DashboardIconKey.KUNJUNGAN -> TourTargetId.ICON_KUNJUNGAN
    DashboardIconKey.ORANG_TUA -> TourTargetId.ICON_ORANG_TUA
    DashboardIconKey.ANAK_ASUH -> TourTargetId.ICON_ANAK_ASUH
    DashboardIconKey.PUASA -> TourTargetId.ICON_PUASA
    DashboardIconKey.HAID -> TourTargetId.ICON_HAID
    DashboardIconKey.PESAN -> TourTargetId.ICON_PESAN
    DashboardIconKey.MENGAJI -> TourTargetId.ICON_MENGAJI
    DashboardIconKey.SHOLAT -> TourTargetId.ICON_SHOLAT
    DashboardIconKey.TABUNGAN -> TourTargetId.ICON_TABUNGAN
    DashboardIconKey.KONSELING -> TourTargetId.ICON_KONSELING
    DashboardIconKey.JADWAL_WALI -> TourTargetId.ICON_JADWAL_WALI
    DashboardIconKey.KLAIM_ANAK_ASUH -> TourTargetId.ICON_KLAIM_ANAK_ASUH
    DashboardIconKey.INFO_PENTING -> TourTargetId.ICON_INFO_PENTING
    DashboardIconKey.BIBLE -> TourTargetId.ICON_BIBLE
    DashboardIconKey.KRISTEN -> TourTargetId.ICON_KRISTEN
}

data class DashboardTourStep(
    val targetId: TourTargetId,
    val chapter: String,
    val title: String,
    val description: String,
    val accentColor: Color,
    val iconRes: Int? = null,
    val fallbackIcon: ImageVector = Icons.Default.AutoAwesome,
    val dashboardIconKey: DashboardIconKey? = null
)

class TourCoordinateRegistry {
    private var rootCoordinates: LayoutCoordinates? = null
    val targets = mutableStateMapOf<TourTargetId, Rect>()

    fun updateRoot(coords: LayoutCoordinates) {
        if (coords.isAttached) {
            rootCoordinates = coords
        }
    }

    fun updateTarget(id: TourTargetId, coords: LayoutCoordinates) {
        val root = rootCoordinates ?: return
        if (!root.isAttached || !coords.isAttached) return
        val rect = runCatching { root.localBoundingBoxOf(coords, clipBounds = false) }.getOrNull() ?: return
        if (rect.width > 0f && rect.height > 0f) {
            targets[id] = rect
        }
    }
}

fun Modifier.tourTarget(
    registry: TourCoordinateRegistry?,
    id: TourTargetId
): Modifier = if (registry == null) {
    this
} else {
    this.onGloballyPositioned { coords ->
        registry.updateTarget(id, coords)
    }
}

fun buildDashboardTourSteps(isEn: Boolean): List<DashboardTourStep> {
    return listOf(
        DashboardTourStep(
            targetId = TourTargetId.HERO_CARE,
            chapter = if (isEn) "Start" else "Mulai",
            title = if (isEn) "Today's Care & Faskes" else "Pantau Berobat Hari Ini",
            description = if (isEn) {
                "See sick students, inpatient status across referral facilities, and tap + Plan for quick scheduling."
            } else {
                "Cek antrean santri sakit, status rawat inap per faskes, dan buat + Rencana dalam 1 ketukan."
            },
            accentColor = Color(0xFF047857),
            fallbackIcon = Icons.Default.LocalHospital
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_KLAIM_ANAK_ASUH,
            chapter = if (isEn) "Foundation" else "Fondasi",
            title = if (isEn) "Claim Foster Students First!" else "Klaim Anak Asuh (Mulai Sini!)",
            description = if (isEn) {
                "Select your assigned foster students here first so Parent Connect, Savings & Counseling unlock."
            } else {
                "Klaim anak asuh binaan Anda di sini terlebih dahulu agar Hubungan Ortu, Tabungan & Konseling aktif."
            },
            accentColor = Color(0xFF4338CA),
            iconRes = R.drawable.img_menu_klaim_anak_asuh,
            dashboardIconKey = DashboardIconKey.KLAIM_ANAK_ASUH
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_BEROBAT,
            chapter = if (isEn) "Health" else "Kesehatan",
            title = if (isEn) "Medical Treatment Hub" else "Layanan Berobat Lengkap",
            description = if (isEn) {
                "Manage clinic queues, log direct treatment, view medical history & referral faskes directory."
            } else {
                "Kelola rencana berobat, input tindakan medis langsung, rekam medis & daftar faskes rujukan."
            },
            accentColor = Color(0xFF0284C7),
            iconRes = R.drawable.img_menu_rencana,
            dashboardIconKey = DashboardIconKey.BEROBAT
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_KUNJUNGAN,
            chapter = if (isEn) "Care" else "Kunjungan",
            title = if (isEn) "Parent Visit & Attendance" else "Jadwal & Hadir Kunjungan",
            description = if (isEn) {
                "Create visit sessions, scan student barcodes for check-in, and view district-level visit recaps."
            } else {
                "Buat sesi kunjungan wali/ortu, scan barcode kehadiran, hingga cek rekap per kecamatan."
            },
            accentColor = Color(0xFF1D4ED8),
            iconRes = R.drawable.img_menu_kunjungan,
            dashboardIconKey = DashboardIconKey.KUNJUNGAN
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_ORANG_TUA,
            chapter = if (isEn) "Connect" else "Komunikasi",
            title = if (isEn) "Parent Connect & Chat" else "Hubungan Orang Tua",
            description = if (isEn) {
                "Share parent portal login cards via WhatsApp, control visible modules, and chat 2-way safely."
            } else {
                "Bagikan kartu akses login ortu ke WhatsApp, atur izin modul, dan chat 2 arah secara aman."
            },
            accentColor = Color(0xFF4F46E5),
            iconRes = R.drawable.img_menu_orang_tua,
            dashboardIconKey = DashboardIconKey.ORANG_TUA
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_ANAK_ASUH,
            chapter = if (isEn) "Directory" else "Direktori",
            title = if (isEn) "All Foster Students" else "Daftar Seluruh Anak Asuh",
            description = if (isEn) {
                "Search all student profiles, dormitories, NIS, medical histories & generate barcode ID cards."
            } else {
                "Cari profil lengkap semua santri, kelas/asrama, riwayat sakit, serta cetak kartu barcode siswa."
            },
            accentColor = Color(0xFF7E22CE),
            iconRes = R.drawable.img_menu_anak_asuh,
            dashboardIconKey = DashboardIconKey.ANAK_ASUH
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_PUASA,
            chapter = if (isEn) "Worship" else "Ibadah",
            title = if (isEn) "Fasting Tracker & Ranking" else "Puasa Sunnah & Wajib",
            description = if (isEn) {
                "Scan barcodes to record fasting, check dorm attendance checklists & view fasting leaderboards."
            } else {
                "Catat santri berpuasa via scan barcode, pantau checklist asrama & ranking rajin puasa."
            },
            accentColor = Color(0xFF0F766E),
            iconRes = R.drawable.img_menu_input_puasa,
            dashboardIconKey = DashboardIconKey.PUASA
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_HAID,
            chapter = if (isEn) "Health" else "Kesehatan",
            title = if (isEn) "Period & Fiqh Monitoring" else "Catatan & Fiqih Haid",
            description = if (isEn) {
                "Track cycle start & purity dates, automatic >15-day Istihadhah alerts & fiqh prayer guides."
            } else {
                "Catat siklus haid & masa suci santriwati, peringatan otomatis istihadhah >15 hari & panduan fiqih."
            },
            accentColor = Color(0xFFDB2777),
            iconRes = R.drawable.img_menu_daftar_haid,
            dashboardIconKey = DashboardIconKey.HAID
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_PESAN,
            chapter = if (isEn) "Handover" else "Estafet",
            title = if (isEn) "Shift Handover Notes" else "Pesan Shift Pengasuh",
            description = if (isEn) {
                "Leave urgent reminders or follow-up tasks so the next duty caregiver never misses anything."
            } else {
                "Tinggalkan catatan penting & tugas prioritas agar pergantian wali shift berikutnya tetap nyambung."
            },
            accentColor = Color(0xFFB45309),
            iconRes = R.drawable.img_menu_pesan_shift,
            dashboardIconKey = DashboardIconKey.PESAN
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_MENGAJI,
            chapter = if (isEn) "Worship" else "Ibadah",
            title = if (isEn) "Qur'an Recitation Log" else "Mengaji & Setoran",
            description = if (isEn) {
                "Log IQRO/Qur'an progress with automatic page continuation and shareable recitation cards."
            } else {
                "Catat setoran Iqro/Al-Qur'an, auto-lanjut halaman terakhir, dan ekspor kartu mengaji."
            },
            accentColor = Color(0xFF047857),
            iconRes = R.drawable.img_menu_mengaji,
            dashboardIconKey = DashboardIconKey.MENGAJI
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_SHOLAT,
            chapter = if (isEn) "Worship" else "Ibadah",
            title = if (isEn) "5 Daily Prayers Attendance" else "Absen Sholat 5 Waktu",
            description = if (isEn) {
                "Mark Fajr to Isha congregation attendance in 1 tap, auto-detect active period Udzur, print PDF & sync to Parents."
            } else {
                "Catat absen Subuh–Isya 1 ketuk, otomatis deteksi Udzur Haid, ekspor PDF & langsung terhubung ke Portal Ortu."
            },
            accentColor = Color(0xFF059669),
            iconRes = R.drawable.img_menu_sholat,
            dashboardIconKey = DashboardIconKey.SHOLAT
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_KRISTEN,
            chapter = if (isEn) "Faith Care" else "Agama Kristen",
            title = if (isEn) "Christian Worship & Portal Sync" else "Ibadah & Pembinaan Agama Kristen",
            description = if (isEn) {
                "Claim Christian students (auto-exempt from Sholat/Mengaji, Puasa kept), log morning/evening/church worship with photos & auto-enable Parent Portal."
            } else {
                "Klaim anak asuh Kristen (otomatis dikecualikan dari Sholat/Mengaji, Puasa tetap aktif), catat Ibadah Pagi/Malam/Gereja + foto & aktifkan Portal Ortu."
            },
            accentColor = Color(0xFF4338CA),
            iconRes = R.drawable.img_menu_kristen,
            dashboardIconKey = DashboardIconKey.KRISTEN
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_TABUNGAN,
            chapter = if (isEn) "Private" else "Privat",
            title = if (isEn) "Private Student Savings" else "Tabungan Anak Binaan",
            description = if (isEn) {
                "Manage deposits & pocket-money expenses privately—strictly visible only for your claimed students."
            } else {
                "Catat titip masuk & pengeluaran saku anak binaan secara privat (tersembunyi dari wali lain)."
            },
            accentColor = Color(0xFFD97706),
            iconRes = R.drawable.img_menu_tabungan,
            dashboardIconKey = DashboardIconKey.TABUNGAN
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_KONSELING,
            chapter = if (isEn) "Confidential" else "Rahasia",
            title = if (isEn) "Confidential Counseling" else "Konseling & Perkembangan",
            description = if (isEn) {
                "Store private mentoring notes, mood check-ins & follow-up plans hidden from everyone else."
            } else {
                "Simpan catatan curhat, tingkat perhatian & rencana pendampingan dengan privasi mutlak."
            },
            accentColor = Color(0xFF6D28D9),
            iconRes = R.drawable.img_menu_konseling,
            dashboardIconKey = DashboardIconKey.KONSELING
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_JADWAL_WALI,
            chapter = if (isEn) "Roster" else "Jadwal",
            title = if (isEn) "Wali Asuh Duty Schedule" else "Jadwal Kerja Wali Asuh",
            description = if (isEn) {
                "View daily grouped shifts, pinned tasks & health facility duties, monthly matrix & official PDF export."
            } else {
                "Cek piket harian, pin tugas & faskes, petugas jam aktif, matriks bulanan hingga ekspor PDF resmi."
            },
            accentColor = Color(0xFF0369A1),
            iconRes = R.drawable.img_menu_jadwal,
            dashboardIconKey = DashboardIconKey.JADWAL_WALI
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_INFO_PENTING,
            chapter = if (isEn) "Channel" else "Pengumuman",
            title = if (isEn) "Info-Info Penting Channel" else "Channel Info-Info Penting",
            description = if (isEn) {
                "Read global official broadcasts (text, photos, videos, PDFs, links), react & reply in comments."
            } else {
                "Baca pengumuman resmi lintas sekolah (teks, foto, video, dokumen, link), suka/tidak suka & balas komentar."
            },
            accentColor = Color(0xFF0F766E),
            iconRes = R.drawable.img_menu_info_penting,
            dashboardIconKey = DashboardIconKey.INFO_PENTING
        ),
        DashboardTourStep(
            targetId = TourTargetId.ICON_BIBLE,
            chapter = if (isEn) "Bible" else "Alkitab",
            title = if (isEn) "Digital Bible & Personal Notes" else "Bible (Alkitab & Catatan)",
            description = if (isEn) {
                "Read all 66 Bible books in IND/ENG with audio reader, devotionals, stories & synced personal notes."
            } else {
                "Baca 66 kitab Alkitab dwibahasa IND/ENG lengkap dengan audio suara, renungan, kisah & catatan tersinkron."
            },
            accentColor = Color(0xFFB45309),
            iconRes = R.drawable.img_menu_bible,
            dashboardIconKey = DashboardIconKey.BIBLE
        ),
        DashboardTourStep(
            targetId = TourTargetId.TOUR_REPLAY_BUTTON,
            chapter = if (isEn) "Ready!" else "Siap!",
            title = if (isEn) "Replay Tour Anytime" else "Putar Ulang Tur Kapan Saja",
            description = if (isEn) {
                "You're all set! Tap this Tour button or visit Profile → User Guide whenever you want a refresher."
            } else {
                "Semua fitur siap dipakai! Ketuk tombol Tur di sini atau lewat Profil → Panduan bila ingin mengulang."
            },
            accentColor = Color(0xFF0284C7),
            fallbackIcon = Icons.Default.HelpOutline
        )
    )
}

@Composable
fun DashboardTourOverlay(
    visible: Boolean,
    steps: List<DashboardTourStep>,
    currentStepIndex: Int,
    registry: TourCoordinateRegistry,
    isEn: Boolean,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && steps.isNotEmpty(),
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        val safeIndex = currentStepIndex.coerceIn(0, steps.lastIndex)
        val step = steps[safeIndex]
        val rawRect = registry.targets[step.targetId]
        val density = LocalDensity.current

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .testTag("dashboard_tour_overlay")
        ) {
            val containerWidthPx = with(density) { maxWidth.toPx() }
            val containerHeightPx = with(density) { maxHeight.toPx() }
            val spotlightPaddingPx = with(density) { 8.dp.toPx() }
            val fallbackRect = remember(containerWidthPx, containerHeightPx) {
                Rect(
                    left = containerWidthPx * 0.25f,
                    top = containerHeightPx * 0.25f,
                    right = containerWidthPx * 0.75f,
                    bottom = containerHeightPx * 0.38f
                )
            }
            val targetSpotlightRect = if (rawRect != null) {
                Rect(
                    left = (rawRect.left - spotlightPaddingPx).coerceAtLeast(4f),
                    top = (rawRect.top - spotlightPaddingPx).coerceAtLeast(4f),
                    right = (rawRect.right + spotlightPaddingPx).coerceAtMost(containerWidthPx - 4f),
                    bottom = (rawRect.bottom + spotlightPaddingPx).coerceAtMost(containerHeightPx - 4f)
                )
            } else {
                fallbackRect
            }

            val animatedRect by animateRectAsState(
                targetValue = targetSpotlightRect,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                ),
                label = "spotlight_rect"
            )
            val animatedProgress by animateFloatAsState(
                targetValue = (safeIndex + 1).toFloat() / steps.size.toFloat(),
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "tour_progress"
            )

            // Scrim Backdrop + Glowing Rounded Cutout (klik di luar tooltip tidak tembus ke dashboard)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {}
                    )
                    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
            ) {
                drawRect(color = Color(0xCC0F172A))

                if (rawRect != null) {
                    val cornerPx = 16.dp.toPx()
                    val topLeft = Offset(animatedRect.left, animatedRect.top)
                    val rectSize = Size(animatedRect.width, animatedRect.height)

                    // Lubang Spotlight terang
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                        blendMode = BlendMode.Clear
                    )

                    // Bingkai cahaya senada tema fitur
                    drawRoundRect(
                        color = step.accentColor.copy(alpha = 0.35f),
                        topLeft = Offset(topLeft.x - 3.dp.toPx(), topLeft.y - 3.dp.toPx()),
                        size = Size(rectSize.width + 6.dp.toPx(), rectSize.height + 6.dp.toPx()),
                        cornerRadius = CornerRadius(cornerPx + 3.dp.toPx(), cornerPx + 3.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawRoundRect(
                        color = Color.White,
                        topLeft = topLeft,
                        size = rectSize,
                        cornerRadius = CornerRadius(cornerPx, cornerPx),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }

            // Penempatan kartu tooltip otomatis di atas atau di bawah area sorotan
            val spotlightCenterY = animatedRect.center.y
            val placeBelow = rawRect == null || spotlightCenterY < containerHeightPx * 0.52f
            val cardMarginPx = with(density) { 14.dp.toPx() }
            val approxCardHeightPx = with(density) { 215.dp.toPx() }
            val desiredTopPx = if (placeBelow) {
                (animatedRect.bottom + cardMarginPx).coerceAtMost(containerHeightPx - approxCardHeightPx - cardMarginPx)
            } else {
                (animatedRect.top - approxCardHeightPx - cardMarginPx).coerceAtLeast(cardMarginPx)
            }.coerceIn(cardMarginPx, (containerHeightPx - approxCardHeightPx - cardMarginPx).coerceAtLeast(cardMarginPx))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, desiredTopPx.roundToInt()) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 520.dp)
                        .testTag("card_tour_tooltip"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.2.dp, step.accentColor.copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                    ) {
                        LinearProgressIndicator(
                            progress = { animatedProgress },
                            color = step.accentColor,
                            trackColor = step.accentColor.copy(alpha = 0.14f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 11.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Header Tooltip: Thumbnail Ikon + Bab + Counter + Lewati
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(11.dp))
                                            .background(step.accentColor.copy(alpha = 0.12f))
                                            .border(1.dp, step.accentColor.copy(alpha = 0.3f), RoundedCornerShape(11.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (step.iconRes != null) {
                                            Image(
                                                painter = painterResource(id = step.iconRes),
                                                contentDescription = step.title,
                                                modifier = Modifier
                                                    .size(32.dp)
                                                    .clip(RoundedCornerShape(9.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = step.fallbackIcon,
                                                contentDescription = step.title,
                                                tint = step.accentColor,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = step.accentColor.copy(alpha = 0.13f)
                                            ) {
                                                Text(
                                                    text = step.chapter,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = step.accentColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.5.dp)
                                                )
                                            }
                                            Text(
                                                text = "${safeIndex + 1} / ${steps.size}",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.testTag("text_tour_step_counter")
                                            )
                                        }
                                        Text(
                                            text = step.title,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("text_tour_title")
                                        )
                                    }
                                }

                                TextButton(
                                    onClick = onSkip,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("btn_tour_skip")
                                ) {
                                    Text(
                                        text = if (isEn) "Skip" else "Lewati",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            // Penjelasan singkat anti-bosan
                            Text(
                                text = step.description,
                                fontSize = 12.sp,
                                lineHeight = 16.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            // Mini Dot Indicator + Navigasi Sebelumnya / Lanjut
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    steps.forEachIndexed { idx, _ ->
                                        val active = idx == safeIndex
                                        Box(
                                            modifier = Modifier
                                                .width(if (active) 14.dp else 5.dp)
                                                .height(5.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (active) step.accentColor
                                                    else step.accentColor.copy(alpha = 0.22f)
                                                )
                                        )
                                    }
                                }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (safeIndex > 0) {
                                        OutlinedButton(
                                            onClick = onPrev,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .heightIn(min = 48.dp)
                                                .testTag("btn_tour_prev")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = if (isEn) "Previous" else "Sebelumnya",
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }

                                    val isLast = safeIndex >= steps.lastIndex
                                    Button(
                                        onClick = onNext,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = step.accentColor,
                                            contentColor = Color.White
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                        modifier = Modifier
                                            .heightIn(min = 48.dp)
                                            .testTag("btn_tour_next")
                                    ) {
                                        Text(
                                            text = if (isLast) {
                                                if (isEn) "Start!" else "Mulai Pakai!"
                                            } else {
                                                if (isEn) "Next" else "Lanjut"
                                            },
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (isLast) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.ArrowForward,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
