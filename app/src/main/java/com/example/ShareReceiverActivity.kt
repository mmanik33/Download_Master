package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.MainViewModel
import com.example.ui.components.VideoQualitySelectionSheet
import com.example.ui.state.DownloadUiState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MyApplicationTheme

class ShareReceiverActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val extractedUrl = extractSharedUrl(intent)
        if (extractedUrl == null) {
            Toast.makeText(this, "No valid video link found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        if (savedInstanceState == null) {
            viewModel.parseMediaUrl(extractedUrl)
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val colorPalette by viewModel.colorPalette.collectAsState()
            val uiState by viewModel.uiState.collectAsState()
            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark, paletteId = colorPalette) {
                val colors = AppTheme.colors
                val qualitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                when (val state = uiState) {
                    is DownloadUiState.Idle,
                    is DownloadUiState.Parsing -> {
                        Dialog(onDismissRequest = { finish() }) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.surface)
                                    .padding(24.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = colors.primary,
                                    modifier = Modifier.size(40.dp)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Finding download options...",
                                    color = colors.textPrimary,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Loading available video qualities",
                                    color = colors.textSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    is DownloadUiState.Error -> {
                        AlertDialog(
                            onDismissRequest = { finish() },
                            containerColor = colors.surface,
                            title = {
                                Text(
                                    text = "Could not load video",
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            },
                            text = {
                                Text(
                                    text = state.message,
                                    color = colors.textSecondary
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { finish() }) {
                                    Text(
                                        text = "Close",
                                        color = colors.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        )
                    }

                    is DownloadUiState.Ready -> {
                        VideoQualitySelectionSheet(
                            media = state.mediaInfo,
                            sheetState = qualitySheetState,
                            onDismiss = {
                                viewModel.resetToIdle()
                                finish()
                            },
                            onDownloadSelected = { quality, isAudioOnly, audioFormat, formatId ->
                                val canStart = viewModel.activeDownloads.value.size < viewModel.maxConcurrentDownloads.value
                                viewModel.selectedAudioFormat.value = audioFormat
                                viewModel.startDownloadWithQuality(
                                    media = state.mediaInfo,
                                    quality = quality,
                                    audioOnly = isAudioOnly,
                                    formatId = formatId
                                )
                                if (canStart) {
                                    Toast.makeText(
                                        this@ShareReceiverActivity,
                                        "Download started",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                finish()
                            }
                        )
                    }

                    is DownloadUiState.Downloading,
                    is DownloadUiState.Success -> {
                        // Handled via finish() upon starting download
                    }
                }
            }
        }
    }

    private fun extractSharedUrl(intent: Intent?): String? {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") {
            return null
        }
        val sharedText = try {
            intent.getStringExtra(Intent.EXTRA_TEXT)
                ?: intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()
        } catch (_: Exception) {
            null
        }
        if (sharedText.isNullOrBlank()) return null
        val urlRegex = Regex("""https?://[^\s]+""")
        return urlRegex.find(sharedText)?.value
    }
}
