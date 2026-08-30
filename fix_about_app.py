import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace About Us
about_us_old = '''                // About Us
                SettingClickableItem(
                    icon = Icons.Default.Info,
                    title = "About Us",
                    subtitle = "Download Master 2.4.0 info & open-source licenses",
                    onClick = { showAboutDialog = true }
                )'''

about_us_new = '''                // About App
                val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                val versionName = packageInfo.versionName ?: "1.0.0"
                SettingClickableItem(
                    icon = Icons.Default.Info,
                    title = "About app",
                    subtitle = "Download Master $versionName info & open-source licenses",
                    onClick = { showAboutDialog = true }
                )'''

content = content.replace(about_us_old, about_us_new)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)

with open('app/src/main/java/com/example/ui/components/AboutDialog.kt', 'r') as f:
    about_content = f.read()

about_old_1 = '''Text("Version 2.4.0 (Latest Release)", fontSize = 12.sp, color = PrimaryPurple)'''
about_new_1 = '''val context = androidx.compose.ui.platform.LocalContext.current
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val versionName = packageInfo.versionName ?: "1.0.0"
                    Text("Version $versionName (Latest Release)", fontSize = 12.sp, color = PrimaryPurple)'''

about_content = about_content.replace(about_old_1, about_new_1)
about_content = about_content.replace('"About Download Master"', '"About app"')
about_content = about_content.replace('About Us', 'About app')

with open('app/src/main/java/com/example/ui/components/AboutDialog.kt', 'w') as f:
    f.write(about_content)

