import re

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

# Add activeDownload state flow
content = content.replace(
    'val activeDownloadsCount = MutableStateFlow(0)',
    'val activeDownload = kotlinx.coroutines.flow.MutableStateFlow<com.example.ui.state.DownloadUiState.Downloading?>(null)\n    val activeDownloadsCount = MutableStateFlow(0)'
)

# Update startDownloadWithQuality
start_download = """
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
        val downloadingState = DownloadUiState.Downloading(
            mediaInfo = media,
            config = config,
            progress = DownloadProgress(stage = DownloadStage.INITIALIZING)
        )
        _uiState.value = downloadingState
        activeDownload.value = downloadingState

        activeDownloadsCount.value = 1
        DownloadForegroundService.startDownload(getApplication(), config)
    }
"""

content = re.sub(r'    fun startDownloadWithQuality\(.*?\n    \}', start_download.strip('\n'), content, flags=re.DOTALL)

# Update observeServiceEvents
content = content.replace(
    '''
                    is DownloadForegroundService.DownloadEvent.Progress -> {
                        val currentState = _uiState.value
                        if (currentState is DownloadUiState.Downloading) {
                            _uiState.value = currentState.copy(progress = event.progress)
                            isDownloadPaused.value = event.progress.isPaused || event.progress.stage == DownloadStage.PAUSED
                        }
                    }
'''.strip('\n'),
    '''
                    is DownloadForegroundService.DownloadEvent.Progress -> {
                        val currentActive = activeDownload.value
                        if (currentActive != null) {
                            val updated = currentActive.copy(progress = event.progress)
                            activeDownload.value = updated
                            if (_uiState.value is DownloadUiState.Downloading) {
                                _uiState.value = updated
                            }
                            isDownloadPaused.value = event.progress.isPaused || event.progress.stage == DownloadStage.PAUSED
                        }
                    }
'''.strip('\n')
)

content = content.replace(
    '''
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
'''.strip('\n'),
    '''
                    is DownloadForegroundService.DownloadEvent.Completed -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        val currentActive = activeDownload.value
                        if (currentActive != null) {
                            if (_uiState.value is DownloadUiState.Downloading) {
                                _uiState.value = DownloadUiState.Success(
                                    mediaInfo = currentActive.mediaInfo,
                                    downloadedFile = event.file
                                )
                            }
                            addHistoryItem(currentActive.mediaInfo, event.file, currentActive.config.isAudioOnly)
                        }
                        activeDownload.value = null
                    }
'''.strip('\n')
)

content = content.replace(
    '''
                    is DownloadForegroundService.DownloadEvent.Failed -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        val currentState = _uiState.value
                        val url = if (currentState is DownloadUiState.Downloading) currentState.config.url else null
                        _uiState.value = DownloadUiState.Error(event.error, url)
                    }
'''.strip('\n'),
    '''
                    is DownloadForegroundService.DownloadEvent.Failed -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        val url = activeDownload.value?.config?.url
                        if (_uiState.value is DownloadUiState.Downloading) {
                            _uiState.value = DownloadUiState.Error(event.error, url)
                        }
                        activeDownload.value = null
                    }
'''.strip('\n')
)

content = content.replace(
    '''
                    is DownloadForegroundService.DownloadEvent.Cancelled -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        _uiState.value = DownloadUiState.Idle
                    }
'''.strip('\n'),
    '''
                    is DownloadForegroundService.DownloadEvent.Cancelled -> {
                        activeDownloadsCount.value = 0
                        isDownloadPaused.value = false
                        if (_uiState.value is DownloadUiState.Downloading) {
                            _uiState.value = DownloadUiState.Idle
                        }
                        activeDownload.value = null
                    }
'''.strip('\n')
)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)
