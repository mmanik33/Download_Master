import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

# Add new imports
if "import androidx.compose.ui.window.Dialog" not in content:
    content = content.replace("import androidx.compose.material3.*", "import androidx.compose.material3.*\nimport androidx.compose.ui.window.Dialog\nimport androidx.compose.foundation.border")

# Inject states for Multi-Select and Info Dialog
state_injections = '''
    var showClearAllDialog by remember { mutableStateOf(false) }

    // Selection mode state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    // File info state
    var fileInfoItem by remember { mutableStateOf<DownloadHistoryItem?>(null) }
'''
content = content.replace('var showClearAllDialog by remember { mutableStateOf(false) }', state_injections.strip('\n'))

# Add File Info Dialog
info_dialog_code = '''
    // File Info Dialog
    fileInfoItem?.let { item ->
        AlertDialog(
            onDismissRequest = { fileInfoItem = null },
            containerColor = colors.surface,
            title = {
                Text(text = "Media Information", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Title:", color = colors.textSecondary, fontSize = 12.sp)
                    Text(item.title, color = colors.textPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Quality / Format:", color = colors.textSecondary, fontSize = 12.sp)
                    Text(if (item.isAudioOnly) "Audio" else "Video", color = colors.textPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("File Size:", color = colors.textSecondary, fontSize = 12.sp)
                    Text(item.fileSizeFormatted, color = colors.textPrimary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("File Path:", color = colors.textSecondary, fontSize = 12.sp)
                    Text(item.filePath, color = colors.textPrimary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Downloaded On:", color = colors.textSecondary, fontSize = 12.sp)
                    Text(java.text.SimpleDateFormat("dd MMM yyyy, hh:mm a", java.util.Locale.getDefault()).format(java.util.Date(item.timestamp)), color = colors.textPrimary, fontSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { fileInfoItem = null }) {
                    Text("Close", color = PrimaryPurple)
                }
            }
        )
    }
'''
content = content.replace('if (itemToDelete != null) {', info_dialog_code.strip('\n') + '\n\n    if (itemToDelete != null) {')

# Delete selected dialog
delete_selected_dialog = '''
    if (showDeleteSelectedDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteSelectedDialog = false },
            containerColor = colors.surface,
            title = {
                Text(text = "Delete Selected?", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text(
                    text = "Are you sure you want to delete ${selectedItems.size} items? The files will be removed from storage.",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        selectedItems.forEach { id -> viewModel.deleteHistoryItem(id) }
                        selectedItems = emptySet()
                        isSelectionMode = false
                        showDeleteSelectedDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteSelectedDialog = false }) {
                    Text("Cancel", color = colors.textPrimary)
                }
            }
        )
    }
'''
content = content.replace('if (showClearAllDialog) {', delete_selected_dialog.strip('\n') + '\n\n    if (showClearAllDialog) {')


# Update History item signature to accept selection callbacks
old_history_item = '''
@Composable
private fun HistoryItemCard(
    item: DownloadHistoryItem,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onReDownload: () -> Unit,
    onDelete: () -> Unit
) {
'''
new_history_item = '''
@Composable
private fun HistoryItemCard(
    item: DownloadHistoryItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onReDownload: () -> Unit,
    onDelete: () -> Unit,
    onInfo: () -> Unit = {}
) {
'''
content = content.replace(old_history_item.strip('\n'), new_history_item.strip('\n'))

# Update the Card modifier to handle long press for selection
old_card_modifier = '''
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
    ) {
'''
new_card_modifier = '''
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) PrimaryPurple else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable {
                if (isSelectionMode) onSelectToggle() else onPlay()
            },
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
    ) {
'''
content = content.replace(old_card_modifier.strip('\n'), new_card_modifier.strip('\n'))


# Add "Info" to dropdown
old_dropdown = '''
                    DropdownMenuItem(
                        text = { Text("Delete File", color = Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
'''
new_dropdown = '''
                    DropdownMenuItem(
                        text = { Text("Media Info", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null, tint = colors.textPrimary) },
                        onClick = {
                            showMenu = false
                            onInfo()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete File", color = Color(0xFFEF4444)) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
'''
content = content.replace(old_dropdown.strip('\n'), new_dropdown.strip('\n'))

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
