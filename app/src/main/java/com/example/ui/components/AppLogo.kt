package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    hasGlow: Boolean = false,
    glowRadius: Dp = 16.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (hasGlow) {
            // Soft purple glowing aura behind the logo
            Canvas(modifier = Modifier.matchParentSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF8B42E8).copy(alpha = 0.5f),
                            Color(0xFF7C3AED).copy(alpha = 0.25f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = size.width * 0.68f
                    ),
                    radius = size.width * 0.68f,
                    center = Offset(cx, cy)
                )
            }
        }

        Canvas(modifier = Modifier.matchParentSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Dark background squircle
            val cornerRadius = CornerRadius(w * 0.22f, h * 0.22f)
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF181230),
                        Color(0xFF0F0C1E),
                        Color(0xFF080611)
                    ),
                    center = Offset(cx, cy * 0.85f),
                    radius = w * 0.65f
                ),
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = cornerRadius
            )

            // Subtle border around squircle
            drawRoundRect(
                color = Color(0xFF372E54).copy(alpha = 0.6f),
                topLeft = Offset.Zero,
                size = Size(w, h),
                cornerRadius = cornerRadius,
                style = Stroke(width = (w * 0.006f).coerceAtLeast(1f))
            )

            // 2. Rosary Thread / Ring
            val ringRadius = w * 0.355f
            drawCircle(
                color = Color(0xFF4C2D80).copy(alpha = 0.7f),
                radius = ringRadius,
                center = Offset(cx, cy),
                style = Stroke(width = (w * 0.005f).coerceAtLeast(1f))
            )

            // 3. Beads (33 beads in a circular chain)
            val numBeads = 33
            for (i in 0 until numBeads) {
                val angleDeg = -90.0 + i * (360.0 / numBeads)
                val angleRad = Math.toRadians(angleDeg)
                val bx = cx + ringRadius * cos(angleRad).toFloat()
                val by = cy + ringRadius * sin(angleRad).toFloat()

                if (i == 0) {
                    // Leader bead (الشاهد) at the top (larger)
                    val r = w * 0.030f
                    // Drop shadow
                    drawCircle(
                        color = Color(0xFF080512).copy(alpha = 0.6f),
                        radius = r * 1.1f,
                        center = Offset(bx, by + w * 0.005f)
                    )
                    // Sphere gradient
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFFFFFFF),
                                Color(0xFFE9D5FF),
                                Color(0xFFB794F6),
                                Color(0xFF7C3AED),
                                Color(0xFF3B0764)
                            ),
                            center = Offset(bx - r * 0.35f, by - r * 0.35f),
                            radius = r * 1.25f
                        ),
                        radius = r,
                        center = Offset(bx, by)
                    )
                } else {
                    // Regular rosary beads (1..32)
                    val r = w * 0.019f
                    // Drop shadow
                    drawCircle(
                        color = Color(0xFF080512).copy(alpha = 0.5f),
                        radius = r * 1.05f,
                        center = Offset(bx, by + w * 0.004f)
                    )
                    // Sphere gradient with specular highlight
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFFF3EEFF),
                                Color(0xFFDDD6FE),
                                Color(0xFFA78BFA),
                                Color(0xFF6D28D9),
                                Color(0xFF3B0764)
                            ),
                            center = Offset(bx - r * 0.35f, by - r * 0.35f),
                            radius = r * 1.25f
                        ),
                        radius = r,
                        center = Offset(bx, by)
                    )
                }
            }

            // 4. Center Glowing Disc Aura
            val auraRadius = w * 0.278f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6).copy(alpha = 0.45f),
                        Color(0xFF6D28D9).copy(alpha = 0.15f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = auraRadius
                ),
                radius = auraRadius,
                center = Offset(cx, cy)
            )

            // 5. Center Glowing Disc
            val discRadius = w * 0.226f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF9D5CFA),
                        Color(0xFF7C3AED),
                        Color(0xFF5B21B6),
                        Color(0xFF37136B)
                    ),
                    center = Offset(cx, cy * 0.95f),
                    radius = discRadius * 1.1f
                ),
                radius = discRadius,
                center = Offset(cx, cy)
            )
            // Disc border
            drawCircle(
                color = Color(0xFFC4B5FD).copy(alpha = 0.4f),
                radius = discRadius,
                center = Offset(cx, cy),
                style = Stroke(width = (w * 0.004f).coerceAtLeast(1f))
            )

            // 6. Crescent Moon (الهلال)
            val moonOuterR = w * 0.165f
            val moonInnerR = w * 0.145f
            val moonOuterCenter = Offset(cx - w * 0.015f, cy)
            val moonInnerCenter = Offset(cx + w * 0.045f, cy)

            val moonPath = Path().apply {
                val startAngle = -65f
                val sweepAngle = 240f
                arcTo(
                    rect = Rect(
                        center = moonOuterCenter,
                        radius = moonOuterR
                    ),
                    startAngleDegrees = startAngle,
                    sweepAngleDegrees = sweepAngle,
                    forceMoveTo = true
                )
                arcTo(
                    rect = Rect(
                        center = moonInnerCenter,
                        radius = moonInnerR
                    ),
                    startAngleDegrees = startAngle + sweepAngle,
                    sweepAngleDegrees = -sweepAngle,
                    forceMoveTo = false
                )
                close()
            }

            drawPath(
                path = moonPath,
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF181329),
                        Color(0xFF0D0A18)
                    ),
                    start = Offset(cx - moonOuterR, cy - moonOuterR),
                    end = Offset(cx + moonOuterR, cy + moonOuterR)
                )
            )

            // Moon outer rim highlight
            val moonRimPath = Path().apply {
                arcTo(
                    rect = Rect(
                        center = moonOuterCenter,
                        radius = moonOuterR
                    ),
                    startAngleDegrees = 110f,
                    sweepAngleDegrees = 130f,
                    forceMoveTo = true
                )
            }
            drawPath(
                path = moonRimPath,
                color = Color(0xFF8B5CF6).copy(alpha = 0.5f),
                style = Stroke(width = (w * 0.005f).coerceAtLeast(1f))
            )

            // 7. Star / White pearl at upper right
            val starCenter = Offset(cx + w * 0.142f, cy - h * 0.12f)
            val starGlowR = w * 0.028f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF),
                        Color(0xFFE9D5FF).copy(alpha = 0.75f),
                        Color(0xFF8B5CF6).copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = starCenter,
                    radius = starGlowR
                ),
                radius = starGlowR,
                center = starCenter
            )
            drawCircle(
                color = Color.White,
                radius = (w * 0.012f).coerceAtLeast(1.5f),
                center = starCenter
            )
        }
    }
}
