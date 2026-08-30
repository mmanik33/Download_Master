package com.example.ui.screens

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.model.BookmarkItem
import com.example.model.PlatformItem
import com.example.model.SupportedPlatformsList
import com.example.ui.MainViewModel
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPink
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.PrimaryPurple

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BrowserScreen(
    viewModel: MainViewModel,
    browserUrlInput: String,
    activeUrl: String?,
    detectedVideoUrl: String?,
    bookmarks: List<BookmarkItem>,
    onDownloadVideoDetected: (String) -> Unit
) {
    val colors = AppTheme.colors
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var webLoadingProgress by remember { mutableFloatStateOf(0f) }
    var isWebLoading by remember { mutableStateOf(false) }
    var webErrorMessage by remember { mutableStateOf<String?>(null) }
    var showAddBookmarkDialog by remember { mutableStateOf(false) }
    var newBookmarkTitle by remember { mutableStateOf("") }
    var newBookmarkUrl by remember { mutableStateOf("") }

    // Intercept back button when actively browsing a webpage
    BackHandler(enabled = activeUrl != null) {
        if (webViewInstance?.canGoBack() == true) {
            webViewInstance?.goBack()
        } else {
            viewModel.activeBrowserUrl.value = null
        }
    }

    if (showAddBookmarkDialog) {
        AlertDialog(
            onDismissRequest = { showAddBookmarkDialog = false },
            containerColor = colors.surface,
            title = { Text("Add Bookmark", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newBookmarkTitle,
                        onValueChange = { newBookmarkTitle = it },
                        label = { Text("Title", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBookmarkUrl,
                        onValueChange = { newBookmarkUrl = it },
                        label = { Text("URL", color = colors.textSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newBookmarkUrl.isNotBlank()) {
                            viewModel.addBookmark(newBookmarkTitle, newBookmarkUrl)
                            showAddBookmarkDialog = false
                            newBookmarkTitle = ""
                            newBookmarkUrl = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBookmarkDialog = false }) {
                    Text("Cancel", color = colors.textSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        // Top Search Bar & Controls matching mockup
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(0.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = browserUrlInput,
                        onValueChange = { viewModel.onBrowserUrlChanged(it) },
                        placeholder = { Text("Search or type URL", color = colors.textSecondary, fontSize = 13.sp) },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary
                        ),
                        singleLine = true
                    )

                    if (browserUrlInput.isNotBlank()) {
                        IconButton(
                            onClick = { viewModel.onBrowserUrlChanged("") },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                        }
                    }

                    // Go button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(PrimaryGradient)
                            .clickable {
                                keyboardController?.hide()
                                focusManager.clearFocus()
                                if (browserUrlInput.isNotBlank()) {
                                    viewModel.navigateBrowserTo(browserUrlInput)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Go", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Browser Navigation toolbar (when in active webpage mode)
                if (activeUrl != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row {
                            IconButton(
                                onClick = { webViewInstance?.let { if (it.canGoBack()) it.goBack() } },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { webViewInstance?.let { if (it.canGoForward()) it.goForward() } },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { webViewInstance?.reload() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                            }
                            IconButton(
                                onClick = { viewModel.activeBrowserUrl.value = null },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(Icons.Default.Home, contentDescription = "Home", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Bookmark current page
                        IconButton(
                            onClick = {
                                activeUrl?.let {
                                    viewModel.addBookmark(webViewInstance?.title ?: "Web Page", it)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.BookmarkAdd, contentDescription = "Add Bookmark", tint = AccentCyan, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }

        if (isWebLoading && activeUrl != null) {
            LinearProgressIndicator(
                progress = { webLoadingProgress },
                modifier = Modifier.fillMaxWidth().height(3.dp),
                color = AccentPink,
                trackColor = Color.Transparent
            )
        }

        // Main Browser Content: Home Hub vs Active WebView
        if (activeUrl == null) {
            // Browser Home Hub (Popular Sites + Bookmarks)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Popular Sites Header
                Text(
                    text = "Popular Sites",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Popular Sites Grid
                val chunked = SupportedPlatformsList.chunked(4)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (row in chunked) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (platform in row) {
                                BrowserPlatformItem(
                                    platform = platform,
                                    onClick = {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        viewModel.navigateBrowserTo(platform.homeUrl)
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (row.size < 4) {
                                for (i in 0 until (4 - row.size)) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Bookmarks Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Bookmarks",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    IconButton(
                        onClick = { showAddBookmarkDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Bookmark", tint = PrimaryPurple)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Bookmarks List
                if (bookmarks.isEmpty()) {
                    Text("No bookmarks added yet", color = colors.textSecondary, fontSize = 12.sp)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (bookmark in bookmarks) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        keyboardController?.hide()
                                        focusManager.clearFocus()
                                        viewModel.navigateBrowserTo(bookmark.url)
                                    },
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bookmark, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(bookmark.title, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                        Text(bookmark.url, color = colors.textSecondary, fontSize = 11.sp)
                                    }
                                    IconButton(
                                        onClick = { viewModel.removeBookmark(bookmark.id) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = colors.textSecondary, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Live Interactive WebView with floating video detector
            Box(modifier = Modifier.fillMaxSize()) {
                if (webErrorMessage != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Webpage Notice",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = webErrorMessage ?: "Webpage rendering reset.",
                            fontSize = 13.sp,
                            color = colors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                webErrorMessage = null
                                webViewInstance?.reload() ?: activeUrl?.let { viewModel.navigateBrowserTo(it) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryPurple)
                        ) {
                            Text("Reload Page", color = Color.White)
                        }
                    }
                } else {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            WebView(ctx).apply {
                                setLayerType(View.LAYER_TYPE_HARDWARE, null)
                                
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    databaseEnabled = true
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    builtInZoomControls = true
                                    displayZoomControls = false
                                    mediaPlaybackRequiresUserGesture = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                                    userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
                                }
                                CookieManager.getInstance().setAcceptCookie(true)
                                CookieManager.getInstance().setAcceptThirdPartyCookies(this, true)

                                webChromeClient = object : WebChromeClient() {
                                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                        webLoadingProgress = newProgress / 100f
                                        isWebLoading = newProgress < 100
                                    }
                                }

                                webViewClient = object : WebViewClient() {
                                    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                        isWebLoading = true
                                        webErrorMessage = null
                                        url?.let { viewModel.onBrowserUrlChanged(it) }
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isWebLoading = false
                                        url?.let { viewModel.onBrowserUrlChanged(it) }
                                    }

                                    override fun onReceivedError(
                                        view: WebView?,
                                        request: WebResourceRequest?,
                                        error: WebResourceError?
                                    ) {
                                        if (request?.isForMainFrame == true) {
                                            isWebLoading = false
                                            Log.w("BrowserScreen", "WebView main frame error: ${error?.description}")
                                        }
                                    }

                                    // CRITICAL: Handle renderer crash so the app process is not terminated by Android OS
                                    override fun onRenderProcessGone(
                                        view: WebView?,
                                        detail: RenderProcessGoneDetail?
                                    ): Boolean {
                                        val didCrash = detail?.didCrash() ?: true
                                        Log.e("BrowserScreen", "Chromium renderer process gone. didCrash=$didCrash")
                                        try {
                                            view?.let {
                                                (it.parent as? android.view.ViewGroup)?.removeView(it)
                                                it.destroy()
                                            }
                                        } catch (e: Exception) {
                                            Log.e("BrowserScreen", "Cleanup error: ${e.message}")
                                        }
                                        webViewInstance = null
                                        webErrorMessage = "The webpage renderer was reset to save memory. Tap below to reload."
                                        return true // Prevents host application crash
                                    }
                                }

                                loadUrl(activeUrl)
                                webViewInstance = this
                            }
                        },
                        update = { view ->
                            if (view.url != activeUrl) {
                                view.loadUrl(activeUrl)
                            }
                        }
                    )
                }

                // Floating Video Detected Download FAB
                if (detectedVideoUrl != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(20.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(PrimaryGradient)
                            .clickable { onDownloadVideoDetected(detectedVideoUrl) }
                            .padding(horizontal = 20.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Download, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download Video Found", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BrowserPlatformItem(
    platform: PlatformItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = AppTheme.colors
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp)
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(platform.brandColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                com.example.ui.components.PlatformBrandIcon(
                    platformId = platform.id,
                    tint = platform.brandColor,
                    size = 20.dp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = platform.name,
            color = colors.textSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
