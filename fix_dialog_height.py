import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

old_card = """        androidx.compose.ui.window.Dialog(onDismissRequest = { showDeveloperInfoDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),"""

new_card = """        androidx.compose.ui.window.Dialog(onDismissRequest = { showDeveloperInfoDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().androidx.compose.foundation.layout.fillMaxHeight(0.85f),
                shape = RoundedCornerShape(28.dp),"""

content = content.replace(old_card, new_card)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
