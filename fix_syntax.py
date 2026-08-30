import re

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'r') as f:
    lines = f.readlines()

new_lines = []
imports = []
package_line = ""

for line in lines:
    if line.startswith("import "):
        imports.append(line)
    elif line.startswith("package "):
        package_line = line
    else:
        new_lines.append(line)

# Clean up any potential duplicates
imports = list(set(imports))

# Ensure required imports exist
req_imports = [
    "import androidx.compose.foundation.border\n",
    "import androidx.compose.material.icons.filled.Info\n",
    "import androidx.compose.material.icons.filled.Checklist\n",
    "import androidx.compose.ui.window.Dialog\n"
]
for req in req_imports:
    if req not in imports:
        imports.append(req)

final_content = package_line + "".join(imports) + "".join(new_lines)

# Now let's fix HistoryItemCard signature
old_sig = '''@Composable
private fun HistoryItemCard(
    item: DownloadHistoryItem,
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onReDownload: () -> Unit,
    onDelete: () -> Unit
) {'''
new_sig = '''@Composable
private fun HistoryItemCard(
    item: DownloadHistoryItem,
    isSelected: Boolean = false,
    isSelectionMode: Boolean = false,
    onSelectToggle: () -> Unit = {},
    onPlay: () -> Unit,
    onShare: () -> Unit,
    onCopyLink: () -> Unit,
    onReDownload: () -> Unit,
    onDelete: () -> Unit,
    onInfo: () -> Unit = {}
) {'''

if old_sig in final_content:
    final_content = final_content.replace(old_sig, new_sig)

with open('app/src/main/java/com/example/ui/screens/DownloadsScreen.kt', 'w') as f:
    f.write(final_content)

