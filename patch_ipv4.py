import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

content = content.replace(
    'request.addOption("--geo-bypass")',
    'request.addOption("--geo-bypass")\n                request.addOption("--force-ipv4")'
)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
