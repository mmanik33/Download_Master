import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Add imports
if "import androidx.compose.material.icons.filled.Warning" not in content:
    content = content.replace("import androidx.compose.material.icons.Icons", "import androidx.compose.material.icons.Icons\\nimport androidx.compose.material.icons.filled.Warning")
if "import androidx.compose.material3.MaterialTheme" not in content:
    content = content.replace("import androidx.compose.material3.TextButton", "import androidx.compose.material3.TextButton\\nimport androidx.compose.material3.MaterialTheme")

# Replace any incorrect MaterialTheme with androidx.compose.material3.MaterialTheme
# Actually, if I just import MaterialTheme, it should be fine.
content = content.replace("androidx.compose.material.icons.Icons.Default.Warning", "Icons.Default.Warning")

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
