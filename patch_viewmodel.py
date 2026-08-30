import re

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

# Add ActiveDownloadData data class if not exists
if "data class ActiveDownloadData" not in content:
    content = content.replace("class MainViewModel(application: Application) : AndroidViewModel(application) {", "data class ActiveDownloadData(\n    val processId: String,\n    val config: com.example.model.DownloadConfig,\n    val mediaInfo: com.example.model.MediaModel,\n    val progress: com.example.model.DownloadProgress,\n    val isPaused: Boolean = false\n)\n\nclass MainViewModel(application: Application) : AndroidViewModel(application) {")

# Replace activeDownload tracking
content = re.sub(r'val activeDownload = .*?\n', 'val activeDownloads = kotlinx.coroutines.flow.MutableStateFlow<Map<String, ActiveDownloadData>>(emptyMap())\n', content)

# Remove the concurrent check that limits to 1 download (the "already in progress" toast)
old_check = '''        if (activeDownload.value != null && activeDownloadsCount.value > 0) {
            android.widget.Toast.makeText(getApplication(), "A download is already in progress. Please wait for it to finish.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }'''
new_check = '''        if (activeDownloads.value.size >= maxConcurrentDownloads.value) {
            android.widget.Toast.makeText(getApplication(), "Maximum concurrent downloads reached (${maxConcurrentDownloads.value}). Please wait.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }'''
content = content.replace(old_check, new_check)

# Let's fix the start loop
content = re.sub(
    r'is DownloadForegroundService.DownloadEvent.Started -> \{[\s\S]*?\}',
    '''is DownloadForegroundService.DownloadEvent.Started -> {
                        val mediaInfo = (uiState.value as? DownloadUiState.Ready)?.mediaInfo ?: (uiState.value as? DownloadUiState.Downloading)?.mediaInfo
                        if (mediaInfo != null) {
                            val newData = ActiveDownloadData(
                                processId = event.id,
                                config = event.config,
                                mediaInfo = mediaInfo,
                                progress = com.example.model.DownloadProgress(stage = com.example.model.DownloadStage.INITIALIZING)
                            )
                            activeDownloads.value = activeDownloads.value + (event.id to newData)
                        }
                    }''',
    content
)

# Progress event
content = re.sub(
    r'is DownloadForegroundService.DownloadEvent.Progress -> \{[\s\S]*?\}',
    '''is DownloadForegroundService.DownloadEvent.Progress -> {
                        val current = activeDownloads.value[event.id]
                        if (current != null) {
                            activeDownloads.value = activeDownloads.value + (event.id to current.copy(progress = event.progress, isPaused = event.isPaused))
                        }
                    }''',
    content
)

# Completed event
content = re.sub(
    r'is DownloadForegroundService.DownloadEvent.Completed -> \{[\s\S]*?\}',
    '''is DownloadForegroundService.DownloadEvent.Completed -> {
                        val current = activeDownloads.value[event.id]
                        if (current != null) {
                            insertHistoryItem(current.mediaInfo, event.file, current.config.isAudioOnly)
                            activeDownloads.value = activeDownloads.value - event.id
                        }
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Idle
                        }
                    }''',
    content
)

# Failed event
content = re.sub(
    r'is DownloadForegroundService.DownloadEvent.Failed -> \{[\s\S]*?\}',
    '''is DownloadForegroundService.DownloadEvent.Failed -> {
                        activeDownloads.value = activeDownloads.value - event.id
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Error(event.error)
                        }
                    }''',
    content
)

# Cancelled event
content = re.sub(
    r'is DownloadForegroundService.DownloadEvent.Cancelled -> \{[\s\S]*?\}',
    '''is DownloadForegroundService.DownloadEvent.Cancelled -> {
                        activeDownloads.value = activeDownloads.value - event.id
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Idle
                        }
                    }''',
    content
)

# togglePauseResumeDownload
content = re.sub(
    r'fun togglePauseResumeDownload\(\) \{[\s\S]*?\}',
    '''fun togglePauseResumeDownload(processId: String) {
        DownloadForegroundService.togglePauseResume(getApplication(), processId)
    }''',
    content
)

# cancelDownload
content = re.sub(
    r'fun cancelDownload\(\) \{[\s\S]*?\}',
    '''fun cancelDownload(processId: String) {
        DownloadForegroundService.cancelDownload(getApplication(), processId)
    }''',
    content
)

# Remove isDownloadPaused and activeDownloadsCount updates? Actually we just won't rely on them. Let's make activeDownloadsCount a flow from activeDownloads size.
content = re.sub(
    r'val activeDownloadsCount = MutableStateFlow\(0\)',
    '// removed count',
    content
)

content = re.sub(
    r'val isDownloadPaused = MutableStateFlow\(false\)',
    '// removed paused',
    content
)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)
