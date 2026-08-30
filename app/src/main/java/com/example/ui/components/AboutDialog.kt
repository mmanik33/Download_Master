package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AppTheme
import com.example.ui.theme.PrimaryGradient
import com.example.ui.theme.PrimaryPurple

@Composable
fun AboutDialog(
    ytDlpVersion: String,
    onDismiss: () -> Unit
) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = if (colors.isDark) 0.dp else 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("About app", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.surfaceVariant)
                        .padding(14.dp)
                ) {
                    Text("Download Master", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    val context = androidx.compose.ui.platform.LocalContext.current
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val versionName = packageInfo.versionName ?: "1.0.0"
                    Text("Version $versionName (Latest Release)", fontSize = 12.sp, color = PrimaryPurple)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Embedded yt-dlp Engine: $ytDlpVersion", fontSize = 12.sp, color = colors.textSecondary)
                    Text("• Embedded FFmpeg 6.0 (Hardware-accelerated muxing)", fontSize = 12.sp, color = colors.textSecondary)
                    Text("• Aria2c Engine (16-thread connection booster)", fontSize = 12.sp, color = colors.textSecondary)
                    Text("• Jetpack Compose & Material 3 UI", fontSize = 12.sp, color = colors.textSecondary)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Download Master allows you to save videos and audio streams on your device for offline consumption, backup, and personal archiving. 1000+ streaming sites supported.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Open-source core powered by yt-dlp, FFmpeg, and aria2 under Unlicense/GPL.",
                    fontSize = 11.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .clip(RoundedCornerShape(23.dp))
                        .background(PrimaryGradient),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                ) {
                    Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
