# Download Master 🚀

Native Android video & audio downloader application powered by embedded **yt-dlp**, **FFmpeg**, **Aria2c**, and modern **Jetpack Compose (Material 3)**.

---

## 🌟 Key Features

- **Embedded yt-dlp & FFmpeg Engine**: Extract and download media directly on-device from over 1,000+ supported websites (YouTube, Instagram, TikTok, Facebook, Twitter/X, Reddit, SoundCloud, etc.).
- **Multiple Video & Audio Formats**:
  - **Video**: 4K, 2K, 1080p, 720p, 480p, 360p, MP4, MKV.
  - **Audio Extraction**: MP3 (320kbps), M4A (AAC), FLAC, Opus, WAV, OGG.
  - **Subtitles & Metadata**: Embed subtitles, album art thumbnails, and ID3 tags.
- **Anti-Bot & Verification Bypass**:
  - In-App YouTube / Google login with instant Netscape `cookies.txt` extraction.
  - Multi-tiered client emulation fallback (`ios,mweb`, `mweb`, `android`, `tv_embedded`).
- **High-Speed Acceleration**: Aria2c multi-connection multi-threaded downloading engine.
- **Foreground Service & Real-Time Tracking**: Progress notifications, speed metrics, ETA countdown, and non-intrusive background processing.
- **Local Persistence & Media Library**: Powered by Android Room database; auto-registered with Android Media Scanner into `Download/DownloadMaster/`.
- **Modern Jetpack Compose UI**: Dynamic Material 3 theming, edge-to-edge support, and responsive layouts.

---

## 🛠️ Tech Stack & Architecture

- **Language**: 100% Kotlin
- **UI Framework**: Jetpack Compose + Material 3
- **Architecture**: MVVM (Model-View-ViewModel) + Clean Architecture
- **State Management**: Kotlin StateFlow / Coroutines
- **Database**: Android Jetpack Room
- **Networking & Media**:
  - `youtubedl-android` (v0.18.1)
  - `ffmpeg-android`
  - `aria2c-android`
  - Retrofit & OkHttp
  - Coil (Async Image Loading)
- **CI/CD**: GitHub Actions (Automatic Build & Release APK generator)

---

## 🚀 Automated GitHub Actions Build (CI/CD)

Whenever you push to GitHub or create a release tag, **GitHub Actions** will automatically build the APKs and attach them as downloadable artifacts:

1. **Push to `main` / `master`**:
   - Runs unit tests
   - Automatically builds `Download-Master-APKs` artifact (`app-debug.apk`) available under the **Actions** tab.
2. **Release on Git Tag**:
   - Tagging a version (e.g. `git tag v1.0.0 && git push origin v1.0.0`) automatically publishes a GitHub Release with the compiled APKs attached.
3. **Manual Trigger**:
   - Go to **Actions** -> **Build and Release Android APK** -> click **Run workflow**.

---

## 💻 Local Build & Development

### Prerequisites
- Android Studio Ladybug (2024.2.1+) or newer
- JDK 17 / JDK 21
- Android SDK 36 (minSdk 24)

### Steps to Run Locally

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/<your-username>/<repo-name>.git
   cd <repo-name>
   ```

2. **Restore Debug Keystore (Optional if needed)**:
   ```bash
   base64 -d debug.keystore.base64 > debug.keystore
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```

4. **Run Unit Tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

5. **Install on Connected Device**:
   ```bash
   ./gradlew installDebug
   ```

---

## 📄 License & Disclaimer

This project is open-source under the MIT License. Embedded binaries (yt-dlp, FFmpeg, aria2c) are licensed under their respective open-source licenses.
