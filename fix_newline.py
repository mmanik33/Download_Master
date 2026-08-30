with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

content = content.replace('private var activeProcessId: String = "dm_download_task"\\n        var activeConfig: DownloadConfig? = null', 'private var activeProcessId: String = "dm_download_task"\n        var activeConfig: DownloadConfig? = null')

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'w') as f:
    f.write(content)
