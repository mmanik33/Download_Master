package com.example.model

/**
 * Real-time progress updates emitted by yt-dlp execution.
 */
data class DownloadProgress(
    val progressPercent: Float = 0f,
    val speedText: String = "0 KB/s",
    val etaText: String = "--:--",
    val lineText: String = "",
    val stage: DownloadStage = DownloadStage.QUEUED,
    val isPaused: Boolean = false
)

enum class DownloadStage {
    QUEUED,
    INITIALIZING,
    DOWNLOADING,
    PAUSED,
    EXTRACTING_AUDIO,
    MERGING_FORMATS,
    EMBEDDING_METADATA,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Entity representing completed or in-progress download history.
 */
data class DownloadHistoryItem(
    val id: String,
    val title: String,
    val webpageUrl: String,
    val localFilePath: String,
    val fileSizeFormatted: String,
    val isAudioOnly: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null
)
