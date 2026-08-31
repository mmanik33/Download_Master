import re

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'r') as f:
    content = f.read()

# Replace rows with single column of cards
old_links = """                        // Row 1
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
                        }"""

new_links = """                        SocialLinkCard("anikdesigner.blogspot.com", "Website", "Visit my personal website", "https://anikdesigner.blogspot.com/", Modifier.fillMaxWidth())
                        SocialLinkCard("behance.net", "Behance", "Explore my creative work", "https://www.behance.net/mmanik", Modifier.fillMaxWidth())
                        SocialLinkCard("pikbest.com", "Pikbest", "Design resources I use", "https://pikbest.com/designers/125135.html", Modifier.fillMaxWidth())
                        SocialLinkCard("upwork.com", "Upwork", "Let's work together", "https://www.upwork.com/freelancers/~01bcd1b585e4c44189", Modifier.fillMaxWidth())
                        SocialLinkCard("linkedin.com", "LinkedIn", "Connect with me", "https://bd.linkedin.com/in/m-m-anik", Modifier.fillMaxWidth())
                        SocialLinkCard("facebook.com", "Facebook", "Follow me on Facebook", "https://www.facebook.com/M.M.Anik.02", Modifier.fillMaxWidth())
                        SocialLinkCard("youtube.com", "YouTube", "Watch my tutorials", "https://youtube.com/@m_m_anik", Modifier.fillMaxWidth())
                        SocialLinkCard("instagram.com", "Instagram", "Behind the scenes", "https://www.instagram.com/m_m_anik_/", Modifier.fillMaxWidth())"""

content = content.replace(old_links, new_links)

with open('app/src/main/java/com/example/ui/screens/SettingsScreen.kt', 'w') as f:
    f.write(content)
