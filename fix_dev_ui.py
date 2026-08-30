import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

dev_ui_old = """    if (showDeveloperInfoDialog) {
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
                        fun SocialLink(domain: String, title: String, url: String) {
                            val iconUrl = "https://www.google.com/s2/favicons?sz=128&domain=$domain"
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
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    coil.compose.AsyncImage(
                                        model = iconUrl,
                                        contentDescription = title,
                                        modifier = Modifier.size(22.dp)
                                    )
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

                        SocialLink("anikdesigner.blogspot.com", "Website", "https://anikdesigner.blogspot.com/")
                        SocialLink("behance.net", "Behance", "https://www.behance.net/mmanik")
                        SocialLink("pikbest.com", "Pikbest", "https://pikbest.com/designers/125135.html")
                        SocialLink("upwork.com", "Upwork", "https://www.upwork.com/freelancers/~01bcd1b585e4c44189")
                        SocialLink("linkedin.com", "LinkedIn", "https://bd.linkedin.com/in/m-m-anik")
                        SocialLink("facebook.com", "Facebook", "https://www.facebook.com/M.M.Anik.02")
                        SocialLink("youtube.com", "YouTube", "https://youtube.com/@m_m_anik")
                        SocialLink("instagram.com", "Instagram", "https://www.instagram.com/m_m_anik_/")
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
    }"""

dev_ui_new = """    if (showDeveloperInfoDialog) {
        val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
        androidx.compose.ui.window.Dialog(onDismissRequest = { showDeveloperInfoDialog = false }) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF141A20)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                                    colors = listOf(colors.primary.copy(alpha = 0.6f), Color.Transparent),
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
                                        .background(Color(0xFF1B242C))
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
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                androidx.compose.material3.Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Default.CheckCircle,
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
                                color = Color.White.copy(alpha = 0.6f), 
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
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B242C)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
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
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.height(1.dp))
                                        Text(
                                            text = subtitle, 
                                            fontSize = 10.sp, 
                                            color = Color.White.copy(alpha = 0.5f), 
                                            maxLines = 1, 
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                    
                                    // Open icon
                                    androidx.compose.material3.Icon(
                                        androidx.compose.material.icons.Icons.Default.OpenInNew,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.4f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                            }
                        }

                        // Row 1
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SocialLinkCard("anikdesigner.blogspot.com", "Website", "Visit my personal website", "https://anikdesigner.blogspot.com/", Modifier.weight(1f))
                            SocialLinkCard("behance.net", "Behance", "Explore my creative work", "https://www.behance.net/mmanik", Modifier.weight(1f))
                        }
                        // Row 2
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SocialLinkCard("pikbest.com", "Pikbest", "Design resources I use", "https://pikbest.com/designers/125135.html", Modifier.weight(1f))
                            SocialLinkCard("upwork.com", "Upwork", "Let's work together", "https://www.upwork.com/freelancers/~01bcd1b585e4c44189", Modifier.weight(1f))
                        }
                        // Row 3
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SocialLinkCard("linkedin.com", "LinkedIn", "Connect with me", "https://bd.linkedin.com/in/m-m-anik", Modifier.weight(1f))
                            SocialLinkCard("facebook.com", "Facebook", "Follow me on Facebook", "https://www.facebook.com/M.M.Anik.02", Modifier.weight(1f))
                        }
                        // Row 4
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SocialLinkCard("youtube.com", "YouTube", "Watch my tutorials", "https://youtube.com/@m_m_anik", Modifier.weight(1f))
                            SocialLinkCard("instagram.com", "Instagram", "Behind the scenes", "https://www.instagram.com/m_m_anik_/", Modifier.weight(1f))
                        }
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
                                        imageVector = androidx.compose.material.icons.Icons.Default.Close,
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
    }"""

if "import androidx.compose.material.icons.filled.CheckCircle" not in content:
    content = content.replace("import androidx.compose.material.icons.filled.Close", "import androidx.compose.material.icons.filled.Close\nimport androidx.compose.material.icons.filled.CheckCircle")

content = content.replace(dev_ui_old, dev_ui_new)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
