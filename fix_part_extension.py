import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

old_download = """                    var counter = 1
                    while (targetFile.exists()) {
                        targetFile = File(downloadDir, "${sanitizedTitle}_[${qualityTag}]_$counter.$extension")
                        counter++
                    }

                    val downloadResult = downloadDirectHttpStream(directUrl, targetFile, processId, onProgress)
                    if (downloadResult.isSuccess) {
                        val finalFile = downloadResult.getOrThrow()
                        scanMediaFile(finalFile)
                        return@withContext Result.success(finalFile)
                    }"""

new_download = """                    var counter = 1
                    while (targetFile.exists()) {
                        targetFile = File(downloadDir, "${sanitizedTitle}_[${qualityTag}]_$counter.$extension")
                        counter++
                    }
                    
                    val partFile = File(targetFile.absolutePath + ".part")

                    val downloadResult = downloadDirectHttpStream(directUrl, partFile, processId, onProgress)
                    if (downloadResult.isSuccess) {
                        val finalPartFile = downloadResult.getOrThrow()
                        if (finalPartFile.exists()) {
                            finalPartFile.renameTo(targetFile)
                        }
                        scanMediaFile(targetFile)
                        return@withContext Result.success(targetFile)
                    } else {
                        if (partFile.exists()) partFile.delete()
                    }"""

content = content.replace(old_download, new_download)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
