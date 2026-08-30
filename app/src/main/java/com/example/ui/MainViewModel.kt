package com.example.ui

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.DownloadMasterApp
import com.example.data.YtDlpRepository
import com.example.model.AudioFormat
import com.example.model.BookmarkItem
import com.example.model.DownloadConfig
import com.example.model.DownloadHistoryItem
import com.example.model.DownloadProgress
import com.example.model.DownloadStage
import com.example.model.MediaModel
import com.example.model.VideoQuality
import com.example.service.DownloadForegroundService
import com.example.ui.state.DownloadUiState
import com.example.ui.state.EngineUpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

enum class MainTab(val title: String) {
    HOME("Home"),
    DOWNLOADS("Downloads"),
    BROWSER("Browser"),
    SETTINGS("Settings")
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = YtDlpRepository(application)
    private val prefs: SharedPreferences = application.getSharedPreferences("download_master_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val TAG = "MainViewModel"
        private const val KEY_HISTORY = "key_download_history"
        private const val KEY_BOOKMARKS = "key_bookmarks"
        private const val KEY_USE_ARIA2C = "key_use_aria2c"
        private const val KEY_EMBED_THUMBNAIL = "key_embed_thumbnail"
        private const val KEY_EMBED_METADATA = "key_embed_metadata"
        private const val KEY_USE_WIFI_ONLY = "key_use_wifi_only"
        private const val KEY_MAX_CONCURRENT = "key_max_concurrent"
        private const val KEY_DEFAULT_QUALITY = "key_default_quality"
        private const val KEY_DOWNLOAD_PATH = "key_download_path"
        private const val KEY_THEME_MODE = "key_theme_mode" // "SYSTEM", "LIGHT", "DARK"
        private const val KEY_COLOR_PALETTE = "key_color_palette" // "DYNAMIC", "PURPLE", "BLUE", "GREEN", "ORANGE", "ROSE"
    }

    // Active Theme Mode
    val themeMode = MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")

    fun setThemeMode(mode: String) {
        themeMode.value = mode
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    // Active Color Palette / Dynamic Theme
    val colorPalette = MutableStateFlow(prefs.getString(KEY_COLOR_PALETTE, "PURPLE") ?: "PURPLE")

    fun setColorPalette(palette: String) {
        colorPalette.value = palette
        prefs.edit().putString(KEY_COLOR_PALETTE, palette).apply()
    }

    // Active Navigation Tab
    private val _currentTab = MutableStateFlow(MainTab.HOME)
    val currentTab: StateFlow<MainTab> = _currentTab.asStateFlow()

    // Home URL Input
    private val _urlInput = MutableStateFlow("")
    val urlInput: StateFlow<String> = _urlInput.asStateFlow()

    // Download UI State
    private val _uiState = MutableStateFlow<DownloadUiState>(DownloadUiState.Idle)
    val uiState: StateFlow<DownloadUiState> = _uiState.asStateFlow()

    // Engine Update State
    private val _engineUpdateState = MutableStateFlow<EngineUpdateState>(EngineUpdateState.Idle)
    val engineUpdateState: StateFlow<EngineUpdateState> = _engineUpdateState.asStateFlow()

    // Download Configuration state
    val isAudioOnly = MutableStateFlow(false)
    val selectedAudioFormat = MutableStateFlow(AudioFormat.MP3)
    val selectedVideoQuality = MutableStateFlow(VideoQuality.BEST)
    val selectedFormatId = MutableStateFlow<String?>(null)
    val embedThumbnail = MutableStateFlow(prefs.getBoolean(KEY_EMBED_THUMBNAIL, true))
    val embedMetadata = MutableStateFlow(prefs.getBoolean(KEY_EMBED_METADATA, true))
    val useAria2c = MutableStateFlow(prefs.getBoolean(KEY_USE_ARIA2C, true))
    val useWifiOnly = MutableStateFlow(prefs.getBoolean(KEY_USE_WIFI_ONLY, false))
    val maxConcurrentDownloads = MutableStateFlow(prefs.getInt(KEY_MAX_CONCURRENT, 3))
    val defaultQualityPreference = MutableStateFlow(prefs.getString(KEY_DEFAULT_QUALITY, "Ask Each Time") ?: "Ask Each Time")
    val downloadLocation = MutableStateFlow(prefs.getString(KEY_DOWNLOAD_PATH, "Internal Storage/Download/DownloadMaster") ?: "Internal Storage/Download/DownloadMaster")
    val customArguments = MutableStateFlow("")

    // Active & Completed Downloads Tracking
    val activeDownloadsCount = MutableStateFlow(0)
    val isDownloadPaused = MutableStateFlow(false)

    // Download History
    private val _downloadHistory = MutableStateFlow<List<DownloadHistoryItem>>(emptyList())
    val downloadHistory: StateFlow<List<DownloadHistoryItem>> = _downloadHistory.asStateFlow()

    // History Search Query
    val historySearchQuery = MutableStateFlow("")

    // Browser State
    val browserUrlInput = MutableStateFlow("")
    val activeBrowserUrl = MutableStateFlow<String?>(null)
    val detectedBrowserVideoUrl = MutableStateFlow<String?>(null)
    private val _bookmarks = MutableStateFlow<List<BookmarkItem>>(emptyList())
    val bookmarks: StateFlow<List<BookmarkItem>> = _bookmarks.asStateFlow()

    init {
        loadHistory()
        loadBookmarks()
        observeServiceEvents()
    }

    fun selectTab(tab: MainTab) {
        _currentTab.value = tab
    }

    private fun observeServiceEvents() {
        viewModelScope.launch {
            DownloadForegroundService.isPaused.collect { paused ->
                isDownloadPaused.value = paused
            }
        }

        viewModelScope.launch {
            DownloadForegroundService.downloadEvents.collect { event ->
                when (event) {
                    is DownloadForegroundService.DownloadEvent.Started -> {
                        activeDownloadsCount.value = 1
                        isDownloadPaused.value = false
                        val currentState = _uiState.value
                        if (currentState is DownloadUiState.Ready) {
                            _uiState.value = DownloadUiState.Downloading(
                                mediaInfo = currentState.mediaInfo,
                                config = currentState.config,
                                progress = DownloadProgress(stage = DownloadStage.INITIALIZING)
                            )
                        }
                    }
                    is DownloadForegroundService.DownloadEvent.Progress -> {
                        val currentState = _uiState.value
                        if (currentState is DownloadUiState.Downloading) {
                            _uiState.value = currentState.copy(progress = event.progress)
                            isDownloadPaused.value = event.progress.isPaused || event.progress.stage == DownloadStage.PAUSED
                        }
                    }
                    is DownloadForegroundService.DownloadEvent.Completed -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        val currentState = _uiState.value
                        if (currentState is DownloadUiState.Downloading) {
                            _uiState.value = DownloadUiState.Success(
                                mediaInfo = currentState.mediaInfo,
                                downloadedFile = event.file
                            )
                            addHistoryItem(currentState.mediaInfo, event.file, currentState.config.isAudioOnly)
                        }
                    }
                    is DownloadForegroundService.DownloadEvent.Failed -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        val currentState = _uiState.value
                        val url = if (currentState is DownloadUiState.Downloading) currentState.config.url else null
                        _uiState.value = DownloadUiState.Error(event.error, url)
                    }
                    is DownloadForegroundService.DownloadEvent.Cancelled -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        _uiState.value = DownloadUiState.Idle
                    }
                }
            }
        }
    }

    fun onUrlInputChanged(newUrl: String) {
        _urlInput.value = newUrl
    }

    fun pasteFromClipboard(clipboardText: String) {
        val clean = clipboardText.trim()
        if (clean.isNotBlank()) {
            _urlInput.value = clean
            parseMediaUrl(clean)
        }
    }

    fun parseMediaUrl(url: String = _urlInput.value) {
        val targetUrl = url.trim()
        if (targetUrl.isBlank()) {
            _uiState.value = DownloadUiState.Error("Please enter a valid video or audio URL")
            return
        }

        viewModelScope.launch {
            _uiState.value = DownloadUiState.Parsing(targetUrl)
            val result = repository.extractMetadata(targetUrl)
            result.fold(
                onSuccess = { media ->
                    val config = buildCurrentConfig(media.title, targetUrl)
                    _uiState.value = DownloadUiState.Ready(
                        mediaInfo = media,
                        config = config
                    )
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to parse URL: $targetUrl", error)
                    val rawMsg = error.localizedMessage ?: "Failed to retrieve media metadata."
                    val friendlyMsg = when {
                        rawMsg.contains("confirm you're not a bot", ignoreCase = true) || rawMsg.contains("bot", ignoreCase = true) ->
                            "YouTube bot verification encountered. Please add cookies in Settings or update the yt-dlp engine."
                        rawMsg.contains("Sign in", ignoreCase = true) ->
                            "This content requires sign-in. Please provide cookies in Settings to proceed."
                        rawMsg.contains("No supported JavaScript runtime", ignoreCase = true) ->
                            "Extractor format fallback used. If this link fails, try updating the engine."
                        else -> rawMsg
                    }
                    _uiState.value = DownloadUiState.Error(
                        message = friendlyMsg,
                        failedUrl = targetUrl
                    )
                }
            )
        }
    }

    fun startDownloadWithQuality(
        media: MediaModel,
        quality: VideoQuality,
        audioOnly: Boolean,
        formatId: String? = null
    ) {
        isAudioOnly.value = audioOnly
        selectedVideoQuality.value = quality
        selectedFormatId.value = formatId

        val config = buildCurrentConfig(media.title, media.webpageUrl)
        _uiState.value = DownloadUiState.Downloading(
            mediaInfo = media,
            config = config,
            progress = DownloadProgress(stage = DownloadStage.INITIALIZING)
        )
        activeDownloadsCount.value = 1
        DownloadForegroundService.startDownload(getApplication(), config)
    }

    fun togglePauseResumeDownload() {
        DownloadForegroundService.togglePauseResume(getApplication())
    }

    fun cancelDownload() {
        DownloadForegroundService.cancelDownload(getApplication())
        activeDownloadsCount.value = 0
        _uiState.value = DownloadUiState.Idle
    }

    fun resetToIdle() {
        _uiState.value = DownloadUiState.Idle
    }

    fun updateYtDlpEngine() {
        _engineUpdateState.value = EngineUpdateState.CheckingOrUpdating
        (getApplication() as DownloadMasterApp).updateEngine { success, message ->
            viewModelScope.launch(Dispatchers.Main) {
                _engineUpdateState.value = if (success) {
                    EngineUpdateState.Success(message)
                } else {
                    EngineUpdateState.Error(message)
                }
            }
        }
    }

    fun dismissEngineUpdateDialog() {
        _engineUpdateState.value = EngineUpdateState.Idle
    }

    fun setEmbedThumbnail(enabled: Boolean) {
        embedThumbnail.value = enabled
        prefs.edit().putBoolean(KEY_EMBED_THUMBNAIL, enabled).apply()
    }

    fun setEmbedMetadata(enabled: Boolean) {
        embedMetadata.value = enabled
        prefs.edit().putBoolean(KEY_EMBED_METADATA, enabled).apply()
    }

    fun setUseAria2c(enabled: Boolean) {
        useAria2c.value = enabled
        prefs.edit().putBoolean(KEY_USE_ARIA2C, enabled).apply()
    }

    fun setUseWifiOnly(enabled: Boolean) {
        useWifiOnly.value = enabled
        prefs.edit().putBoolean(KEY_USE_WIFI_ONLY, enabled).apply()
    }

    fun setMaxConcurrentDownloads(count: Int) {
        maxConcurrentDownloads.value = count
        prefs.edit().putInt(KEY_MAX_CONCURRENT, count).apply()
    }

    fun setDefaultQualityPreference(quality: String) {
        defaultQualityPreference.value = quality
        prefs.edit().putString(KEY_DEFAULT_QUALITY, quality).apply()
    }

    fun setDownloadLocation(path: String) {
        downloadLocation.value = path
        prefs.edit().putString(KEY_DOWNLOAD_PATH, path).apply()
    }

    fun getCookiesContent(): String = repository.getCookiesContent()
    fun saveCookies(cookiesText: String): Boolean = repository.saveCookies(cookiesText)
    fun clearCookies(): Boolean = repository.clearCookies()

    // Browser Helpers
    fun navigateBrowserTo(url: String) {
        val target = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            if (url.contains(".") && !url.contains(" ")) "https://$url" else "https://www.google.com/search?q=$url"
        } else {
            url
        }
        browserUrlInput.value = target
        activeBrowserUrl.value = target
        _currentTab.value = MainTab.BROWSER
    }

    fun onBrowserUrlChanged(url: String) {
        browserUrlInput.value = url
        // Detect video link presence
        if (isVideoLink(url)) {
            detectedBrowserVideoUrl.value = url
        }
    }

    private fun isVideoLink(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("youtube.com/watch") ||
                lower.contains("youtu.be/") ||
                lower.contains("youtube.com/shorts") ||
                lower.contains("facebook.com/watch") ||
                lower.contains("fb.watch") ||
                lower.contains("tiktok.com/@") ||
                lower.contains("instagram.com/p/") ||
                lower.contains("instagram.com/reel/") ||
                lower.contains("vimeo.com/") ||
                lower.contains("dailymotion.com/video") ||
                lower.contains("twitter.com/") ||
                lower.contains("x.com/")
    }

    fun addBookmark(title: String, url: String) {
        val newBookmark = BookmarkItem(
            id = System.currentTimeMillis().toString(),
            title = title.ifBlank { "Bookmark" },
            url = url
        )
        val updated = listOf(newBookmark) + _bookmarks.value.filter { it.url != url }
        _bookmarks.value = updated
        saveBookmarks()
    }

    fun removeBookmark(id: String) {
        _bookmarks.value = _bookmarks.value.filter { it.id != id }
        saveBookmarks()
    }

    private fun saveBookmarks() {
        try {
            val arr = JSONArray()
            for (b in _bookmarks.value) {
                val obj = JSONObject().apply {
                    put("id", b.id)
                    put("title", b.title)
                    put("url", b.url)
                }
                arr.put(obj)
            }
            prefs.edit().putString(KEY_BOOKMARKS, arr.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save bookmarks", e)
        }
    }

    private fun loadBookmarks() {
        try {
            val json = prefs.getString(KEY_BOOKMARKS, null)
            if (json.isNullOrBlank()) {
                // Initial default bookmarks matching the screenshot
                _bookmarks.value = listOf(
                    BookmarkItem("1", "YouTube", "https://youtube.com"),
                    BookmarkItem("2", "Facebook", "https://facebook.com"),
                    BookmarkItem("3", "Twitter", "https://twitter.com")
                )
                saveBookmarks()
                return
            }
            val arr = JSONArray(json)
            val list = mutableListOf<BookmarkItem>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    BookmarkItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.optString("title", "Bookmark"),
                        url = obj.optString("url", "")
                    )
                )
            }
            _bookmarks.value = list
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load bookmarks", e)
        }
    }

    private fun buildCurrentConfig(title: String, url: String): DownloadConfig {
        return DownloadConfig(
            url = url,
            title = title,
            isAudioOnly = isAudioOnly.value,
            audioFormat = selectedAudioFormat.value,
            videoQuality = selectedVideoQuality.value,
            selectedFormatId = selectedFormatId.value,
            embedThumbnail = embedThumbnail.value,
            embedMetadata = embedMetadata.value,
            useAria2c = useAria2c.value,
            customArguments = customArguments.value
        )
    }

    private fun addHistoryItem(media: MediaModel, file: File, audioOnly: Boolean) {
        val newItem = DownloadHistoryItem(
            id = System.currentTimeMillis().toString(),
            title = media.title,
            webpageUrl = media.webpageUrl,
            localFilePath = file.absolutePath,
            fileSizeFormatted = formatFileSize(file.length()),
            isAudioOnly = audioOnly,
            thumbnailUrl = media.thumbnailUrl
        )
        val updated = listOf(newItem) + _downloadHistory.value.filter { it.localFilePath != file.absolutePath }
        _downloadHistory.value = updated.take(50)
        saveHistory()
    }

    fun removeHistoryItem(id: String) {
        val updated = _downloadHistory.value.filter { it.id != id }
        _downloadHistory.value = updated
        saveHistory()
    }

    fun clearHistory() {
        _downloadHistory.value = emptyList()
        prefs.edit().remove(KEY_HISTORY).apply()
    }

    private fun saveHistory() {
        try {
            val jsonArray = JSONArray()
            for (item in _downloadHistory.value) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("webpageUrl", item.webpageUrl)
                    put("localFilePath", item.localFilePath)
                    put("fileSizeFormatted", item.fileSizeFormatted)
                    put("isAudioOnly", item.isAudioOnly)
                    put("timestamp", item.timestamp)
                    put("thumbnailUrl", item.thumbnailUrl ?: "")
                }
                jsonArray.put(obj)
            }
            prefs.edit().putString(KEY_HISTORY, jsonArray.toString()).apply()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to serialize download history", e)
        }
    }

    private fun loadHistory() {
        try {
            val jsonStr = prefs.getString(KEY_HISTORY, null) ?: return
            val jsonArray = JSONArray(jsonStr)
            val items = mutableListOf<DownloadHistoryItem>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                items.add(
                    DownloadHistoryItem(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        title = obj.optString("title", "Media"),
                        webpageUrl = obj.optString("webpageUrl", ""),
                        localFilePath = obj.optString("localFilePath", ""),
                        fileSizeFormatted = obj.optString("fileSizeFormatted", ""),
                        isAudioOnly = obj.optBoolean("isAudioOnly", false),
                        timestamp = obj.optLong("timestamp", 0L),
                        thumbnailUrl = obj.optString("thumbnailUrl").takeIf { it.isNotBlank() }
                    )
                )
            }
            _downloadHistory.value = items
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load download history", e)
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }
}
