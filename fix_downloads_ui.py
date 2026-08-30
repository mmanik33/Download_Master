import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# Introduce cancelProcessId state
state_old = '''    var showCancelDownloadDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<DownloadHistoryItem?>(null) }'''
state_new = '''    var showCancelDownloadDialog by remember { mutableStateOf(false) }
    var cancelProcessId by remember { mutableStateOf<String?>(null) }
    var itemToDelete by remember { mutableStateOf<DownloadHistoryItem?>(null) }'''
content = content.replace(state_old, state_new)

# Update Cancel Confirm dialog
dialog_old = '''    // Active Download Cancel Confirmation Dialog
    if (showCancelDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDownloadDialog = false },
            containerColor = colors.surface,
            title = {
                Text(text = "Cancel Download?", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Text(text = "Are you sure you want to cancel this download? It will be moved to history as Cancelled.", color = colors.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.id?.let { viewModel.cancelDownload(it) } // using itemToDelete for holding active processId temporarily
                    showCancelDownloadDialog = false
                    itemToDelete = null
                }) {
                    Text("Yes, Cancel", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCancelDownloadDialog = false 
                    itemToDelete = null
                }) {
                    Text("No", color = colors.textSecondary)
                }
            }
        )
    }'''
dialog_new = '''    // Active Download Cancel Confirmation Dialog
    if (showCancelDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDownloadDialog = false },
            containerColor = colors.surface,
            title = {
                Text(text = "Cancel Download?", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Text(text = "Are you sure you want to cancel this download? It will be moved to history as Cancelled.", color = colors.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    cancelProcessId?.let { viewModel.cancelDownload(it) }
                    showCancelDownloadDialog = false
                    cancelProcessId = null
                }) {
                    Text("Yes, Cancel", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showCancelDownloadDialog = false 
                    cancelProcessId = null
                }) {
                    Text("No", color = colors.textSecondary)
                }
            }
        )
    }'''
content = content.replace(dialog_old, dialog_new)

# Update ActiveDownloadCard onCancel
oncancel_old = '''                        onCancel = { 
                            // Use dummy history item just to hold processId for the dialog
                            itemToDelete = DownloadHistoryItem(id = downloadingState.processId, title = "", webpageUrl = "", localFilePath = "", fileSizeFormatted = "", isAudioOnly = false)
                            showCancelDownloadDialog = true
                        }'''
oncancel_new = '''                        onCancel = { 
                            cancelProcessId = downloadingState.processId
                            showCancelDownloadDialog = true
                        }'''
content = content.replace(oncancel_old, oncancel_new)

# Update long press logic in CompletedDownloadCard
long_press_old = '''            .combinedClickable(
                onClick = { if (isSelectionMode) onSelectToggle() else onPlay() },
                onLongClick = { onSelectToggle() }
            ),'''
long_press_new = '''            .combinedClickable(
                onClick = { if (isSelectionMode) onSelectToggle() else onPlay() },
                onLongClick = { 
                    // When long clicked, always force selection mode and select this item
                    onSelectToggle() 
                }
            ),'''
content = content.replace(long_press_old, long_press_new)

# There is a problem: if isSelectionMode is false, and I long click, it calls onSelectToggle.
# But inside onSelectToggle, it does:
# selectedItems = selectedItems + item.id, and if selectedItems is not empty, isSelectionMode = true.
# Wait, I didn't set isSelectionMode = true in onSelectToggle! Let's fix that too.
select_toggle_old = '''                            onSelectToggle = {
                                selectedItems = if (selectedItems.contains(item.id)) {
                                    selectedItems - item.id
                                } else {
                                    selectedItems + item.id
                                }
                                if (selectedItems.isEmpty()) {
                                    isSelectionMode = false
                                }
                            },'''
select_toggle_new = '''                            onSelectToggle = {
                                selectedItems = if (selectedItems.contains(item.id)) {
                                    selectedItems - item.id
                                } else {
                                    selectedItems + item.id
                                }
                                if (selectedItems.isEmpty()) {
                                    isSelectionMode = false
                                } else {
                                    isSelectionMode = true
                                }
                            },'''
content = content.replace(select_toggle_old, select_toggle_new)

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)

