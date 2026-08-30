package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Custom Day/Night Toggle Switch designed precisely matching the user's reference mockup:
 * - "Light" label on left
 * - Sky blue pill with white sun & clouds in Light mode
 * - Midnight navy pill with crescent moon & stars in Dark mode
 * - "Dark" label on right
 */
@Composable
fun DayNightToggleSwitch(
    isDark: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    switchWidth: Dp = 76.dp,
    switchHeight: Dp = 38.dp,
    showLabels: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Smooth animation progress: 0f = Light, 1f = Dark
    val animProgress by animateFloatAsState(
        targetValue = if (isDark) 1f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "DayNightProgress"
    )

    // Pill background color transition
    val pillBgColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF131927) else Color(0xFF6B9EFF),
        animationSpec = tween(durationMillis = 350),
        label = "PillBgColor"
    )

    val pillBorderColor by animateColorAsState(
        targetValue = if (isDark) Color(0xFF2E384D) else Color(0x33FFFFFF),
        animationSpec = tween(durationMillis = 350),
        label = "PillBorderColor"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onToggle(!isDark) }
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (showLabels) {
            // Light label
            Text(
                text = "Light",
                color = if (!isDark) (if (isDark) Color(0xFF94A3B8) else Color(0xFF0F172A)) else Color(0xFF64748B),
                fontSize = 13.sp,
                fontWeight = if (!isDark) FontWeight.ExtraBold else FontWeight.Normal
            )

            Spacer(modifier = Modifier.width(8.dp))
        }

        // The Switch Capsule
        Box(
            modifier = Modifier
                .size(width = switchWidth, height = switchHeight)
                .clip(CircleShape)
                .background(pillBgColor)
                .border(1.2.dp, pillBorderColor, CircleShape)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val radius = h / 2f
                val thumbRadius = radius * 0.72f

                // Draw background decorations (Clouds in Day, Stars in Night)
                // In day (progress -> 0): draw cloud bubbles on the right
                if (animProgress < 0.95f) {
                    val dayAlpha = (1f - animProgress * 1.2f).coerceIn(0f, 1f)
                    val cloudColor = Color.White.copy(alpha = dayAlpha * 0.85f)
                    // Cloud bubble 1
                    drawCircle(
                        color = cloudColor,
                        radius = thumbRadius * 0.42f,
                        center = Offset(w * 0.72f, h * 0.38f)
                    )
                    // Cloud bubble 2
                    drawCircle(
                        color = cloudColor,
                        radius = thumbRadius * 0.28f,
                        center = Offset(w * 0.84f, h * 0.65f)
                    )
                    // Cloud bubble 3
                    drawCircle(
                        color = cloudColor,
                        radius = thumbRadius * 0.22f,
                        center = Offset(w * 0.64f, h * 0.68f)
                    )
                }

                // In night (progress -> 1): draw stars on the left
                if (animProgress > 0.05f) {
                    val nightAlpha = ((animProgress - 0.2f) * 1.25f).coerceIn(0f, 1f)
                    val starColor = Color.White.copy(alpha = nightAlpha * 0.9f)
                    // Star 1 (4-pointed star)
                    drawStar(Offset(w * 0.25f, h * 0.32f), size = thumbRadius * 0.38f, color = starColor)
                    // Star 2 (small dot)
                    drawCircle(color = starColor, radius = thumbRadius * 0.14f, center = Offset(w * 0.36f, h * 0.28f))
                    // Star 3 (small dot)
                    drawCircle(color = starColor, radius = thumbRadius * 0.16f, center = Offset(w * 0.18f, h * 0.68f))
                    // Star 4 (small dot)
                    drawCircle(color = starColor, radius = thumbRadius * 0.14f, center = Offset(w * 0.34f, h * 0.70f))
                }

                // Thumb Position interpolation (Left when Day, Right when Night)
                val leftCenter = radius
                val rightCenter = w - radius
                val thumbCenterX = leftCenter + (rightCenter - leftCenter) * animProgress
                val thumbCenterY = h / 2f

                // Draw Thumb (Sun when Day, Crescent Moon when Night)
                if (animProgress < 0.5f) {
                    // Sun (Pure White Circle with soft glow)
                    drawCircle(
                        color = Color.White,
                        radius = thumbRadius,
                        center = Offset(thumbCenterX, thumbCenterY),
                        style = Fill
                    )
                } else {
                    // Crescent Moon
                    drawCrescentMoon(
                        center = Offset(thumbCenterX, thumbCenterY),
                        radius = thumbRadius,
                        color = Color.White
                    )
                }
            }
        }

        if (showLabels) {
            Spacer(modifier = Modifier.width(8.dp))

            // Dark label
            Text(
                text = "Dark",
                color = if (isDark) Color.White else Color(0xFF94A3B8),
                fontSize = 13.sp,
                fontWeight = if (isDark) FontWeight.ExtraBold else FontWeight.Normal
            )
        }
    }
}

private fun DrawScope.drawStar(center: Offset, size: Float, color: Color) {
    val path = Path().apply {
        moveTo(center.x, center.y - size)
        cubicTo(center.x, center.y, center.x, center.y, center.x + size, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y + size)
        cubicTo(center.x, center.y, center.x, center.y, center.x - size, center.y)
        cubicTo(center.x, center.y, center.x, center.y, center.x, center.y - size)
        close()
    }
    drawPath(path, color = color)
}

private fun DrawScope.drawCrescentMoon(center: Offset, radius: Float, color: Color) {
    // Outer circle arc and inner subtraction to form a crisp crescent facing left
    val path = Path().apply {
        // Outer arc (full circle on right)
        addOval(
            androidx.compose.ui.geometry.Rect(
                center = center,
                radius = radius
            )
        )
    }
    // Subtraction circle to cut out inner part of moon
    val cutPath = Path().apply {
        addOval(
            androidx.compose.ui.geometry.Rect(
                center = Offset(center.x - radius * 0.52f, center.y - radius * 0.18f),
                radius = radius * 0.88f
            )
        )
    }

    val finalMoon = Path().apply {
        op(path, cutPath, androidx.compose.ui.graphics.PathOperation.Difference)
    }
    drawPath(finalMoon, color = color)
}
