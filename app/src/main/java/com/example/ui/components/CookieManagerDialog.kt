package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cookie
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun CookieManagerDialog(
    initialCookies: String,
    onSaveCookies: (String) -> Unit,
    onClearCookies: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var cookiesText by remember { mutableStateOf(initialCookies) }
    var showLoginWebView by remember { mutableStateOf(false) }

    if (showLoginWebView) {
        YouTubeLoginDialog(
            onCookiesExtracted = { extractedCookies ->
                cookiesText = extractedCookies
                onSaveCookies(extractedCookies)
                showLoginWebView = false
            },
            onDismiss = { showLoginWebView = false }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Cookie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cookie Settings")
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Bypass bot challenges & sign-in requirements for YouTube and restricted videos:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))

                FilledTonalButton(
                    onClick = { showLoginWebView = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("in_app_login_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sign In to YouTube (In-App)")
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Or paste Netscape cookies.txt manually:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = cookiesText,
                    onValueChange = { cookiesText = it },
                    label = { Text("cookies.txt content") },
                    placeholder = { Text("# Netscape HTTP Cookie File\n.youtube.com\tTRUE\t/\tTRUE\t...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("cookies_text_field"),
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveCookies(cookiesText)
                    onDismiss()
                },
                modifier = Modifier.testTag("save_cookies_button")
            ) {
                Text("Save Cookies")
            }
        },
        dismissButton = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (initialCookies.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onClearCookies()
                            cookiesText = ""
                            onDismiss()
                        },
                        modifier = Modifier.testTag("clear_cookies_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Cookies",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        },
        modifier = modifier.testTag("cookie_manager_dialog")
    )
}
