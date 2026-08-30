import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# 1. Remove Download Location
download_location_pattern = r'// Download Location\s*SettingClickableItem\(\s*icon = Icons\.Default\.Folder,\s*title = "Download Location",\s*subtitle = downloadLocation,\s*onClick = \{[\s\S]*?\}\s*\)'
content = re.sub(download_location_pattern, '', content)

# 2. Modernize Dynamic Color Theme Palettes
# I'll replace the block from '// Dynamic Color Theme Palettes' up to the end of its Column/Row.
theme_block_start = content.find('// Dynamic Color Theme Palettes')
theme_block_end = content.find('// SECTION: General')
if theme_block_start != -1 and theme_block_end != -1:
    # Just to be safe, I'll extract everything in between and replace it.
    original_theme_block = content[theme_block_start:theme_block_end]
    
    modern_theme_block = '''// Dynamic Color Theme Palettes
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Palette, contentDescription = null, tint = colors.primary, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Accent Color",
                            color = colors.textPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Personalize your app's theme",
                            color = colors.textSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Selection Swatches
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ColorThemePreset.values().forEach { preset ->
                        val isSelected = colorPalette == preset.id
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .clickable { viewModel.setColorPalette(preset.id) }
                                .background(
                                    Brush.linearGradient(
                                        listOf(preset.primary, preset.secondary)
                                    )
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) colors.surface else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isSelected) 4.dp else 0.dp,
                                    color = if (isSelected) preset.primary else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        '''
    content = content.replace(original_theme_block, modern_theme_block)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
