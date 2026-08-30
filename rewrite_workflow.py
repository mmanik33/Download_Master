with open('.github/workflows/build-apk.yml', 'r') as f:
    content = f.read()

# Let's cleanly replace the entire Verify and Package Release Assets, Publish Release, and Report sections.

import re

# Remove AAB block entirely
content = re.sub(
    r'      - name: Build Google Play Release AAB[\s\S]*?(?=      - name: Verify and Package Release Assets)',
    '',
    content
)

# Fix verify section
verify_start = content.find('      - name: Verify and Package Release Assets')
publish_start = content.find('      - name: Publish Release Assets to GitHub Releases')
if verify_start != -1 and publish_start != -1:
    verify_new = """      - name: Verify and Package Release Assets
        id: prepare_assets
        run: |
          echo "=== Verifying and Packaging Release Assets ==="
          TEMP_ASSET_DIR="${{ runner.temp }}/release-assets"
          mkdir -p "$TEMP_ASSET_DIR"
          
          APK_DIR="app/build/outputs/apk/release"
          
          # Universal APK Verification
          SRC_APK=$(find "$APK_DIR" -name "*.apk" -type f | head -n 1)
          DEST_APK="$TEMP_ASSET_DIR/Download-Master-universal-release.apk"
          
          if [ -z "$SRC_APK" ] || [ ! -f "$SRC_APK" ] || [ ! -s "$SRC_APK" ]; then
            echo "ERROR: Universal APK not found or empty in $APK_DIR!"
            exit 1
          fi
          
          cp "$SRC_APK" "$DEST_APK"
          echo "✓ Verified Universal APK: Download-Master-universal-release.apk ($(du -h "$DEST_APK" | cut -f1))"
          
          echo "asset_dir=$TEMP_ASSET_DIR" >> "$GITHUB_OUTPUT"
"""
    content = content[:verify_start] + verify_new + content[publish_start:]


publish_start = content.find('      - name: Publish Release Assets to GitHub Releases')
report_start = content.find('      - name: Report Build & Release Result')

if publish_start != -1 and report_start != -1:
    publish_new = """      - name: Publish Release Assets to GitHub Releases
        id: publish_release
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          echo "=== Publishing to GitHub Releases ==="
          ASSET_DIR="${{ steps.prepare_assets.outputs.asset_dir }}"
          
          RELEASE_TAG="v1.0.0-build-${{ github.run_number }}"
          RELEASE_TITLE="Download Master Release Build #${{ github.run_number }}"
          
          APK_SIZE=$(du -h "$ASSET_DIR/Download-Master-universal-release.apk" | cut -f1)
          
          RELEASE_NOTES="### Download Master Release Build #${{ github.run_number }}
          
          **Release Artifacts:**
          
          #### 📱 Universal Release APK (Direct Device Installation):
          - **Universal APK:** \`Download-Master-universal-release.apk\` (~$APK_SIZE)
            *(Optimized universal installation package containing full video & audio download capabilities for all Android smartphones and tablets)*
          
          ---
          - **Commit SHA:** \`${{ github.sha }}\`
          - **Build:** GitHub Actions Run #${{ github.run_number }}"
          
          gh release create "$RELEASE_TAG" \\
            "$ASSET_DIR/Download-Master-universal-release.apk" \\
            --title "$RELEASE_TITLE" \\
            --notes "$RELEASE_NOTES" \\
            --target "${{ github.sha }}" \\
            --latest
"""
    content = content[:publish_start] + publish_new + content[report_start:]

report_start = content.find('      - name: Report Build & Release Result')
if report_start != -1:
    report_new = """      - name: Report Build & Release Result
        if: always()
        env:
          GH_TOKEN: ${{ github.token }}
        run: |
          echo "=== Build & Release Summary ==="
          RELEASE_TAG="v1.0.0-build-${{ github.run_number }}"
          RELEASE_TITLE="Download Master Release Build #${{ github.run_number }}"
          
          if [ "${{ steps.publish_release.outcome }}" = "success" ]; then
            RELEASE_URL="https://github.com/${{ github.repository }}/releases/tag/${RELEASE_TAG}"
            echo "Release URL: $RELEASE_URL"
            
            cat << EOF >> "$GITHUB_STEP_SUMMARY"
          ## 🚀 Download Master Release Assets Published!
          
          - **GitHub Release:** [$RELEASE_TITLE]($RELEASE_URL)
          
          ### 📱 Available Downloads:
          1. **\`Download-Master-universal-release.apk\`** — Universal installation APK for all Android devices.
          EOF
          fi
"""
    content = content[:report_start] + report_new


with open('.github/workflows/build-apk.yml', 'w') as f:
    f.write(content)
