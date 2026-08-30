package com.example.ui.state

import com.example.model.DownloadConfig
import com.example.model.DownloadProgress
import com.example.model.MediaModel
import java.io.File

/**
 * UI State representing the state of media parsing, configuring, and downloading.
 */
sealed interface DownloadUiState {
    data object Idle : DownloadUiState
    
    data class Parsing(val url: String) : DownloadUiState
    
    data class Ready(
        val mediaInfo: MediaModel,
        val config: DownloadConfig
    ) : DownloadUiState
    
    data class Downloading(
        val mediaInfo: MediaModel,
        val config: DownloadConfig,
        val progress: DownloadProgress
    ) : DownloadUiState
    
    data class Success(
        val mediaInfo: MediaModel,
        val downloadedFile: File,
        val filePath: String = downloadedFile.absolutePath
    ) : DownloadUiState
    
    data class Error(
        val message: String,
        val failedUrl: String? = null
    ) : DownloadUiState
}

/**
 * UI State for yt-dlp binary update status.
 */
sealed interface EngineUpdateState {
    data object Idle : EngineUpdateState
    data object CheckingOrUpdating : EngineUpdateState
    data class Success(val message: String) : EngineUpdateState
    data class Error(val message: String) : EngineUpdateState
}
