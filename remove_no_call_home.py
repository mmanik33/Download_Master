with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

content = content.replace('request.addOption("--no-call-home")\n', '')
content = content.replace('request.addOption("--no-call-home")', '')

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
