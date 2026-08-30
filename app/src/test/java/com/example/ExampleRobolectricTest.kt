package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.AudioFormat
import com.example.model.DownloadConfig
import com.example.model.VideoQuality
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Download Master", appName)
  }

  @Test
  fun `verify YouTube quality mappings`() {
    val config1080 = DownloadConfig(
      url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      title = "Test 1080p Video",
      videoQuality = VideoQuality.P1080,
      selectedFormatId = "137"
    )
    assertEquals("137", config1080.selectedFormatId)
    assertEquals(VideoQuality.P1080, config1080.videoQuality)

    val config1440 = DownloadConfig(
      url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      title = "Test 1440p Video",
      videoQuality = VideoQuality.P1440,
      selectedFormatId = "271"
    )
    assertEquals("271", config1440.selectedFormatId)
    assertEquals(VideoQuality.P1440, config1440.videoQuality)

    val config2160 = DownloadConfig(
      url = "https://www.youtube.com/watch?v=dQw4w9WgXcQ",
      title = "Test 4K Video",
      videoQuality = VideoQuality.P2160,
      selectedFormatId = "313"
    )
    assertEquals("313", config2160.selectedFormatId)
    assertEquals(VideoQuality.P2160, config2160.videoQuality)
  }

  @Test
  fun `verify audio formats`() {
    assertEquals("mp3", AudioFormat.MP3.ext)
    assertEquals("m4a", AudioFormat.M4A.ext)
    assertEquals("opus", AudioFormat.OPUS.ext)
    assertEquals("flac", AudioFormat.FLAC.ext)
    assertEquals("wav", AudioFormat.WAV.ext)
  }
}

