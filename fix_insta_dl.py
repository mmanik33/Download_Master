import re

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# We need to insert isInstagram strategies into downloadMedia
# Currently it is:
#         val strategies: List<(YoutubeDLRequest) -> Unit> = when {
#             isTikTok -> listOf(
# ...
#             isYouTube -> listOf(
# ...
#             )
#             else -> listOf({ _ -> /* standard */ })
#         }

dl_old = """            isYouTube -> listOf(
                // 1. Default yt-dlp client (gets 4K + bypasses SABR natively)
                { _ -> /* default yt-dlp */ },
                // 2. Fallback to mobile clients
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android,ios,web")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=ios")
                }
            )
            else -> listOf({ _ -> /* standard */ })"""

dl_new = """            isYouTube -> listOf(
                // 1. Default yt-dlp client (gets 4K + bypasses SABR natively)
                { _ -> /* default yt-dlp */ },
                // 2. Fallback to mobile clients
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android,ios,web")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=android")
                },
                { req ->
                    req.addOption("--extractor-args", "youtube:player_client=ios")
                }
            )
            isInstagram -> listOf(
                { _ -> /* default yt-dlp */ },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36")
                },
                { req ->
                    req.addOption("--user-agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 17_5 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.5 Mobile/15E148 Safari/604.1")
                }
            )
            else -> listOf({ _ -> /* standard */ })"""

content = content.replace(dl_old, dl_new)

with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
    f.write(content)
