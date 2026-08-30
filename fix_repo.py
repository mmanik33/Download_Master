import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Add cancellation check in the try-catch block of downloadMedia
old_catch = '''            } catch (e: Exception) {
                Log.w(TAG, "Download strategy $index failed for ${config.url}: ${e.message}")
                lastException = e
                val isBotError = e.message?.contains("Sign in to confirm you’re not a bot", ignoreCase = true) == true ||
                        e.message?.contains("bot", ignoreCase = true) == true
                if (!isBotError && index > 0) {
                    break
                }
            }'''
new_catch = '''            } catch (e: Exception) {
                Log.w(TAG, "Download strategy $index failed for ${config.url}: ${e.message}")
                lastException = e
                if (cancelledProcessIds[processId] == true) {
                    Log.i(TAG, "Process $processId was cancelled. Aborting strategies.")
                    break
                }
                val isBotError = e.message?.contains("Sign in to confirm you’re not a bot", ignoreCase = true) == true ||
                        e.message?.contains("bot", ignoreCase = true) == true
                if (!isBotError && index > 0) {
                    break
                }
            }'''
content = content.replace(old_catch, new_catch)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)

