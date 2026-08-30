import re

with open('app/src/main/java/com/example/MainActivity.kt', 'r') as f:
    content = f.read()

# I will add AlertDialog code to MainActivity.kt inside DownloadMasterApp

state_code = '''    var showCookieDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }'''

new_state_code = '''    var showCookieDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    
    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
    var hasAcceptedWarning by remember { mutableStateOf(prefs.getBoolean("has_accepted_warning", false)) }'''

content = content.replace(state_code, new_state_code)

modal_code = '''    // Engine Update Dialog'''

new_modal_code = '''    if (!hasAcceptedWarning) {
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

    // Engine Update Dialog'''

content = content.replace(modal_code, new_modal_code)

with open('app/src/main/java/com/example/MainActivity.kt', 'w') as f:
    f.write(content)
