with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val isYouTube = config.url.contains("youtube.com", ignoreCase = true) || config.url.contains("youtu.be", ignoreCase = true)\n',
    'val isYouTube = config.url.contains("youtube.com", ignoreCase = true) || config.url.contains("youtu.be", ignoreCase = true)\n        val isInstagram = config.url.contains("instagram.com", ignoreCase = true)\n'
)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
