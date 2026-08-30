package com.example.model

/**
 * Model representing extracted media metadata from yt-dlp.
 */
data class MediaModel(
    val id: String,
    val title: String,
    val uploader: String,
    val uploaderUrl: String? = null,
    val durationSeconds: Long = 0,
    val thumbnailUrl: String? = null,
    val webpageUrl: String,
    val extractorName: String = "Generic",
    val description: String? = null,
    val viewCount: Long? = null,
    val availableFormats: List<FormatModel> = emptyList(),
    val directVideoUrl: String? = null
) {
    val durationFormatted: String
        get() {
            if (durationSeconds <= 0) return "--:--"
            val hours = durationSeconds / 3600
            val minutes = (durationSeconds % 3600) / 60
            val seconds = durationSeconds % 60
            return if (hours > 0) {
                String.format("%d:%02d:%02d", hours, minutes, seconds)
            } else {
                String.format("%02d:%02d", minutes, seconds)
            }
        }
}

/**
 * Representation of an individual media stream format.
 */
data class FormatModel(
    val formatId: String,
    val ext: String,
    val resolution: String? = null,
    val width: Int = 0,
    val height: Int = 0,
    val fps: Double = 0.0,
    val vcodec: String? = null,
    val acodec: String? = null,
    val fileSize: Long = 0,
    val formatNote: String? = null,
    val isVideo: Boolean = true,
    val isAudioOnly: Boolean = false
) {
    val displayLabel: String
        get() {
            return when {
                height > 0 -> "${height}p ($ext)"
                resolution != null && resolution.isNotBlank() && resolution != "audio only" -> "$resolution ($ext)"
                isAudioOnly -> "Audio Only ($ext)"
                else -> "${ext.uppercase()} - $formatId"
            }
        }
}
