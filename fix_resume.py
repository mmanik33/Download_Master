import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Add reset method
reset_code = '''
    fun cancelDownload(processId: String): Boolean {
'''
new_reset_code = '''
    fun resetProcessState(processId: String) {
        cancelledProcessIds.remove(processId)
        pausedProcessIds.remove(processId)
    }

    fun cancelDownload(processId: String): Boolean {
'''
content = content.replace(reset_code, new_reset_code)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

# Fix effectiveProcessId
content = re.sub(
    r'val effectiveProcessId = .*?\n',
    r'val effectiveProcessId = jobState.processId\n        repository.resetProcessState(effectiveProcessId)\n',
    content
)

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'w') as f:
    f.write(content)

