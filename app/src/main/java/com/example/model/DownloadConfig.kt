package com.example.model

/**
 * Configuration options for downloading a video or audio stream.
 */
data class DownloadConfig(
    val url: String,
    val title: String,
    val isAudioOnly: Boolean = false,
    val audioFormat: AudioFormat = AudioFormat.MP3,
    val videoQuality: VideoQuality = VideoQuality.BEST,
    val selectedFormatId: String? = null,
    val embedThumbnail: Boolean = true,
    val embedMetadata: Boolean = true,
    val useAria2c: Boolean = true,
    val customArguments: String = "",
    val outputDir: String = ""
)

enum class AudioFormat(val ext: String, val label: String) {
    MP3("mp3", "MP3"),
    M4A("m4a", "M4A (AAC)"),
    OPUS("opus", "OPUS"),
    FLAC("flac", "FLAC (Lossless)"),
    WAV("wav", "WAV")
}

enum class VideoQuality(val label: String, val heightFilter: String) {
    BEST("Best Available", "bv*+ba/b"),
    P2160("4K (2160p)", "bv*[height<=2160]+ba/b[height<=2160]/bv*+ba/b"),
    P1440("2K (1440p)", "bv*[height<=1440]+ba/b[height<=1440]/bv*+ba/b"),
    P1080("1080p", "bv*[height<=1080]+ba/b[height<=1080]/bv*+ba/b"),
    P720("720p", "bv*[height<=720]+ba/b[height<=720]/bv*+ba/b"),
    P480("480p", "bv*[height<=480]+ba/b[height<=480]/bv*+ba/b"),
    P360("360p", "bv*[height<=360]+ba/b[height<=360]/bv*+ba/b")
}
