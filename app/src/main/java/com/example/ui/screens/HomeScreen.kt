package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DownloadHistoryItem
import com.example.ui.MainViewModel
import com.example.ui.components.ActiveDownloadCard
import com.example.ui.components.CompletedDownloadCard
import com.example.ui.components.DayNightToggleSwitch
import com.example.ui.components.EmptyDownloadsView
import com.example.ui.components.openMediaFile
import com.example.ui.components.shareMediaFile
import com.example.ui.components.shareMultipleMediaFiles
import com.example.ui.components.toPlainLabel
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimaryPurple

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    urlInput: String,
    onUrlChanged: (String) -> Unit,
    onDownloadClicked: () -> Unit,
    onMenuClicked: () -> Unit,
    historyItems: List<DownloadHistoryItem>,
    onReDownload: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val activeDownloads by viewModel.activeDownloads.collectAsStateWithLifecycle()

    var showCancelDownloadDialog by remember { mutableStateOf(false) }
    var cancelProcessId by remember { mutableStateOf<String?>(null) }
    var itemToDelete by remember { mutableStateOf<DownloadHistoryItem?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    // Selection mode state
    var isSelectionMode by remember { mutableStateOf(false) }
    var selectedItems by remember { mutableStateOf(setOf<String>()) }
    var showDeleteSelectedDialog by remember { mutableStateOf(false) }

    // File info state
    var fileInfoItem by remember { mutableStateOf<DownloadHistoryItem?>(null) }

    // Subtle breathing animation for download button when URL is entered
    val infiniteTransition = rememberInfiniteTransition(label = "download_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (urlInput.isNotBlank()) 1.025f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Active Download Cancel Confirmation Dialog
    if (showCancelDownloadDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDownloadDialog = false },
            containerColor = colors.surface,
            title = {
                Text(text = "Cancel Download?", fontWeight = FontWeight.Bold, color = colors.textPrimary)
            },
            text = {
                Text(text = "Are you sure you want to cancel this download?", color = colors.textSecondary)
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

    // File Info Dialog
    if (fileInfoItem != null) {
        AlertDialog(
            onDismissRequest = { fileInfoItem = null },
            containerColor = colors.surface,
            title = { Text("File Details", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Title: ${fileInfoItem?.title}", color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                    Text("Status: ${fileInfoItem?.status}", color = colors.textSecondary)
                    Text("Format: ${if (fileInfoItem?.isAudioOnly == true) "Audio" else "Video"} (${fileInfoItem?.resolution})", color = colors.textSecondary)
                    Text("Wi-Fi Only: ${if (fileInfoItem?.wifiOnly == true) "Yes" else "No"}", color = colors.textSecondary)
                    Text("Can Resume: ${if (fileInfoItem?.isResumable == true) "Yes" else "No"}", color = colors.textSecondary)
                    Text("Size: ${fileInfoItem?.fileSizeFormatted}", color = colors.textSecondary)
                    Text("Saved at: ${fileInfoItem?.localFilePath}", color = colors.textSecondary)
                }
            },
            confirmButton = {
                TextButton(onClick = { fileInfoItem = null }) { Text("Close") }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Header Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onMenuClicked,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = colors.textPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Download ",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.textPrimary
                )
                Text(
                    text = "Master",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = colors.secondary
                )
            }

            DayNightToggleSwitch(
                isDark = colors.isDark,
                onToggle = { isDark ->
                    viewModel.setThemeMode(if (isDark) "DARK" else "LIGHT")
                },
                switchWidth = 64.dp,
                switchHeight = 32.dp,
                showLabels = false
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        // URL Input Field with Link Icon & Paste Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Link,
                    contentDescription = null,
                    tint = colors.textSecondary,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = urlInput,
                    onValueChange = onUrlChanged,
                    placeholder = {
                        Text(
                            text = "Paste video link here...",
                            color = colors.textSecondary,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("url_input_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary
                    ),
                    singleLine = true
                )

                if (urlInput.isNotBlank()) {
                    IconButton(
                        onClick = { onUrlChanged("") },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Paste Button - Automatically hides keyboard on paste
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(colors.primaryGradient)
                        .clickable {
                            keyboardController?.hide()
                            focusManager.clearFocus()
                            val clipMgr = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = clipMgr.primaryClip
                            if (clipData != null && clipData.itemCount > 0) {
                                val text = clipData.getItemAt(0).text?.toString() ?: ""
                                if (text.isNotBlank()) {
                                    viewModel.pasteFromClipboard(text)
                                }
                            }
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.ContentPaste,
                            contentDescription = "Paste",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Paste",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Glowing Gradient Download Button - Automatically hides keyboard on click
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .scale(if (urlInput.isNotBlank()) pulseScale else 1f)
                .clip(RoundedCornerShape(27.dp))
                .background(colors.primaryGradient)
                .clickable {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                    onDownloadClicked()
                }
                .testTag("main_download_button"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Download",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Download",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Active Downloads Section (shown above Recent Downloads when a download is active)
        if (activeDownloads.isNotEmpty()) {
            Spacer(modifier = Modifier.height(28.dp))
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                activeDownloads.values.toList().forEach { downloadingState ->
                    val isWaitingForWifi = downloadingState.progress.speedText == "Waiting for Wi-Fi" ||
                        downloadingState.progress.lineText == "Waiting for Wi-Fi"
                    ActiveDownloadCard(
                        title = downloadingState.mediaInfo.title,
                        thumbnailUrl = downloadingState.mediaInfo.thumbnailUrl,
                        qualityTag = if (downloadingState.config.isAudioOnly) downloadingState.config.audioFormat.name else downloadingState.config.videoQuality.label,
                        progressPercent = downloadingState.progress.progressPercent.toInt(),
                        downloadedMB = downloadingState.progress.downloadedMB,
                        totalMB = downloadingState.progress.totalMB,
                        progressText = when {
                            isWaitingForWifi -> "Waiting for Wi-Fi"
                            downloadingState.isPaused -> "Paused • Tap play to resume"
                            else -> downloadingState.progress.lineText.ifBlank { "${downloadingState.progress.progressPercent.toInt()}%" }
                        },
                        speedText = when {
                            isWaitingForWifi -> "Waiting for Wi-Fi"
                            downloadingState.isPaused -> "Paused"
                            else -> downloadingState.progress.speedText
                        },
                        etaText = downloadingState.progress.etaText,
                        stageText = if (downloadingState.isPaused) "Paused" else downloadingState.progress.stage.toPlainLabel(),
                        isPaused = downloadingState.isPaused,
                        onPauseResume = { viewModel.togglePauseResumeDownload(downloadingState.processId) },
                        onCancel = {
                            cancelProcessId = downloadingState.processId
                            showCancelDownloadDialog = true
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Recent Downloads Section Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isSelectionMode) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        isSelectionMode = false
                        selectedItems = emptySet()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Selection",
                            tint = colors.textPrimary
                        )
                    }
                    Text(
                        text = "${selectedItems.size} Selected",
                        color = colors.textPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Row {
                    if (selectedItems.isNotEmpty()) {
                        IconButton(onClick = {
                            if (selectedItems.size == 1) {
                                val id = selectedItems.first()
                                val item = historyItems.find { it.id == id }
                                item?.let { shareMediaFile(context, it.localFilePath) }
                            } else {
                                val selectedFilePaths = historyItems
                                    .filter { selectedItems.contains(it.id) }
                                    .map { it.localFilePath }
                                shareMultipleMediaFiles(context, selectedFilePaths)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Selected",
                                tint = PrimaryPurple
                            )
                        }
                        IconButton(onClick = { showDeleteSelectedDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Selected",
                                tint = Color(0xFFEF4444)
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = "Recent Downloads",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                if (historyItems.isNotEmpty()) {
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

        Spacer(modifier = Modifier.height(12.dp))

        if (historyItems.isEmpty()) {
            EmptyDownloadsView(message = "No completed downloads yet. Paste a link to start downloading!")
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                historyItems.forEach { item ->
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
                            isSelectionMode = selectedItems.isNotEmpty()
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
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}
