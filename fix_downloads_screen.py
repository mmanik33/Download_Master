import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# 1. Imports for combinedClickable
if "androidx.compose.foundation.combinedClickable" not in content:
    content = content.replace("import androidx.compose.foundation.background",
        "import androidx.compose.foundation.background\nimport androidx.compose.foundation.combinedClickable\nimport androidx.compose.foundation.ExperimentalFoundationApi")

# 2. Add Cancelled to tabs
old_tabs = '''    val tabs = listOf(
        "All (${downloadingCount + completedCount})",
        "Downloading ($downloadingCount)",
        "Completed ($completedCount)"
    )'''
new_tabs = '''    val completedOnlyCount = historyItems.count { it.status == "Completed" }
    val cancelledCount = historyItems.count { it.status == "Cancelled" }
    
    val tabs = listOf(
        "All (${downloadingCount + historyItems.size})",
        "Downloading ($downloadingCount)",
        "Completed ($completedOnlyCount)",
        "Cancelled ($cancelledCount)"
    )'''
content = content.replace(old_tabs, new_tabs)

# 3. Filter items for Completed / Cancelled
old_filter = '''    val filteredHistoryItems = remember(historyItems, searchQuery, mediaTypeFilter) {
        historyItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||'''
new_filter = '''    val filteredHistoryItems = remember(historyItems, searchQuery, mediaTypeFilter, selectedTab) {
        historyItems.filter { item ->
            val matchesTab = when (selectedTab) {
                0 -> true // All
                2 -> item.status == "Completed"
                3 -> item.status == "Cancelled"
                else -> false
            }
            val matchesSearch = searchQuery.isBlank() ||'''
content = content.replace(old_filter, new_filter)

# Add matchesTab to the filter boolean
old_matches_return = '''            matchesSearch && matchesType
        }'''
new_matches_return = '''            matchesTab && matchesSearch && matchesType
        }'''
content = content.replace(old_matches_return, new_matches_return)

# 4. Remove Select Mode icon
old_select_icon = '''                        IconButton(onClick = { isSelectionMode = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Checklist,
                                contentDescription = "Select Mode",
                                tint = colors.textSecondary
                            )
                        }'''
content = content.replace(old_select_icon, '')

# 5. Cancel Confirm dialog for Active Downloads
# Let's add the dialog and the logic. Wait, ActiveDownloadCard's cancel button calls `viewModel.cancelDownload(id)` directly.
# Let's inject a new dialog above `LazyColumn`.
cancel_confirm_dialog = '''    // Active Download Cancel Confirmation Dialog
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
    }

    // Delete Single Item Dialog'''
content = content.replace('    // Delete Single Item Dialog', cancel_confirm_dialog)

# Update ActiveDownloadCard call to use this dialog
old_oncancel = '''                        onCancel = { 
                            // Quick way to cancel without dialog for multi-downloads, or use dialog but track ID
                            viewModel.cancelDownload(downloadingState.processId) 
                        }'''
new_oncancel = '''                        onCancel = { 
                            // Use dummy history item just to hold processId for the dialog
                            itemToDelete = DownloadHistoryItem(id = downloadingState.processId, title = "", webpageUrl = "", localFilePath = "", fileSizeFormatted = "", isAudioOnly = false)
                            showCancelDownloadDialog = true
                        }'''
content = content.replace(old_oncancel, new_oncancel)

# 6. Update LazyColumn condition for Completed Cards
old_completed_cond = '''            if (selectedTab == 0 || selectedTab == 2) {
                if (filteredHistoryItems.isEmpty()) {
                    if (historyItems.isEmpty() && (!activeDownloads.isNotEmpty() || selectedTab == 2)) {'''
new_completed_cond = '''            if (selectedTab == 0 || selectedTab == 2 || selectedTab == 3) {
                if (filteredHistoryItems.isEmpty()) {
                    if (historyItems.isEmpty() && (!activeDownloads.isNotEmpty() || selectedTab == 2 || selectedTab == 3)) {'''
content = content.replace(old_completed_cond, new_completed_cond)

# 7. Add CombinedClickable
old_clickable = '''            .clickable {
                if (isSelectionMode) onSelectToggle() else onPlay()
            },'''
new_clickable = '''            .combinedClickable(
                onClick = { if (isSelectionMode) onSelectToggle() else onPlay() },
                onLongClick = { onSelectToggle() }
            ),'''
content = content.replace(old_clickable, new_clickable)

# 8. Add @OptIn to CompletedDownloadCard
old_card_fun = '''@Composable
private fun CompletedDownloadCard('''
new_card_fun = '''@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CompletedDownloadCard('''
content = content.replace(old_card_fun, new_card_fun)

# 9. File Info changes
old_fileinfo = '''                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Title: ${fileInfoItem?.title}", color = colors.textSecondary)
                    Text("Format: ${if (fileInfoItem?.isAudioOnly == true) "Audio" else "Video"}", color = colors.textSecondary)
                    Text("Size: ${fileInfoItem?.fileSizeFormatted}", color = colors.textSecondary)
                    Text("Saved at: ${fileInfoItem?.localFilePath}", color = colors.textSecondary)
                }'''
new_fileinfo = '''                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Title: ${fileInfoItem?.title}", color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Status: ${fileInfoItem?.status}", color = colors.textSecondary)
                    Text("Format: ${if (fileInfoItem?.isAudioOnly == true) "Audio" else "Video"} (${fileInfoItem?.resolution})", color = colors.textSecondary)
                    Text("WiFi Only: ${if (fileInfoItem?.wifiOnly == true) "Yes" else "No"}", color = colors.textSecondary)
                    Text("Resumable: ${if (fileInfoItem?.isResumable == true) "Yes" else "No"}", color = colors.textSecondary)
                    Text("Size: ${fileInfoItem?.fileSizeFormatted}", color = colors.textSecondary)
                    Text("Saved at: ${fileInfoItem?.localFilePath}", color = colors.textSecondary)
                }'''
content = content.replace(old_fileinfo, new_fileinfo)

# 10. Update CompletedDownloadCard layout to show Status for cancelled items
# Let's just add it below size
old_size = '''                        Text(
                            text = item.fileSizeFormatted,
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )'''
new_size = '''                        Text(
                            text = item.fileSizeFormatted + if (item.status != "Completed") " • ${item.status}" else "",
                            color = if (item.status == "Cancelled") Color(0xFFEF4444) else colors.textSecondary,
                            fontSize = 12.sp
                        )'''
content = content.replace(old_size, new_size)


with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
