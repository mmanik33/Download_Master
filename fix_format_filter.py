with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'r') as f:
    content = f.read()

# Swap the two blocks using string finding
idx_start = content.find("if (targetHeight != null && targetHeight > 0) {")
idx_mid = content.find("if (!fid.isNullOrBlank() && fid != \"best\") {")
idx_end = content.find("return \"bestvideo+bestaudio/best\"", idx_mid)

if idx_start != -1 and idx_mid != -1 and idx_end != -1 and idx_start < idx_mid:
    target_block = content[idx_start:idx_mid]
    fid_block = content[idx_mid:idx_end]
    
    # We also want to replace the string in fid_block:
    # "$streamId+bestaudio[ext=m4a]/$streamId+bestaudio/$streamId+ba/bestvideo[height<=$h]+bestaudio/best[height<=$h]/bestvideo+bestaudio/best"
    # with
    # "$streamId+bestaudio[ext=m4a]/$streamId+bestaudio/$streamId+ba/$streamId/bestvideo[height<=$h]+bestaudio/best[height<=$h]/bestvideo+bestaudio/best"
    fid_block = fid_block.replace(
        "return \"$streamId+bestaudio[ext=m4a]/$streamId+bestaudio/$streamId+ba/bestvideo",
        "return \"$streamId+bestaudio[ext=m4a]/$streamId+bestaudio/$streamId+ba/$streamId/bestvideo"
    )
    
    new_content = content[:idx_start] + fid_block + "\n        " + target_block + content[idx_end:]
    
    with open('app/src/main/java/com/example/data/YtDlpRepository.kt', 'w') as f:
        f.write(new_content)
    print("Done")
else:
    print("Could not find blocks")
