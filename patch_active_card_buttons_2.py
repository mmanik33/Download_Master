import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

content = re.sub(r'Spacer\(modifier = Modifier\.width\(6\.dp\)\)\s*IconButton\(\s*onClick = onCancel,', r'Spacer(modifier = Modifier.width(16.dp))\n                    IconButton(\n                        onClick = onCancel,', content)

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
