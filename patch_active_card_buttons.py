import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("Spacer(modifier = Modifier.width(6.dp))\\n                    IconButton(", "Spacer(modifier = Modifier.width(16.dp))\\n                    IconButton(")

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
