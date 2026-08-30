import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Remove verifyDownloadedMedia calls
content = re.sub(r'\s*verifyDownloadedMedia\(newFile\)', '', content)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
