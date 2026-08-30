package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioFormat
import com.example.model.VideoQuality

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DownloadFormatSection(
    isAudioOnly: Boolean,
    onAudioOnlyToggle: (Boolean) -> Unit,
    selectedVideoQuality: VideoQuality,
    onVideoQualitySelected: (VideoQuality) -> Unit,
    selectedAudioFormat: AudioFormat,
    onAudioFormatSelected: (AudioFormat) -> Unit,
    embedThumbnail: Boolean,
    onEmbedThumbnailToggle: (Boolean) -> Unit,
    embedMetadata: Boolean,
    onEmbedMetadataToggle: (Boolean) -> Unit,
    useAria2c: Boolean,
    onUseAria2cToggle: (Boolean) -> Unit,
    customArgs: String,
    onCustomArgsChanged: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isAdvancedExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("download_format_section"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Mode Selector: Video vs Audio
            Text(
                text = "Format & Quality",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = !isAudioOnly,
                    onClick = { onAudioOnlyToggle(false) },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("video_mode_button")
                ) {
                    Text("Video (MP4/MKV)")
                }

                SegmentedButton(
                    selected = isAudioOnly,
                    onClick = { onAudioOnlyToggle(true) },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Audiotrack,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    modifier = Modifier.testTag("audio_mode_button")
                ) {
                    Text("Audio Only")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isAudioOnly) {
                // Video Resolution Chips
                Text(
                    text = "Target Resolution",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    VideoQuality.values().forEach { quality ->
                        val isSelected = selectedVideoQuality == quality
                        FilterChip(
                            selected = isSelected,
                            onClick = { onVideoQualitySelected(quality) },
                            label = { Text(quality.label, fontSize = 13.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("quality_chip_${quality.name}")
                        )
                    }
                }
            } else {
                // Audio Extraction Chips
                Text(
                    text = "Audio Codec / Format",
                    style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AudioFormat.values().forEach { format ->
                        val isSelected = selectedAudioFormat == format
                        FilterChip(
                            selected = isSelected,
                            onClick = { onAudioFormatSelected(format) },
                            label = { Text(format.label, fontSize = 13.sp) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier.testTag("audio_format_chip_${format.name}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Advanced Options Collapsible Accordion
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Advanced & Downloader Engine",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { isAdvancedExpanded = !isAdvancedExpanded },
                    modifier = Modifier.testTag("advanced_options_toggle")
                ) {
                    Icon(
                        imageVector = if (isAdvancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle advanced options",
                        tint = MaterialTheme.colorScheme.outline
                    )
                }
            }

            AnimatedVisibility(visible = isAdvancedExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Aria2c multi-thread downloader switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Aria2c Multi-Thread Engine", style = MaterialTheme.typography.bodyMedium)
                                Text("Accelerates downloads with multi-connections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Switch(
                            checked = useAria2c,
                            onCheckedChange = onUseAria2cToggle,
                            modifier = Modifier.testTag("aria2c_switch")
                        )
                    }

                    // Embed Thumbnail switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Embed Thumbnail", style = MaterialTheme.typography.bodyMedium)
                                Text("Attach album cover or thumbnail into file", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Switch(
                            checked = embedThumbnail,
                            onCheckedChange = onEmbedThumbnailToggle,
                            modifier = Modifier.testTag("embed_thumbnail_switch")
                        )
                    }

                    // Embed Metadata switch
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Embed Metadata Tags", style = MaterialTheme.typography.bodyMedium)
                                Text("Save artist, title, date into media container", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                        Switch(
                            checked = embedMetadata,
                            onCheckedChange = onEmbedMetadataToggle,
                            modifier = Modifier.testTag("embed_metadata_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Custom yt-dlp CLI arguments
                    OutlinedTextField(
                        value = customArgs,
                        onValueChange = onCustomArgsChanged,
                        label = { Text("Custom yt-dlp CLI Flags (optional)") },
                        placeholder = { Text("--cookies-from-browser or --limit-rate 5M") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("custom_cli_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }
    }
}
