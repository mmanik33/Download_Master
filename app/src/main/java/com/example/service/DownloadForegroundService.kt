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
import com.example.DownloadMasterApp
import com.example.R
import com.example.data.YtDlpRepository
import com.example.model.AudioFormat
import com.example.model.DownloadConfig
import com.example.model.DownloadProgress
import com.example.model.DownloadStage
import com.example.model.VideoQuality
import com.example.MainActivity
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class DownloadForegroundService : Service() {

    companion object {
        private const val TAG = "DownloadForegroundService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_DOWNLOAD = "action_start_download"
        const val ACTION_TOGGLE_PAUSE = "action_toggle_pause"
        const val ACTION_CANCEL_DOWNLOAD = "action_cancel_download"
        const val ACTION_PAUSE_DOWNLOAD = "action_pause_download"
        const val ACTION_RESUME_DOWNLOAD = "action_resume_download"

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
        const val EXTRA_PROCESS_ID = "extra_process_id"

        private val _downloadEvents = MutableSharedFlow<DownloadEvent>(extraBufferCapacity = 50)
        val downloadEvents: SharedFlow<DownloadEvent> = _downloadEvents.asSharedFlow()

        fun startDownload(context: Context, config: DownloadConfig, processId: String = "dl_${System.currentTimeMillis()}") {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_START_DOWNLOAD
                putExtra(EXTRA_PROCESS_ID, processId)
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

        fun togglePauseResume(context: Context, processId: String) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_TOGGLE_PAUSE
                putExtra(EXTRA_PROCESS_ID, processId)
            }
            context.startService(intent)
        }

        fun cancelDownload(context: Context, processId: String) {
            val intent = Intent(context, DownloadForegroundService::class.java).apply {
                action = ACTION_CANCEL_DOWNLOAD
                putExtra(EXTRA_PROCESS_ID, processId)
            }
            context.startService(intent)
        }
    }

    sealed interface DownloadEvent {
        val id: String
        data class Started(override val id: String, val title: String, val config: DownloadConfig) : DownloadEvent
        data class Progress(override val id: String, val progress: DownloadProgress, val isPaused: Boolean) : DownloadEvent
        data class Completed(override val id: String, val file: File, val title: String) : DownloadEvent
        data class Failed(override val id: String, val error: String) : DownloadEvent
        data class Cancelled(override val id: String) : DownloadEvent
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: YtDlpRepository
    private lateinit var notificationManager: NotificationManager

    class ActiveJobState(
        val processId: String,
        val config: DownloadConfig,
        var job: Job? = null,
        var isPaused: Boolean = false,
        var progress: DownloadProgress = DownloadProgress(stage = DownloadStage.QUEUED)
    )

    private val activeJobs = ConcurrentHashMap<String, ActiveJobState>()

    override fun onCreate() {
        super.onCreate()
        repository = YtDlpRepository(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_DOWNLOAD -> {
                val processId = intent.getStringExtra(EXTRA_PROCESS_ID) ?: return START_NOT_STICKY
                val url = intent.getStringExtra(EXTRA_URL) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Media Download"
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
                
                val jobState = ActiveJobState(processId, config)
                activeJobs[processId] = jobState
                
                updateForegroundState()
                performDownload(jobState)
            }
            ACTION_TOGGLE_PAUSE -> {
                val processId = intent.getStringExtra(EXTRA_PROCESS_ID) ?: return START_NOT_STICKY
                val jobState = activeJobs[processId] ?: return START_NOT_STICKY
                if (jobState.isPaused) {
                    handleResume(jobState)
                } else {
                    handlePause(jobState)
                }
            }
            ACTION_CANCEL_DOWNLOAD -> {
                val processId = intent.getStringExtra(EXTRA_PROCESS_ID) ?: return START_NOT_STICKY
                handleCancellation(processId)
            }
        }
        return START_NOT_STICKY
    }

    private fun handlePause(jobState: ActiveJobState) {
        Log.i(TAG, "Pausing download process: ${jobState.processId}")
        jobState.isPaused = true
        repository.cancelDownload(jobState.processId) // Force interrupt yt-dlp to 'pause'
        val pausedProgress = jobState.progress.copy(
            speedText = "Paused",
            stage = DownloadStage.PAUSED,
            isPaused = true
        )
        jobState.progress = pausedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(jobState.processId, pausedProgress, true)) }
        updateForegroundState()
    }

    private fun handleResume(jobState: ActiveJobState) {
        Log.i(TAG, "Resuming download process: ${jobState.processId}")
        jobState.isPaused = false
        val resumedProgress = jobState.progress.copy(
            speedText = "Resuming...",
            stage = DownloadStage.DOWNLOADING,
            isPaused = false
        )
        jobState.progress = resumedProgress
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(jobState.processId, resumedProgress, false)) }
        updateForegroundState()
        performDownload(jobState, isResume = true)
    }

    private fun performDownload(jobState: ActiveJobState, isResume: Boolean = false) {
        jobState.job?.cancel()
        
        // Re-generate ID on resume for yt-dlp process
        val effectiveProcessId = jobState.processId
        repository.resetProcessState(effectiveProcessId)

        jobState.job = serviceScope.launch {
            if (!isResume) {
                _downloadEvents.emit(DownloadEvent.Started(jobState.processId, jobState.config.title, jobState.config))
            }
            
            jobState.progress = jobState.progress.copy(
                progressPercent = if (isResume) jobState.progress.progressPercent else 0f,
                speedText = "Connecting...",
                stage = DownloadStage.INITIALIZING,
                isPaused = false
            )
            
            var lastNotificationUpdateTime = 0L

            val result = repository.downloadMedia(jobState.config, effectiveProcessId) { progressPercent, etaSeconds, line ->
                if (jobState.isPaused) return@downloadMedia
                
                val etaFormatted = if (etaSeconds > 0) {
                    val m = etaSeconds / 60
                    val s = etaSeconds % 60
                    String.format("%02d:%02d", m, s)
                } else {
                    "--:--"
                }

                val speedMatch = Regex("""\\bat\\s+([0-9.]+\\s*[KMGT]?i?B/s)""").find(line)
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
                    else -> line
                }

                val p = DownloadProgress(
                    progressPercent = progressPercent,
                    speedText = speedStr,
                    etaText = etaFormatted,
                    lineText = cleanLineText,
                    stage = stage,
                    isPaused = false
                )
                
                jobState.progress = p
                serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(jobState.processId, p, false)) }

                val now = System.currentTimeMillis()
                if (now - lastNotificationUpdateTime > 600) {
                    lastNotificationUpdateTime = now
                    updateForegroundState()
                }
            }

            if (jobState.isPaused) return@launch

            result.fold(
                onSuccess = { file ->
                    Log.i(TAG, "Download completed successfully: ${file.absolutePath}")
                    _downloadEvents.emit(DownloadEvent.Completed(jobState.processId, file, jobState.config.title))
                    showCompletionNotification(jobState.config.title, file)
                    removeJobAndCheckStop(jobState.processId)
                },
                onFailure = { error ->
                    Log.e(TAG, "Download failed", error)
                    repository.cleanupIncompleteFiles()
                    _downloadEvents.emit(DownloadEvent.Failed(jobState.processId, error.localizedMessage ?: "Unknown error"))
                    showFailureNotification(jobState.config.title, error.localizedMessage ?: "Download failed")
                    removeJobAndCheckStop(jobState.processId)
                }
            )
        }
    }

    private fun handleCancellation(processId: String) {
        val jobState = activeJobs[processId] ?: return
        Log.i(TAG, "Cancelling active download process: $processId")
        repository.cancelDownload(jobState.processId)
        jobState.job?.cancel()
        repository.cleanupIncompleteFiles()
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Cancelled(processId)) }
        removeJobAndCheckStop(processId)
    }
    
    private fun removeJobAndCheckStop(processId: String) {
        activeJobs.remove(processId)
        updateForegroundState()
        if (activeJobs.isEmpty()) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }
    
    private fun updateForegroundState() {
        if (activeJobs.isEmpty()) return
        
        // Find aggregate progress or show summary
        val totalActive = activeJobs.size
        if (totalActive == 0) return
        
        val firstJob = activeJobs.values.firstOrNull() ?: return
        
        val title = if (totalActive > 1) "Downloading $totalActive files" else firstJob.config.title
        val progressText = if (totalActive > 1) {
            "${activeJobs.values.count { !it.isPaused }} active, ${activeJobs.values.count { it.isPaused }} paused"
        } else {
            if (firstJob.isPaused) "Paused" else "${firstJob.progress.progressPercent.toInt()}% • ${firstJob.progress.speedText}"
        }
        
        val progressVal = if (totalActive > 1) 0 else firstJob.progress.progressPercent.toInt()
        val isIndeterminate = totalActive > 1
        
        val openIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, DownloadMasterApp.DOWNLOAD_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(progressText)
            .setSmallIcon(R.drawable.ic_notification_download)
            .setColor(0xFF8B5CF6.toInt())
            .setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setProgress(100, progressVal, isIndeterminate)
            .setContentIntent(openPendingIntent)
            .setSilent(true)
            .build()
            
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun showCompletionNotification(title: String, file: File) {
        val openIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            (System.currentTimeMillis() % 10000).toInt(),
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
        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
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
        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    override fun onDestroy() {
        super.onDestroy()
        activeJobs.values.forEach { it.job?.cancel() }
        activeJobs.clear()
    }
}
