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
import androidx.compose.material.icons.filled.Security
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
fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
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
                        Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Privacy Policy", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "1. Zero Data Collection: Download Master does not track your downloads, search history, or personal identifiers. All downloading logic runs directly on your local device.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "2. No External Servers: Videos and audio streams are directly transferred between your device and the media host. We do not proxy or store files on intermediate servers.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "3. Storage & Permissions: Storage access is used solely to write downloaded media files into your public Movies/Music directories. Notification permission is used strictly to display download speed and progress.",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
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
                    Text("I Understand", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
