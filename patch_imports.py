with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

content = "import androidx.compose.foundation.border\n" + content
content = "import androidx.compose.material.icons.filled.Info\n" + content
content = "import androidx.compose.material.icons.filled.Checklist\n" + content
content = "import androidx.compose.ui.window.Dialog\n" + content

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
