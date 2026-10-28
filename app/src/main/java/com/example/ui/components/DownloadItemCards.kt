package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.model.DownloadHistoryItem
import com.example.model.DownloadStage
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimaryPurple
import java.io.File

@Composable
fun ActiveDownloadCard(
    title: String,
    thumbnailUrl: String?,
    qualityTag: String,
    progressPercent: Int,
    downloadedMB: Double = 0.0,
    totalMB: Double = 0.0,
    progressText: String?,
    speedText: String,
    etaText: String = "--:--",
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
                        text = when {
                            speedText == "Waiting for Wi-Fi" -> "Waiting for Wi-Fi"
                            isPaused -> "Paused • Tap play to resume"
                            else -> "$speedText • $stageText"
                        },
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
                    Spacer(modifier = Modifier.width(16.dp))
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
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentPink,
                    trackColor = colors.surfaceVariant
                )
            } else {
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
                    speedText == "Waiting for Wi-Fi" -> "Waiting for Wi-Fi"
                    isPaused -> "Paused • Tap play to resume"
                    progressPercent <= 0 -> "Preparing download..."
                    stageText.equals("Merging", ignoreCase = true) -> "Merging video & audio..."
                    stageText.equals("Converting", ignoreCase = true) -> "Converting audio..."
                    stageText.equals("Finishing", ignoreCase = true) -> "Finishing file..."
                    else -> "$speedText • ETA $etaText"
                }

                Text(
                    text = cleanStatus,
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )

                val rightProgressLabel = if (totalMB > 0.0) {
                    String.format(java.util.Locale.US, "%.1f MB / %.1f MB • %d%%", downloadedMB, totalMB, progressPercent)
                } else if (progressPercent > 0) {
                    "$progressPercent%"
                } else {
                    "Preparing..."
                }

                Text(
                    text = rightProgressLabel,
                    color = colors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompletedDownloadCard(
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
) {
    val colors = AppTheme.colors
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(if (isSelected) 2.dp else 0.dp, if (isSelected) PrimaryPurple else Color.Transparent, RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = { if (isSelectionMode) onSelectToggle() else onPlay() },
                onLongClick = {
                    onSelectToggle()
                }
            ),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) PrimaryPurple.copy(alpha = 0.1f) else colors.surface),
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
                val completedStatsText = buildList {
                    add(if (item.isAudioOnly) "Audio" else "Video")
                    add(item.fileSizeFormatted)
                    if (item.elapsedTimeFormatted.isNotBlank()) {
                        add(item.elapsedTimeFormatted)
                    }
                    if (item.averageSpeedFormatted.isNotBlank()) {
                        add(item.averageSpeedFormatted)
                    }
                }.joinToString(" • ")
                Text(
                    text = completedStatsText,
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
                        text = { Text("File Details", color = colors.textPrimary) },
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
            }
        }
    }
}

@Composable
fun EmptyDownloadsView(message: String = "No downloads yet") {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp),
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

fun DownloadStage.toPlainLabel(): String = when (this) {
    DownloadStage.DOWNLOADING -> "Downloading"
    DownloadStage.INITIALIZING -> "Connecting"
    DownloadStage.MERGING_FORMATS -> "Merging"
    DownloadStage.EXTRACTING_AUDIO -> "Converting"
    DownloadStage.EMBEDDING_METADATA -> "Finishing"
    DownloadStage.QUEUED -> "Queued"
    DownloadStage.PAUSED -> "Paused"
    DownloadStage.COMPLETED -> "Completed"
    DownloadStage.FAILED -> "Failed"
    DownloadStage.CANCELLED -> "Cancelled"
}

private fun resolveMediaUri(context: Context, filePath: String): Uri? {
    if (filePath.isBlank()) return null
    if (filePath.startsWith("content://")) {
        val uri = Uri.parse(filePath)
        val exists = try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
        return if (exists) uri else null
    }

    val file = File(filePath)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
        val resolver = context.contentResolver
        val collection = android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(android.provider.MediaStore.MediaColumns._ID)
        try {
            resolver.query(
                collection,
                projection,
                "${android.provider.MediaStore.MediaColumns.DATA} = ?",
                arrayOf(filePath),
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(0)
                    return android.content.ContentUris.withAppendedId(collection, id)
                }
            }
        } catch (_: Exception) {
        }
        if (file.name.isNotBlank()) {
            try {
                resolver.query(
                    collection,
                    projection,
                    "${android.provider.MediaStore.MediaColumns.DISPLAY_NAME} = ?",
                    arrayOf(file.name),
                    "${android.provider.MediaStore.MediaColumns.DATE_ADDED} DESC"
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val id = cursor.getLong(0)
                        return android.content.ContentUris.withAppendedId(collection, id)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }

    if (file.exists()) {
        return try {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (_: Exception) {
            Uri.fromFile(file)
        }
    }
    return null
}

fun openMediaFile(context: Context, filePath: String, isAudio: Boolean) {
    try {
        val uri = resolveMediaUri(context, filePath)
        if (uri == null) {
            Toast.makeText(context, "File no longer exists on storage", Toast.LENGTH_SHORT).show()
            return
        }
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

fun shareMediaFile(context: Context, filePath: String) {
    try {
        val uri = resolveMediaUri(context, filePath)
        if (uri == null) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }
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

fun shareMultipleMediaFiles(context: Context, filePaths: List<String>) {
    try {
        val uris = ArrayList<Uri>()
        for (path in filePaths) {
            resolveMediaUri(context, path)?.let { uris.add(it) }
        }
        if (uris.isEmpty()) {
            Toast.makeText(context, "File does not exist", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "*/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share media via"))
    } catch (e: Exception) {
        Toast.makeText(context, "Share error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
    }
}
