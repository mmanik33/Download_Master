import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

old_modal = '''    if (showDeveloperInfoDialog) {
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
    }'''

new_modal = '''    if (showDeveloperInfoDialog) {
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDeveloperInfoDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Header Banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(colors.primary, colors.primary.copy(alpha = 0.5f))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .padding(3.dp)
                                .clip(CircleShape)
                                .background(colors.surface),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                androidx.compose.material.icons.Icons.Default.Person,
                                contentDescription = null,
                                tint = colors.primary,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "M. M. Anik", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = colors.textPrimary)
                        Text(text = "UX/UI Designer & Developer", fontSize = 14.sp, color = colors.textSecondary, fontWeight = FontWeight.Medium)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    androidx.compose.material3.HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        @Composable
                        fun SocialLink(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, url: String) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(colors.surfaceVariant.copy(alpha = 0.5f))
                                    .clickable { uriHandler.openUri(url) }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(colors.primary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Icon(icon, contentDescription = title, tint = colors.primary, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                                Spacer(modifier = Modifier.weight(1f))
                                androidx.compose.material3.Icon(
                                    androidx.compose.material.icons.Icons.Default.OpenInNew, 
                                    contentDescription = null, 
                                    tint = colors.textSecondary, 
                                    modifier = Modifier.size(16.dp)
                                )
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
                    
                    androidx.compose.material3.HorizontalDivider(color = colors.textSecondary.copy(alpha = 0.1f))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.material3.Button(
                            onClick = { showDeveloperInfoDialog = false },
                            modifier = Modifier.fillMaxWidth(),
                            colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = colors.primary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Close", color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }'''

content = content.replace(old_modal, new_modal)

if "import androidx.compose.material.icons.filled.OpenInNew" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Palette", "import androidx.compose.material.icons.filled.Palette\nimport androidx.compose.material.icons.filled.OpenInNew")

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
