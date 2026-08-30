import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# Pass arguments to HistoryItemCard
old_list_item = '''
                    item(key = item.id) {
                        HistoryItemCard(
                            item = item,
                            onPlay = { openMediaFile(context, item.filePath, item.isAudioOnly) },
                            onShare = { shareMediaFile(context, item.filePath) },
                            onCopyLink = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", item.url))
                                Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                            },
                            onReDownload = {
                                viewModel.startDownloadWithQuality(item.toMediaModel(), VideoQuality.BEST, item.isAudioOnly)
                            },
                            onDelete = { itemToDelete = item }
                        )
                    }
'''
new_list_item = '''
                    item(key = item.id) {
                        HistoryItemCard(
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
                            onPlay = { openMediaFile(context, item.filePath, item.isAudioOnly) },
                            onShare = { shareMediaFile(context, item.filePath) },
                            onCopyLink = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", item.url))
                                Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                            },
                            onReDownload = {
                                viewModel.startDownloadWithQuality(item.toMediaModel(), VideoQuality.BEST, item.isAudioOnly)
                            },
                            onDelete = { itemToDelete = item },
                            onInfo = { fileInfoItem = item }
                        )
                    }
'''
content = content.replace(old_list_item.strip('\n'), new_list_item.strip('\n'))

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
