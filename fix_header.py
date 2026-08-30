import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

old_header_regex = r"Row\(\s*modifier = Modifier\s*\.fillMaxWidth\(\)\s*\.padding\(horizontal = 16\.dp, vertical = 4\.dp\),\s*horizontalArrangement = Arrangement\.SpaceBetween,\s*verticalAlignment = Alignment\.CenterVertically\s*\)\s*\{\s*Text\(\s*text = \"Downloads & Library\",\s*fontSize = 20\.sp,\s*fontWeight = FontWeight\.Bold,\s*color = colors\.textPrimary\s*\)\s*if \(historyItems\.isNotEmpty\(\) && selectedTab != 1\) \{\s*IconButton\(onClick = \{ showClearAllDialog = true \}\) \{\s*Icon\(\s*imageVector = Icons\.Default\.DeleteSweep,\s*contentDescription = \"Clear All History\",\s*tint = Color\(0xFFEF4444\)\s*\)\s*\}\s*\}\s*\}"

new_header = '''Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { 
                        isSelectionMode = false
                        selectedItems = emptySet()
                    }) {
                        Icon(imageVector = androidx.compose.material.icons.Icons.Default.Close, contentDescription = "Cancel", tint = colors.textPrimary)
                    }
                    Text(
                        text = "${selectedItems.size} Selected",
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedItems.isNotEmpty()) {
                        IconButton(onClick = {
                            val itemsToShare = historyItems.filter { selectedItems.contains(it.id) }.map { it.localFilePath }
                            if (itemsToShare.isNotEmpty()) {
                                try {
                                    val uris = itemsToShare.mapNotNull { filePath ->
                                        val file = java.io.File(filePath)
                                        if (file.exists()) {
                                            androidx.core.content.FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                                        } else null
                                    }
                                    if (uris.isNotEmpty()) {
                                        val intent = android.content.Intent(android.content.Intent.ACTION_SEND_MULTIPLE).apply {
                                            type = "*/*"
                                            putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, java.util.ArrayList(uris))
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share media via"))
                                    } else {
                                        android.widget.Toast.makeText(context, "Files do not exist", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Share error: ${e.localizedMessage}", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Share, contentDescription = "Share Selected", tint = PrimaryPurple)
                        }
                        IconButton(onClick = { showDeleteSelectedDialog = true }) {
                            Icon(imageVector = androidx.compose.material.icons.Icons.Default.Delete, contentDescription = "Delete Selected", tint = androidx.compose.ui.graphics.Color(0xFFEF4444))
                        }
                    }
                }
            } else {
                Text(
                    text = "Downloads & Library",
                    fontSize = 20.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = colors.textPrimary
                )

                if (historyItems.isNotEmpty() && selectedTab != 1) {
                    Row {
                        IconButton(onClick = { isSelectionMode = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.Checklist,
                                contentDescription = "Select Mode",
                                tint = colors.textSecondary
                            )
                        }
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(
                                imageVector = androidx.compose.material.icons.Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = androidx.compose.ui.graphics.Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }'''

if re.search(old_header_regex, content):
    content = re.sub(old_header_regex, new_header, content)
    with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
        f.write(content)
    print("Success")
else:
    print("Failed to find header")
