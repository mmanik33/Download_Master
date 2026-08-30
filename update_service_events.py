import re

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

events_old = '''    sealed interface DownloadEvent {
        data class Started(val title: String) : DownloadEvent
        data class Progress(val progress: DownloadProgress) : DownloadEvent
        data class Completed(val file: File, val title: String) : DownloadEvent
        data class Failed(val error: String) : DownloadEvent
        data object Cancelled : DownloadEvent
    }'''

events_new = '''    sealed interface DownloadEvent {
        val id: String
        data class Started(override val id: String, val title: String, val config: DownloadConfig) : DownloadEvent
        data class Progress(override val id: String, val progress: DownloadProgress, val isPaused: Boolean) : DownloadEvent
        data class Completed(override val id: String, val file: File, val title: String) : DownloadEvent
        data class Failed(override val id: String, val error: String) : DownloadEvent
        data class Cancelled(override val id: String) : DownloadEvent
    }'''

content = content.replace(events_old, events_new)
with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'w') as f:
    f.write(content)
