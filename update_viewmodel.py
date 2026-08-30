import re

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

# Add Cancelled to addHistoryItem and replace the signature
old_addHistoryItem = '''    private fun addHistoryItem(media: MediaModel, file: File, audioOnly: Boolean) {
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
    }'''

new_addHistoryItem = '''    private fun addHistoryItem(
        media: MediaModel,
        file: File?,
        config: DownloadConfig,
        status: String = "Completed"
    ) {
        val newItem = DownloadHistoryItem(
            id = System.currentTimeMillis().toString(),
            title = media.title,
            webpageUrl = media.webpageUrl,
            localFilePath = file?.absolutePath ?: "",
            fileSizeFormatted = file?.let { formatFileSize(it.length()) } ?: "0 B",
            isAudioOnly = config.isAudioOnly,
            thumbnailUrl = media.thumbnailUrl,
            status = status,
            resolution = if (config.isAudioOnly) config.audioFormat.name else config.videoQuality.label,
            wifiOnly = config.useWifiOnly,
            isResumable = true
        )
        // If there's an existing item for this file (and file is not null), replace it. 
        // For cancelled items, we might not have a file, just append.
        val updated = if (file != null) {
            listOf(newItem) + _downloadHistory.value.filter { it.localFilePath != file.absolutePath }
        } else {
            listOf(newItem) + _downloadHistory.value
        }
        _downloadHistory.value = updated.take(50)
        saveHistory()
    }'''

content = content.replace(old_addHistoryItem, new_addHistoryItem)

# Update saveHistory
old_saveHistory = '''                    put("timestamp", item.timestamp)
                    put("thumbnailUrl", item.thumbnailUrl ?: "")
                }
                jsonArray.put(obj)'''

new_saveHistory = '''                    put("timestamp", item.timestamp)
                    put("thumbnailUrl", item.thumbnailUrl ?: "")
                    put("status", item.status)
                    put("resolution", item.resolution)
                    put("wifiOnly", item.wifiOnly)
                    put("isResumable", item.isResumable)
                }
                jsonArray.put(obj)'''

content = content.replace(old_saveHistory, new_saveHistory)

# Update loadHistory
old_loadHistory = '''                        thumbnailUrl = obj.optString("thumbnailUrl").takeIf { it.isNotBlank() }
                    )
                )
            }'''

new_loadHistory = '''                        thumbnailUrl = obj.optString("thumbnailUrl").takeIf { it.isNotBlank() },
                        status = obj.optString("status", "Completed"),
                        resolution = obj.optString("resolution", ""),
                        wifiOnly = obj.optBoolean("wifiOnly", false),
                        isResumable = obj.optBoolean("isResumable", true)
                    )
                )
            }'''

content = content.replace(old_loadHistory, new_loadHistory)

# In observeServiceEvents: Completed and Cancelled calls to addHistoryItem
# Completed: addHistoryItem(current.mediaInfo, event.file, current.config.isAudioOnly) -> addHistoryItem(current.mediaInfo, event.file, current.config, "Completed")
old_completed_add = '''addHistoryItem(current.mediaInfo, event.file, current.config.isAudioOnly)'''
new_completed_add = '''addHistoryItem(current.mediaInfo, event.file, current.config, "Completed")'''
content = content.replace(old_completed_add, new_completed_add)

# Cancelled: 
# Currently it just does:
# is DownloadForegroundService.DownloadEvent.Cancelled -> {
#     activeDownloads.value = activeDownloads.value - event.id
#     if (activeDownloads.value.isEmpty()) { ... }
# }
# Need to inject addHistoryItem there.
# Let's replace the block for Cancelled.
old_cancelled_block = '''                    is DownloadForegroundService.DownloadEvent.Cancelled -> {
                        activeDownloads.value = activeDownloads.value - event.id
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Idle
                        }
                    }'''
new_cancelled_block = '''                    is DownloadForegroundService.DownloadEvent.Cancelled -> {
                        val current = activeDownloads.value[event.id]
                        if (current != null) {
                            addHistoryItem(current.mediaInfo, null, current.config, "Cancelled")
                        }
                        activeDownloads.value = activeDownloads.value - event.id
                        if (activeDownloads.value.isEmpty()) {
                            _uiState.value = DownloadUiState.Idle
                        }
                    }'''
content = content.replace(old_cancelled_block, new_cancelled_block)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)
