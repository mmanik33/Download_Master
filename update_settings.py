import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove Default Quality
default_quality_pattern = r'// Default Quality\s*SettingClickableItem\(\s*icon = Icons\.Default\.HighQuality,\s*title = "Default Quality",\s*subtitle = defaultQuality,\s*onClick = \{ showQualityPrefDialog = true \}\s*\)'
content = re.sub(default_quality_pattern, '', content)

# 2. Remove Rate Us
rate_us_pattern = r'// Rate Us\s*SettingClickableItem\(\s*icon = Icons\.Default\.RateReview,\s*title = "Rate Us",\s*subtitle = "Support Download Master with 5 stars",\s*onClick = \{[\s\S]*?\}\s*\)'
content = re.sub(rate_us_pattern, '', content)

# 3. Add Developer Info above Share App
developer_info_code = '''
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
'''
content = content.replace('// Share App', developer_info_code + '\n                // Share App')

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
