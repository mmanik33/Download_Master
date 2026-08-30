import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# We need to replace from `val filteredHistoryItems = ... }` all the way to `) {\n                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Share`

pattern = r'val filteredHistoryItems = remember[\s\S]*?\)\s*\{\s*Icon\(imageVector = androidx\.compose\.material\.icons\.Icons\.Default\.Share'

replacement = '''val filteredHistoryItems = remember(historyItems, searchQuery, mediaTypeFilter) {
        historyItems.filter { item ->
            val matchesSearch = searchQuery.isBlank() ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.webpageUrl.contains(searchQuery, ignoreCase = true)
            val matchesType = when (mediaTypeFilter) {
                1 -> !item.isAudioOnly
                2 -> item.isAudioOnly
                else -> true
            }
            matchesSearch && matchesType
        }
    }

    // Delete Single Item Dialog
    if (itemToDelete != null) {
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Delete Download?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove '${itemToDelete?.title}' from history? The downloaded file will NOT be deleted from your device storage.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    itemToDelete?.let { viewModel.removeHistoryItem(it.id) }
                    itemToDelete = null
                }) {
                    Text("Remove", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // Clear All History Dialog
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Clear All History?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear all your download history? Your downloaded files will remain on your device.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearAllDialog = false
                    selectedTab = 0
                }) {
                    Text("Clear All", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // Delete Selected Items Dialog
    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Delete Selected?",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to remove ${selectedItems.size} items from history? Files will not be deleted from storage.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedItems.forEach { viewModel.removeHistoryItem(it) }
                    selectedItems = emptySet()
                    isSelectionMode = false
                    showDeleteSelectedDialog = false
                }) {
                    Text("Remove", color = Color(0xFFEF4444))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // File Info Bottom Sheet / Dialog
    if (fileInfoItem != null) {
        AlertDialog(
            onDismissRequest = { fileInfoItem = null },
            containerColor = colors.surface,
            title = { Text("Media Information", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Title: ${fileInfoItem?.title}", color = colors.textSecondary)
                    Text("Format: ${if (fileInfoItem?.isAudioOnly == true) "Audio" else "Video"}", color = colors.textSecondary)
                    Text("Size: ${fileInfoItem?.fileSizeFormatted}", color = colors.textSecondary)
                    Text("Saved at: ${fileInfoItem?.localFilePath}", color = colors.textSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { fileInfoItem = null }) { Text("Close") }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        isSelectionMode = false
                        selectedItems = emptySet()
                    }) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Close Selection", tint = colors.textPrimary)
                    }
                    Text(text = "${selectedItems.size} Selected", color = colors.textPrimary, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                }
                Row {
                    if (selectedItems.size == 1) {
                        IconButton(onClick = {
                            val id = selectedItems.first()
                            val item = historyItems.find { it.id == id }
                            item?.let { shareMediaFile(context, it.localFilePath) }
                        }) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Share'''

content = re.sub(pattern, replacement, content)

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
