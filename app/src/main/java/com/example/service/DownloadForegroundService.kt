package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.DownloadMasterApp
import com.example.data.YtDlpRepository
import com.example.model.AudioFormat
import com.example.model.DownloadConfig
import com.example.model.DownloadProgress
import com.example.model.DownloadStage
import com.example.model.VideoQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

class DownloadForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadJob: Job? = null
    private lateinit var repository: YtDlpRepository
    private lateinit var notificationManager: NotificationManager

    companion object {
        const val TAG = "DownloadService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_DOWNLOAD = "com.example.action.START_DOWNLOAD"
        const val ACTION_CANCEL_DOWNLOAD = "com.example.action.CANCEL_DOWNLOAD"
        const val ACTION_TOGGLE_PAUSE = "com.example.action.TOGGLE_PAUSE"
        const val ACTION_PAUSE_DOWNLOAD = "com.example.action.PAUSE_DOWNLOAD"
        const val ACTION_RESUME_DOWNLOAD = "com.example.action.RESUME_DOWNLOAD"

        const val EXTRA_URL = "extra_url"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_IS_AUDIO = "extra_is_audio"
        const val EXTRA_AUDIO_FORMAT = "extra_audio_format"
        const val EXTRA_VIDEO_QUALITY = "extra_video_quality"
        const val EXTRA_FORMAT_ID = "extra_format_id"
        const val EXTRA_EMBED_THUMBNAIL = "extra_embed_thumbnail"
        const val EXTRA_EMBED_METADATA = "extra_embed_metadata"
        const val EXTRA_USE_ARIA2C = "extra_use_aria2c"
        const val EXTRA_CUSTOM_ARGS = "extra_custom_args"

        // State flows to communicate with UI
        private val _activeDownload = MutableStateFlow<com.example.model.ActiveDownload?>(null)
        val activeDownload: StateFlow<com.example.model.ActiveDownload?> = _activeDownload.asStateFlow()
        private val _currentProgress = MutableStateFlow<DownloadProgress?>(null)
        val currentProgress: StateFlow<DownloadProgress?> = _currentProgress.asStateFlow()

        private val _isPaused = MutableStateFlow(false)
        val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

        private val _downloadEvents = MutableSharedFlow<DownloadEvent>(extraBufferCapacity = 10)
        val downloadEvents: SharedFlow<DownloadEvent> = _downloadEvents.asSharedFlow()

        private var activeProcessId: String = "dm_download_task"
        private var activeTitle: String = "Media Download"

        fun startDownload(context: Context, config: DownloadConfig) {
            _isPaused.value = false
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_URL, config.url)
                putExtra(EXTRA_TITLE, config.title)
                putExtra(EXTRA_IS_AUDIO, config.isAudioOnly)
                putExtra(EXTRA_AUDIO_FORMAT, config.audioFormat.name)
                putExtra(EXTRA_VIDEO_QUALITY, config.videoQuality.name)
                putExtra(EXTRA_FORMAT_ID, config.selectedFormatId)
                putExtra(EXTRA_EMBED_THUMBNAIL, config.embedThumbnail)
                putExtra(EXTRA_EMBED_METADATA, config.embedMetadata)
                putExtra(EXTRA_USE_ARIA2C, config.useAria2c)
                putExtra(EXTRA_CUSTOM_ARGS, config.customArguments)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun togglePauseResume(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_TOGGLE_PAUSE
            }
            context.startService(intent)
        }

        fun pauseDownload(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_PAUSE_DOWNLOAD
            }
            context.startService(intent)
        }

        fun resumeDownload(context: Context) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_RESUME_DOWNLOAD
            }
            context.startService(intent)
        }

        fun cancelDownload(context: Context) {
            _isPaused.value = false
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
            }
            context.startService(intent)
        }
    }

    sealed interface DownloadEvent {
        data class Started(val title: String) : DownloadEvent
        data class Progress(val progress: DownloadProgress) : DownloadEvent
        data class Completed(val file: File, val title: String) : DownloadEvent
        data class Failed(val error: String) : DownloadEvent
        data object Cancelled : DownloadEvent
    }

    override fun onCreate() {
        super.onCreate()
        repository = YtDlpRepository(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Media Download"
                activeTitle = title
                _isPaused.value = false
                val isAudio = intent.getBooleanExtra(EXTRA_IS_AUDIO, false)
                val audioFormatStr = intent.getStringExtra(EXTRA_AUDIO_FORMAT) ?: AudioFormat.MP3.name
                val videoQualityStr = intent.getStringExtra(EXTRA_VIDEO_QUALITY) ?: VideoQuality.BEST.name
                val formatId = intent.getStringExtra(EXTRA_FORMAT_ID)
                val embedThumbnail = intent.getBooleanExtra(EXTRA_EMBED_THUMBNAIL, true)
                val embedMetadata = intent.getBooleanExtra(EXTRA_EMBED_METADATA, true)
                val useAria2c = intent.getBooleanExtra(EXTRA_USE_ARIA2C, true)
                val customArgs = intent.getStringExtra(EXTRA_CUSTOM_ARGS) ?: ""

                val config = DownloadConfig(
                    url = url,
                    title = title,
                    isAudioOnly = isAudio,
                    audioFormat = try { AudioFormat.valueOf(audioFormatStr) } catch (e: Exception) { AudioFormat.MP3 },
                    videoQuality = try { VideoQuality.valueOf(videoQualityStr) } catch (e: Exception) { VideoQuality.BEST },
                    selectedFormatId = formatId,
                    embedThumbnail = embedThumbnail,
                    embedMetadata = embedMetadata,
                    useAria2c = useAria2c,
                    customArguments = customArgs
                )

                startForeground(NOTIFICATION_ID, buildInitialNotification(title))
                performDownload(config)
            }
            ACTION_TOGGLE_PAUSE -> {
                if (_isPaused.value) {
                    handleResume()
                } else {
                    handlePause()
                }
            }
            ACTION_PAUSE_DOWNLOAD -> {
                handlePause()
            }
            ACTION_RESUME_DOWNLOAD -> {
                handleResume()
            }
            ACTION_CANCEL_DOWNLOAD -> {
                handleCancellation()
            }
        }
        return START_NOT_STICKY
    }

    private fun handlePause() {
        Log.i(TAG, "Pausing active download process: $activeProcessId")
        _isPaused.value = true
        repository.setPaused(activeProcessId, true)
        val cur = _currentProgress.value
        val pausedProgress = (cur ?: DownloadProgress()).copy(
            speedText = "Paused",
            stage = DownloadStage.PAUSED,
            isPaused = true
        )
        _currentProgress.value = pausedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(pausedProgress)) }
        updateProgressNotification(activeTitle, pausedProgress.progressPercent, "Paused", pausedProgress.etaText, isPaused = true)
    }

    private fun handleResume() {
        Log.i(TAG, "Resuming active download process: $activeProcessId")
        _isPaused.value = false
        repository.setPaused(activeProcessId, false)
        val cur = _currentProgress.value
        val resumedProgress = (cur ?: DownloadProgress()).copy(
            speedText = "Resuming...",
            stage = DownloadStage.DOWNLOADING,
            isPaused = false
        )
        _currentProgress.value = resumedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(resumedProgress)) }
        updateProgressNotification(activeTitle, resumedProgress.progressPercent, "Resuming...", resumedProgress.etaText, isPaused = false)
    }

    private fun performDownload(config: DownloadConfig) {
        downloadJob?.cancel()
        activeProcessId = "dl_${System.currentTimeMillis()}"

        downloadJob = serviceScope.launch {
            _downloadEvents.emit(DownloadEvent.Started(config.title))
            _currentProgress.value = DownloadProgress(
                progressPercent = 0f,
                speedText = "Connecting...",
                etaText = "--:--",
                lineText = "Initializing yt-dlp binary...",
                stage = DownloadStage.INITIALIZING
            )

            var lastNotificationUpdateTime = 0L

            val result = repository.downloadMedia(config, activeProcessId) { progressPercent, etaSeconds, line ->
                val etaFormatted = if (etaSeconds > 0) {
                    val m = etaSeconds / 60
                    val s = etaSeconds % 60
                    String.format("%02d:%02d", m, s)
                } else {
                    "--:--"
                }

                val speedMatch = Regex("""\bat\s+([0-9.]+\s*[KMGT]?i?B/s)""").find(line)
                val speedStr = speedMatch?.groupValues?.get(1) ?: if (progressPercent > 0) "Downloading" else "Connecting..."

                val stage = when {
                    "ExtractAudio" in line || "ffmpeg" in line.lowercase() -> DownloadStage.EXTRACTING_AUDIO
                    "Merger" in line -> DownloadStage.MERGING_FORMATS
                    "Thumbnails" in line || "Metadata" in line -> DownloadStage.EMBEDDING_METADATA
                    progressPercent >= 0 -> DownloadStage.DOWNLOADING
                    else -> DownloadStage.INITIALIZING
                }

                val cleanLineText = when (stage) {
                    DownloadStage.INITIALIZING -> "Preparing download..."
                    DownloadStage.EXTRACTING_AUDIO -> "Extracting audio track..."
                    DownloadStage.MERGING_FORMATS -> "Merging video & audio..."
                    DownloadStage.EMBEDDING_METADATA -> "Finalizing media..."
                    DownloadStage.DOWNLOADING -> if (speedMatch != null) "Speed: ${speedMatch.groupValues[1]}" else "Downloading media..."
                    else -> "Downloading..."
                }

                val progressObj = DownloadProgress(
                    progressPercent = if (progressPercent >= 0) progressPercent else 0f,
                    speedText = speedStr,
                    etaText = etaFormatted,
                    lineText = cleanLineText,
                    stage = stage
                )

                _currentProgress.value = progressObj
                serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(progressObj)) }

                // Throttle notification updates to at most once every 600ms
                val now = System.currentTimeMillis()
                if (now - lastNotificationUpdateTime > 600) {
                    lastNotificationUpdateTime = now
                    updateProgressNotification(config.title, progressObj.progressPercent, speedStr, etaFormatted)
                }
            }

            result.fold(
                onSuccess = { file ->
                    Log.i(TAG, "Download completed successfully: ${file.absolutePath}")
                    _currentProgress.value = DownloadProgress(
                        progressPercent = 100f,
                        speedText = "Done",
                        etaText = "00:00",
                        lineText = "Saved to: ${file.name}",
                        stage = DownloadStage.COMPLETED
                    )
                    _downloadEvents.emit(DownloadEvent.Completed(file, config.title))
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    notificationManager.cancel(NOTIFICATION_ID)
                    showCompletionNotification(config.title, file)
                    stopSelf()
                },
                onFailure = { error ->
                    Log.e(TAG, "Download failed", error)
                    _currentProgress.value = DownloadProgress(
                        progressPercent = 0f,
                        speedText = "Error",
                        etaText = "--:--",
                        lineText = error.localizedMessage ?: "Download failed",
                        stage = DownloadStage.FAILED
                    )
                    _downloadEvents.emit(DownloadEvent.Failed(error.localizedMessage ?: "Unknown error"))
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    notificationManager.cancel(NOTIFICATION_ID)
                    showFailureNotification(config.title, error.localizedMessage ?: "Download failed")
                    stopSelf()
                }
            )
        }
    }

    private fun handleCancellation() {
        Log.i(TAG, "Cancelling active download process: $activeProcessId")
        repository.cancelDownload(activeProcessId)
        downloadJob?.cancel()
        _currentProgress.value = DownloadProgress(
            progressPercent = 0f,
            speedText = "Cancelled",
            etaText = "--:--",
            lineText = "Download cancelled by user",
            stage = DownloadStage.CANCELLED
        )
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Cancelled) }
        stopForeground(STOP_FOREGROUND_REMOVE)
        notificationManager.cancel(NOTIFICATION_ID)
        stopSelf()
    }

    private fun buildInitialNotification(title: String): Notification {
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            this,
            2,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_CANCEL_DOWNLOAD
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, DownloadMasterApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Downloading: $title")
            .setContentText("Initializing download engine...")
            .setSmallIcon(R.drawable.ic_notification_download)
            .setColor(0xFF8B5CF6.toInt())
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setProgress(100, 0, true)
            .setContentIntent(openPendingIntent)
            .addAction(android.R.drawable.ic_media_pause, "Pause", pausePendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
            .build()
    }

    private fun updateProgressNotification(
        title: String,
        progress: Float,
        speed: String,
        eta: String,
        isPaused: Boolean = false
    ) {
        val pauseIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_TOGGLE_PAUSE
        }
        val pausePendingIntent = PendingIntent.getService(
            this,
            2,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val cancelIntent = Intent(this, DownloadForegroundService::class.java).apply {
            action = ACTION_CANCEL_DOWNLOAD
        }
        val cancelPendingIntent = PendingIntent.getService(
            this,
            1,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val text = if (isPaused) "Paused • ${progress.toInt()}%" else "${progress.toInt()}% • $speed • ETA: $eta"

        val notification = NotificationCompat.Builder(this, DownloadMasterApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(R.drawable.ic_notification_download)
            .setColor(0xFF8B5CF6.toInt())
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setProgress(100, progress.toInt().coerceIn(0, 100), isPaused)
            .setContentIntent(openPendingIntent)
            .addAction(
                if (isPaused) android.R.drawable.ic_media_play else android.R.drawable.ic_media_pause,
                if (isPaused) "Resume" else "Pause",
                pausePendingIntent
            )
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancel", cancelPendingIntent)
            .setSilent(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(title: String, file: File) {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            2,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DownloadMasterApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Download Complete")
            .setContentText("$title (${formatFileSize(file.length())})")
            .setSmallIcon(R.drawable.ic_notification_download)
            .setColor(0xFF10B981.toInt())
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }

    private fun showFailureNotification(title: String, error: String) {
        val notification = NotificationCompat.Builder(this, DownloadMasterApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle("Download Failed: $title")
            .setContentText(error)
            .setSmallIcon(R.drawable.ic_notification_download)
            .setColor(0xFFEC4899.toInt())
            .setCategory(NotificationCompat.CATEGORY_ERROR)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(NOTIFICATION_ID + 2, notification)
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadJob?.cancel()
    }
}
