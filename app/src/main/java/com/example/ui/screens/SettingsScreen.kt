package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainViewModel
import com.example.ui.components.AboutDialog
import com.example.ui.components.DayNightToggleSwitch
import com.example.ui.components.PrivacyPolicyDialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.ColorThemePreset

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    ytDlpVersion: String,
    embedThumbnail: Boolean,
    embedMetadata: Boolean,
    useAria2c: Boolean,
    useWifiOnly: Boolean,
    maxConcurrent: Int,
    defaultQuality: String,
    downloadLocation: String,
    onOpenCookieManager: () -> Unit,
    onCheckEngineUpdate: () -> Unit
) {
    val context = LocalContext.current
    var showDeveloperInfoDialog by remember { mutableStateOf(false) }
    val colors = AppTheme.colors
    val themeMode by viewModel.themeMode.collectAsState()
    val colorPalette by viewModel.colorPalette.collectAsState()

    var showConcurrentDialog by remember { mutableStateOf(false) }
    var showQualityPrefDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(ytDlpVersion = ytDlpVersion, onDismiss = { showAboutDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    if (showDisclaimerDialog) {
        DisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    }


    if (showDeveloperInfoDialog) {
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDeveloperInfoDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.85f),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = if (colors.isDark) Color(0xFF141A20) else colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Area (Gradient background with profile)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(colors.primary.copy(alpha = if (colors.isDark) 0.6f else 0.3f), Color.Transparent),
                                    startY = 0f,
                                    endY = 500f
                                )
                            )
                            .padding(top = 32.dp, bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Profile Image (User Icon) with glow
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(colors.primary.copy(alpha = 0.4f), Color.Transparent),
                                        ),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(if (colors.isDark) Color(0xFF1B242C) else colors.surface)
                                        .border(2.dp, colors.primary.copy(alpha = 0.8f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Icon(
                                        androidx.compose.material.icons.Icons.Default.Person,
                                        contentDescription = null,
                                        tint = colors.primary,
                                        modifier = Modifier.size(36.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Name & Verified Badge
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "M. M. Anik", 
                                    fontSize = 24.sp, 
                                    fontWeight = FontWeight.ExtraBold, 
                                    color = if (colors.isDark) Color.White else colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Verified",
                                    tint = colors.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Subtitle
                            Text(
                                text = "UX/UI Designer & Developer", 
                                fontSize = 14.sp, 
                                color = if (colors.isDark) Color.White.copy(alpha = 0.6f) else colors.textSecondary, 
                                fontWeight = FontWeight.Medium
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Small decorative line
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(colors.primary)
                            )
                        }
                    }

                    // Links Grid
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        @Composable
                        fun SocialLinkCard(
                            domain: String, 
                            title: String, 
                            subtitle: String, 
                            url: String, 
                            modifier: Modifier = Modifier
                        ) {
                            val iconUrl = "https://www.google.com/s2/favicons?sz=128&domain=$domain"
                            Card(
                                modifier = modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .clickable { uriHandler.openUri(url) },
                                colors = CardDefaults.cardColors(containerColor = if (colors.isDark) Color(0xFF1B242C) else colors.surfaceVariant),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (colors.isDark) Color.White.copy(alpha = 0.05f) else colors.textSecondary.copy(alpha = 0.1f))
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().height(64.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Left accent bar
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .height(36.dp)
                                            .background(
                                                colors.primary, 
                                                shape = RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp)
                                            )
                                    )
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                    
                                    // Icon Box
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        coil.compose.AsyncImage(
                                            model = iconUrl,
                                            contentDescription = title,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                    
                                    // Texts
                                    Column(
                                        modifier = Modifier.weight(1f), 
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = title, 
                                            fontSize = 14.sp, 
                                            fontWeight = FontWeight.SemiBold, 
                                            color = if (colors.isDark) Color.White else colors.textPrimary
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = subtitle, 
                                            fontSize = 10.sp, 
                                            color = if (colors.isDark) Color.White.copy(alpha = 0.5f) else colors.textSecondary.copy(alpha = 0.8f), 
                                            maxLines = 1, 
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    // Open icon
                                    androidx.compose.material3.Icon(
                                        androidx.compose.material.icons.Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = if (colors.isDark) Color.White.copy(alpha = 0.4f) else colors.textSecondary.copy(alpha = 0.5f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                            }
                        }

                        SocialLinkCard("anikdesigner.blogspot.com", "Website", "Visit my personal website", "https://anikdesigner.blogspot.com/", Modifier.fillMaxWidth())
                        SocialLinkCard("behance.net", "Behance", "Explore my creative work", "https://www.behance.net/mmanik", Modifier.fillMaxWidth())
                        SocialLinkCard("pikbest.com", "Pikbest", "Design resources I use", "https://pikbest.com/designers/125135.html", Modifier.fillMaxWidth())
                        SocialLinkCard("upwork.com", "Upwork", "Let's work together", "https://www.upwork.com/freelancers/~01bcd1b585e4c44189", Modifier.fillMaxWidth())
                        SocialLinkCard("linkedin.com", "LinkedIn", "Connect with me", "https://bd.linkedin.com/in/m-m-anik", Modifier.fillMaxWidth())
                        SocialLinkCard("facebook.com", "Facebook", "Follow me on Facebook", "https://www.facebook.com/M.M.Anik.02", Modifier.fillMaxWidth())
                        SocialLinkCard("youtube.com", "YouTube", "Watch my tutorials", "https://youtube.com/@m_m_anik", Modifier.fillMaxWidth())
                        SocialLinkCard("instagram.com", "Instagram", "Behind the scenes", "https://www.instagram.com/m_m_anik_/", Modifier.fillMaxWidth())
                    }
                    
                    // Close Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Button(
                            onClick = { showDeveloperInfoDialog = false },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(colors.primary, colors.primary.copy(alpha = 0.6f))
                                        ),
                                        shape = RoundedCornerShape(24.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    androidx.compose.material3.Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showConcurrentDialog) {
        AlertDialog(
            onDismissRequest = { showConcurrentDialog = false },
            containerColor = colors.surface,
            title = { Text("Simultaneous Downloads", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf(1, 2, 3, 5).forEach { count ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setMaxConcurrentDownloads(count)
                                    showConcurrentDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = maxConcurrent == count,
                                onClick = {
                                    viewModel.setMaxConcurrentDownloads(count)
                                    showConcurrentDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$count downloads at the same time", color = colors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showConcurrentDialog = false }) {
                    Text("Done", color = colors.primary)
                }
            }
        )
    }

    if (showQualityPrefDialog) {
        AlertDialog(
            onDismissRequest = { showQualityPrefDialog = false },
            containerColor = colors.surface,
            title = { Text("Default Video Quality", color = colors.textPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    listOf("Ask Each Time", "Highest Quality (4K/1080p)", "720p HD", "Audio Only (MP3)").forEach { q ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setDefaultQualityPreference(q)
                                    showQualityPrefDialog = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = defaultQuality == q,
                                onClick = {
                                    viewModel.setDefaultQualityPreference(q)
                                    showQualityPrefDialog = false
                                },
                                colors = RadioButtonDefaults.colors(selectedColor = colors.primary)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(q, color = colors.textPrimary)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showQualityPrefDialog = false }) {
                    Text("Done", color = colors.primary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Settings",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION: Appearance & Theme
        Text(
            text = "Appearance & Theme",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Day/Night Toggle Switch Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Theme Mode",
                            color = colors.textPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (colors.isDark) "Currently in Dark Mode" else "Currently in Light Mode",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }

                    DayNightToggleSwitch(
                        isDark = colors.isDark,
                        onToggle = { isDark ->
                            viewModel.setThemeMode(if (isDark) "DARK" else "LIGHT")
                        },
                        switchWidth = 74.dp,
                        switchHeight = 36.dp,
                        showLabels = true
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Dynamic Color Theme Palettes
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accent Color",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Personalize your app's theme",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Selection Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorThemePreset.values().forEach { preset ->
                        val isSelected = colorPalette == preset.id
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.setColorPalette(preset.id) }
                                .background(
                                    Brush.linearGradient(
                                        listOf(preset.primary, preset.secondary)
                                    )
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) colors.surface else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = if (isSelected) preset.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: General
        Text(
            text = "General",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                

                // Simultaneous Downloads
                SettingClickableItem(
                    icon = Icons.Default.NetworkCheck,
                    title = "Simultaneous Downloads",
                    subtitle = "$maxConcurrent downloads at once",
                    onClick = { showConcurrentDialog = true }
                )

                

                // Wi-Fi Only Toggle
                SettingSwitchItem(
                    icon = Icons.Default.Wifi,
                    title = "Download on Wi-Fi only",
                    subtitle = "Save mobile internet data",
                    checked = useWifiOnly,
                    onCheckedChange = { viewModel.setUseWifiOnly(it) }
                )

                // Turbo Speed Booster (Simplified from Aria2c)
                SettingSwitchItem(
                    icon = Icons.Default.Speed,
                    title = "Turbo Download Speed",
                    subtitle = "Multi-connection booster for maximum speed",
                    checked = useAria2c,
                    onCheckedChange = { viewModel.setUseAria2c(it) }
                )

                // Cover Art & Audio Info (Simplified from Embed Thumbnail & Metadata)
                SettingSwitchItem(
                    icon = Icons.Default.Photo,
                    title = "Save Video Cover & Audio Info",
                    subtitle = "Attach thumbnail & song info to downloaded files",
                    checked = embedThumbnail,
                    onCheckedChange = {
                        viewModel.setEmbedThumbnail(it)
                        viewModel.setEmbedMetadata(it)
                    }
                )

                // Account Login & Access
                SettingClickableItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Account Login & Access",
                    subtitle = "Unlock member-only and age-restricted videos",
                    onClick = onOpenCookieManager
                )

                // Downloader Core Engine
                SettingClickableItem(
                    icon = Icons.Default.Sync,
                    title = "Downloader Core Engine",
                    subtitle = "Version: $ytDlpVersion • Tap to check for updates",
                    onClick = onCheckEngineUpdate
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Other
        Text(
            text = "Other",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = colors.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 2.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                

                
                // Developer Info
                SettingClickableItem(
                    icon = Icons.Default.Info,
                    title = "Developer Info",
                    subtitle = "M. M. Anik • Portfolios & Socials",
                    onClick = { showDeveloperInfoDialog = true }
                )

                // Share App
                SettingClickableItem(
                    icon = Icons.Default.Share,
                    title = "Share App",
                    subtitle = "Share Download Master with friends",
                    onClick = {
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Download Master - 1000+ Websites Video & Audio Downloader: https://github.com")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Download Master"))
                    }
                )

                // Disclaimer
                SettingClickableItem(
                    icon = Icons.Default.Warning,
                    title = "Disclaimer",
                    subtitle = "Important notice regarding app usage",
                    onClick = { showDisclaimerDialog = true }
                )

                // Privacy Policy
                SettingClickableItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "100% on-device processing & zero tracking",
                    onClick = { showPrivacyDialog = true }
                )

                // About App
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionName = packageInfo.versionName ?: "1.0.0"
                SettingClickableItem(
                    icon = Icons.Default.Info,
                    title = "About app",
                    subtitle = "Download Master $versionName info & open-source licenses",
                    onClick = { showAboutDialog = true }
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun SettingClickableItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = colors.textSecondary, fontSize = 11.sp)
        }
        Text(text = ">", color = colors.textSecondary, fontSize = 14.sp)
    }
}

@Composable
private fun SettingSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = colors.primary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, color = colors.textPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(text = subtitle, color = colors.textSecondary, fontSize = 11.sp)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = colors.primary,
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.surfaceVariant
            )
        )
    }
}


@Composable
fun DisclaimerDialog(onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Disclaimer",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "If anyone downloads any immoral or sinful content using this app, the burden of that sin rests solely on the user; the developer shall bear no responsibility. However, if the app is used for righteous or rewarding purposes, a portion of that reward (Sawab) will be credited to the developer.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("I Understand", color = colors.primary, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.surface,
        shape = RoundedCornerShape(24.dp)
    )
}
