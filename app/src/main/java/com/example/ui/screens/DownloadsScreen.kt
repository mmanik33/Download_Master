package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.DownloadHistoryItem
import com.example.ui.MainViewModel
import com.example.ui.state.DownloadUiState
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimaryPurple
import java.io.File

@Composable
fun DownloadsScreen(
    viewModel: MainViewModel,
    uiState: DownloadUiState,
    historyItems: List<DownloadHistoryItem>,
    onReDownload: (String) -> Unit
) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Downloading, 2: Completed
    var mediaTypeFilter by remember { mutableIntStateOf(0) } // 0: All, 1: Videos, 2: Audio
    var searchQuery by remember { mutableStateOf("") }

    val isPaused by viewModel.isDownloadPaused.collectAsStateWithLifecycle()
    val isDownloading = uiState is DownloadUiState.Downloading
    val downloadingCount = if (isDownloading) 1 else 0
    val completedCount = historyItems.size

    var showCancelDownloadDialog by remember { mutableStateOf(false) }
    var itemToDelete by remember { mutableStateOf<DownloadHistoryItem?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    val tabs = listOf(
        "All (${downloadingCount + completedCount})",
        "Downloading ($downloadingCount)",
        "Completed ($completedCount)"
    )

    // Filtered Completed Items based on search and media type filter
    val filteredHistoryItems = remember(historyItems, searchQuery, mediaTypeFilter) {
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

    // Confirmation Dialog for Active Download Cancellation
    if (showCancelDownloadDialog) {
        val activeTitle = (uiState as? DownloadUiState.Downloading)?.mediaInfo?.title ?: "Download"
        AlertDialog(
            onDismissRequest = { showCancelDownloadDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Cancel Download?",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to cancel downloading \"$activeTitle\"? Any downloaded progress will be discarded.",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelDownload()
                        showCancelDownloadDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Yes, Cancel", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDownloadDialog = false }) {
                    Text("Keep Downloading", color = colors.primary, fontWeight = FontWeight.Medium)
                }
            }
        )
    }

    // Confirmation Dialog for Deleting Completed Download
    if (itemToDelete != null) {
        val item = itemToDelete!!
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Delete File?",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = "Delete \"${item.title}\" and remove the file from device storage?",
                    color = colors.textSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val f = File(item.localFilePath)
                            if (f.exists()) f.delete()
                        } catch (e: Exception) {
                            // ignore
                        }
                        viewModel.removeHistoryItem(item.id)
                        itemToDelete = null
                        Toast.makeText(context, "File deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    // Confirmation Dialog for Clearing All History
    if (showClearAllDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllDialog = false },
            containerColor = colors.surface,
            title = {
                Text("Clear All Completed?", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            text = {
                Text("Are you sure you want to clear all completed download records?", color = colors.textSecondary, fontSize = 14.sp)
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearHistory()
                        showClearAllDialog = false
                        Toast.makeText(context, "History cleared", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .padding(top = 12.dp)
    ) {
        // Top Header with Clear All button if completed items exist
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

        Spacer(modifier = Modifier.height(8.dp))

        // Main Navigation Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.surface,
            contentColor = PrimaryPurple,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = PrimaryPurple
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == index) colors.textPrimary else colors.textSecondary,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Search Bar & Filter Chips (shown on All or Completed tabs when records exist)
        if ((selectedTab == 0 || selectedTab == 2) && historyItems.isNotEmpty()) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                // Search Input Field
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search title or URL...", color = colors.textSecondary, fontSize = 13.sp) },
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedTextColor = colors.textPrimary,
                                unfocusedTextColor = colors.textPrimary
                            ),
                            singleLine = true
                        )
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Media type filter chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All (${historyItems.size})", "Videos", "Audio").forEachIndexed { idx, label ->
                        FilterChip(
                            selected = mediaTypeFilter == idx,
                            onClick = { mediaTypeFilter = idx },
                            label = { Text(label, fontSize = 12.sp, fontWeight = if (mediaTypeFilter == idx) FontWeight.Bold else FontWeight.Normal) },
                            leadingIcon = {
                                when (idx) {
                                    1 -> Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                                    2 -> Icon(Icons.Default.MusicNote, contentDescription = null, modifier = Modifier.size(14.dp))
                                    else -> null
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = colors.surface,
                                labelColor = colors.textSecondary,
                                iconColor = colors.textSecondary,
                                selectedContainerColor = PrimaryPurple,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Active Downloading Card (shown in All or Downloading tabs)
            if (isDownloading && (selectedTab == 0 || selectedTab == 1)) {
                val downloadingState = uiState as DownloadUiState.Downloading
                item(key = "active_download_item") {
                    ActiveDownloadCard(
                        title = downloadingState.mediaInfo.title,
                        thumbnailUrl = downloadingState.mediaInfo.thumbnailUrl,
                        qualityTag = if (downloadingState.config.isAudioOnly) downloadingState.config.audioFormat.name else downloadingState.config.videoQuality.label,
                        progressPercent = downloadingState.progress.progressPercent.toInt(),
                        progressText = if (isPaused) "Paused • Tap play to resume" else downloadingState.progress.lineText.ifBlank { "${downloadingState.progress.progressPercent.toInt()}%" },
                        speedText = if (isPaused) "Paused" else downloadingState.progress.speedText,
                        stageText = if (isPaused) "PAUSED" else downloadingState.progress.stage.name,
                        isPaused = isPaused,
                        onPauseResume = { viewModel.togglePauseResumeDownload() },
                        onCancel = { showCancelDownloadDialog = true }
                    )
                }
            }

            // 2. Downloading Tab Empty State
            if (selectedTab == 1 && !isDownloading) {
                item {
                    EmptyDownloadsView(message = "No active downloads currently in progress")
                }
            }

            // 3. Completed Download Cards (shown in All or Completed tabs)
            if (selectedTab == 0 || selectedTab == 2) {
                if (filteredHistoryItems.isEmpty()) {
                    if (historyItems.isEmpty() && (!isDownloading || selectedTab == 2)) {
                        item {
                            EmptyDownloadsView(message = "No completed downloads yet. Paste a link to start downloading!")
                        }
                    } else if (searchQuery.isNotBlank() || mediaTypeFilter != 0) {
                        item {
                            EmptyDownloadsView(message = "No matching items found for your filter")
                        }
                    }
                } else {
                    items(filteredHistoryItems, key = { it.id }) { item ->
                        CompletedDownloadCard(
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
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}

@Composable
private fun ActiveDownloadCard(
    title: String,
    thumbnailUrl: String?,
    qualityTag: String,
    progressPercent: Int,
    progressText: String?,
    speedText: String,
    stageText: String,
    isPaused: Boolean,
    onPauseResume: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = AppTheme.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("active_download_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(colors.surfaceVariant)
                ) {
                    if (!thumbnailUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(thumbnailUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    }

                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentPink)
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = qualityTag.take(6),
                            color = Color.White,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = colors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isPaused) "Paused • Tap play to resume" else "$speedText • $stageText",
                        color = if (isPaused) Color(0xFFF59E0B) else AccentCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPauseResume,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isPaused) PrimaryPurple else colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (isPaused) "Resume" else "Pause",
                            tint = if (isPaused) Color.White else colors.textPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    IconButton(
                        onClick = onCancel,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.surfaceVariant)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (progressPercent <= 0 && !isPaused) {
                // Smooth Indeterminate Progress Loading Animation while connecting or merging
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentPink,
                    trackColor = colors.surfaceVariant
                )
            } else {
                // Determinate Animated Progress
                val animatedProgress by animateFloatAsState(
                    targetValue = (progressPercent / 100f).coerceIn(0f, 1f),
                    animationSpec = tween(durationMillis = 350),
                    label = "download_progress"
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = if (isPaused) Color(0xFFF59E0B) else AccentPink,
                    trackColor = colors.surfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val cleanStatus = when {
                    isPaused -> "Paused • Tap play to resume"
                    progressPercent <= 0 -> "Preparing download engine..."
                    stageText.contains("MERG", ignoreCase = true) -> "Merging video & audio..."
                    stageText.contains("AUDIO", ignoreCase = true) -> "Converting audio track..."
                    stageText.contains("META", ignoreCase = true) -> "Finalizing media file..."
                    else -> speedText
                }

                Text(
                    text = cleanStatus,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Text(
                    text = if (progressPercent > 0) "$progressPercent%" else "Preparing...",
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun CompletedDownloadCard(
    item: DownloadHistoryItem,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onDelete: () -> Unit,
    onReDownload: () -> Unit
) {
    val colors = AppTheme.colors
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onPlay() },
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(colors.surfaceVariant)
            ) {
                if (!item.thumbnailUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(item.thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Thumbnail",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )
                }

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (item.isAudioOnly) Icons.Default.MusicNote else Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${if (item.isAudioOnly) "AUDIO" else "VIDEO"} • ${item.fileSizeFormatted}",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "More Options",
                        tint = colors.textSecondary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Play Media", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.PlayArrow, contentDescription = null, tint = PrimaryPurple) },
                        onClick = {
                            showMenu = false
                            onPlay()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Share File", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = colors.textPrimary) },
                        onClick = {
                            showMenu = false
                            onShare()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Copy Link", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null, tint = colors.textPrimary) },
                        onClick = {
                            showMenu = false
                            onCopyLink()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Re-download", color = colors.textPrimary) },
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null, tint = colors.textPrimary) },
                        onClick = {
                            showMenu = false
                            onReDownload()
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
            }
        }
    }
}

@Composable
private fun EmptyDownloadsView(message: String = "No downloads yet") {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 50.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Download,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = message,
            color = colors.textSecondary,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

private fun openMediaFile(context: Context, filePath: String, isAudio: Boolean) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File no longer exists on storage", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val mimeType = if (isAudio) "audio/*" else "video/*"
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Could not open media: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}

private fun shareMediaFile(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share media via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
