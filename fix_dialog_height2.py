import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

old_card = """            Card(
                modifier = Modifier.fillMaxWidth().androidx.compose.foundation.layout.fillMaxHeight(0.85f),
                shape = RoundedCornerShape(28.dp),"""

new_card = """            Card(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
                shape = RoundedCornerShape(28.dp),"""

content = content.replace(old_card, new_card)

# Add import
import_str = "import androidx.compose.foundation.layout.fillMaxHeight"
if import_str not in content:
    content = content.replace("import androidx.compose.foundation.layout.fillMaxWidth", "import androidx.compose.foundation.layout.fillMaxWidth\\n" + import_str)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
