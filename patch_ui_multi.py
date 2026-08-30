import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# Fix NPE on activeDownload!!
content = content.replace(
    '''
            if (isDownloading && (selectedTab == 0 || selectedTab == 1)) {
                val downloadingState = activeDownload!!
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
'''.strip('\n'),
    '''
            activeDownload?.let { downloadingState ->
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
            }
'''.strip('\n')
)

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
