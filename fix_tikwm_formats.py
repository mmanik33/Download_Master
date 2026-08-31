import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

old_hd_play = """                    val playUrl = data.optString("play").takeIf { it.isNotBlank() } // Direct HD No-Watermark
                    val hdPlayUrl = data.optString("hdplay").takeIf { it.isNotBlank() } ?: playUrl"""
new_hd_play = """                    val playUrl = data.optString("play").takeIf { it.isNotBlank() } // Direct HD No-Watermark
                    val hdPlayUrl = data.optString("hdplay").takeIf { it.isNotBlank() }
                    
                    val actualHdPlayUrl = if (hdPlayUrl != null && hdPlayUrl != playUrl) hdPlayUrl else null"""

content = content.replace(old_hd_play, new_hd_play)

old_hd_check = """                    // HD format
                    if (!hdPlayUrl.isNullOrBlank()) {
                        formats.add(
                            FormatModel(
                                formatId = "tik_hd",
                                ext = "mp4",
                                resolution = "1080p (HD)",
                                width = 1080,
                                height = 1920,
                                fileSize = if (size > 0) size else 0L,
                                formatNote = "HD No Watermark",
                                isVideo = true,
                                isAudioOnly = false,
                                directUrl = hdPlayUrl
                            )
                        )
                    }"""

new_hd_check = """                    // HD format
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
                    }"""

content = content.replace(old_hd_check, new_hd_check)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
