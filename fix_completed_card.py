import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

old_sig = '''@Composable
private fun CompletedDownloadCard(
    item: DownloadHistoryItem,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onDelete: () -> Unit,
    onReDownload: () -> Unit
) {'''

new_sig = '''@Composable
private fun CompletedDownloadCard(
    item: DownloadHistoryItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onDelete: () -> Unit,
    onInfo: () -> Unit = {},
    onReDownload: () -> Unit
) {'''

content = content.replace(old_sig, new_sig)

# Also fix the list item call site.
old_list_item = '''
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
                            onPlay = { openMediaFile(context, item.localFilePath, item.isAudioOnly) },
                            onShare = { shareMediaFile(context, item.localFilePath) },
                            onCopyLink = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", item.webpageUrl))
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

new_list_item = '''
                    item(key = item.id) {
                        CompletedDownloadCard(
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
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText("URL", item.webpageUrl))
                                Toast.makeText(context, "Link copied", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { itemToDelete = item },
                            onInfo = { fileInfoItem = item },
                            onReDownload = {
                                viewModel.startDownloadWithQuality(item.toMediaModel(), VideoQuality.BEST, item.isAudioOnly)
                            }
                        )
                    }
'''
content = content.replace(old_list_item.strip('\n'), new_list_item.strip('\n'))

# Make sure we didn't miss replacing HistoryItemCard in the actual code
content = content.replace('HistoryItemCard(', 'CompletedDownloadCard(')

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)

