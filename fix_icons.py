import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace('androidx.compose.material.icons.Icons.Default.CheckCircle', 'Icons.Default.Check')
content = content.replace('androidx.compose.material.icons.Icons.Default.Close', 'Icons.Default.Close')

if "import androidx.compose.material.icons.filled.Close" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Check", "import androidx.compose.material.icons.filled.Check\nimport androidx.compose.material.icons.filled.Close")

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
