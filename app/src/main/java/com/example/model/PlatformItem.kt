package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DailymotionBlue
import com.example.ui.theme.FacebookBlue
import com.example.ui.theme.InstagramPink
import com.example.ui.theme.LikeePink
import com.example.ui.theme.PinterestRed
import com.example.ui.theme.RedditOrange
import com.example.ui.theme.SoundCloudOrange
import com.example.ui.theme.TikTokCyan
import com.example.ui.theme.TumblrBlue
import com.example.ui.theme.TwitterBlue
import com.example.ui.theme.VimeoBlue
import com.example.ui.theme.YouTubeRed

/**
 * Model representing supported video & audio streaming platforms with vector brand icons.
 */
data class PlatformItem(
    val id: String,
    val name: String,
    val homeUrl: String,
    val brandColor: Color
)

val SupportedPlatformsList = listOf(
    PlatformItem("youtube", "YouTube", "https://m.youtube.com", YouTubeRed),
    PlatformItem("facebook", "Facebook", "https://m.facebook.com", FacebookBlue),
    PlatformItem("twitter", "Twitter / X", "https://mobile.twitter.com", TwitterBlue),
    PlatformItem("tiktok", "TikTok", "https://www.tiktok.com", TikTokCyan),
    PlatformItem("instagram", "Instagram", "https://www.instagram.com", InstagramPink),
    PlatformItem("vimeo", "Vimeo", "https://vimeo.com", VimeoBlue),
    PlatformItem("dailymotion", "Dailymotion", "https://www.dailymotion.com", DailymotionBlue),
    PlatformItem("likee", "Likee", "https://likee.video", LikeePink),
    PlatformItem("pinterest", "Pinterest", "https://www.pinterest.com", PinterestRed),
    PlatformItem("reddit", "Reddit", "https://www.reddit.com", RedditOrange),
    PlatformItem("soundcloud", "SoundCloud", "https://soundcloud.com", SoundCloudOrange),
    PlatformItem("tumblr", "Tumblr", "https://www.tumblr.com", TumblrBlue)
)

