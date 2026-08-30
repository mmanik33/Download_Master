package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BilibiliPink
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.InstagramPurple
import com.example.ui.theme.RedditOrange
import com.example.ui.theme.SoundCloudOrange
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.YouTubeRed

@Composable
fun PlatformBadge(
    extractorName: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, label) = when {
        extractorName.contains("youtube", ignoreCase = true) -> Triple(YouTubeRed, Color.White, "YouTube")
        extractorName.contains("tiktok", ignoreCase = true) -> Triple(Color(0xFF010101), TikTokCyan, "TikTok")
        extractorName.contains("twitter", ignoreCase = true) || extractorName.contains("x", ignoreCase = true) -> Triple(TwitterBlue, Color.White, "Twitter / X")
        extractorName.contains("instagram", ignoreCase = true) -> Triple(InstagramPurple, Color.White, "Instagram")
        extractorName.contains("facebook", ignoreCase = true) -> Triple(FacebookBlue, Color.White, "Facebook")
        extractorName.contains("reddit", ignoreCase = true) -> Triple(RedditOrange, Color.White, "Reddit")
        extractorName.contains("bilibili", ignoreCase = true) -> Triple(BilibiliPink, Color.White, "Bilibili")
        extractorName.contains("soundcloud", ignoreCase = true) -> Triple(SoundCloudOrange, Color.White, "SoundCloud")
        else -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.onSecondaryContainer, extractorName)
    }

    Box(
        modifier = modifier
            .testTag("platform_badge")
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor.copy(alpha = 0.9f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                color = textColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
