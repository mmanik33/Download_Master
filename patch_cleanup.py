import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'val filesAfter = downloadDir.listFiles() ?: emptyArray()',
    'File(downloadDir, "temp_${processId}").deleteRecursively()\n\n                val filesAfter = downloadDir.listFiles() ?: emptyArray()'
)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
