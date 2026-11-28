package com.example.ui.dialogs

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SchoolProfile
import com.example.util.i18n.LocalAppStrings

private data class FaskesPastelStyle(
    val cardBgLight: Color,
    val cardBgDark: Color,
    val borderLight: Color,
    val borderDark: Color,
    val badgeBgLight: Color,
    val badgeBgDark: Color,
    val accentLight: Color,
    val accentDark: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FaskesReferralSheet(
    schoolProfile: SchoolProfile,
    title: String? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val s = LocalAppStrings.current
    val isEn = s.isEnglish
    val isLight = MaterialTheme.colorScheme.surface.luminance() > 0.5f
    val resolvedTitle = title ?: if (isEn) "Referral Puskesmas Facilities" else "Puskesmas Langganan Rujukan"
    val allFaskes = schoolProfile.getAllFaskesWithDetails()
    val hasIncompleteInfo = allFaskes.drop(1).any { it.address.isBlank() || it.phone.isBlank() }

    val sheetBg = if (isLight) Color(0xFFFFF8FA) else MaterialTheme.colorScheme.surface

    val faskesPalettes = listOf(
        // Faskes Utama (Mint Emerald Lembut)
        FaskesPastelStyle(
            cardBgLight = Color(0xFFF0FDF6),
            cardBgDark = Color(0xFF10251C),
            borderLight = Color(0xFF86EFAC),
            borderDark = Color(0xFF16A34A).copy(alpha = 0.45f),
            badgeBgLight = Color(0xFFDCFCE7),
            badgeBgDark = Color(0xFF14532D).copy(alpha = 0.7f),
            accentLight = Color(0xFF15803D),
            accentDark = Color(0xFF6EE7B7)
        ),
        // Rujukan #1 (Sky Cerah Lembut)
        FaskesPastelStyle(
            cardBgLight = Color(0xFFF0F9FF),
            cardBgDark = Color(0xFF112230),
            borderLight = Color(0xFF7DD3FC),
            borderDark = Color(0xFF0284C7).copy(alpha = 0.45f),
            badgeBgLight = Color(0xFFE0F2FE),
            badgeBgDark = Color(0xFF0C4A6E).copy(alpha = 0.7f),
            accentLight = Color(0xFF0369A1),
            accentDark = Color(0xFF7DD3FC)
        ),
        // Rujukan #2 (Lavender Ungu Lembut)
        FaskesPastelStyle(
            cardBgLight = Color(0xFFF7F5FF),
            cardBgDark = Color(0xFF1D1A30),
            borderLight = Color(0xFFC4B5FD),
            borderDark = Color(0xFF7C3AED).copy(alpha = 0.45f),
            badgeBgLight = Color(0xFFEDE9FE),
            badgeBgDark = Color(0xFF4C1D95).copy(alpha = 0.7f),
            accentLight = Color(0xFF6D28D9),
            accentDark = Color(0xFFC4B5FD)
        ),
        // Rujukan #3 (Peach Amber Hangat)
        FaskesPastelStyle(
            cardBgLight = Color(0xFFFFFBF0),
            cardBgDark = Color(0xFF282012),
            borderLight = Color(0xFFFCD34D),
            borderDark = Color(0xFFD97706).copy(alpha = 0.45f),
            badgeBgLight = Color(0xFFFEF3C7),
            badgeBgDark = Color(0xFF78350F).copy(alpha = 0.7f),
            accentLight = Color(0xFFB45309),
            accentDark = Color(0xFFFCD34D)
        ),
        // Cadangan (Teal Mint Lembut)
        FaskesPastelStyle(
            cardBgLight = Color(0xFFF0FDFA),
            cardBgDark = Color(0xFF112423),
            borderLight = Color(0xFF5EEAD4),
            borderDark = Color(0xFF0D9488).copy(alpha = 0.45f),
            badgeBgLight = Color(0xFFCCFBF1),
            badgeBgDark = Color(0xFF134E4A).copy(alpha = 0.7f),
            accentLight = Color(0xFF0F766E),
            accentDark = Color(0xFF5EEAD4)
        )
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null,
        containerColor = sheetBg
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 600.dp)
            ) {
                // Header Gradien Rose–Coral Lembut Memanjakan Mata
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF9F1239),
                                    Color(0xFFBE123C),
                                    Color(0xFFE11D48)
                                )
                            )
                        )
                        .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.48f))
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(1.5.dp, Color(0xFFFECDD3), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalHospital,
                                contentDescription = null,
                                tint = Color(0xFFBE123C),
                                modifier = Modifier.size(23.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = resolvedTitle,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.5.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isEn) {
                                    "Registered Faskes • ${schoolProfile.schoolName}"
                                } else {
                                    "Fasilitas kesehatan terdaftar • ${schoolProfile.schoolName}"
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFFFFE4E6),
                                maxLines = 2,
                                lineHeight = 15.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = s.close,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                // Isi Kartu Faskes Berwarna Pastel Lembut
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    allFaskes.forEachIndexed { index, info ->
                        val isPrimaryInternal = index == 0
                        val palette = faskesPalettes[index % faskesPalettes.size]
                        val accentColor = if (isLight) palette.accentLight else palette.accentDark
                        val badgeBg = if (isLight) palette.badgeBgLight else palette.badgeBgDark
                        val cardBg = if (isLight) palette.cardBgLight else palette.cardBgDark
                        val borderColor = if (isLight) palette.borderLight else palette.borderDark

                        val displayTitle = if (isPrimaryInternal) {
                            info.name
                        } else {
                            "$index. ${info.name}"
                        }
                        val badgeLabel = if (isPrimaryInternal) {
                            if (isEn) "Primary Faskes" else "Faskes Utama"
                        } else {
                            if (isEn) "Referral #$index" else "Rujukan #$index"
                        }
                        val resolvedAddress = when {
                            info.address.isNotBlank() -> info.address
                            schoolProfile.city.isNotBlank() -> if (isEn) "${schoolProfile.city} Area" else "Wilayah ${schoolProfile.city}"
                            else -> if (isEn) "Address not set by School Admin" else "Alamat belum diisi Admin Sekolah"
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("faskes_sheet_card_$index"),
                            colors = CardDefaults.cardColors(containerColor = cardBg),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(13.dp),
                                verticalArrangement = Arrangement.spacedBy(7.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(end = 8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(badgeBg),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (isPrimaryInternal) Icons.Default.MedicalServices else Icons.Default.LocalHospital,
                                                contentDescription = null,
                                                tint = accentColor,
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                        Text(
                                            text = displayTitle,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(9.dp),
                                        color = badgeBg,
                                        border = BorderStroke(0.6.dp, borderColor.copy(alpha = 0.7f))
                                    ) {
                                        Text(
                                            text = badgeLabel,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = accentColor,
                                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.Top,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = accentColor.copy(alpha = 0.85f),
                                        modifier = Modifier
                                            .size(15.dp)
                                            .padding(top = 1.dp)
                                    )
                                    Text(
                                        text = resolvedAddress,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Call,
                                            contentDescription = null,
                                            tint = accentColor.copy(alpha = 0.85f),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Text(
                                            text = if (info.phone.isNotBlank()) {
                                                if (isEn) "Phone: ${info.phone}" else "Telepon: ${info.phone}"
                                            } else {
                                                if (isEn) "Phone not set by Admin" else "Telepon belum diisi Admin"
                                            },
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (info.phone.isNotBlank()) {
                                        OutlinedButton(
                                            onClick = {
                                                val tel = info.phone.filter { it.isDigit() || it == '+' }
                                                if (tel.isNotBlank()) {
                                                    runCatching {
                                                        context.startActivity(
                                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:$tel"))
                                                        )
                                                    }
                                                }
                                            },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, borderColor),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                containerColor = badgeBg.copy(alpha = 0.55f),
                                                contentColor = accentColor
                                            ),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            modifier = Modifier
                                                .heightIn(min = 38.dp)
                                                .testTag("btn_dial_faskes_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Call,
                                                contentDescription = if (isEn) "Call ${info.name}" else "Hubungi ${info.name}",
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                if (isEn) "Call" else "Hubungi",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (hasIncompleteInfo) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isLight) Color(0xFFFFFBEB) else Color(0xFF282115),
                            border = BorderStroke(
                                1.dp,
                                if (isLight) Color(0xFFFDE68A) else Color(0xFFD97706).copy(alpha = 0.4f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null,
                                    tint = if (isLight) Color(0xFFB45309) else Color(0xFFFBBF24),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isEn) {
                                        "School Admin can complete Faskes address & phone details via School Admin -> Edit School Profile."
                                    } else {
                                        "Admin Sekolah dapat melengkapi alamat & nomor telepon faskes melalui menu Admin Sekolah → Edit Profil Sekolah."
                                    },
                                    fontSize = 11.5.sp,
                                    lineHeight = 15.5.sp,
                                    color = if (isLight) Color(0xFF78350F) else Color(0xFFFDE68A)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFBE123C),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Text(
                                text = s.close,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
