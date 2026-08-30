import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Sanitize qualityTag fully
old_code = '''
                val qualityTag = if (config.isAudioOnly) {
                    config.audioFormat.name
                } else {
                    val fid = config.selectedFormatId ?: ""
                    if (fid.isNotEmpty()) {
                        val displayTag = if (fid.contains("_")) fid.substringAfter("_") else fid
                        displayTag.replace(" ", "_")
                    } else {
                        config.videoQuality.label.replace(" ", "_")
                    }
                }
'''

new_code = '''
                var rawQualityTag = if (config.isAudioOnly) {
                    config.audioFormat.name
                } else {
                    val fid = config.selectedFormatId ?: ""
                    if (fid.isNotEmpty()) {
                        if (fid.contains("_")) fid.substringAfter("_") else fid
                    } else {
                        config.videoQuality.label
                    }
                }
                // Strictly sanitize quality tag to prevent filesystem errors (e.g. if fid contains '/')
                val qualityTag = rawQualityTag.replace(Regex("[^a-zA-Z0-9.-]"), "_")
'''

content = content.replace(old_code.strip('\n'), new_code.strip('\n'))

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
