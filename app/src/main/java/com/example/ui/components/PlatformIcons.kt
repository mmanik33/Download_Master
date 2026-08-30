package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders authentic vector brand icons for streaming & social platforms with zero emojis.
 */
@Composable
fun PlatformBrandIcon(
    platformId: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    tint: Color = Color.White
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            when (platformId.lowercase()) {
                "youtube" -> drawYouTubeIcon(w, h, tint)
                "facebook" -> drawFacebookIcon(w, h, tint)
                "twitter", "x" -> drawXTwitterIcon(w, h, tint)
                "tiktok" -> drawTikTokIcon(w, h, tint)
                "instagram" -> drawInstagramIcon(w, h, tint)
                "vimeo" -> drawVimeoIcon(w, h, tint)
                "dailymotion" -> drawDailymotionIcon(w, h, tint)
                "likee" -> drawLikeeIcon(w, h, tint)
                "pinterest" -> drawPinterestIcon(w, h, tint)
                "reddit" -> drawRedditIcon(w, h, tint)
                "soundcloud" -> drawSoundCloudIcon(w, h, tint)
                "tumblr" -> drawTumblrIcon(w, h, tint)
                else -> drawGenericPlayIcon(w, h, tint)
            }
        }
    }
}

private fun DrawScope.drawYouTubeIcon(w: Float, h: Float, tint: Color) {
    // Outer rounded rectangle
    val padX = w * 0.08f
    val padY = h * 0.20f
    val rectW = w - padX * 2
    val rectH = h - padY * 2
    val cornerRadius = rectH * 0.35f

    drawRoundRect(
        color = tint,
        topLeft = Offset(padX, padY),
        size = Size(rectW, rectH),
        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
        style = Fill
    )

    // Inner play triangle (cutout)
    val triPath = Path().apply {
        moveTo(w * 0.42f, h * 0.36f)
        lineTo(w * 0.65f, h * 0.50f)
        lineTo(w * 0.42f, h * 0.64f)
        close()
    }
    drawPath(triPath, color = Color.White)
}

private fun DrawScope.drawFacebookIcon(w: Float, h: Float, tint: Color) {
    val path = Path().apply {
        moveTo(w * 0.58f, h * 0.92f)
        lineTo(w * 0.58f, h * 0.54f)
        lineTo(w * 0.70f, h * 0.54f)
        lineTo(w * 0.72f, h * 0.38f)
        lineTo(w * 0.58f, h * 0.38f)
        lineTo(w * 0.58f, h * 0.28f)
        cubicTo(w * 0.58f, h * 0.22f, w * 0.61f, h * 0.16f, w * 0.72f, h * 0.16f)
        lineTo(w * 0.79f, h * 0.16f)
        lineTo(w * 0.79f, h * 0.04f)
        cubicTo(w * 0.75f, h * 0.03f, w * 0.67f, h * 0.02f, w * 0.55f, h * 0.02f)
        cubicTo(w * 0.38f, h * 0.02f, w * 0.28f, h * 0.12f, w * 0.28f, h * 0.32f)
        lineTo(w * 0.28f, h * 0.38f)
        lineTo(w * 0.18f, h * 0.38f)
        lineTo(w * 0.18f, h * 0.54f)
        lineTo(w * 0.28f, h * 0.54f)
        lineTo(w * 0.28f, h * 0.92f)
        close()
    }
    drawPath(path, color = tint, style = Fill)
}

private fun DrawScope.drawXTwitterIcon(w: Float, h: Float, tint: Color) {
    val strokeWidth = w * 0.14f
    // Stroke 1: Top-left to bottom-right
    drawLine(
        color = tint,
        start = Offset(w * 0.18f, h * 0.18f),
        end = Offset(w * 0.82f, h * 0.82f),
        strokeWidth = strokeWidth,
        cap = StrokeCap.Round
    )
    // Stroke 2: Top-right to bottom-left
    drawLine(
        color = tint,
        start = Offset(w * 0.82f, h * 0.18f),
        end = Offset(w * 0.18f, h * 0.82f),
        strokeWidth = strokeWidth * 0.65f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawTikTokIcon(w: Float, h: Float, tint: Color) {
    val path = Path().apply {
        moveTo(w * 0.55f, h * 0.12f)
        cubicTo(w * 0.58f, h * 0.25f, w * 0.68f, h * 0.35f, w * 0.84f, h * 0.36f)
        lineTo(w * 0.84f, h * 0.50f)
        cubicTo(w * 0.72f, h * 0.50f, w * 0.62f, h * 0.44f, w * 0.55f, h * 0.38f)
        lineTo(w * 0.55f, h * 0.68f)
        cubicTo(w * 0.55f, h * 0.82f, w * 0.44f, h * 0.90f, w * 0.32f, h * 0.90f)
        cubicTo(w * 0.18f, h * 0.90f, w * 0.08f, h * 0.78f, w * 0.08f, h * 0.64f)
        cubicTo(w * 0.08f, h * 0.50f, w * 0.18f, h * 0.38f, w * 0.32f, h * 0.38f)
        cubicTo(w * 0.36f, h * 0.38f, w * 0.40f, h * 0.39f, w * 0.43f, h * 0.41f)
        lineTo(w * 0.43f, h * 0.55f)
        cubicTo(w * 0.40f, h * 0.53f, w * 0.36f, h * 0.52f, w * 0.32f, h * 0.52f)
        cubicTo(w * 0.26f, h * 0.52f, w * 0.22f, h * 0.57f, w * 0.22f, h * 0.64f)
        cubicTo(w * 0.22f, h * 0.71f, w * 0.26f, h * 0.76f, w * 0.32f, h * 0.76f)
        cubicTo(w * 0.39f, h * 0.76f, w * 0.44f, h * 0.70f, w * 0.44f, h * 0.64f)
        lineTo(w * 0.44f, h * 0.12f)
        close()
    }
    drawPath(path, color = tint, style = Fill)
}

private fun DrawScope.drawInstagramIcon(w: Float, h: Float, tint: Color) {
    val stroke = w * 0.10f
    val pad = w * 0.12f
    val r = (w - pad * 2) * 0.28f

    // Outer rounded square outline
    drawRoundRect(
        color = tint,
        topLeft = Offset(pad, pad),
        size = Size(w - pad * 2, h - pad * 2),
        cornerRadius = CornerRadius(r, r),
        style = Stroke(width = stroke, cap = StrokeCap.Round, join = StrokeJoin.Round)
    )

    // Center lens circle
    drawCircle(
        color = tint,
        radius = w * 0.18f,
        center = Offset(w * 0.5f, h * 0.5f),
        style = Stroke(width = stroke)
    )

    // Top-right flash dot
    drawCircle(
        color = tint,
        radius = w * 0.045f,
        center = Offset(w * 0.72f, h * 0.28f),
        style = Fill
    )
}

private fun DrawScope.drawVimeoIcon(w: Float, h: Float, tint: Color) {
    val path = Path().apply {
        moveTo(w * 0.15f, h * 0.36f)
        lineTo(w * 0.35f, h * 0.85f)
        lineTo(w * 0.52f, h * 0.85f)
        lineTo(w * 0.85f, h * 0.20f)
        lineTo(w * 0.68f, h * 0.20f)
        lineTo(w * 0.44f, h * 0.68f)
        lineTo(w * 0.30f, h * 0.36f)
        close()
    }
    drawPath(path, color = tint, style = Fill)
}

private fun DrawScope.drawDailymotionIcon(w: Float, h: Float, tint: Color) {
    // Dailymotion lowercase 'd'
    val path = Path().apply {
        moveTo(w * 0.70f, h * 0.12f)
        lineTo(w * 0.70f, h * 0.88f)
        lineTo(w * 0.54f, h * 0.88f)
        lineTo(w * 0.54f, h * 0.76f)
        cubicTo(w * 0.48f, h * 0.86f, w * 0.36f, h * 0.90f, w * 0.24f, h * 0.85f)
        cubicTo(w * 0.12f, h * 0.78f, w * 0.08f, h * 0.64f, w * 0.08f, h * 0.52f)
        cubicTo(w * 0.08f, h * 0.38f, w * 0.14f, h * 0.26f, w * 0.26f, h * 0.22f)
        cubicTo(w * 0.38f, h * 0.18f, w * 0.48f, h * 0.24f, w * 0.54f, h * 0.32f)
        lineTo(w * 0.54f, h * 0.12f)
        close()
        // Inner cutout
        moveTo(w * 0.54f, h * 0.52f)
        cubicTo(w * 0.54f, h * 0.42f, w * 0.46f, h * 0.36f, w * 0.36f, h * 0.36f)
        cubicTo(w * 0.26f, h * 0.36f, w * 0.20f, h * 0.43f, w * 0.20f, h * 0.52f)
        cubicTo(w * 0.20f, h * 0.62f, w * 0.26f, h * 0.70f, w * 0.36f, h * 0.70f)
        cubicTo(w * 0.46f, h * 0.70f, w * 0.54f, h * 0.62f, w * 0.54f, h * 0.52f)
        close()
    }
    drawPath(path, color = tint, style = Fill)
}

private fun DrawScope.drawLikeeIcon(w: Float, h: Float, tint: Color) {
    // Likee heart shape
    val path = Path().apply {
        moveTo(w * 0.5f, h * 0.85f)
        cubicTo(w * 0.15f, h * 0.60f, w * 0.05f, h * 0.35f, w * 0.20f, h * 0.20f)
        cubicTo(w * 0.35f, h * 0.08f, w * 0.48f, h * 0.20f, w * 0.5f, h * 0.30f)
        cubicTo(w * 0.52f, h * 0.20f, w * 0.65f, h * 0.08f, w * 0.80f, h * 0.20f)
        cubicTo(w * 0.95f, h * 0.35f, w * 0.85f, h * 0.60f, w * 0.5f, h * 0.85f)
        close()
    }
    drawPath(path, color = tint, style = Fill)
}

private fun DrawScope.drawPinterestIcon(w: Float, h: Float, tint: Color) {
    val path = Path().apply {
        moveTo(w * 0.42f, h * 0.88f)
        lineTo(w * 0.48f, h * 0.52f)
        cubicTo(w * 0.52f, h * 0.58f, w * 0.60f, h * 0.62f, w * 0.68f, h * 0.58f)
        cubicTo(w * 0.80f, h * 0.50f, w * 0.85f, h * 0.36f, w * 0.78f, h * 0.22f)
        cubicTo(w * 0.70f, h * 0.10f, w * 0.52f, h * 0.08f, w * 0.38f, h * 0.14f)
        cubicTo(w * 0.22f, h * 0.22f, w * 0.16f, h * 0.40f, w * 0.22f, h * 0.54f)
        cubicTo(w * 0.24f, h * 0.58f, w * 0.28f, h * 0.60f, w * 0.30f, h * 0.55f)
        cubicTo(w * 0.32f, h * 0.50f, w * 0.34f, h * 0.42f, w * 0.32f, h * 0.38f)
        cubicTo(w * 0.28f, h * 0.30f, w * 0.32f, h * 0.20f, w * 0.42f, h * 0.18f)
        cubicTo(w * 0.52f, h * 0.16f, w * 0.64f, h * 0.22f, w * 0.66f, h * 0.34f)
        cubicTo(w * 0.68f, h * 0.46f, w * 0.60f, h * 0.54f, w * 0.50f, h * 0.52f)
        cubicTo(w * 0.45f, h * 0.50f, w * 0.42f, h * 0.44f, w * 0.42f, h * 0.38f)
        lineTo(w * 0.35f, h * 0.88f)
        close()
    }
    drawPath(path, color = tint, style = Fill)
}

private fun DrawScope.drawRedditIcon(w: Float, h: Float, tint: Color) {
    val stroke = w * 0.08f
    // Head ellipse
    drawOval(
        color = tint,
        topLeft = Offset(w * 0.15f, h * 0.28f),
        size = Size(w * 0.70f, h * 0.52f),
        style = Stroke(width = stroke)
    )
    // Left eye
    drawCircle(
        color = tint,
        radius = w * 0.06f,
        center = Offset(w * 0.38f, h * 0.52f),
        style = Fill
    )
    // Right eye
    drawCircle(
        color = tint,
        radius = w * 0.06f,
        center = Offset(w * 0.62f, h * 0.52f),
        style = Fill
    )
    // Antenna
    val antenna = Path().apply {
        moveTo(w * 0.50f, h * 0.28f)
        lineTo(w * 0.56f, h * 0.14f)
        lineTo(w * 0.70f, h * 0.16f)
    }
    drawPath(antenna, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
    drawCircle(
        color = tint,
        radius = w * 0.05f,
        center = Offset(w * 0.72f, h * 0.16f),
        style = Fill
    )
}

private fun DrawScope.drawSoundCloudIcon(w: Float, h: Float, tint: Color) {
    // Soundcloud cloud waveform
    val stroke = w * 0.08f
    val bars = listOf(
        Offset(w * 0.16f, h * 0.58f) to Offset(w * 0.16f, h * 0.72f),
        Offset(w * 0.26f, h * 0.46f) to Offset(w * 0.26f, h * 0.72f),
        Offset(w * 0.36f, h * 0.36f) to Offset(w * 0.36f, h * 0.72f),
        Offset(w * 0.46f, h * 0.28f) to Offset(w * 0.46f, h * 0.72f)
    )
    for ((start, end) in bars) {
        drawLine(color = tint, start = start, end = end, strokeWidth = stroke, cap = StrokeCap.Round)
    }
    // Cloud arc on right
    val cloudArc = Path().apply {
        moveTo(w * 0.46f, h * 0.28f)
        cubicTo(w * 0.65f, h * 0.18f, w * 0.85f, h * 0.32f, w * 0.85f, h * 0.52f)
        cubicTo(w * 0.94f, h * 0.54f, w * 0.94f, h * 0.72f, w * 0.82f, h * 0.72f)
        lineTo(w * 0.46f, h * 0.72f)
    }
    drawPath(cloudArc, color = tint, style = Fill)
}

private fun DrawScope.drawTumblrIcon(w: Float, h: Float, tint: Color) {
    val path = Path().apply {
        moveTo(w * 0.48f, h * 0.12f)
        lineTo(w * 0.48f, h * 0.30f)
        lineTo(w * 0.66f, h * 0.30f)
        lineTo(w * 0.66f, h * 0.46f)
        lineTo(w * 0.48f, h * 0.46f)
        lineTo(w * 0.48f, h * 0.72f)
        cubicTo(w * 0.48f, h * 0.82f, w * 0.54f, h * 0.86f, w * 0.66f, h * 0.84f)
        lineTo(w * 0.66f, h * 0.96f)
        cubicTo(w * 0.46f, h * 0.98f, w * 0.32f, h * 0.92f, w * 0.32f, h * 0.72f)
        lineTo(w * 0.32f, h * 0.46f)
        lineTo(w * 0.18f, h * 0.46f)
        lineTo(w * 0.18f, h * 0.34f)
        cubicTo(w * 0.28f, h * 0.32f, w * 0.32f, h * 0.22f, w * 0.32f, h * 0.12f)
        close()
    }
    drawPath(path, color = tint, style = Fill)
}

private fun DrawScope.drawGenericPlayIcon(w: Float, h: Float, tint: Color) {
    val triPath = Path().apply {
        moveTo(w * 0.32f, h * 0.22f)
        lineTo(w * 0.78f, h * 0.50f)
        lineTo(w * 0.32f, h * 0.78f)
        close()
    }
    drawPath(triPath, color = tint, style = Fill)
}
