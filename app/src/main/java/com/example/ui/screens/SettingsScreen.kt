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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Palette
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
    val colors = AppTheme.colors
    val themeMode by viewModel.themeMode.collectAsState()
    val colorPalette by viewModel.colorPalette.collectAsState()

    var showConcurrentDialog by remember { mutableStateOf(false) }
    var showQualityPrefDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    if (showAboutDialog) {
        AboutDialog(ytDlpVersion = ytDlpVersion, onDismiss = { showAboutDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
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
                Text(
                    text = "Dynamic Color & Accent",
                    color = colors.textPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Tap to change the app's dynamic color buttons & highlights",
                    color = colors.textSecondary,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Color Selection Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorThemePreset.values().forEach { preset ->
                        val isSelected = colorPalette == preset.id
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setColorPalette(preset.id) }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(preset.primary, preset.secondary)
                                        )
                                    )
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) Color.White else Color.Transparent,
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

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = preset.label.split(" ").first(),
                                fontSize = 10.sp,
                                color = if (isSelected) colors.primary else colors.textSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
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
                // Download Location
                SettingClickableItem(
                    icon = Icons.Default.Folder,
                    title = "Download Location",
                    subtitle = downloadLocation,
                    onClick = {
                        Toast.makeText(context, "Downloads saved to: $downloadLocation", Toast.LENGTH_SHORT).show()
                    }
                )

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
                    subtitle = "M. M. Anik • anikdesigner.blogspot.com",
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://anikdesigner.blogspot.com/"))
                        context.startActivity(intent)
                    }
                )

                // Social Links & Portfolios
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    
                    val linkColors = colors.primary
                    androidx.compose.foundation.lazy.LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        item {
                            androidx.compose.material3.Text(
                                text = "Behance", 
                                color = linkColors,
                                modifier = Modifier.clickable { uriHandler.openUri("https://www.behance.net/mmanik") }
                            )
                        }
                        item {
                            androidx.compose.material3.Text(
                                text = "Pikbest", 
                                color = linkColors,
                                modifier = Modifier.clickable { uriHandler.openUri("https://pikbest.com/designers/125135.html") }
                            )
                        }
                        item {
                            androidx.compose.material3.Text(
                                text = "Upwork", 
                                color = linkColors,
                                modifier = Modifier.clickable { uriHandler.openUri("https://www.upwork.com/freelancers/~01bcd1b585e4c44189") }
                            )
                        }
                        item {
                            androidx.compose.material3.Text(
                                text = "LinkedIn", 
                                color = linkColors,
                                modifier = Modifier.clickable { uriHandler.openUri("https://bd.linkedin.com/in/m-m-anik") }
                            )
                        }
                        item {
                            androidx.compose.material3.Text(
                                text = "Facebook", 
                                color = linkColors,
                                modifier = Modifier.clickable { uriHandler.openUri("https://www.facebook.com/M.M.Anik.02") }
                            )
                        }
                        item {
                            androidx.compose.material3.Text(
                                text = "YouTube", 
                                color = linkColors,
                                modifier = Modifier.clickable { uriHandler.openUri("https://youtube.com/@m_m_anik") }
                            )
                        }
                        item {
                            androidx.compose.material3.Text(
                                text = "Instagram", 
                                color = linkColors,
                                modifier = Modifier.clickable { uriHandler.openUri("https://www.instagram.com/m_m_anik_/") }
                            )
                        }
                    }
                }

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

                // Privacy Policy
                SettingClickableItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "100% on-device processing & zero tracking",
                    onClick = { showPrivacyDialog = true }
                )

                // About Us
                SettingClickableItem(
                    icon = Icons.Default.Info,
                    title = "About Us",
                    subtitle = "Download Master 2.4.0 info & open-source licenses",
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

