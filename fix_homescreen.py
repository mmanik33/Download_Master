import re

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove How To Download card
how_to_pattern = r'// "How to Download\?" Guide Banner Card[\s\S]*?Spacer\(modifier = Modifier\.height\(24\.dp\)\)'
content = re.sub(how_to_pattern, '', content)

# 2. Add 8K to Choose any quality
content = content.replace('"4K / 1080p / MP3"', '"8K / 4K / 1080p"')
content = content.replace('"Choose any quality"', '"Choose any quality & MP3"')

with open('app/src/main/java/com/example/ui/screens/HomeScreen.kt', 'w') as f:
    f.write(content)
