import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# Fix isDownloadPaused and activeDownload bindings
old_bindings = '''    val isPaused by viewModel.isDownloadPaused.collectAsStateWithLifecycle()
    val activeDownload by viewModel.activeDownload.collectAsStateWithLifecycle()
    val isDownloading = activeDownload != null
    val downloadingCount = if (isDownloading) 1 else 0'''

new_bindings = '''    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()
    val downloadingCount = activeDownloads.size'''

content = content.replace(old_bindings, new_bindings)

# Fix the LazyColumn mapping for active downloads
old_mapping = '''            activeDownload?.let { downloadingState ->
                if (selectedTab == 0 || selectedTab == 1) {
                    item(key = "active_download_item") {
                        ActiveDownloadCard(
                            title = downloadingState.mediaInfo.title,
                            thumbnailUrl = downloadingState.mediaInfo.thumbnailUrl,
                            qualityTag = if (downloadingState.config.isAudioOnly) downloadingState.config.audioFormat.name else downloadingState.config.videoQuality.label,
                            progressPercent = downloadingState.progress.progressPercent.toInt(),
                            progressText = if (isPaused) "Paused • Tap play to resume" else downloadingState.progress.lineText.ifBlank { "${downloadingState.progress.progressPercent.toInt()}%" },
                            speedText = if (isPaused) "Paused" else downloadingState.progress.speedText,
                            stageText = if (isPaused) "PAUSED" else downloadingState.progress.stage.name,
                            isPaused = isPaused,
                            onPauseResume = { viewModel.togglePauseResumeDownload() },
                            onCancel = { showCancelDownloadDialog = true }
                        )
                    }
                }
            }'''

new_mapping = '''            if (selectedTab == 0 || selectedTab == 1) {
                items(activeDownloads.values.toList(), key = { it.processId }) { downloadingState ->
                    ActiveDownloadCard(
                        title = downloadingState.mediaInfo.title,
                        thumbnailUrl = downloadingState.mediaInfo.thumbnailUrl,
                        qualityTag = if (downloadingState.config.isAudioOnly) downloadingState.config.audioFormat.name else downloadingState.config.videoQuality.label,
                        progressPercent = downloadingState.progress.progressPercent.toInt(),
                        progressText = if (downloadingState.isPaused) "Paused • Tap play to resume" else downloadingState.progress.lineText.ifBlank { "${downloadingState.progress.progressPercent.toInt()}%" },
                        speedText = if (downloadingState.isPaused) "Paused" else downloadingState.progress.speedText,
                        stageText = if (downloadingState.isPaused) "PAUSED" else downloadingState.progress.stage.name,
                        isPaused = downloadingState.isPaused,
                        onPauseResume = { viewModel.togglePauseResumeDownload(downloadingState.processId) },
                        onCancel = { 
                            // Quick way to cancel without dialog for multi-downloads, or use dialog but track ID
                            viewModel.cancelDownload(downloadingState.processId) 
                        }
                    )
                }
            }'''

content = content.replace(old_mapping, new_mapping)

# Remove the cancel dialog usage or fix it, right now it is hardcoded to single, so we removed it above.
# Let's also check if `activeDownload` or `isPaused` is used elsewhere in DownloadsScreen.kt
with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
