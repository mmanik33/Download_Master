import re

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

# In onFailure
old_fail = """                onFailure = { error ->
                    Log.e(TAG, "Download failed", error)
                    _downloadEvents.emit(DownloadEvent.Failed(jobState.processId, error.localizedMessage ?: "Unknown error"))
                    showFailureNotification(jobState.config.title, error.localizedMessage ?: "Download failed")
                    removeJobAndCheckStop(jobState.processId)
                }"""

new_fail = """                onFailure = { error ->
                    Log.e(TAG, "Download failed", error)
                    repository.cleanupIncompleteFiles()
                    _downloadEvents.emit(DownloadEvent.Failed(jobState.processId, error.localizedMessage ?: "Unknown error"))
                    showFailureNotification(jobState.config.title, error.localizedMessage ?: "Download failed")
                    removeJobAndCheckStop(jobState.processId)
                }"""

content = content.replace(old_fail, new_fail)

# In handleCancellation
old_cancel = """    private fun handleCancellation(processId: String) {
        val jobState = activeJobs[processId] ?: return
        Log.i(TAG, "Cancelling active download process: $processId")
        repository.cancelDownload(jobState.processId)
        jobState.job?.cancel()
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Cancelled(processId)) }
        removeJobAndCheckStop(processId)
    }"""

new_cancel = """    private fun handleCancellation(processId: String) {
        val jobState = activeJobs[processId] ?: return
        Log.i(TAG, "Cancelling active download process: $processId")
        repository.cancelDownload(jobState.processId)
        jobState.job?.cancel()
        repository.cleanupIncompleteFiles()
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Cancelled(processId)) }
        removeJobAndCheckStop(processId)
    }"""

content = content.replace(old_cancel, new_cancel)

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    yt_content = f.read()

cleanup_func = """
    fun cleanupIncompleteFiles() {
        try {
            val downloadDir = getDownloadDirectory()
            if (downloadDir.exists() && downloadDir.isDirectory) {
                val incompleteFiles = downloadDir.listFiles { file ->
                    val name = file.name.lowercase()
                    name.endsWith(".part") || name.endsWith(".ytdl") || name.startsWith("temp_dl_")
                }
                incompleteFiles?.forEach { file ->
                    try {
                        file.delete()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete incomplete file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cleanup incomplete files error", e)
        }
    }
}"""

# Insert before the last closing brace
yt_content = yt_content.rsplit('}', 1)[0] + cleanup_func

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(yt_content)
