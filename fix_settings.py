import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Add state variable
state_var = '    var showDeveloperInfoDialog by remember { mutableStateOf(false) }'
content = content.replace('    val context = LocalContext.current', f'    val context = LocalContext.current\n{state_var}')

# 2. Add Developer Info Dialog (at the bottom of SettingsScreen, or right before the closing brace of SettingsScreen)
# Wait, it's better to add the dialog next to `showQualityPrefDialog` logic
dialog_code = '''
    if (showDeveloperInfoDialog) {
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        AlertDialog(
            onDismissRequest = { showDeveloperInfoDialog = false },
            containerColor = colors.surface,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PrimaryPurple.copy(alpha = 0.1f)),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Default.Person, 
                            contentDescription = null, 
                            tint = PrimaryPurple, 
                            modifier = Modifier.size(40.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "M. M. Anik", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                    Text(text = "UX/UI Designer & Developer", fontSize = 14.sp, color = colors.textSecondary)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val linkColors = colors.textPrimary
                    
                    @Composable
                    fun SocialLink(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, url: String) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { uriHandler.openUri(url) }
                                .padding(12.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            androidx.compose.material3.Icon(icon, contentDescription = title, tint = PrimaryPurple, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = linkColors)
                        }
                    }

                    SocialLink(androidx.compose.material.icons.Icons.Default.Language, "Website", "https://anikdesigner.blogspot.com/")
                    SocialLink(androidx.compose.material.icons.Icons.Default.Palette, "Behance", "https://www.behance.net/mmanik")
                    SocialLink(androidx.compose.material.icons.Icons.Default.Image, "Pikbest", "https://pikbest.com/designers/125135.html")
                    SocialLink(androidx.compose.material.icons.Icons.Default.Work, "Upwork", "https://www.upwork.com/freelancers/~01bcd1b585e4c44189")
                    SocialLink(androidx.compose.material.icons.Icons.Default.BusinessCenter, "LinkedIn", "https://bd.linkedin.com/in/m-m-anik")
                    SocialLink(androidx.compose.material.icons.Icons.Default.Group, "Facebook", "https://www.facebook.com/M.M.Anik.02")
                    SocialLink(androidx.compose.material.icons.Icons.Default.PlayArrow, "YouTube", "https://youtube.com/@m_m_anik")
                    SocialLink(androidx.compose.material.icons.Icons.Default.CameraAlt, "Instagram", "https://www.instagram.com/m_m_anik_/")
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { showDeveloperInfoDialog = false }) {
                    Text("Close", color = PrimaryPurple)
                }
            }
        )
    }
'''
content = content.replace('    if (showConcurrentDialog) {', dialog_code + '\n    if (showConcurrentDialog) {')

# 3. Replace the old Developer Info and Social Links block
start_str = '                // Developer Info'
end_str = '                // Share App'

idx_start = content.find(start_str)
idx_end = content.find(end_str, idx_start)

if idx_start != -1 and idx_end != -1:
    new_dev_item = '''                // Developer Info
                SettingClickableItem(
                    icon = Icons.Default.Info,
                    title = "Developer Info",
                    subtitle = "M. M. Anik • Portfolios & Socials",
                    onClick = { showDeveloperInfoDialog = true }
                )

'''
    content = content[:idx_start] + new_dev_item + content[idx_end:]

# 4. Add missing imports if needed
if "androidx.compose.material.icons.filled.BusinessCenter" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.AccountCircle", 
        "import androidx.compose.material.icons.filled.AccountCircle\nimport androidx.compose.material.icons.filled.BusinessCenter\nimport androidx.compose.material.icons.filled.Group\nimport androidx.compose.material.icons.filled.Language\nimport androidx.compose.material.icons.filled.PlayArrow\nimport androidx.compose.material.icons.filled.CameraAlt\nimport androidx.compose.material.icons.filled.Person\nimport androidx.compose.material.icons.filled.Image\nimport androidx.compose.material.icons.filled.Work")

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
