import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

old_social_link = '''                        @Composable
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
                        SocialLink(androidx.compose.material.icons.Icons.Default.CameraAlt, "Instagram", "https://www.instagram.com/m_m_anik_/")'''

new_social_link = '''                        @Composable
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
                        SocialLink("instagram.com", "Instagram", "https://www.instagram.com/m_m_anik_/")'''

content = content.replace(old_social_link, new_social_link)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)

