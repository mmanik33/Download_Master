import re

with open('app/src/main/java/com/example/service/DownloadForegroundService.kt', 'r') as f:
    content = f.read()

# Replace variables with maps
vars_old = '''        private var activeProcessId: String = "dm_download_task"
        var activeConfig: DownloadConfig? = null
        private var activeTitle: String = "Media Download"'''

vars_new = '''        const val EXTRA_PROCESS_ID = "extra_process_id"
'''

content = content.replace(vars_old, vars_new)

# Since it's a huge change, let's just create a new Kotlin file for the service.
