package com.example.ui.components

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.AudioFormat
import com.example.model.MediaModel
import com.example.model.VideoQuality
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.PrimaryPurple

data class QualityOption(
    val label: String,
    val resolutionTag: String,
    val format: String,
    val estimatedSize: String,
    val quality: VideoQuality,
    val isAudio: Boolean = false,
    val audioFormat: AudioFormat = AudioFormat.MP3,
    val formatId: String? = null
)

private const val TAG = "QualitySheet"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoQualitySelectionSheet(
    media: MediaModel,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onDownloadSelected: (quality: VideoQuality, isAudioOnly: Boolean, audioFormat: AudioFormat, formatId: String?) -> Unit
) {
    val colors = AppTheme.colors
    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Video, 1: Audio

    // Dynamically calculate real video resolutions strictly from actual media formats
    val videoOptions = remember(media) {
        // Strictly filter valid video streams (exclude storyboards, mhtml, audio-only, or thumbnails)
        val videoFormats = media.availableFormats.filter { fmt ->
            fmt.isVideo &&
            !fmt.ext.equals("mhtml", ignoreCase = true) &&
            !fmt.formatId.startsWith("sb", ignoreCase = true) &&
            fmt.formatNote?.contains("storyboard", ignoreCase = true) != true &&
            (fmt.height >= 140 || (!fmt.resolution.isNullOrBlank() && fmt.resolution != "Audio"))
        }

        // Find the best audio stream size to add to adaptive (video-only) streams
        val audioFormats = media.availableFormats.filter { it.isAudioOnly }
        val bestAudioFileSize = audioFormats.map { it.fileSize }.filter { it > 0 }.maxOrNull() ?: 0L

        // Extract all distinct real heights available in the media stream
        val groupedByHeight = videoFormats.mapNotNull { fmt ->
            val h = if (fmt.height >= 140) {
                fmt.height
            } else {
                val match = Regex("""(\d{3,4})p?""").find(fmt.resolution ?: "")
                match?.groupValues?.get(1)?.toIntOrNull()
            }
            if (h != null && h >= 140) Pair(h, fmt) else null
        }.groupBy({ it.first }, { it.second })

        val isYouTube = media.webpageUrl.contains("youtube.com", ignoreCase = true) ||
                        media.webpageUrl.contains("youtu.be", ignoreCase = true) ||
                        media.extractorName.contains("youtube", ignoreCase = true)

        val list = mutableListOf<QualityOption>()

        // Check if explicit TikTok or specialized stream format models exist
        val hasExplicitFormats = videoFormats.any { it.formatId.startsWith("tik_") }
        if (hasExplicitFormats) {
            videoFormats.forEach { fmt ->
                val qual = when {
                    fmt.height >= 2160 -> VideoQuality.P2160
                    fmt.height >= 1440 -> VideoQuality.P1440
                    fmt.height >= 1080 -> VideoQuality.P1080
                    fmt.height >= 720 -> VideoQuality.P720
                    fmt.height >= 480 -> VideoQuality.P480
                    else -> VideoQuality.P360
                }
                list.add(
                    QualityOption(
                        label = fmt.formatNote ?: if (fmt.height > 0) "${fmt.height}p" else "Standard Quality",
                        resolutionTag = if (fmt.height > 0) "${fmt.height}p" else "HD",
                        format = fmt.ext.uppercase(),
                        estimatedSize = if (fmt.fileSize > 0) formatBytes(fmt.fileSize) else calculateEstimatedSize(media.durationSeconds, fmt.height.coerceAtLeast(720), fmt.fps),
                        quality = qual,
                        formatId = fmt.formatId
                    )
                )
            }
        } else if (groupedByHeight.isNotEmpty()) {
            val sortedHeights = groupedByHeight.keys.sortedDescending()
            val maxHeight = sortedHeights.first()

            sortedHeights.forEach { h ->
                val formats = groupedByHeight[h] ?: emptyList()
                // Pick best format: prefer mp4/avc, higher fps, valid filesize
                val bestFormat = formats.maxByOrNull { fmt ->
                    var score = (fmt.fps * 1000).toLong()
                    val ext = fmt.ext.lowercase()
                    val vcodec = fmt.vcodec?.lowercase() ?: ""
                    if (ext.contains("mp4")) score += 200
                    if (vcodec.contains("avc") || vcodec.contains("h264")) score += 150
                    else if (vcodec.contains("vp9") || vcodec.contains("vp09")) score += 100
                    else if (vcodec.contains("av01")) score += 50
                    if (fmt.fileSize > 0) score += (fmt.fileSize / 1000000)
                    score
                } ?: formats.first()

                val fps = bestFormat.fps.toInt()
                val fpsSuffix = if (fps >= 45) " ${fps}fps" else ""

                val baseLabel = when {
                    h >= 4320 -> "8K (4320p$fpsSuffix)"
                    h >= 2160 -> "4K (2160p$fpsSuffix)"
                    h >= 1440 -> "2K (1440p$fpsSuffix)"
                    h in 1000..1120 -> "1080p (Full HD$fpsSuffix)"
                    h in 680..760 -> "720p (HD$fpsSuffix)"
                    h in 440..520 -> "480p (SD)"
                    h in 320..380 -> "360p (Data Saver)"
                    h in 200..280 -> "240p (Low)"
                    h >= 144 -> "${h}p (Eco)"
                    else -> "${h}p"
                }

                val fullLabel = if (h == maxHeight && h >= 720) "$baseLabel • Max" else baseLabel
                val resTag = when {
                    h >= 4320 -> "8K"
                    h >= 2160 -> "4K"
                    h >= 1440 -> "2K"
                    h in 1000..1120 -> "1080p"
                    h in 680..760 -> "720p"
                    h in 440..520 -> "480p"
                    h in 320..380 -> "360p"
                    h in 200..280 -> "240p"
                    else -> "${h}p"
                }

                val qual = when {
                    h >= 2160 -> VideoQuality.P2160
                    h >= 1440 -> VideoQuality.P1440
                    h >= 1000 -> VideoQuality.P1080
                    h >= 680 -> VideoQuality.P720
                    h >= 440 -> VideoQuality.P480
                    else -> VideoQuality.P360
                }

                // Compute combined filesize (video stream + audio stream for DASH adaptive streams)
                val isMuxed = bestFormat.acodec != null && bestFormat.acodec != "none"
                val combinedFileSize = if (bestFormat.fileSize > 0) {
                    if (isMuxed) bestFormat.fileSize else (bestFormat.fileSize + bestAudioFileSize)
                } else {
                    0L
                }

                val estSize = if (combinedFileSize > 0) {
                    formatBytes(combinedFileSize)
                } else {
                    calculateEstimatedSize(media.durationSeconds, h, bestFormat.fps)
                }

                Log.d(TAG, "Mapped resolution ${h}p -> formatId: ${bestFormat.formatId}, ext: ${bestFormat.ext}, size: $estSize")

                list.add(
                    QualityOption(
                        label = fullLabel,
                        resolutionTag = resTag,
                        format = "MP4",
                        estimatedSize = estSize,
                        quality = qual,
                        formatId = if (bestFormat.formatId.isNotBlank()) "${bestFormat.formatId}@${h}p" else "${h}p"
                    )
                )
            }

            // If no video formats were extracted, provide standard fallback presets
            if (list.isEmpty()) {
                val fallbackPresets = listOf(
                    Triple(2160, "4K (2160p)", VideoQuality.P2160),
                    Triple(1440, "2K (1440p)", VideoQuality.P1440),
                    Triple(1080, "1080p (Full HD)", VideoQuality.P1080),
                    Triple(720, "720p (HD)", VideoQuality.P720),
                    Triple(480, "480p (SD)", VideoQuality.P480),
                    Triple(360, "360p (Data Saver)", VideoQuality.P360)
                )
                for ((stdH, stdLabel, stdQual) in fallbackPresets) {
                    val tag = when (stdH) {
                        2160 -> "4K"
                        1440 -> "2K"
                        else -> "${stdH}p"
                    }
                    list.add(
                        QualityOption(
                            label = stdLabel,
                            resolutionTag = tag,
                            format = "MP4",
                            estimatedSize = calculateEstimatedSize(media.durationSeconds, stdH),
                            quality = stdQual,
                            formatId = "${stdH}p"
                        )
                    )
                }
            } else if (isYouTube && maxHeight < 1080) {
                // If YouTube returned lower stream formats, also ensure 1080p and 720p options exist
                val presets = listOf(
                    Triple(1080, "1080p (Full HD)", VideoQuality.P1080),
                    Triple(720, "720p (HD)", VideoQuality.P720),
                    Triple(480, "480p (SD)", VideoQuality.P480),
                    Triple(360, "360p (Data Saver)", VideoQuality.P360)
                )
                for ((stdH, stdLabel, stdQual) in presets) {
                    val alreadyPresent = list.any { opt ->
                        val h = opt.resolutionTag.removeSuffix("p").toIntOrNull() ?: 0
                        h in (stdH - 40)..(stdH + 40)
                    }
                    if (!alreadyPresent) {
                        list.add(
                            QualityOption(
                                label = stdLabel,
                                resolutionTag = "${stdH}p",
                                format = "MP4",
                                estimatedSize = calculateEstimatedSize(media.durationSeconds, stdH),
                                quality = stdQual,
                                formatId = "${stdH}p"
                            )
                        )
                    }
                }
            }

            // Deduplicate options by label or resolutionTag so identical quality cards are never duplicated
            val distinctList = list.distinctBy { it.resolutionTag }
            list.clear()
            list.addAll(distinctList)

            list.sortByDescending { opt ->
                val h = opt.resolutionTag.removeSuffix("p").removeSuffix("K").toIntOrNull()
                if (opt.resolutionTag == "4K") 2160
                else if (opt.resolutionTag == "8K") 4320
                else if (opt.resolutionTag == "2K") 1440
                else h ?: (opt.quality.ordinal * 100)
            }
        } else {
            // Fallback when no stream heights were pre-extracted (YouTube presets up to 4K)
            if (isYouTube) {
                list.add(QualityOption("4K (2160p)", "4K", "MP4", calculateEstimatedSize(media.durationSeconds, 2160), VideoQuality.P2160, formatId = "2160p"))
                list.add(QualityOption("2K (1440p)", "2K", "MP4", calculateEstimatedSize(media.durationSeconds, 1440), VideoQuality.P1440, formatId = "1440p"))
            }
            list.add(QualityOption("1080p (Full HD)", "1080p", "MP4", calculateEstimatedSize(media.durationSeconds, 1080), VideoQuality.P1080, formatId = "1080p"))
            list.add(QualityOption("720p (HD)", "720p", "MP4", calculateEstimatedSize(media.durationSeconds, 720), VideoQuality.P720, formatId = "720p"))
            list.add(QualityOption("480p (SD)", "480p", "MP4", calculateEstimatedSize(media.durationSeconds, 480), VideoQuality.P480, formatId = "480p"))
            list.add(QualityOption("360p (Data Saver)", "360p", "MP4", calculateEstimatedSize(media.durationSeconds, 360), VideoQuality.P360, formatId = "360p"))
        }

        list
    }

    val audioOptions = remember(media) {
        listOf(
            QualityOption("MP3 (320kbps High Quality)", "MP3", "MP3", calculateEstimatedAudioSize(media.durationSeconds, 320), VideoQuality.BEST, isAudio = true, audioFormat = AudioFormat.MP3),
            QualityOption("M4A (AAC 256kbps)", "M4A", "M4A", calculateEstimatedAudioSize(media.durationSeconds, 256), VideoQuality.BEST, isAudio = true, audioFormat = AudioFormat.M4A),
            QualityOption("FLAC (Lossless Studio)", "FLAC", "FLAC", calculateEstimatedAudioSize(media.durationSeconds, 800), VideoQuality.BEST, isAudio = true, audioFormat = AudioFormat.FLAC),
            QualityOption("Opus (High Efficiency)", "OPUS", "OPUS", calculateEstimatedAudioSize(media.durationSeconds, 160), VideoQuality.BEST, isAudio = true, audioFormat = AudioFormat.OPUS),
            QualityOption("WAV (Uncompressed)", "WAV", "WAV", calculateEstimatedAudioSize(media.durationSeconds, 1411), VideoQuality.BEST, isAudio = true, audioFormat = AudioFormat.WAV)
        )
    }

    var selectedVideoIndex by remember(videoOptions) { mutableIntStateOf(0) }
    var selectedAudioIndex by remember { mutableIntStateOf(0) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.background,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Media Quality",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Video Preview Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(100.dp)
                            .aspectRatio(16f / 9f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.surfaceVariant)
                    ) {
                        if (!media.thumbnailUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(media.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Thumbnail",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        }
                        // Duration Tag
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color.Black.copy(alpha = 0.8f))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = media.durationFormatted,
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = media.title,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = media.webpageUrl,
                            color = colors.textSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tab Row: Video vs Audio
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = colors.surface,
                contentColor = PrimaryPurple,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PrimaryPurple
                    )
                }
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Video Formats", fontWeight = FontWeight.Bold, color = if (selectedTabIndex == 0) colors.textPrimary else colors.textSecondary) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Audio Extraction", fontWeight = FontWeight.Bold, color = if (selectedTabIndex == 1) colors.textPrimary else colors.textSecondary) }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (selectedTabIndex == 0) "Available Resolutions (${videoOptions.size})" else "Select Audio Quality",
                color = colors.textSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quality Options List
            val currentOptions = if (selectedTabIndex == 0) videoOptions else audioOptions
            val currentIndex = if (selectedTabIndex == 0) selectedVideoIndex else selectedAudioIndex

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                currentOptions.forEachIndexed { index, option ->
                    val isSelected = index == currentIndex
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.surfaceVariant else colors.surface)
                            .clickable {
                                if (selectedTabIndex == 0) selectedVideoIndex = index else selectedAudioIndex = index
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                if (selectedTabIndex == 0) selectedVideoIndex = index else selectedAudioIndex = index
                            },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = PrimaryPurple,
                                unselectedColor = colors.textSecondary
                            ),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option.label,
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = option.format,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = option.estimatedSize,
                            color = colors.textSecondary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Glowing Gradient Download Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(PrimaryGradient)
                    .clickable {
                        if (selectedTabIndex == 0 && videoOptions.isNotEmpty()) {
                            val opt = videoOptions[selectedVideoIndex.coerceIn(0, videoOptions.lastIndex)]
                            Log.i(TAG, "Selected Video Quality: label=${opt.label}, quality=${opt.quality}, formatId=${opt.formatId}")
                            onDownloadSelected(opt.quality, false, AudioFormat.MP3, opt.formatId)
                        } else if (audioOptions.isNotEmpty()) {
                            val opt = audioOptions[selectedAudioIndex.coerceIn(0, audioOptions.lastIndex)]
                            Log.i(TAG, "Selected Audio Quality: label=${opt.label}, audioFormat=${opt.audioFormat}, formatId=${opt.formatId}")
                            onDownloadSelected(opt.quality, true, opt.audioFormat, opt.formatId)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Download",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0) return "--"
    val mb = bytes / (1024.0 * 1024.0)
    return if (mb >= 1000) {
        String.format("%.2f GB", mb / 1024.0)
    } else {
        String.format("%.1f MB", mb)
    }
}

private fun calculateEstimatedSize(durationSec: Long, height: Int, fps: Double = 30.0): String {
    val fpsMultiplier = if (fps > 45) 1.35 else 1.0
    if (durationSec <= 0) {
        return when {
            height >= 4320 -> "4.80 GB"
            height >= 2160 -> if (fps > 45) "2.85 GB" else "2.10 GB"
            height >= 1440 -> if (fps > 45) "1.50 GB" else "1.15 GB"
            height >= 1080 -> if (fps > 45) "850 MB" else "650 MB"
            height >= 720 -> if (fps > 45) "480 MB" else "350 MB"
            height >= 480 -> "160 MB"
            height >= 360 -> "110 MB"
            height >= 240 -> "70 MB"
            else -> "40 MB"
        }
    }
    val bitrateKbps = when {
        height >= 4320 -> (30000L * fpsMultiplier).toLong()
        height >= 2160 -> (16000L * fpsMultiplier).toLong()
        height >= 1440 -> (8500L * fpsMultiplier).toLong()
        height >= 1080 -> (4500L * fpsMultiplier).toLong()
        height >= 720 -> (2400L * fpsMultiplier).toLong()
        height >= 480 -> 1000L
        height >= 360 -> 600L
        height >= 240 -> 350L
        else -> 200L
    }
    val totalBytes = (durationSec * bitrateKbps * 1000) / 8
    val mb = totalBytes / (1024 * 1024)
    return if (mb >= 1000) {
        String.format("%.2f GB", mb / 1024.0)
    } else {
        "${mb} MB"
    }
}

private fun calculateEstimatedAudioSize(durationSec: Long, kbps: Int): String {
    if (durationSec <= 0) {
        return when (kbps) {
            1411 -> "45.0 MB"
            800 -> "25.0 MB"
            320 -> "10.5 MB"
            256 -> "8.2 MB"
            else -> "5.0 MB"
        }
    }
    val totalBytes = (durationSec * kbps * 1000) / 8
    return String.format("%.1f MB", totalBytes / (1024.0 * 1024.0))
}
