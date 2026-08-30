import re

with open('.github/workflows/build-apk.yml', 'r') as f:
    content = f.read()

# Remove Build AAB block
aab_build_pattern = r'      - name: Build Google Play Release AAB[\s\S]*?(?=      - name: Verify and Package Release Assets)'
content = re.sub(aab_build_pattern, '', content)

# Remove AAB verification in Verify and Package Release Assets
aab_verify_pattern = r'                    # AAB Bundle Verification[\s\S]*?cut -f1\)\)"'
content = re.sub(aab_verify_pattern, '', content)

# Remove AAB upload in gh release create
gh_upload_pattern = r'            "\$ASSET_DIR/Download-Master-release.aab" \\\n'
content = re.sub(gh_upload_pattern, '', content)

# Remove AAB size
aab_size_pattern = r'          AAB_SIZE=\$\(du -h "\$ASSET_DIR/Download-Master-release.aab" \| cut -f1\)\n'
content = re.sub(aab_size_pattern, '', content)

# Remove AAB from release notes
aab_notes_pattern = r'                    #### 📦 Google Play Store Distribution:[\s\S]*?for Google Play Store\)\*'
content = re.sub(aab_notes_pattern, '', content)

# Remove AAB from report build
aab_report_pattern = r'          2\. \*\*\`Download-Master-release.aab\`\*\* — Android App Bundle for Google Play Store console upload\.'
content = re.sub(aab_report_pattern, '', content)

# Change Build & Release APKs and AAB to Build & Release APK
content = content.replace('name: Build & Release APKs and AAB', 'name: Build & Release APK')

with open('.github/workflows/build-apk.yml', 'w') as f:
    f.write(content)

