import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    content = f.read()

old_header = '''
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Downloads & Library",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            if (historyItems.isNotEmpty() && selectedTab != 1) {
                IconButton(onClick = { showClearAllDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All History",
                        tint = Color(0xFFEF4444)
                    )
                }
            }
        }
'''

new_header = '''
        Row(
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
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Cancel", tint = colors.textPrimary)
                    }
                    Text(
                        text = "${selectedItems.size} Selected",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedItems.isNotEmpty()) {
                        IconButton(onClick = {
                            val itemsToShare = historyItems.filter { selectedItems.contains(it.id) }.map { it.filePath }
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
                                            putParcelableArrayListExtra(android.content.Intent.EXTRA_STREAM, ArrayList(uris))
                                            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                        }
                                        context.startActivity(android.content.Intent.createChooser(intent, "Share media via"))
                                    } else {
                                        Toast.makeText(context, "Files do not exist", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Share error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share Selected", tint = PrimaryPurple)
                        }
                        IconButton(onClick = { showDeleteSelectedDialog = true }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color(0xFFEF4444))
                        }
                    }
                }
            } else {
                Text(
                    text = "Downloads & Library",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                if (historyItems.isNotEmpty() && selectedTab != 1) {
                    Row {
                        IconButton(onClick = { isSelectionMode = true }) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = "Select Mode",
                                tint = colors.textSecondary
                            )
                        }
                        IconButton(onClick = { showClearAllDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = "Clear All History",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            }
        }
'''

content = content.replace(old_header.strip('\n'), new_header.strip('\n'))

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(content)
