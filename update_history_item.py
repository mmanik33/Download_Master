with open('app/src/main/java/com/example/model/DownloadProgress.kt', 'r') as f:
    content = f.read()

old_item = '''data class DownloadHistoryItem(
    val id: String,
    val title: String,
    val webpageUrl: String,
    val localFilePath: String,
    val fileSizeFormatted: String,
    val isAudioOnly: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null
)'''

new_item = '''data class DownloadHistoryItem(
    val id: String,
    val title: String,
    val webpageUrl: String,
    val localFilePath: String,
    val fileSizeFormatted: String,
    val isAudioOnly: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val thumbnailUrl: String? = null,
    val status: String = "Completed",
    val resolution: String = "",
    val wifiOnly: Boolean = false,
    val isResumable: Boolean = false
)'''

content = content.replace(old_item, new_item)

with open('app/src/main/java/com/example/model/DownloadProgress.kt', 'w') as f:
    f.write(content)
