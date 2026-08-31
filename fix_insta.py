with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

insta_old = """            isInstagram -> listOf(
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1")
                }
            )"""

insta_new = """            isInstagram -> listOf(
                { _ -> /* default yt-dlp */ },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1")
                }
            )"""

content = content.replace(insta_old, insta_new)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
