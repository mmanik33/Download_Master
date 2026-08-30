import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('item.filePath', 'item.localFilePath')
content = content.replace('item.url', 'item.webpageUrl')
content = content.replace('viewModel.deleteHistoryItem', 'viewModel.removeHistoryItem')

# Add missing import for Icons.Default.Info and Icons.Default.Checklist
if 'import androidx.compose.material.icons.filled.Info' not in content:
    content = content.replace('import androidx.compose.material.icons.filled.*', 'import androidx.compose.material.icons.filled.*\nimport androidx.compose.material.icons.filled.Info\nimport androidx.compose.material.icons.filled.Checklist')

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
