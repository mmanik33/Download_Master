with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("isAudioOnly = true\n                            , directUrl = musicUrl\n                            )", "isAudioOnly = true,\n                                directUrl = musicUrl\n                            )")

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
