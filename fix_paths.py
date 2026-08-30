import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Make -o relative and use --paths for the absolute path
content = content.replace(
    'val outputTemplate = "${downloadDir.absolutePath}/%(title).80B-%(id)s-[$qualityTag].%(ext)s"',
    'val outputTemplate = "%(title).80B-%(id)s-[$qualityTag].%(ext)s"'
)

content = content.replace(
    'request.addOption("--paths", "temp:${downloadDir.absolutePath}/temp_${processId}")',
    'request.addOption("--paths", downloadDir.absolutePath)\n                request.addOption("--paths", "temp:${downloadDir.absolutePath}/temp_${processId}")'
)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
