package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.MainTab
import com.example.ui.MainViewModel
import com.example.ui.components.CookieManagerDialog
import com.example.ui.components.EngineUpdateDialog
import com.example.ui.components.VideoQualitySelectionSheet
import com.example.ui.screens.BrowserScreen
import com.example.ui.screens.DownloadsScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.state.DownloadUiState
import com.example.ui.state.EngineUpdateState
import com.example.ui.theme.AppTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryPurple
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            val colorPalette by viewModel.colorPalette.collectAsState()
            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark, paletteId = colorPalette) {
                DownloadMasterApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                val extractedUrl = extractUrlFromText(sharedText)
                if (extractedUrl.isNotBlank()) {
                    viewModel.pasteFromClipboard(extractedUrl)
                }
            }
        }
    }

    private fun extractUrlFromText(text: String): String {
        val urlRegex = Regex("""https?://[^\s]+""")
        val match = urlRegex.find(text)
        return match?.value ?: text.trim()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadMasterApp(viewModel: MainViewModel) {
    val context = LocalContext.current
    val colors = AppTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val currentTab by viewModel.currentTab.collectAsState()
    val urlInput by viewModel.urlInput.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val engineUpdateState by viewModel.engineUpdateState.collectAsState()
    val ytDlpVersion by DownloadMasterApp.ytDlpVersion.collectAsState()
    val historyItems by viewModel.downloadHistory.collectAsState()
    val historySearchQuery by viewModel.historySearchQuery.collectAsState()

    val browserUrlInput by viewModel.browserUrlInput.collectAsState()
    val activeBrowserUrl by viewModel.activeBrowserUrl.collectAsState()
    val detectedVideoUrl by viewModel.detectedBrowserVideoUrl.collectAsState()
    val bookmarks by viewModel.bookmarks.collectAsState()

    val embedThumbnail by viewModel.embedThumbnail.collectAsState()
    val embedMetadata by viewModel.embedMetadata.collectAsState()
    val useAria2c by viewModel.useAria2c.collectAsState()
    val useWifiOnly by viewModel.useWifiOnly.collectAsState()
    val maxConcurrent by viewModel.maxConcurrentDownloads.collectAsState()
    val defaultQuality by viewModel.defaultQualityPreference.collectAsState()
    val downloadLocation by viewModel.downloadLocation.collectAsState()

    var showCookieDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    var hasAcceptedWarning by remember { mutableStateOf(prefs.getBoolean("has_accepted_warning", false)) }
    val qualitySheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Intercept Back Button: If user is on any other page, navigate back to Home instead of quitting
    BackHandler(enabled = currentTab != MainTab.HOME) {
        viewModel.selectTab(MainTab.HOME)
    }

    var isPermissionDone by remember { mutableStateOf(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) }

    // Notification Permission for background downloads
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isPermissionDone = true
        if (!isGranted) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("Notifications allow real-time download progress.")
            }
        }
    }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                isPermissionDone = true
            }
        } else {
            isPermissionDone = true
        }
    }

    // Handle UI errors in Snackbar
    LaunchedEffect(uiState) {
        if (uiState is DownloadUiState.Error) {
            val errorMsg = (uiState as DownloadUiState.Error).message
            snackbarHostState.showSnackbar(errorMsg)
        }
    }

    // Quality Selection Modal Sheet when media is ready
    if (uiState is DownloadUiState.Ready) {
        val readyState = uiState as DownloadUiState.Ready
        VideoQualitySelectionSheet(
            media = readyState.mediaInfo,
            sheetState = qualitySheetState,
            onDismiss = { viewModel.resetToIdle() },
            onDownloadSelected = { quality, isAudioOnly, audioFormat, formatId ->
                viewModel.startDownloadWithQuality(readyState.mediaInfo, quality, isAudioOnly, formatId)
                viewModel.selectTab(MainTab.DOWNLOADS)
            }
        )
    }

    if (isPermissionDone && !hasAcceptedWarning) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { /* No dismiss by tapping outside */ },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Warning",
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "If anyone downloads anything unethical using this app, the burden of that sin is solely on the user. The developer will in no way share this sin.",
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = {
                    prefs.edit().putBoolean("has_accepted_warning", true).apply()
                    hasAcceptedWarning = true
                }) {
                    Text("Accept", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = {
                    val activity = context as? android.app.Activity
                    activity?.finishAffinity()
                }) {
                    Text("Reject", color = Color(0xFFEF4444))
                }
            }
        )
    }

    // Engine Update Dialog
    if (showUpdateDialog || engineUpdateState !is EngineUpdateState.Idle) {
        EngineUpdateDialog(
            currentVersion = ytDlpVersion,
            updateState = engineUpdateState,
            onUpdateClick = { viewModel.updateYtDlpEngine() },
            onDismiss = {
                showUpdateDialog = false
                viewModel.dismissEngineUpdateDialog()
            }
        )
    }

    // Cookie Manager Dialog
    if (showCookieDialog) {
        CookieManagerDialog(
            initialCookies = viewModel.getCookiesContent(),
            onDismiss = { showCookieDialog = false },
            onSaveCookies = { text ->
                val success = viewModel.saveCookies(text)
                if (success) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Cookies updated successfully") }
                }
            },
            onClearCookies = {
                val success = viewModel.clearCookies()
                if (success) {
                    coroutineScope.launch { snackbarHostState.showSnackbar("Cookies cleared") }
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                contentColor = colors.textSecondary,
                modifier = Modifier
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .testTag("bottom_nav_bar")
            ) {
                // Tab 1: Home
                NavigationBarItem(
                    selected = currentTab == MainTab.HOME,
                    onClick = { viewModel.selectTab(MainTab.HOME) },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.HOME) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary,
                        indicatorColor = colors.primary
                    )
                )

                // Tab 2: Downloads
                NavigationBarItem(
                    selected = currentTab == MainTab.DOWNLOADS,
                    onClick = { viewModel.selectTab(MainTab.DOWNLOADS) },
                    icon = { Icon(Icons.Default.Download, contentDescription = "Downloads") },
                    label = { Text("Downloads", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.DOWNLOADS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary,
                        indicatorColor = colors.primary
                    )
                )

                // Tab 3: Browser
                NavigationBarItem(
                    selected = currentTab == MainTab.BROWSER,
                    onClick = { viewModel.selectTab(MainTab.BROWSER) },
                    icon = { Icon(Icons.Default.Public, contentDescription = "Browser") },
                    label = { Text("Browser", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.BROWSER) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary,
                        indicatorColor = colors.primary
                    )
                )

                // Tab 4: Settings
                NavigationBarItem(
                    selected = currentTab == MainTab.SETTINGS,
                    onClick = { viewModel.selectTab(MainTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings", fontSize = 11.sp, fontWeight = if (currentTab == MainTab.SETTINGS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = colors.primary,
                        unselectedIconColor = colors.textSecondary,
                        unselectedTextColor = colors.textSecondary,
                        indicatorColor = colors.primary
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Smooth Animated Content transition across tabs
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    val duration = 280
                    if (targetState.ordinal > initialState.ordinal) {
                        (slideInHorizontally(animationSpec = tween(duration)) { width -> width / 4 } + fadeIn(animationSpec = tween(duration)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { width -> -width / 4 } + fadeOut(animationSpec = tween(duration)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(duration)) { width -> -width / 4 } + fadeIn(animationSpec = tween(duration)))
                            .togetherWith(slideOutHorizontally(animationSpec = tween(duration)) { width -> width / 4 } + fadeOut(animationSpec = tween(duration)))
                    }
                },
                label = "tab_navigation_animation"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.HOME -> {
                        HomeScreen(
                            viewModel = viewModel,
                            urlInput = urlInput,
                            onUrlChanged = { viewModel.onUrlInputChanged(it) },
                            onDownloadClicked = {
                                if (urlInput.isNotBlank()) {
                                    viewModel.parseMediaUrl(urlInput)
                                } else {
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Please enter or paste a video link first")
                                    }
                                }
                            },
                            onPlatformClicked = { platform ->
                                viewModel.navigateBrowserTo(platform.homeUrl)
                            },
                            onMenuClicked = {
                                viewModel.selectTab(MainTab.SETTINGS)
                            }
                        )
                    }

                    MainTab.DOWNLOADS -> {
                        DownloadsScreen(
                            viewModel = viewModel,
                            uiState = uiState,
                            historyItems = historyItems,
                            onReDownload = { url ->
                                viewModel.onUrlInputChanged(url)
                                viewModel.parseMediaUrl(url)
                                viewModel.selectTab(MainTab.HOME)
                            }
                        )
                    }

                    MainTab.BROWSER -> {
                        BrowserScreen(
                            viewModel = viewModel,
                            browserUrlInput = browserUrlInput,
                            activeUrl = activeBrowserUrl,
                            detectedVideoUrl = detectedVideoUrl,
                            bookmarks = bookmarks,
                            onDownloadVideoDetected = { videoUrl ->
                                viewModel.parseMediaUrl(videoUrl)
                            }
                        )
                    }

                    MainTab.SETTINGS -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            ytDlpVersion = ytDlpVersion,
                            embedThumbnail = embedThumbnail,
                            embedMetadata = embedMetadata,
                            useAria2c = useAria2c,
                            useWifiOnly = useWifiOnly,
                            maxConcurrent = maxConcurrent,
                            defaultQuality = defaultQuality,
                            downloadLocation = downloadLocation,
                            onOpenCookieManager = { showCookieDialog = true },
                            onCheckEngineUpdate = { showUpdateDialog = true }
                        )
                    }
                }
            }

            // Parsing Loading Overlay with Animation
            AnimatedVisibility(
                visible = uiState is DownloadUiState.Parsing,
                enter = fadeIn(tween(200)),
                exit = fadeOut(tween(200))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.surface)
                            .padding(24.dp)
                    ) {
                        CircularProgressIndicator(color = colors.primary, modifier = Modifier.size(40.dp))
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
        }
    }
}
