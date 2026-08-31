package com.example.data

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.util.Log
import android.util.LruCache
import com.example.model.AudioFormat
import com.example.model.DownloadConfig
import com.example.model.FormatModel
import com.example.model.MediaModel
import com.example.model.VideoQuality
import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest
import com.yausername.youtubedl_android.mapper.VideoInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class YtDlpRepository(private val context: Context) {

    companion object {
        private const val TAG = "YtDlpRepository"
        const val DOWNLOADS_SUBDIR = "DownloadMaster"
        private val metadataCache = LruCache<String, MediaModel>(50)
    }

    /**
     * Extracts full media metadata and available stream formats (adaptive DASH & muxed).
     * Uses optimized yt-dlp arguments with fast timeout handling to guarantee
     * all resolutions (144p to 4K+) and audio tracks are extracted quickly.
     */
    suspend fun extractMetadata(url: String): Result<MediaModel> = withContext(Dispatchers.IO) {
        val cleanUrl = url.trim()
        if (cleanUrl.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("URL cannot be empty"))
        }

        // Return from memory cache if available for instant response
        metadataCache.get(cleanUrl)?.let { cached ->
            Log.d(TAG, "Returning cached metadata for: $cleanUrl")
            return@withContext Result.success(cached)
        }

        val isTikTok = isTikTokUrl(cleanUrl)
        val isYouTube = cleanUrl.contains("youtube.com", ignoreCase = true) || cleanUrl.contains("youtu.be", ignoreCase = true)
        val isFacebook = cleanUrl.contains("facebook.com", ignoreCase = true) || cleanUrl.contains("fb.watch", ignoreCase = true) || cleanUrl.contains("fb.com", ignoreCase = true)
        val isInstagram = cleanUrl.contains("instagram.com", ignoreCase = true)

        // 1. Instant TikTok Parser via direct high-speed endpoint (sub-second response)
        if (isTikTok) {
            val tikTokModel = extractTikTokFast(cleanUrl)
            if (tikTokModel != null) {
                metadataCache.put(cleanUrl, tikTokModel)
                return@withContext Result.success(tikTokModel)
            }
        }

        ensureEngineReady()
        val cookiesFile = getCookiesFile()

        // List of candidate extraction configurations to try
        val strategies: List<(YoutubeDLRequest) -> Unit> = when {
            isTikTok -> listOf(
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    req.addOption("--add-header", "Referer:https://www.tiktok.com/")
                }
            )
            isYouTube -> listOf(
                // 1. Default yt-dlp client (gets 4K + bypasses SABR natively)
                { _ -> /* default yt-dlp */ },
                // 2. Fallback to mobile clients
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android,ios,web")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=ios")
                }
            )
            isFacebook -> listOf(
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/125.0.0.0 Safari/537.36")
                }
            )
            isInstagram -> listOf(
                { _ -> /* default yt-dlp */ },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1")
                }
            )
            else -> listOf({ _ -> /* standard */ })
        }

        var lastException: Exception? = null

        for ((index, strategy) in strategies.withIndex()) {
            try {
                Log.d(TAG, "Fetching media metadata (strategy #$index) for: $cleanUrl")
                val request = YoutubeDLRequest(cleanUrl)
                request.addOption("--no-playlist")
                request.addOption("--no-check-certificates")
                request.addOption("--geo-bypass")
                request.addOption("--force-ipv4")
                request.addOption("--skip-download")
                request.addOption("--no-warnings")
                                request.addOption("--socket-timeout", "7")
                request.addOption("--extractor-retries", "1")
                request.addOption("--flat-playlist")
                request.addOption("--compat-options", "no-youtube-unavailable-videos")

                if (cookiesFile != null && cookiesFile.exists() && cookiesFile.length() > 0) {
                    request.addOption("--cookies", cookiesFile.absolutePath)
                }

                strategy(request)

                val videoInfo: VideoInfo = YoutubeDL.getInstance().getInfo(request)

                val formats = videoInfo.formats?.mapNotNull { f ->
                    val fId = f.formatId ?: return@mapNotNull null
                    val ext = f.ext?.lowercase()?.trim() ?: ""
                    val rawNote = f.formatNote
                    val fNote = rawNote?.lowercase()?.trim() ?: ""
                    val vcodec = f.vcodec?.lowercase()?.trim() ?: "none"
                    val acodec = f.acodec?.lowercase()?.trim() ?: "none"

                    // 1. REJECT storyboard, mhtml, thumbnails, and preview sprite sheets
                    if (ext == "mhtml" || ext == "jpg" || ext == "jpeg" || ext == "png" || ext == "webp" ||
                        fId.startsWith("sb") || fNote.contains("storyboard") || fNote.contains("mhtml")
                    ) {
                        return@mapNotNull null
                    }

                    // 2. REJECT formats where both video and audio codecs are none or missing
                    val hasVideo = vcodec != "none" && vcodec.isNotBlank() && vcodec != "null"
                    val hasAudio = acodec != "none" && acodec.isNotBlank() && acodec != "null"

                    if (!hasVideo && !hasAudio) {
                        return@mapNotNull null
                    }

                    // Parse dimensions accurately from width/height or resolution string like "1920x1080"
                    var parsedHeight = f.height
                    var parsedWidth = f.width

                    if (hasVideo && parsedHeight <= 0 && !rawNote.isNullOrBlank()) {
                        val noteMatch = Regex("""(\d{3,4})p""").find(rawNote)
                        if (noteMatch != null) {
                            parsedHeight = noteMatch.groupValues[1].toIntOrNull() ?: 0
                        }
                    }

                    // Ignore invalid micro-dimensions from broken thumbnail/sprite streams
                    if (hasVideo && parsedHeight > 0 && parsedHeight < 140) {
                        return@mapNotNull null
                    }

                    val isAudioOnly = !hasVideo && hasAudio
                    val isVideo = hasVideo

                    val resString = if (isVideo && parsedHeight > 0) {
                        "${parsedHeight}p"
                    } else if (isAudioOnly) {
                        "Audio"
                    } else {
                        rawNote ?: "Video"
                    }

                    FormatModel(
                        formatId = fId,
                        ext = f.ext ?: if (isAudioOnly) "m4a" else "mp4",
                        resolution = resString,
                        width = parsedWidth,
                        height = parsedHeight,
                        fps = f.fps.toDouble(),
                        vcodec = f.vcodec,
                        acodec = f.acodec,
                        fileSize = f.fileSize,
                        formatNote = rawNote,
                        isVideo = isVideo,
                        isAudioOnly = isAudioOnly
                    )
                } ?: emptyList()

                Log.i(TAG, "Metadata extraction success: title='${videoInfo.title}', formatsCount=${formats.size}")

                val parsedViewCount: Long? = try {
                    videoInfo.viewCount?.toLongOrNull()
                } catch (e: Exception) {
                    null
                }

                val mediaModel = MediaModel(
                    id = videoInfo.id ?: System.currentTimeMillis().toString(),
                    title = videoInfo.title ?: "Downloaded Media",
                    uploader = videoInfo.uploader ?: videoInfo.uploaderId ?: "Unknown Creator",
                    uploaderUrl = null,
                    durationSeconds = videoInfo.duration.toLong(),
                    thumbnailUrl = videoInfo.thumbnail,
                    webpageUrl = cleanUrl,
                    extractorName = videoInfo.extractor ?: detectPlatformFromUrl(cleanUrl),
                    description = videoInfo.description,
                    viewCount = parsedViewCount,
                    availableFormats = formats,
                    directVideoUrl = videoInfo.url
                )

                metadataCache.put(cleanUrl, mediaModel)
                return@withContext Result.success(mediaModel)
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Strategy #$index failed: ${e.message}")
            }
        }

        val finalError = lastException ?: IllegalStateException("Extraction failed")
        Log.e(TAG, "All extraction strategies failed for $cleanUrl", finalError)
        return@withContext Result.failure(finalError)
    }

    private fun isTikTokUrl(url: String): Boolean {
        val lower = url.lowercase()
        return lower.contains("tiktok.com") || lower.contains("douyin.com")
    }

    /**
     * Fast TikTok metadata & direct stream extractor using TikWM API (sub-second response).
     */
    private fun extractTikTokFast(cleanUrl: String): MediaModel? {
        return try {
            val encoded = URLEncoder.encode(cleanUrl, "UTF-8")
            val apiUrl = "https://www.tikwm.com/api/?url=$encoded&hd=1"
            val conn = (URL(apiUrl).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setRequestProperty("Accept", "application/json")
            }

            if (conn.responseCode in 200..299) {
                val jsonStr = conn.inputStream.bufferedReader().use { it.readText() }
                val root = JSONObject(jsonStr)
                val code = root.optInt("code", -1)
                if (code == 0 && root.has("data")) {
                    val data = root.getJSONObject("data")
                    val id = data.optString("id", System.currentTimeMillis().toString())
                    val title = data.optString("title").ifBlank { "TikTok Video ($id)" }
                    val cover = data.optString("cover").takeIf { it.isNotBlank() }
                    val duration = data.optLong("duration", 0L)
                    val playUrl = data.optString("play").takeIf { it.isNotBlank() } // Direct HD No-Watermark
                    val hdPlayUrl = data.optString("hdplay").takeIf { it.isNotBlank() }
                    
                    val actualHdPlayUrl = if (hdPlayUrl != null && hdPlayUrl != playUrl) hdPlayUrl else null
                    val musicUrl = data.optString("music").takeIf { it.isNotBlank() }
                    val size = data.optLong("size", 0L)

                    val authorObj = data.optJSONObject("author")
                    val nickname = authorObj?.optString("nickname") ?: "TikTok Creator"
                    val uniqueId = authorObj?.optString("unique_id") ?: ""

                    val formats = mutableListOf<FormatModel>()

                    // HD format
                    if (!actualHdPlayUrl.isNullOrBlank()) {
                        formats.add(
                            FormatModel(
                                formatId = "tik_hd",
                                ext = "mp4",
                                resolution = "1080p (HD)",
                                width = 1080,
                                height = 1920,
                                fileSize = if (size > 0) (size * 1.5).toLong() else 0L,
                                formatNote = "HD No Watermark",
                                isVideo = true,
                                isAudioOnly = false,
                                directUrl = actualHdPlayUrl
                            )
                        )
                    }

                    // Standard format
                    if (!playUrl.isNullOrBlank()) {
                        formats.add(
                            FormatModel(
                                formatId = "tik_sd",
                                ext = "mp4",
                                resolution = "720p",
                                width = 720,
                                height = 1280,
                                fileSize = if (size > 0) (size * 0.8).toLong() else 0L,
                                formatNote = "Standard No Watermark",
                                isVideo = true,
                                isAudioOnly = false,
                                directUrl = playUrl
                            )
                        )
                    }

                    // Audio MP3 format
                    if (!musicUrl.isNullOrBlank()) {
                        formats.add(
                            FormatModel(
                                formatId = "tik_audio",
                                ext = "mp3",
                                resolution = "Audio Only",
                                width = 0,
                                height = 0,
                                fileSize = 0L,
                                formatNote = "Original Audio MP3",
                                isVideo = false,
                                isAudioOnly = true,
                                directUrl = musicUrl
                            )
                        )
                    }

                    return MediaModel(
                        id = id,
                        title = title,
                        uploader = if (uniqueId.isNotBlank()) "$nickname (@$uniqueId)" else nickname,
                        uploaderUrl = if (uniqueId.isNotBlank()) "https://www.tiktok.com/@$uniqueId" else null,
                        durationSeconds = duration,
                        thumbnailUrl = cover,
                        webpageUrl = cleanUrl,
                        extractorName = "TikTok",
                        description = title,
                        viewCount = null,
                        availableFormats = formats,
                        directVideoUrl = hdPlayUrl ?: playUrl
                    )
                }
            }
            null
        } catch (e: Exception) {
            Log.w(TAG, "Fast TikTok extractor error", e)
            null
        }
    }

    /**
     * Executes download with speed optimization, direct streaming for TikTok / direct URLs,
     * and fallback to yt-dlp binary with robust retry strategies and non-colliding filenames.
     */
    suspend fun downloadMedia(
        config: DownloadConfig,
        processId: String,
        onProgress: (progress: Float, etaSeconds: Long, line: String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        val downloadDir = getDownloadDirectory()
        if (!downloadDir.exists()) {
            downloadDir.mkdirs()
        }

        val isTikTok = isTikTokUrl(config.url)
        val isYouTube = config.url.contains("youtube.com", ignoreCase = true) || config.url.contains("youtu.be", ignoreCase = true)
        val isInstagram = config.url.contains("instagram.com", ignoreCase = true)

        // 1. Direct High-Speed Stream Download for TikTok
        if (isTikTok) {
            try {
                Log.d(TAG, "Attempting high-speed direct download for TikTok: ${config.url}")
                val cleanUrl = config.url.trim()
                val directMedia = metadataCache.get(cleanUrl) ?: extractTikTokFast(cleanUrl)
                val directUrl = if (config.isAudioOnly) {
                    directMedia?.availableFormats?.firstOrNull { it.isAudioOnly }?.directUrl ?: directMedia?.directVideoUrl
                } else {
                    val fid = config.selectedFormatId
                    val selectedFormat = directMedia?.availableFormats?.firstOrNull { it.formatId == fid }
                    selectedFormat?.directUrl ?: directMedia?.directVideoUrl
                }

                if (!directUrl.isNullOrBlank() && (directUrl.startsWith("http://") || directUrl.startsWith("https://"))) {
                    val sanitizedTitle = sanitizeFilename(config.title.ifBlank { "TikTok_${System.currentTimeMillis()}" })
                    val extension = if (config.isAudioOnly) config.audioFormat.ext else "mp4"
                    val qualityTag = if (config.isAudioOnly) config.audioFormat.name else "Video"
                    
                    var targetFile = File(downloadDir, "${sanitizedTitle}_[${qualityTag}].$extension")
                    var counter = 1
                    while (targetFile.exists()) {
                        targetFile = File(downloadDir, "${sanitizedTitle}_[${qualityTag}]_$counter.$extension")
                        counter++
                    }
                    
                    val partFile = File(targetFile.absolutePath + ".part")

                    val downloadResult = downloadDirectHttpStream(directUrl, partFile, processId, onProgress)
                    if (downloadResult.isSuccess) {
                        val finalPartFile = downloadResult.getOrThrow()
                        if (finalPartFile.exists()) {
                            finalPartFile.renameTo(targetFile)
                        }
                        scanMediaFile(targetFile)
                        return@withContext Result.success(targetFile)
                    } else {
                        if (partFile.exists()) partFile.delete()
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Direct TikTok stream download failed, falling back to yt-dlp engine", e)
            }
        }

        ensureEngineReady()

        val cookiesFile = getCookiesFile()

        val strategies: List<(YoutubeDLRequest) -> Unit> = when {
            isTikTok -> listOf(
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                    req.addOption("--add-header", "Referer:https://www.tiktok.com/")
                }
            )
            isYouTube -> listOf(
                // 1. Default yt-dlp client (gets 4K + bypasses SABR natively)
                { _ -> /* default yt-dlp */ },
                // 2. Fallback to mobile clients
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android,ios,web")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=ios")
                }
            )
            isInstagram -> listOf(
                { _ -> /* default yt-dlp */ },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1")
                }
            )
            else -> listOf({ _ -> /* standard */ })
        }

        var lastException: Exception? = null

        for ((index, applyStrategy) in strategies.withIndex()) {
            try {
                val request = YoutubeDLRequest(config.url)
                applyStrategy(request)

                if (cookiesFile != null && cookiesFile.exists() && cookiesFile.length() > 0) {
                    request.addOption("--cookies", cookiesFile.absolutePath)
                }

                // Distinct output template by resolution/quality so multiple resolutions of the same video never clash
                var rawQualityTag = if (config.isAudioOnly) {
                    config.audioFormat.name
                } else {
                    val fid = config.selectedFormatId ?: ""
                    if (fid.isNotEmpty()) {
                        if (fid.contains("_")) fid.substringAfter("_") else fid
                    } else {
                        config.videoQuality.label
                    }
                }
                // Strictly sanitize quality tag to prevent filesystem errors (e.g. if fid contains '/')
                val qualityTag = rawQualityTag.replace(Regex("[^a-zA-Z0-9.-]"), "_")
                
                // Format output file template with quality indicator to prevent file collisions
                val outputTemplate = "%(title).80B-%(id)s-[$qualityTag].%(ext)s"
                request.addOption("-o", outputTemplate)
                request.addOption("--paths", downloadDir.absolutePath)
                request.addOption("--paths", "temp:${downloadDir.absolutePath}/temp_${processId}")

                // General performance and integrity options
                request.addOption("--continue")
                request.addOption("--no-mtime")
                request.addOption("--no-playlist")
                request.addOption("--no-check-certificates")
                request.addOption("--geo-bypass")
                request.addOption("--force-ipv4")
                request.addOption("--retries", "10")
                request.addOption("--fragment-retries", "10")
                request.addOption("--concurrent-fragments", "4")
                request.addOption("--hls-prefer-native")

                // Format sorting and resolution prioritization (Seal algorithm)
                val targetHeight = resolveTargetHeight(config)
                val formatSortArg = if (targetHeight != null) {
                    "res:$targetHeight,fps,quality,size,br"
                } else {
                    "res,fps,quality,size,br"
                }
                request.addOption("--format-sort", formatSortArg)

                // Metadata embedding options
                if (config.embedMetadata) {
                    request.addOption("--embed-metadata")
                }
                if (config.embedThumbnail) {
                    request.addOption("--embed-thumbnail")
                }

                // High-Quality Audio Extraction vs Adaptive Video options
                if (config.isAudioOnly) {
                    // Extract best available audio stream and transcode to requested high-fidelity bitrate
                    request.addOption("-f", "bestaudio/best")
                    request.addOption("-x")
                    request.addOption("--audio-format", config.audioFormat.ext)
                    request.addOption("--audio-quality", "0")
                    
                    when (config.audioFormat) {
                        AudioFormat.MP3 -> {
                            request.addOption("--postprocessor-args", "ExtractAudio:-b:a 320k")
                        }
                        AudioFormat.M4A -> {
                            request.addOption("--postprocessor-args", "ExtractAudio:-b:a 256k")
                        }
                        AudioFormat.OPUS -> {
                            request.addOption("--postprocessor-args", "ExtractAudio:-b:a 160k")
                        }
                        AudioFormat.FLAC -> {
                            // Lossless FLAC
                        }
                        AudioFormat.WAV -> {
                            // Lossless WAV PCM
                        }
                    }
                } else {
                    val formatFilter = resolveFormatFilter(config)
                    Log.i(TAG, "Selected Format Filter: -f '$formatFilter' for config: selectedFormatId='${config.selectedFormatId}', quality=${config.videoQuality.label}")
                    request.addOption("-f", formatFilter)
                    request.addOption("--merge-output-format", "mp4")
                }

                // High-speed Aria2c multi-threaded downloader (for supported direct platforms)
                val isFacebook = config.url.contains("facebook.com", true) || config.url.contains("fb.watch", true) || config.url.contains("fb.com", true)
                val isSocialMedia = isFacebook || config.url.contains("instagram", true) || config.url.contains("tiktok", true) || config.url.contains("twitter", true) || config.url.contains("x.com", true)
                if (config.useAria2c && !isYouTube && !isSocialMedia) {
                    try {
                        request.addOption("--external-downloader", "aria2c")
                        request.addOption("--external-downloader-args", "aria2c:-s 8 -x 8 -k 1M -j 8")
                    } catch (e: Exception) {
                        Log.w(TAG, "Aria2c flag setup fallback", e)
                    }
                }

                if (config.customArguments.isNotBlank()) {
                    val splitArgs = config.customArguments.split(" ")
                    for (arg in splitArgs) {
                        if (arg.isNotBlank()) {
                            request.addOption(arg)
                        }
                    }
                }

                Log.d(TAG, "Starting yt-dlp execution strategy $index for process: $processId")

                val filesBefore = downloadDir.listFiles()?.map { it.name }?.toSet() ?: emptySet()

                YoutubeDL.getInstance().execute(request, processId) { progress, etaInSeconds, line ->
                    onProgress(progress, etaInSeconds, line)
                }

                File(downloadDir, "temp_${processId}").deleteRecursively()

                val filesAfter = downloadDir.listFiles() ?: emptyArray()
                val newFile = filesAfter.filter { it.name !in filesBefore }
                    .maxByOrNull { it.lastModified() }
                    ?: filesAfter.filter { !it.name.endsWith(".part") && !it.name.endsWith(".ytdl") }
                        .maxByOrNull { it.lastModified() }
                    ?: File(downloadDir, "downloaded_media")

                if (newFile.exists()) {
                    scanMediaFile(newFile)
                }

                return@withContext Result.success(newFile)
            } catch (e: Exception) {
                Log.w(TAG, "Download strategy $index failed for ${config.url}: ${e.message}")
                lastException = e
                if (cancelledProcessIds[processId] == true) {
                    Log.i(TAG, "Process $processId was cancelled. Aborting strategies.")
                    break
                }
                val isBotError = e.message?.contains("Sign in to confirm you’re not a bot", ignoreCase = true) == true ||
                        e.message?.contains("bot", ignoreCase = true) == true
                if (!isBotError && index > 0) {
                    break
                }
            }
        }

        val finalError = lastException ?: IllegalStateException("Download failed")
        Log.e(TAG, "All download strategies failed for process $processId", finalError)
        return@withContext Result.failure(finalError)
    }

    /**
     * Inspects and logs actual dimensions, file size, and codecs of the downloaded file.
     */
    private fun verifyDownloadedMedia(file: File) {
        val retriever = android.media.MediaMetadataRetriever()
        try {
            retriever.setDataSource(file.absolutePath)
            val w = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val h = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val duration = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)
            val mime = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
            Log.i(TAG, "DOWNLOAD VERIFICATION SUCCESS: File='${file.name}', Size=${formatBytes(file.length())}, Dimensions=${w}x${h}, Mime=$mime, Duration=${duration}ms")
        } catch (e: Exception) {
            Log.w(TAG, "Media metadata retrieval: ${e.message}")
        } finally {
            try {
                retriever.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    /**
     * Downloads direct video / audio stream over HTTP with real-time progress, speed, and ETA callbacks.
     * Implements true HTTP Range header resuming on pause/resume with zero background data wastage.
     */
    private fun downloadDirectHttpStream(
        streamUrl: String,
        targetFile: File,
        processId: String,
        onProgress: (progress: Float, etaSeconds: Long, line: String) -> Unit
    ): Result<File> {
        return try {
            var totalBytesRead = if (targetFile.exists()) targetFile.length() else 0L
            val url = URL(streamUrl)
            
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10000
                readTimeout = 15000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                setRequestProperty("Accept", "*/*")
                if (totalBytesRead > 0) {
                    setRequestProperty("Range", "bytes=$totalBytesRead-")
                }
            }

            val responseCode = connection.responseCode
            if (responseCode !in 200..299 && responseCode != 206) {
                return Result.failure(IllegalStateException("HTTP $responseCode: ${connection.responseMessage}"))
            }

            val contentLen = connection.contentLengthLong
            val totalBytes = if (responseCode == 206) {
                totalBytesRead + contentLen
            } else {
                contentLen
            }

            val appendMode = (responseCode == 206 && totalBytesRead > 0)
            if (!appendMode) {
                totalBytesRead = 0L
            }

            val inputStream = BufferedInputStream(connection.inputStream)
            val outputStream = FileOutputStream(targetFile, appendMode)

            val buffer = ByteArray(64 * 1024)
            val startTime = System.currentTimeMillis()
            var lastProgressUpdate = 0L

            var bytesRead: Int
            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (isCancelled(processId)) {
                    outputStream.close()
                    inputStream.close()
                    targetFile.delete()
                    return Result.failure(IllegalStateException("Download cancelled"))
                }

                // If paused, cleanly close streams and wait without background network drain
                if (isPaused(processId)) {
                    outputStream.flush()
                    outputStream.close()
                    inputStream.close()
                    connection.disconnect()
                    
                    while (isPaused(processId)) {
                        Thread.sleep(300)
                        if (isCancelled(processId)) {
                            targetFile.delete()
                            return Result.failure(IllegalStateException("Download cancelled"))
                        }
                    }

                    // Resumed: recursively resume with HTTP Range
                    return downloadDirectHttpStream(streamUrl, targetFile, processId, onProgress)
                }

                outputStream.write(buffer, 0, bytesRead)
                totalBytesRead += bytesRead

                val now = System.currentTimeMillis()
                if (now - lastProgressUpdate > 250 || totalBytesRead == totalBytes) {
                    lastProgressUpdate = now
                    val progressPercent = if (totalBytes > 0) {
                        (totalBytesRead * 100f / totalBytes).coerceIn(0f, 99f)
                    } else {
                        50f
                    }

                    val elapsedSec = (now - startTime) / 1000.0
                    val speedBytesPerSec = if (elapsedSec > 0) totalBytesRead / elapsedSec else 0.0
                    val speedText = formatSpeed(speedBytesPerSec)

                    val remainingBytes = if (totalBytes > totalBytesRead) totalBytes - totalBytesRead else 0L
                    val etaSec = if (speedBytesPerSec > 0) (remainingBytes / speedBytesPerSec).toLong() else 0L

                    val line = "[download] ${progressPercent.toInt()}% of ${formatBytes(totalBytes)} at $speedText ETA ${etaSec}s"
                    onProgress(progressPercent, etaSec, line)
                }
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            onProgress(100f, 0L, "[download] 100% completed")
            Result.success(targetFile)
        } catch (e: Exception) {
            Log.e(TAG, "Direct stream download error", e)
            Result.failure(e)
        }
    }

    private fun formatSpeed(bytesPerSec: Double): String {
        return when {
            bytesPerSec >= 1024 * 1024 -> String.format("%.2f MB/s", bytesPerSec / (1024 * 1024))
            bytesPerSec >= 1024 -> String.format("%.1f KB/s", bytesPerSec / 1024)
            else -> String.format("%.0f B/s", bytesPerSec)
        }
    }

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "Unknown"
        val mb = bytes / (1024.0 * 1024.0)
        return String.format("%.2f MB", mb)
    }

    private fun sanitizeFilename(name: String): String {
        return name.replace(Regex("""[\\/:*?"<>|]"""), "_").trim().take(100)
    }

    private fun scanMediaFile(file: File) {
        try {
            MediaScannerConnection.scanFile(
                context,
                arrayOf(file.absolutePath),
                null
            ) { path, uri ->
                Log.i(TAG, "Media scanned into Android library: $path -> $uri")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Media scan failed: ${e.message}")
        }
    }

    private fun ensureEngineReady() {
        try {
            YoutubeDL.getInstance().init(context)
        } catch (e: Exception) {
            Log.w(TAG, "YoutubeDL engine init verification: ${e.message}")
        }
        try {
            com.yausername.ffmpeg.FFmpeg.getInstance().init(context)
        } catch (e: Exception) {
            Log.w(TAG, "FFmpeg engine init verification: ${e.message}")
        }
    }

    private fun resolveTargetHeight(config: DownloadConfig): Int? {
        val fid = config.selectedFormatId?.trim() ?: ""
        if (fid.equals("4k", ignoreCase = true) || fid.contains("2160", ignoreCase = true)) return 2160
        if (fid.equals("2k", ignoreCase = true) || fid.contains("1440", ignoreCase = true)) return 1440
        
        val compositeMatch = Regex("""^([a-zA-Z0-9_-]+)_(\d{3,4})p$""").find(fid)
        if (compositeMatch != null) {
            return compositeMatch.groupValues[2].toIntOrNull()
        }
        val atMatch = Regex("""@(\d{3,4})p$""").find(fid)
        if (atMatch != null) {
            return atMatch.groupValues[1].toIntOrNull()
        }
        val heightMatch = Regex("""^(\d{3,4})p?$""").find(fid)
        if (heightMatch != null) {
            return heightMatch.groupValues[1].toIntOrNull()
        }
        return when (config.videoQuality) {
            VideoQuality.P2160 -> 2160
            VideoQuality.P1440 -> 1440
            VideoQuality.P1080 -> 1080
            VideoQuality.P720 -> 720
            VideoQuality.P480 -> 480
            VideoQuality.P360 -> 360
            VideoQuality.BEST -> null
        }
    }

    private fun resolveFormatFilter(config: DownloadConfig): String {
        val fid = config.selectedFormatId?.trim()
        val targetHeight = resolveTargetHeight(config)
        Log.i(TAG, "Resolving format filter: selectedFormatId='$fid', quality=${config.videoQuality.label}, targetHeight=$targetHeight")

        if (targetHeight != null && targetHeight > 0) {
            // Strict resolution prioritizing best video at target height merged with best audio, followed by closest resolutions
            return "bestvideo[height<=$targetHeight]+bestaudio[ext=m4a]/bestvideo[height<=$targetHeight]+bestaudio/bestvideo[height<=$targetHeight]+ba/bestvideo[height<=${targetHeight + 80}]+bestaudio/bestvideo+bestaudio/best[height<=$targetHeight]/best"
        }

        if (!fid.isNullOrBlank() && fid != "best") {
            // 1. If formatId is encoded as "<format_id>@<height>p" (e.g. "137@1080p")
            if (fid.contains("@")) {
                val streamId = fid.substringBefore("@").trim()
                val heightStr = fid.substringAfter("@").removeSuffix("p").trim()
                val h = heightStr.toIntOrNull()
                if (h != null && h > 0) {
                    return "$streamId+bestaudio[ext=m4a]/$streamId+bestaudio/$streamId+ba/bestvideo[height<=$h]+bestaudio/best[height<=$h]/bestvideo+bestaudio/best"
                } else if (streamId.isNotBlank()) {
                    return "$streamId+bestaudio/$streamId+ba/$streamId/best"
                }
            }

            // 2. If it's a specific numeric/alphanumeric format_id from yt-dlp (e.g. "137", "248", "hd", "sd")
            if (fid.toIntOrNull() != null || (fid.matches(Regex("""^[0-9a-zA-Z_-]+$""")) && fid != "best" && !fid.endsWith("p"))) {
                return "$fid+bestaudio[ext=m4a]/$fid+bestaudio/$fid+ba/$fid/bestvideo+bestaudio/best"
            }

            // 3. TikTok formats
            if (fid == "tik_hd") {
                return "bestvideo[height<=1080]+bestaudio/bestvideo[height<=1080]+ba/best[height<=1080]/best"
            } else if (fid == "tik_sd") {
                return "bestvideo[height<=720]+bestaudio/bestvideo[height<=720]+ba/best[height<=720]/best"
            }
        }

        return "bestvideo+bestaudio/best"
    }

    fun getCookiesFile(): File? {
        val file = File(context.filesDir, "cookies.txt")
        return if (file.exists()) file else null
    }

    fun saveCookies(cookiesContent: String): Boolean {
        return try {
            val file = File(context.filesDir, "cookies.txt")
            if (cookiesContent.isBlank()) {
                if (file.exists()) file.delete()
            } else {
                file.writeText(cookiesContent)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save cookies", e)
            false
        }
    }

    fun getCookiesContent(): String {
        return try {
            val file = File(context.filesDir, "cookies.txt")
            if (file.exists()) file.readText() else ""
        } catch (e: Exception) {
            ""
        }
    }

    private val pausedProcessIds = java.util.concurrent.ConcurrentHashMap<String, Boolean>()
    private val cancelledProcessIds = java.util.concurrent.ConcurrentHashMap<String, Boolean>()

    fun setPaused(processId: String, paused: Boolean) {
        pausedProcessIds[processId] = paused
    }

    fun isPaused(processId: String): Boolean = pausedProcessIds[processId] == true

    fun isCancelled(processId: String): Boolean = cancelledProcessIds[processId] == true

    fun clearCookies(): Boolean {
        return try {
            val file = File(context.filesDir, "cookies.txt")
            if (file.exists()) file.delete() else true
        } catch (e: Exception) {
            false
        }
    }

    fun resetProcessState(processId: String) {
        cancelledProcessIds.remove(processId)
        pausedProcessIds.remove(processId)
    }

    fun cancelDownload(processId: String): Boolean {
        cancelledProcessIds[processId] = true
        pausedProcessIds.remove(processId)
        return try {
            YoutubeDL.getInstance().destroyProcessById(processId)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to destroy process $processId", e)
            false
        }
    }

    fun getDownloadDirectory(): File {
        val publicDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val dmDir = File(publicDir, DOWNLOADS_SUBDIR)
        return if (dmDir.exists() || dmDir.mkdirs()) {
            dmDir
        } else {
            File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), DOWNLOADS_SUBDIR).apply {
                if (!exists()) mkdirs()
            }
        }
    }

    private fun detectPlatformFromUrl(url: String): String {
        val lower = url.lowercase()
        return when {
            "youtube.com" in lower || "youtu.be" in lower -> "YouTube"
            "tiktok.com" in lower || "douyin.com" in lower -> "TikTok"
            "twitter.com" in lower || "x.com" in lower -> "Twitter / X"
            "instagram.com" in lower -> "Instagram"
            "facebook.com" in lower || "fb.watch" in lower || "fb.com" in lower -> "Facebook"
            "reddit.com" in lower -> "Reddit"
            "bilibili.com" in lower -> "Bilibili"
            "soundcloud.com" in lower -> "SoundCloud"
            "vimeo.com" in lower -> "Vimeo"
            "threads.net" in lower -> "Threads"
            "twitch.tv" in lower -> "Twitch"
            "pinterest.com" in lower -> "Pinterest"
            else -> "Web Media"
        }
    }

    fun cleanupIncompleteFiles() {
        try {
            val downloadDir = getDownloadDirectory()
            if (downloadDir.exists() && downloadDir.isDirectory) {
                val incompleteFiles = downloadDir.listFiles { file ->
                    val name = file.name.lowercase()
                    name.endsWith(".part") || name.endsWith(".ytdl") || name.startsWith("temp_dl_")
                }
                incompleteFiles?.forEach { file ->
                    try {
                        file.delete()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to delete incomplete file: ${file.name}")
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Cleanup incomplete files error", e)
        }
    }
}