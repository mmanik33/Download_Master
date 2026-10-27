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

## 🚀 GitHub Actions Release Build (CI/CD)

There are **no automatic builds on push**. Release APKs are built exclusively via manual workflow runs:

1. **Configure Signing Secrets**:
   - In your repository **Settings** -> **Secrets and variables** -> **Actions**, create:
     - `KEYSTORE_BASE64`: Base64-encoded release keystore file (containing key alias `upload`).
     - `STORE_PASSWORD`: Keystore password.
     - `KEY_PASSWORD`: Key password for alias `upload`.
2. **Run Manual Release Workflow**:
   - Go to **Actions**, select **Build & Release Download Master**, and click **Run workflow**.
   - The workflow decodes the release keystore to a temporary location, runs `:app:assembleRelease` to produce a signed universal release APK (`Download-Master-universal-release.apk`), and publishes it to **GitHub Releases**.

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
