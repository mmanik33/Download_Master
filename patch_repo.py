import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# 1. Isolate temporary files using paths argument
content = content.replace(
    'request.addOption("-o", outputTemplate)',
    'request.addOption("-o", outputTemplate)\n                request.addOption("--paths", f"temp:{downloadDir.absolutePath}/temp_{processId}")'
)

# 2. Add HLS and robust downloading options
content = content.replace(
    'request.addOption("--fragment-retries", "10")',
    'request.addOption("--fragment-retries", "10")\n                request.addOption("--concurrent-fragments", "4")\n                request.addOption("--hls-prefer-native")'
)

# 3. Disable aria2c for social media sites (Facebook, TikTok, Twitter, Instagram) because aria2c handles DASH/m3u8 poorly
content = content.replace(
    'if (config.useAria2c && !isYouTube) {',
    'val isSocialMedia = isFacebook || config.url.contains("instagram", true) || config.url.contains("tiktok", true) || config.url.contains("twitter", true) || config.url.contains("x.com", true)\n                if (config.useAria2c && !isYouTube && !isSocialMedia) {'
)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
