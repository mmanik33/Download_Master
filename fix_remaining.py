import re

# 1. Fix DownloadForegroundService.kt imports
with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

content = content.replace("import com.example.ui.MainActivity", "import com.example.MainActivity")

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'w') as f:
    f.write(content)


# 2. Fix MainViewModel.kt leftover activeDownload usages
with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()

content = re.sub(r'activeDownload\.value = null', '', content)
content = re.sub(r'activeDownloadsCount\.value = 0', '', content)

with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)


# 3. Fix DownloadsScreen.kt
with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# isDownloading check inside dialog
content = content.replace("activeDownload != null", "activeDownloads.isNotEmpty()")

# cancelDownload() no args (probably in clear history dialog?)
# If there is `viewModel.cancelDownload()` with no args, it should be changed.
# Let's just find and replace `viewModel.cancelDownload()` with a loop over active downloads.
content = content.replace(
    "viewModel.cancelDownload()", 
    "activeDownloads.keys.forEach { viewModel.cancelDownload(it) }"
)

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)

