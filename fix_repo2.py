import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Clean up the mess from the previous replace
content = content.replace("isAudioOnly = false,\n                                directUrl = hdPlayUrl\n                            )", "isAudioOnly = false\n                            )")
content = content.replace("isAudioOnly = true,\n                                directUrl = hdPlayUrl\n                            )", "isAudioOnly = true\n                            )")
content = content.replace("isAudioOnly = true,\n                                directUrl = musicUrl\n                            )", "isAudioOnly = true\n                            )")

# Now do it correctly with regex ONLY
content = re.sub(
    r'(formatId = "tik_hd",.*?isAudioOnly = false)(\n\s+)\)',
    r'\1,\n                                directUrl = hdPlayUrl\2)',
    content,
    flags=re.DOTALL
)

content = re.sub(
    r'(formatId = "tik_sd",.*?isAudioOnly = false)(\n\s+)\)',
    r'\1,\n                                directUrl = playUrl\2)',
    content,
    flags=re.DOTALL
)

content = re.sub(
    r'(formatId = "tik_audio",.*?isAudioOnly = true)(\n\s+)\)',
    r'\1,\n                                directUrl = musicUrl\2)',
    content,
    flags=re.DOTALL
)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
