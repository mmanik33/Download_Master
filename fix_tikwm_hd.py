import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

old_url = 'val apiUrl = "https://www.tikwm.com/api/?url=$encoded"'
new_url = 'val apiUrl = "https://www.tikwm.com/api/?url=$encoded&hd=1"'

content = content.replace(old_url, new_url)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
