import re

# Fix MainViewModel
with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'r') as f:
    content = f.read()
content = content.replace("activeDownload.value = downloadingState", "")
content = content.replace("activeDownloadsCount.value = 1", "")
with open('app/src/main/java/com/example/ui/MainViewModel.kt', 'w') as f:
    f.write(content)

# Fix DownloadsScreen
with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# Delete the whole Confirmation Dialog block
pattern = r'// Confirmation Dialog for Active Download Cancellation[\s\S]*?\}\s*\}\s*\}'
content = re.sub(pattern, '', content)

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)

