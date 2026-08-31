import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# In downloadMedia, fetch from cache first
old_extract = """                Log.d(TAG, "Attempting high-speed direct download for TikTok: ${config.url}")
                val directMedia = extractTikTokFast(config.url)"""
new_extract = """                Log.d(TAG, "Attempting high-speed direct download for TikTok: ${config.url}")
                val cleanUrl = config.url.trim()
                val directMedia = metadataCache.get(cleanUrl) ?: extractTikTokFast(cleanUrl)"""
content = content.replace(old_extract, new_extract)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
