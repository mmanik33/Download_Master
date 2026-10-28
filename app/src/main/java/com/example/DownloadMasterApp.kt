package com.example

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.File
import com.yausername.aria2c.Aria2c
import com.yausername.ffmpeg.FFmpeg
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DownloadMasterApp : Application() {

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        const val TAG = "DownloadMasterApp"
        const val DOWNLOAD_CHANNEL_ID = "download_master_channel"
        lateinit var instance: DownloadMasterApp
            private set

        private val _ytDlpVersion = MutableStateFlow<String>("Loading...")
        val ytDlpVersion: StateFlow<String> = _ytDlpVersion.asStateFlow()
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Safe Uncaught Exception Handler
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)
            defaultHandler?.uncaughtException(thread, throwable)
        }

        try {
            initWebViewEnvironment()
            createNotificationChannel()
            initYtDlpEngine()
        } catch (t: Throwable) {
            Log.e(TAG, "Exception during Application.onCreate", t)
        }
    }

    private fun initWebViewEnvironment() {
        try {
            val webViewJsCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/js")
            if (!webViewJsCache.exists()) webViewJsCache.mkdirs()
            val webViewWasmCache = File(cacheDir, "WebView/Default/HTTP Cache/Code Cache/wasm")
            if (!webViewWasmCache.exists()) webViewWasmCache.mkdirs()
        } catch (e: Exception) {
            Log.w(TAG, "WebView cache directory init: ${e.message}")
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.download_notification_channel_name)
            val descriptionText = getString(R.string.download_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_LOW
            val channel = NotificationChannel(DOWNLOAD_CHANNEL_ID, name, importance).apply {
                description = descriptionText
                setShowBadge(false)
                enableVibration(false)
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun initYtDlpEngine() {
        applicationScope.launch {
            try {
                Log.d(TAG, "Initializing YoutubeDL NDK engine...")
                YoutubeDL.getInstance().init(this@DownloadMasterApp)
                
                try {
                    Log.d(TAG, "Initializing embedded FFmpeg...")
                    FFmpeg.getInstance().init(this@DownloadMasterApp)
                } catch (e: Exception) {
                    Log.w(TAG, "FFmpeg initialization warning: ${e.message}")
                }

                try {
                    Log.d(TAG, "Initializing embedded Aria2c...")
                    Aria2c.getInstance().init(this@DownloadMasterApp)
                } catch (e: Exception) {
                    Log.w(TAG, "Aria2c initialization warning: ${e.message}")
                }

                val version = try {
                    YoutubeDL.getInstance().version(this@DownloadMasterApp) ?: "Built-in"
                } catch (e: Exception) {
                    "Built-in"
                }

                _ytDlpVersion.value = version
                Log.i(TAG, "YoutubeDL engine successfully initialized. Version: $version")

                // Automatically check & update engine in background upon startup
                launch {
                    try {
                        Log.d(TAG, "Checking for yt-dlp engine background update...")
                        val updateStatus = YoutubeDL.getInstance().updateYoutubeDL(this@DownloadMasterApp, YoutubeDL.UpdateChannel.STABLE)
                        val updatedVersion = YoutubeDL.getInstance().version(this@DownloadMasterApp) ?: version
                        _ytDlpVersion.value = updatedVersion
                        Log.i(TAG, "Engine auto-update check completed. Version: $updatedVersion (Status: $updateStatus)")
                    } catch (e: Exception) {
                        Log.w(TAG, "Background engine auto-update skipped or failed: ${e.message}")
                    }
                }
            } catch (e: YoutubeDLException) {
                Log.e(TAG, "Failed to initialize YoutubeDL engine", e)
            } catch (e: Throwable) {
                Log.e(TAG, "Unexpected error during YoutubeDL initialization", e)
            }
        }
    }

    fun updateEngine(onResult: (Boolean, String) -> Unit) {
        applicationScope.launch {
            try {
                YoutubeDL.getInstance().updateYoutubeDL(this@DownloadMasterApp, YoutubeDL.UpdateChannel.STABLE)
                val newVer = YoutubeDL.getInstance().version(this@DownloadMasterApp) ?: "Updated"
                _ytDlpVersion.value = newVer
                onResult(true, "Updated to version $newVer")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update yt-dlp", e)
                onResult(false, "Could not update: ${e.message}")
            }
        }
    }
}
