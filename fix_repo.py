import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Fix HD format
content = content.replace('isAudioOnly = false\n                            )', 'isAudioOnly = false,\n                                directUrl = hdPlayUrl\n                            )')
# Fix SD format (wait, I need to make sure I don't replace both with hdPlayUrl)
# Let's use regex
content = re.sub(
    r'(formatId = "tik_hd",.*?isAudioOnly = false\n\s+)\)',
    r'\1, directUrl = hdPlayUrl\n                            )',
    content,
    flags=re.DOTALL
)

content = re.sub(
    r'(formatId = "tik_sd",.*?isAudioOnly = false\n\s+)\)',
    r'\1, directUrl = playUrl\n                            )',
    content,
    flags=re.DOTALL
)

content = re.sub(
    r'(formatId = "tik_audio",.*?isAudioOnly = true\n\s+)\)',
    r'\1, directUrl = musicUrl\n                            )',
    content,
    flags=re.DOTALL
)

# Fix downloadMedia URL selection
old_url_sel = """                val directUrl = if (config.isAudioOnly) {
                    directMedia?.availableFormats?.firstOrNull { it.isAudioOnly }?.formatId ?: directMedia?.directVideoUrl
                } else {
                    directMedia?.directVideoUrl
                }"""

new_url_sel = """                val directUrl = if (config.isAudioOnly) {
                    directMedia?.availableFormats?.firstOrNull { it.isAudioOnly }?.directUrl ?: directMedia?.directVideoUrl
                } else {
                    val fid = config.selectedFormatId
                    val selectedFormat = directMedia?.availableFormats?.firstOrNull { it.formatId == fid }
                    selectedFormat?.directUrl ?: directMedia?.directVideoUrl
                }"""
content = content.replace(old_url_sel, new_url_sel)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
