import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

old_item = '''                        CompletedDownloadCard(
                            item = item,
                            onPlay = { openMediaFile(context, item.localFilePath, item.isAudioOnly) },
                            onShare = { shareMediaFile(context, item.localFilePath) },
                            onCopyLink = {
                                val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipMgr.setPrimaryClip(android.content.ClipData.newPlainText("URL", item.webpageUrl))
                                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { itemToDelete = item },
                            onReDownload = { onReDownload(item.webpageUrl) }
                        )'''

new_item = '''                        CompletedDownloadCard(
                            item = item,
                            isSelected = selectedItems.contains(item.id),
                            isSelectionMode = isSelectionMode,
                            onSelectToggle = {
                                selectedItems = if (selectedItems.contains(item.id)) {
                                    selectedItems - item.id
                                } else {
                                    selectedItems + item.id
                                }
                                if (selectedItems.isEmpty()) {
                                    isSelectionMode = false
                                }
                            },
                            onPlay = { openMediaFile(context, item.localFilePath, item.isAudioOnly) },
                            onShare = { shareMediaFile(context, item.localFilePath) },
                            onCopyLink = {
                                val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipMgr.setPrimaryClip(android.content.ClipData.newPlainText("URL", item.webpageUrl))
                                Toast.makeText(context, "Link copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { itemToDelete = item },
                            onReDownload = { onReDownload(item.webpageUrl) },
                            onInfo = { fileInfoItem = item }
                        )'''

if old_item in content:
    content = content.replace(old_item, new_item)
    with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
        f.write(content)
    print("Success replacing list item mapping")
else:
    print("Could not find old_item mapping")
