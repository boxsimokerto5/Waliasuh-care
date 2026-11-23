package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class LuxuryNavItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val testTag: String
)

private object LuxuryDockPalette {
    val DeepSapphireTop = Color(0xFF0B3B38)
    val DeepSapphireMid = Color(0xFF062726)
    val DeepSapphireBottom = Color(0xFF031919)

    val GoldBright = Color(0xFFFEF08A)
    val GoldWarm = Color(0xFFF59E0B)
    val GoldAmber = Color(0xFFD97706)

    val SapphireGlow = Color(0xFF34D399)
    val SapphireSoft = Color(0xFF6EE7B7)

    val InactiveTint = Color(0xFFD1E5E1).copy(alpha = 0.78f)
    val InactiveSubtle = Color(0xFFB6D4CE).copy(alpha = 0.72f)
}

/**
 * Floating 5-tab luxury dock with an organic elevated center medallion dome.
 * Styled in Deep Emerald-Sapphire velvet with warm Gold & Sapphire radial glow.
 */
@Composable
fun LuxuryCurvedCenterBottomBar(
    items: List<LuxuryNavItem>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier,
    centerIndex: Int = 2
) {
    require(items.size == 5) { "LuxuryCurvedCenterBottomBar expects exactly 5 items." }

    val animatedSelectedPos by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "luxury_dock_selected_pos"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .height(88.dp)
                .testTag("bottom_nav_bar")
        ) {
            // Custom curved dock background + metallic rim + gold/sapphire radial glow
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height

                val pillTop = 22.dp.toPx()
                val pillBottom = h - 4.dp.toPx()
                val pillHeight = pillBottom - pillTop
                val cornerR = pillHeight / 2f

                val cx = w / 2f
                val domeR = minOf(33.dp.toPx(), w * 0.145f)
                val domeCenterY = pillTop + 12.dp.toPx()
                val neckHalfW = domeR + 18.dp.toPx()
                val cradleDip = 4.dp.toPx()

                // 20 deg below horizontal on each side of the dome circle -> ~200..340 deg arc
                val joinSin = 0.34202f
                val joinCos = 0.93969f
                val joinLeftX = cx - domeR * joinCos
                val joinRightX = cx + domeR * joinCos
                val joinY = domeCenterY - domeR * joinSin

                val dockPath = Path().apply {
                    moveTo(cornerR, pillTop)
                    lineTo(cx - neckHalfW, pillTop)
                    cubicTo(
                        cx - domeR * 1.18f, pillTop,
                        cx - domeR * 1.03f, joinY + 2.dp.toPx(),
                        joinLeftX, joinY
                    )
                    arcTo(
                        rect = Rect(
                            left = cx - domeR,
                            top = domeCenterY - domeR,
                            right = cx + domeR,
                            bottom = domeCenterY + domeR
                        ),
                        startAngleDegrees = 200f,
                        sweepAngleDegrees = 140f,
                        forceMoveTo = false
                    )
                    cubicTo(
                        cx + domeR * 1.03f, joinY + 2.dp.toPx(),
                        cx + domeR * 1.18f, pillTop,
                        cx + neckHalfW, pillTop
                    )
                    lineTo(w - cornerR, pillTop)
                    arcTo(
                        rect = Rect(w - pillHeight, pillTop, w, pillBottom),
                        startAngleDegrees = -90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    lineTo(cx + neckHalfW, pillBottom)
                    cubicTo(
                        cx + neckHalfW * 0.55f, pillBottom,
                        cx + domeR * 0.65f, pillBottom + cradleDip,
                        cx, pillBottom + cradleDip
                    )
                    cubicTo(
                        cx - domeR * 0.65f, pillBottom + cradleDip,
                        cx - neckHalfW * 0.55f, pillBottom,
                        cx - neckHalfW, pillBottom
                    )
                    lineTo(cornerR, pillBottom)
                    arcTo(
                        rect = Rect(0f, pillTop, pillHeight, pillBottom),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 180f,
                        forceMoveTo = false
                    )
                    close()
                }

                // Soft ambient shadow & sapphire-gold halo under dome
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LuxuryDockPalette.GoldBright.copy(alpha = 0.22f),
                            LuxuryDockPalette.SapphireGlow.copy(alpha = 0.14f),
                            Color.Transparent
                        ),
                        center = Offset(cx, domeCenterY - 2.dp.toPx()),
                        radius = domeR * 1.55f
                    ),
                    radius = domeR * 1.55f,
                    center = Offset(cx, domeCenterY - 2.dp.toPx())
                )

                drawPath(
                    path = dockPath,
                    color = Color.Black.copy(alpha = 0.24f),
                    style = Stroke(width = 6.dp.toPx())
                )

                // Main Deep Emerald-Sapphire velvet fill
                drawPath(
                    path = dockPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LuxuryDockPalette.DeepSapphireTop,
                            LuxuryDockPalette.DeepSapphireMid,
                            LuxuryDockPalette.DeepSapphireBottom
                        ),
                        startY = domeCenterY - domeR,
                        endY = pillBottom + cradleDip
                    )
                )

                // Top specular glass highlight inside the dock
                drawPath(
                    path = dockPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.12f),
                            LuxuryDockPalette.SapphireGlow.copy(alpha = 0.05f),
                            Color.Transparent
                        ),
                        startY = domeCenterY - domeR,
                        endY = pillTop + pillHeight * 0.55f
                    )
                )

                // Moving spotlight radial glow under the active tab
                val slotWidth = w / items.size.toFloat()
                val activeCenterX = (animatedSelectedPos + 0.5f) * slotWidth
                val activeCenterY = if (kotlin.math.abs(animatedSelectedPos - centerIndex) < 0.5f) {
                    domeCenterY - 1.dp.toPx()
                } else {
                    pillTop + pillHeight * 0.45f
                }
                val glowRadius = slotWidth * 0.85f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LuxuryDockPalette.GoldBright.copy(alpha = 0.26f),
                            LuxuryDockPalette.SapphireGlow.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(activeCenterX, activeCenterY),
                        radius = glowRadius
                    ),
                    radius = glowRadius,
                    center = Offset(activeCenterX, activeCenterY)
                )

                // Subtle golden-sapphire outer rim border
                drawPath(
                    path = dockPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LuxuryDockPalette.GoldBright.copy(alpha = 0.72f),
                            LuxuryDockPalette.SapphireSoft.copy(alpha = 0.42f),
                            LuxuryDockPalette.SapphireGlow.copy(alpha = 0.22f)
                        ),
                        startY = domeCenterY - domeR,
                        endY = pillBottom + cradleDip
                    ),
                    style = Stroke(width = 1.35.dp.toPx(), cap = StrokeCap.Round)
                )

                // Concentric luminous medallion ring around center Jadwal tab
                val isCenterSelected = selectedIndex == centerIndex
                val medallionRingRadius = domeR - 7.5.dp.toPx()
                val medallionCenter = Offset(cx, domeCenterY - 2.dp.toPx())
                if (isCenterSelected) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LuxuryDockPalette.GoldWarm.copy(alpha = 0.28f),
                                LuxuryDockPalette.SapphireGlow.copy(alpha = 0.14f),
                                Color.Transparent
                            ),
                            center = medallionCenter,
                            radius = medallionRingRadius + 4.dp.toPx()
                        ),
                        radius = medallionRingRadius + 4.dp.toPx(),
                        center = medallionCenter
                    )
                }
                drawCircle(
                    brush = Brush.linearGradient(
                        colors = if (isCenterSelected) {
                            listOf(
                                LuxuryDockPalette.GoldBright,
                                LuxuryDockPalette.GoldWarm,
                                LuxuryDockPalette.GoldBright
                            )
                        } else {
                            listOf(
                                LuxuryDockPalette.GoldBright.copy(alpha = 0.42f),
                                LuxuryDockPalette.SapphireSoft.copy(alpha = 0.45f),
                                LuxuryDockPalette.GoldWarm.copy(alpha = 0.30f)
                            )
                        },
                        start = Offset(cx - medallionRingRadius, medallionCenter.y - medallionRingRadius),
                        end = Offset(cx + medallionRingRadius, medallionCenter.y + medallionRingRadius)
                    ),
                    radius = medallionRingRadius,
                    center = medallionCenter,
                    style = Stroke(width = if (isCenterSelected) 1.8.dp.toPx() else 1.15.dp.toPx())
                )
            }

            // Interactive 5-item Row
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEachIndexed { index, item ->
                    val isCenter = index == centerIndex
                    val selected = selectedIndex == index
                    val iconTint by animateColorAsState(
                        targetValue = when {
                            selected -> LuxuryDockPalette.GoldBright
                            isCenter -> LuxuryDockPalette.SapphireSoft
                            else -> LuxuryDockPalette.InactiveTint
                        },
                        animationSpec = tween(220),
                        label = "nav_icon_tint_$index"
                    )
                    val labelTint by animateColorAsState(
                        targetValue = when {
                            selected -> LuxuryDockPalette.GoldBright
                            isCenter -> LuxuryDockPalette.SapphireSoft.copy(alpha = 0.92f)
                            else -> LuxuryDockPalette.InactiveSubtle
                        },
                        animationSpec = tween(220),
                        label = "nav_label_tint_$index"
                    )
                    val itemScale by animateFloatAsState(
                        targetValue = if (selected) 1.08f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "nav_scale_$index"
                    )
                    val interactionSource = remember { MutableInteractionSource() }

                    if (isCenter) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(bounded = false, color = LuxuryDockPalette.GoldBright),
                                    onClick = { onSelectIndex(index) }
                                )
                                .padding(bottom = 8.dp)
                                .testTag(item.testTag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .scale(itemScale)
                                    .clip(CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = iconTint,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            Text(
                                text = item.label,
                                fontSize = 10.sp,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                color = labelTint,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(64.dp)
                                .clickable(
                                    interactionSource = interactionSource,
                                    indication = ripple(bounded = false, color = LuxuryDockPalette.GoldBright),
                                    onClick = { onSelectIndex(index) }
                                )
                                .padding(top = 6.dp, bottom = 8.dp)
                                .testTag(item.testTag),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = iconTint,
                                modifier = Modifier
                                    .size(22.dp)
                                    .scale(itemScale)
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = item.label,
                                fontSize = 9.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = labelTint,
                                maxLines = 1,
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Symmetrical floating luxury pill dock (for 4-tab portals like Petugas Kesehatan).
 * Matches the Deep Emerald-Sapphire velvet body + golden-sapphire luminous halo & rim glow.
 */
@Composable
fun LuxuryFloatingPillBottomBar(
    items: List<LuxuryNavItem>,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedSelectedPos by animateFloatAsState(
        targetValue = selectedIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "luxury_pill_selected_pos"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .height(66.dp)
                .testTag("bottom_nav_bar_luxury_pill")
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val corner = CornerRadius(h / 2f, h / 2f)

                // Ambient shadow
                drawRoundRect(
                    color = Color.Black.copy(alpha = 0.22f),
                    topLeft = Offset(0f, 3.dp.toPx()),
                    size = Size(w, h),
                    cornerRadius = corner
                )

                // Deep Emerald-Sapphire gradient pill body
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LuxuryDockPalette.DeepSapphireTop,
                            LuxuryDockPalette.DeepSapphireMid,
                            LuxuryDockPalette.DeepSapphireBottom
                        )
                    ),
                    cornerRadius = corner
                )

                // Subtle top specular highlight
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.11f),
                            LuxuryDockPalette.SapphireGlow.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        endY = h * 0.55f
                    ),
                    cornerRadius = corner
                )

                // Moving glowing golden-sapphire halo under the active tab
                val count = items.size.coerceAtLeast(1)
                val slotWidth = w / count.toFloat()
                val activeCenterX = (animatedSelectedPos + 0.5f) * slotWidth
                val activeCenterY = h * 0.45f
                val haloRadius = slotWidth * 0.78f

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            LuxuryDockPalette.GoldBright.copy(alpha = 0.28f),
                            LuxuryDockPalette.SapphireGlow.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        center = Offset(activeCenterX, activeCenterY),
                        radius = haloRadius
                    ),
                    radius = haloRadius,
                    center = Offset(activeCenterX, activeCenterY)
                )

                // Active luminous ring pill indicator
                val pillW = (slotWidth - 18.dp.toPx()).coerceAtLeast(48.dp.toPx())
                val pillH = h - 14.dp.toPx()
                drawRoundRect(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LuxuryDockPalette.GoldBright.copy(alpha = 0.48f),
                            LuxuryDockPalette.SapphireSoft.copy(alpha = 0.35f)
                        )
                    ),
                    topLeft = Offset(activeCenterX - pillW / 2f, 7.dp.toPx()),
                    size = Size(pillW, pillH),
                    cornerRadius = CornerRadius(pillH / 2f, pillH / 2f),
                    style = Stroke(width = 1.1.dp.toPx())
                )

                // Rim border around the whole floating pill
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LuxuryDockPalette.GoldBright.copy(alpha = 0.65f),
                            LuxuryDockPalette.SapphireSoft.copy(alpha = 0.38f),
                            LuxuryDockPalette.SapphireGlow.copy(alpha = 0.22f)
                        )
                    ),
                    cornerRadius = corner,
                    style = Stroke(width = 1.3.dp.toPx())
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(33.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    val selected = selectedIndex == index
                    val iconTint by animateColorAsState(
                        targetValue = if (selected) LuxuryDockPalette.GoldBright else LuxuryDockPalette.InactiveTint,
                        animationSpec = tween(220),
                        label = "pill_icon_tint_$index"
                    )
                    val labelTint by animateColorAsState(
                        targetValue = if (selected) LuxuryDockPalette.GoldBright else LuxuryDockPalette.InactiveSubtle,
                        animationSpec = tween(220),
                        label = "pill_label_tint_$index"
                    )
                    val scale by animateFloatAsState(
                        targetValue = if (selected) 1.07f else 1f,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "pill_scale_$index"
                    )
                    val iconLift by animateDpAsState(
                        targetValue = if (selected) (-1).dp else 0.dp,
                        animationSpec = tween(180),
                        label = "pill_lift_$index"
                    )
                    val interactionSource = remember { MutableInteractionSource() }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = interactionSource,
                                indication = ripple(bounded = false, color = LuxuryDockPalette.GoldBright),
                                onClick = { onSelectIndex(index) }
                            )
                            .padding(horizontal = 4.dp)
                            .testTag(item.testTag),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.label,
                            tint = iconTint,
                            modifier = Modifier
                                .size(22.dp)
                                .offset(y = iconLift)
                                .scale(scale)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = item.label,
                            fontSize = 10.sp,
                            fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                            color = labelTint,
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
