package com.example.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
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
import java.util.concurrent.atomic.AtomicBoolean

class DownloadForegroundService : Service() {

    companion object {
        private const val TAG = "DownloadForegroundService"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START_DOWNLOAD = "action_start_download"
        const val ACTION_TOGGLE_PAUSE = "action_toggle_pause"
        const val ACTION_CANCEL_DOWNLOAD = "action_cancel_download"
        const val ACTION_WIFI_SETTING_CHANGED = "action_wifi_setting_changed"

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
        const val EXTRA_OUTPUT_DIR = "extra_output_dir"
        const val EXTRA_PROCESS_ID = "extra_process_id"

        const val STATUS_WAITING_FOR_WIFI = "Waiting for Wi-Fi"
        private const val PREFS_NAME = "download_master_prefs"
        private const val KEY_USE_WIFI_ONLY = "key_use_wifi_only"

        private val hasActiveDownloads = AtomicBoolean(false)

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
                putExtra(EXTRA_OUTPUT_DIR, config.outputDir)
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

        fun onWifiOnlySettingChanged(context: Context) {
            if (!hasActiveDownloads.get()) return
            try {
                val intent = Intent(context, DownloadForegroundService::class.java).apply {
                    action = ACTION_WIFI_SETTING_CHANGED
                }
                context.startService(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Unable to notify service of Wi-Fi setting change: ${e.message}")
            }
        }
    }

    sealed interface DownloadEvent {
        val id: String
        data class Started(override val id: String, val title: String, val config: DownloadConfig) : DownloadEvent
        data class Progress(override val id: String, val progress: DownloadProgress, val isPaused: Boolean) : DownloadEvent
        data class Completed(
            override val id: String,
            val file: File,
            val title: String,
            val elapsedTimeFormatted: String = "",
            val averageSpeedFormatted: String = ""
        ) : DownloadEvent
        data class Failed(override val id: String, val error: String) : DownloadEvent
        data class Cancelled(override val id: String) : DownloadEvent
    }

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var repository: YtDlpRepository
    private lateinit var notificationManager: NotificationManager

    class ActiveJobState(
        val processId: String,
        val config: DownloadConfig,
        val startTimeMs: Long = System.currentTimeMillis(),
        var job: Job? = null,
        var isPaused: Boolean = false,
        var isWaitingForWifi: Boolean = false,
        var hasEmittedStarted: Boolean = false,
        var progress: DownloadProgress = DownloadProgress(stage = DownloadStage.QUEUED)
    )

    private val activeJobs = ConcurrentHashMap<String, ActiveJobState>()
    private val wifiCallbackLock = Any()
    private var wifiNetworkCallback: ConnectivityManager.NetworkCallback? = null

    override fun onCreate() {
        super.onCreate()
        repository = YtDlpRepository(applicationContext)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun isWifiOnlyEnabled(): Boolean {
        val prefs = applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_USE_WIFI_ONLY, false)
    }

    private fun isWifiConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun putJobInWaitingForWifi(jobState: ActiveJobState) {
        Log.i(TAG, "Wi-Fi only is ON and active network is not Wi-Fi; placing ${jobState.processId} in '$STATUS_WAITING_FOR_WIFI'")
        jobState.isPaused = true
        jobState.isWaitingForWifi = true
        repository.cancelDownload(jobState.processId)
        jobState.job?.cancel()

        val waitingProgress = jobState.progress.copy(
            speedText = STATUS_WAITING_FOR_WIFI,
            lineText = STATUS_WAITING_FOR_WIFI,
            stage = DownloadStage.PAUSED,
            isPaused = true
        )
        jobState.progress = waitingProgress

        serviceScope.launch {
            if (!jobState.hasEmittedStarted) {
                jobState.hasEmittedStarted = true
                _downloadEvents.emit(DownloadEvent.Started(jobState.processId, jobState.config.title, jobState.config))
            }
            _downloadEvents.emit(DownloadEvent.Progress(jobState.processId, waitingProgress, true))
        }

        updateForegroundState()
        syncWifiCallbackRegistration()
    }

    private fun syncWifiCallbackRegistration() {
        synchronized(wifiCallbackLock) {
            val hasWaitingJobs = activeJobs.values.any { it.isWaitingForWifi }
            val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return
            if (hasWaitingJobs && wifiNetworkCallback == null) {
                val callback = object : ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: Network) {
                        val caps = cm.getNetworkCapabilities(network)
                        val isWifi = (caps != null &&
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) || isWifiConnected()
                        if (isWifi) {
                            resumeAllWifiWaitingJobs()
                        }
                    }

                    override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                        if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) &&
                            networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        ) {
                            resumeAllWifiWaitingJobs()
                        }
                    }

                    override fun onLost(network: Network) {
                        if (isWifiOnlyEnabled() && !isWifiConnected()) {
                            activeJobs.values.filter { !it.isPaused }.forEach { runningJob ->
                                putJobInWaitingForWifi(runningJob)
                            }
                        }
                    }
                }
                val request = NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build()
                try {
                    cm.registerNetworkCallback(request, callback)
                    wifiNetworkCallback = callback
                    Log.i(TAG, "Registered Wi-Fi NetworkCallback while Wi-Fi-waiting download exists")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to register Wi-Fi NetworkCallback", e)
                }
            } else if (!hasWaitingJobs && wifiNetworkCallback != null) {
                try {
                    cm.unregisterNetworkCallback(wifiNetworkCallback!!)
                    Log.i(TAG, "Unregistered Wi-Fi NetworkCallback (no Wi-Fi-waiting downloads remain)")
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to unregister Wi-Fi NetworkCallback", e)
                } finally {
                    wifiNetworkCallback = null
                }
            }
        }
    }

    private fun resumeAllWifiWaitingJobs() {
        if (isWifiOnlyEnabled() && !isWifiConnected()) return
        val waitingJobs = activeJobs.values.filter { it.isWaitingForWifi }
        if (waitingJobs.isEmpty()) {
            syncWifiCallbackRegistration()
            return
        }
        Log.i(TAG, "Wi-Fi network available; automatically resuming ${waitingJobs.size} waiting download(s)")
        for (jobState in waitingJobs) {
            jobState.isWaitingForWifi = false
            jobState.isPaused = false
            val resumedProgress = jobState.progress.copy(
                speedText = "Resuming...",
                lineText = "Resuming...",
                stage = DownloadStage.DOWNLOADING,
                isPaused = false
            )
            jobState.progress = resumedProgress
            serviceScope.launch { _downloadEvents.emit(DownloadEvent.Progress(jobState.processId, resumedProgress, false)) }
            performDownload(jobState, isResume = true)
        }
        updateForegroundState()
        syncWifiCallbackRegistration()
    }

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
                val outputDir = intent.getStringExtra(EXTRA_OUTPUT_DIR) ?: ""

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
                    customArguments = customArgs,
                    outputDir = outputDir
                )
                
                val jobState = ActiveJobState(processId, config)
                activeJobs[processId] = jobState
                hasActiveDownloads.set(true)
                
                updateForegroundState()
                if (isWifiOnlyEnabled() && !isWifiConnected()) {
                    putJobInWaitingForWifi(jobState)
                } else {
                    performDownload(jobState)
                }
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
            ACTION_WIFI_SETTING_CHANGED -> {
                if (isWifiOnlyEnabled() && !isWifiConnected()) {
                    activeJobs.values.filter { !it.isPaused }.forEach { jobState ->
                        putJobInWaitingForWifi(jobState)
                    }
                } else if (!isWifiOnlyEnabled()) {
                    resumeAllWifiWaitingJobs()
                }
            }
        }
        return START_NOT_STICKY
    }

    private fun handlePause(jobState: ActiveJobState) {
        Log.i(TAG, "Pausing download process: ${jobState.processId}")
        jobState.isWaitingForWifi = false
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
        syncWifiCallbackRegistration()
    }

    private fun handleResume(jobState: ActiveJobState) {
        if (isWifiOnlyEnabled() && !isWifiConnected()) {
            Log.i(TAG, "Cannot resume ${jobState.processId}: Wi-Fi only is ON and active network is not Wi-Fi")
            putJobInWaitingForWifi(jobState)
            return
        }
        Log.i(TAG, "Resuming download process: ${jobState.processId}")
        jobState.isWaitingForWifi = false
        jobState.isPaused = false
        syncWifiCallbackRegistration()
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
        if (isWifiOnlyEnabled() && !isWifiConnected()) {
            putJobInWaitingForWifi(jobState)
            return
        }

        jobState.job?.cancel()
        
        // Re-generate ID on resume for yt-dlp process
        val effectiveProcessId = jobState.processId
        repository.resetProcessState(effectiveProcessId)

        jobState.job = serviceScope.launch {
            if (!isResume && !jobState.hasEmittedStarted) {
                jobState.hasEmittedStarted = true
                _downloadEvents.emit(DownloadEvent.Started(jobState.processId, jobState.config.title, jobState.config))
            }

            if (isWifiOnlyEnabled() && !isWifiConnected()) {
                putJobInWaitingForWifi(jobState)
                return@launch
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
                if (isWifiOnlyEnabled() && !isWifiConnected()) {
                    putJobInWaitingForWifi(jobState)
                    return@downloadMedia
                }
                
                val etaFormatted = if (etaSeconds > 0) {
                    val m = etaSeconds / 60
                    val s = etaSeconds % 60
                    String.format("%02d:%02d", m, s)
                } else {
                    "--:--"
                }

                val speedMatch = Regex("""\\bat\\s+([0-9.]+\\s*[KMGT]?i?B/s)""").find(line)
                val speedStr = speedMatch?.groupValues?.get(1) ?: if (progressPercent > 0) "Downloading" else "Connecting..."

                val parsedTotalMB = parseTotalSizeMBFromLine(line)
                val totalMB = if (parsedTotalMB != null && parsedTotalMB > 0.0) {
                    parsedTotalMB
                } else {
                    jobState.progress.totalMB
                }
                val downloadedMB = if (parsedTotalMB != null && parsedTotalMB > 0.0) {
                    (progressPercent.coerceIn(0f, 100f) / 100.0) * totalMB
                } else {
                    jobState.progress.downloadedMB
                }

                val stage = when {
                    "ExtractAudio" in line || "ffmpeg" in line.lowercase() -> DownloadStage.EXTRACTING_AUDIO
                    "Merger" in line -> DownloadStage.MERGING_FORMATS
                    "Thumbnails" in line || "Metadata" in line -> DownloadStage.EMBEDDING_METADATA
                    progressPercent >= 0 -> DownloadStage.DOWNLOADING
                    else -> DownloadStage.INITIALIZING
                }

                val cleanLineText = when (stage) {
                    DownloadStage.INITIALIZING -> "Preparing download..."
                    DownloadStage.EXTRACTING_AUDIO -> "Converting audio..."
                    DownloadStage.MERGING_FORMATS -> "Merging video & audio..."
                    DownloadStage.EMBEDDING_METADATA -> "Finishing file..."
                    else -> line
                }

                val p = DownloadProgress(
                    progressPercent = progressPercent,
                    downloadedMB = downloadedMB,
                    totalMB = totalMB,
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
                    val endTimeMs = System.currentTimeMillis()
                    val elapsedMs = (endTimeMs - jobState.startTimeMs).coerceAtLeast(1L)
                    val elapsedSeconds = (elapsedMs / 1000L).coerceAtLeast(1L)
                    val elapsedFormatted = formatElapsedTime(elapsedSeconds)
                    val avgBytesPerSec = file.length().toDouble() / (elapsedMs / 1000.0)
                    val averageSpeedFormatted = formatAverageSpeed(avgBytesPerSec)

                    Log.i(TAG, "Download completed successfully: ${file.absolutePath} ($elapsedFormatted, $averageSpeedFormatted)")
                    _downloadEvents.emit(
                        DownloadEvent.Completed(
                            id = jobState.processId,
                            file = file,
                            title = jobState.config.title,
                            elapsedTimeFormatted = elapsedFormatted,
                            averageSpeedFormatted = averageSpeedFormatted
                        )
                    )
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
        jobState.isWaitingForWifi = false
        repository.cancelDownload(jobState.processId)
        jobState.job?.cancel()
        repository.cleanupIncompleteFiles()
        serviceScope.launch { _downloadEvents.emit(DownloadEvent.Cancelled(processId)) }
        removeJobAndCheckStop(processId)
    }
    
    private fun removeJobAndCheckStop(processId: String) {
        activeJobs.remove(processId)
        hasActiveDownloads.set(activeJobs.isNotEmpty())
        syncWifiCallbackRegistration()
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
            val waitingCount = activeJobs.values.count { it.isWaitingForWifi }
            if (waitingCount > 0) {
                "$waitingCount waiting for Wi-Fi, ${activeJobs.values.count { !it.isPaused }} active"
            } else {
                "${activeJobs.values.count { !it.isPaused }} active, ${activeJobs.values.count { it.isPaused }} paused"
            }
        } else {
            when {
                firstJob.isWaitingForWifi || firstJob.progress.speedText == STATUS_WAITING_FOR_WIFI -> STATUS_WAITING_FOR_WIFI
                firstJob.isPaused -> "Paused"
                else -> "${firstJob.progress.progressPercent.toInt()}% • ${firstJob.progress.speedText}"
            }
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

    private fun parseTotalSizeMBFromLine(line: String): Double? {
        val match = Regex("""\bof\s+~?\s*([0-9]+(?:\.[0-9]+)?)\s*(GiB|GB|MiB|MB|KiB|KB|B)\b""", RegexOption.IGNORE_CASE).find(line)
            ?: return null
        val value = match.groupValues[1].toDoubleOrNull() ?: return null
        if (value <= 0.0) return null
        return when (match.groupValues[2].uppercase()) {
            "GIB", "GB" -> value * 1024.0
            "MIB", "MB" -> value
            "KIB", "KB" -> value / 1024.0
            "B" -> value / (1024.0 * 1024.0)
            else -> null
        }
    }

    private fun formatElapsedTime(elapsedSeconds: Long): String {
        val totalSecs = elapsedSeconds.coerceAtLeast(1L)
        val hours = totalSecs / 3600L
        val minutes = (totalSecs % 3600L) / 60L
        val seconds = totalSecs % 60L
        return when {
            hours > 0L -> "${hours}h ${minutes}m ${seconds}s"
            minutes > 0L -> "${minutes}m ${seconds}s"
            else -> "${seconds}s"
        }
    }

    private fun formatAverageSpeed(bytesPerSec: Double): String {
        if (bytesPerSec <= 0.0 || bytesPerSec.isNaN() || bytesPerSec.isInfinite()) return ""
        val mbPerSec = bytesPerSec / (1024.0 * 1024.0)
        val kbPerSec = bytesPerSec / 1024.0
        return when {
            mbPerSec >= 1.0 -> String.format(java.util.Locale.US, "%.1f MB/s", mbPerSec)
            kbPerSec >= 1.0 -> String.format(java.util.Locale.US, "%.1f KB/s", kbPerSec)
            else -> String.format(java.util.Locale.US, "%.0f B/s", bytesPerSec)
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val units = arrayOf("B", "KB", "MB", "GB")
        val digitGroups = (Math.log10(bytes.toDouble()) / Math.log10(1024.0)).toInt()
        return String.format("%.2f %s", bytes / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
    }

    override fun onDestroy() {
        super.onDestroy()
        activeJobs.values.forEach {
            it.isWaitingForWifi = false
            it.job?.cancel()
        }
        activeJobs.clear()
        hasActiveDownloads.set(false)
        syncWifiCallbackRegistration()
    }
}

