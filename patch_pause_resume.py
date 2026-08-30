import re

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

# Add activeConfig variable
if "private var activeConfig: DownloadConfig?" not in content:
    content = content.replace("private var activeProcessId: String = \"\"", "private var activeProcessId: String = \"\"\n    private var activeConfig: DownloadConfig? = null")

# Update handlePause
old_pause = '''    private fun handlePause() {
        Log.i(TAG, "Pausing active download process: $activeProcessId")
        _isPaused.value = true
        repository.setPaused(activeProcessId, true)
        val cur = _currentProgress.value
        val pausedProgress = (cur ?: DownloadProgress()).copy(
            speedText = "Paused",
            stage = DownloadStage.PAUSED,
            isPaused = true
        )
        _currentProgress.value = pausedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(pausedProgress)) }
        updateProgressNotification(activeTitle, pausedProgress.progressPercent, "Paused", pausedProgress.etaText, isPaused = true)
    }'''

new_pause = '''    private fun handlePause() {
        Log.i(TAG, "Pausing active download process: $activeProcessId")
        _isPaused.value = true
        repository.cancelDownload(activeProcessId)
        val cur = _currentProgress.value
        val pausedProgress = (cur ?: DownloadProgress()).copy(
            speedText = "Paused",
            stage = DownloadStage.PAUSED,
            isPaused = true
        )
        _currentProgress.value = pausedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(pausedProgress)) }
        updateProgressNotification(activeTitle, pausedProgress.progressPercent, "Paused", pausedProgress.etaText, isPaused = true)
    }'''
content = content.replace(old_pause, new_pause)

# Update handleResume
old_resume = '''    private fun handleResume() {
        Log.i(TAG, "Resuming active download process: $activeProcessId")
        _isPaused.value = false
        repository.setPaused(activeProcessId, false)
        val cur = _currentProgress.value
        val resumedProgress = (cur ?: DownloadProgress()).copy(
            speedText = "Resuming...",
            stage = DownloadStage.DOWNLOADING,
            isPaused = false
        )
        _currentProgress.value = resumedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(resumedProgress)) }
        updateProgressNotification(activeTitle, resumedProgress.progressPercent, "Resuming...", resumedProgress.etaText, isPaused = false)
    }'''

new_resume = '''    private fun handleResume() {
        Log.i(TAG, "Resuming active download process: $activeProcessId")
        _isPaused.value = false
        val cur = _currentProgress.value
        val resumedProgress = (cur ?: DownloadProgress()).copy(
            speedText = "Resuming...",
            stage = DownloadStage.DOWNLOADING,
            isPaused = false
        )
        _currentProgress.value = resumedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(resumedProgress)) }
        updateProgressNotification(activeTitle, resumedProgress.progressPercent, "Resuming...", resumedProgress.etaText, isPaused = false)
        activeConfig?.let { performDownload(it, isResume = true) }
    }'''
content = content.replace(old_resume, new_resume)

# Update performDownload signature and logic
old_perform = '''    private fun performDownload(config: DownloadConfig) {
        downloadJob?.cancel()
        activeProcessId = "dl_${System.currentTimeMillis()}"'''

new_perform = '''    private fun performDownload(config: DownloadConfig, isResume: Boolean = false) {
        activeConfig = config
        downloadJob?.cancel()
        activeProcessId = "dl_${System.currentTimeMillis()}"'''
content = content.replace(old_perform, new_perform)

# Update onFailure
old_failure = '''                onFailure = { error ->
                    Log.e(TAG, "Download failed", error)
                    _currentProgress.value = DownloadProgress(
                        progressPercent = 0f,
                        speedText = "Error",
                        etaText = "--:--",
                        lineText = error.localizedMessage ?: "Download failed",
                        stage = DownloadStage.FAILED
                    )
                    _downloadEvents.emit(DownloadEvent.Failed(error.localizedMessage ?: "Unknown error"))
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    notificationManager.cancel(NOTIFICATION_ID)
                    showFailureNotification(config.title, error.localizedMessage ?: "Download failed")
                    stopSelf()
                }'''

new_failure = '''                onFailure = { error ->
                    if (_isPaused.value) {
                        Log.i(TAG, "Download paused intentionally, ignoring failure.")
                    } else {
                        Log.e(TAG, "Download failed", error)
                        _currentProgress.value = DownloadProgress(
                            progressPercent = 0f,
                            speedText = "Error",
                            etaText = "--:--",
                            lineText = error.localizedMessage ?: "Download failed",
                            stage = DownloadStage.FAILED
                        )
                        _downloadEvents.emit(DownloadEvent.Failed(error.localizedMessage ?: "Unknown error"))
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        notificationManager.cancel(NOTIFICATION_ID)
                        showFailureNotification(config.title, error.localizedMessage ?: "Download failed")
                        stopSelf()
                    }
                }'''
content = content.replace(old_failure, new_failure)

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'w') as f:
    f.write(content)
