import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

old_options = '''                request.addOption("--skip-download")
                request.addOption("--no-warnings")
                request.addOption("--no-call-home")
                request.addOption("--socket-timeout", "7")
                request.addOption("--extractor-retries", "1")'''

new_options = '''                request.addOption("--skip-download")
                request.addOption("--no-warnings")
                request.addOption("--no-call-home")
                request.addOption("--socket-timeout", "7")
                request.addOption("--extractor-retries", "1")
                request.addOption("--flat-playlist")
                request.addOption("--compat-options", "no-youtube-unavailable-videos")'''

content = content.replace(old_options, new_options)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
