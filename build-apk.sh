#!/bin/bash
# ETV Go APK Builder - works around AAPT2 daemon issue on ARM64
set -e

PROJECT_DIR="/root/Documents/Codex/2026-07-02/hi-4"
cd "$PROJECT_DIR"

export ANDROID_SDK_ROOT=/opt/android-sdk
GRADLE="/root/.gradle/wrapper/dists/gradle-8.9-bin/90cnw93cvbtalezasaz0blq0a/gradle-8.9/bin/gradle"

echo "=== ETV Go APK Builder ==="
echo ""

# Phase 1: Run the build (will fail at AAPT2 daemon startup, but extracts aapt2)
echo ">>> Phase 1: Running build to extract AAPT2 binary..."
$GRADLE --no-daemon --console=plain :app:processDebugResources 2>&1 | tail -10 || true

# Phase 2: Find ALL extracted aapt2 binaries and replace with QEMU wrapper
echo ""
echo ">>> Phase 2: Replacing AAPT2 binaries with QEMU wrapper..."

WRAPPER_SCRIPT='/usr/local/bin/wrapped-aapt2'
AAPT2_REAL='/tmp/aapt2-binary/aapt2'

# Search the entire gradle cache for aapt2 binaries
find /root/.gradle/caches/transforms-3 -name "aapt2" -type f 2>/dev/null | while read BINARY; do
    # Skip if it's already our wrapper
    if head -1 "$BINARY" 2>/dev/null | grep -q "#!/bin/bash"; then
        echo "Already wrapped: $BINARY"
        continue
    fi
    
    echo "Replacing: $BINARY"
    # Move the real binary aside
    mv "$BINARY" "${BINARY}.real" 2>/dev/null || true
    
    # Create wrapper script
    cat > "$BINARY" << WRAPPER_EOF
#!/bin/bash
exec /usr/bin/qemu-x86_64-static -L /tmp/x86_64-sysroot /tmp/aapt2-binary/aapt2 "\$@"
WRAPPER_EOF
    chmod +x "$BINARY"
    echo "  -> Replaced with QEMU wrapper"
done

# Also check the build directory for any aapt2 binaries
find "$PROJECT_DIR" -name "aapt2" -type f 2>/dev/null | while read BINARY; do
    if head -1 "$BINARY" 2>/dev/null | grep -q "ELF"; then
        echo "Found ELF aapt2 in build dir: $BINARY"
        mv "$BINARY" "${BINARY}.real" 2>/dev/null || true
        cat > "$BINARY" << WRAPPER_EOF
#!/bin/bash
exec /usr/bin/qemu-x86_64-static -L /tmp/x86_64-sysroot /tmp/aapt2-binary/aapt2 "\$@"
WRAPPER_EOF
        chmod +x "$BINARY"
    fi
done

# Phase 3: Clean project build and rebuild
echo ""
echo ">>> Phase 3: Cleaning and rebuilding..."
$GRADLE --no-daemon --console=plain :app:clean 2>&1 | tail -3 || true
echo ">>> Building APK..."
$GRADLE --no-daemon --console=plain :app:assembleDebug 2>&1 | tail -30

# Check for APK
APK=$(find "$PROJECT_DIR/app/build/outputs" -name "*.apk" 2>/dev/null | head -1)
if [ -n "$APK" ]; then
    cp "$APK" "$PROJECT_DIR/ETVGo.apk"
    echo ""
    echo "========================================="
    echo "  SUCCESS! APK built successfully!"
    echo "========================================="
    ls -lh "$PROJECT_DIR/ETVGo.apk"
else
    echo ""
    echo "=== BUILD FAILED ==="
    echo "Check the output above for errors."
fi
