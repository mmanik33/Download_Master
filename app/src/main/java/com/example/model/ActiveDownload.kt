package com.example.model

data class ActiveDownload(
    val title: String,
    val thumbnailUrl: String?,
    val qualityTag: String,
    val progress: DownloadProgress
)
