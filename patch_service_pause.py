import re

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

# Instead of native pause which isn't working smoothly, let's toast and hide pause button if it's too complex, 
# BUT wait, maybe I can just send cancel and manage resume state?
# Actually, the user also mentioned that the Cancel (X) and Pause buttons are too close. Let me fix the spacing first.
