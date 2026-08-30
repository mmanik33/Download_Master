import re

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

old_start = '''    fun startDownloadWithQuality(
        media: MediaModel,
        quality: VideoQuality,
        audioOnly: Boolean,
        formatId: String? = null
    ) {
        isAudioOnly.value = audioOnly
        selectedVideoQuality.value = quality
        selectedFormatId.value = formatId

        val config = buildCurrentConfig(media.title, media.webpageUrl)'''

new_start = '''    fun startDownloadWithQuality(
        media: MediaModel,
        quality: VideoQuality,
        audioOnly: Boolean,
        formatId: String? = null
    ) {
        if (activeDownload.value != null && activeDownloadsCount.value > 0) {
            android.widget.Toast.makeText(getApplication(), "A download is already in progress. Please wait for it to finish.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        isAudioOnly.value = audioOnly
        selectedVideoQuality.value = quality
        selectedFormatId.value = formatId

        val config = buildCurrentConfig(media.title, media.webpageUrl)'''

content = content.replace(old_start, new_start)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)
