import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

old_state = """    var showPrivacyDialog by remember { mutableStateOf(false) }"""
new_state = """    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }"""
content = content.replace(old_state, new_state)

old_dialog = """    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }"""
new_dialog = """    if (showPrivacyDialog) {
        PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
    }
    if (showDisclaimerDialog) {
        DisclaimerDialog(onDismiss = { showDisclaimerDialog = false })
    }"""
content = content.replace(old_dialog, new_dialog)

old_item = """                // Privacy Policy
                SettingClickableItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "100% on-device processing & zero tracking",
                    onClick = { showPrivacyDialog = true }
                )"""
new_item = """                // Disclaimer
                SettingClickableItem(
                    icon = androidx.compose.material.icons.Icons.Default.Warning,
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
                )"""
content = content.replace(old_item, new_item)

dialog_code = """
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
"""

content = content + dialog_code

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
