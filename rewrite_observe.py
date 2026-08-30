import re

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

# We will completely replace the `private fun observeServiceEvents()` block up to `fun onUrlInputChanged`
pattern = r'private fun observeServiceEvents\(\) \{[\s\S]*?fun onUrlInputChanged'

replacement = '''private fun observeServiceEvents() {
        viewModelScope.launch {
            DownloadForegroundService.downloadEvents.collect { event ->
                when (event) {
                    is DownloadForegroundService.DownloadEvent.Started -> {
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
                    }
                    is DownloadForegroundService.DownloadEvent.Progress -> {
                        val current = activeDownloads.value[event.id]
                        if (current != null) {
                            activeDownloads.value = activeDownloads.value + (event.id to current.copy(progress = event.progress, isPaused = event.isPaused))
                        }
                    }
                    is DownloadForegroundService.DownloadEvent.Completed -> {
                        val current = activeDownloads.value[event.id]
                        if (current != null) {
                            addHistoryItem(current.mediaInfo, event.file, current.config.isAudioOnly)
                            activeDownloads.value = activeDownloads.value - event.id
                        }
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Idle
                        }
                    }
                    is DownloadForegroundService.DownloadEvent.Failed -> {
                        activeDownloads.value = activeDownloads.value - event.id
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Error(event.error)
                        }
                    }
                    is DownloadForegroundService.DownloadEvent.Cancelled -> {
                        activeDownloads.value = activeDownloads.value - event.id
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Idle
                        }
                    }
                }
            }
        }
    }

    fun onUrlInputChanged'''

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)
