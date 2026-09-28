#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/go-tdlib"

echo "=== Building mobile.aar from Go source ==="

if ! command -v gomobile &>/dev/null; then
    echo "gomobile not found. Installing..."
    go install golang.org/x/mobile/cmd/gomobile@latest
    gomobile init
fi

gomobile bind \
    -target=android \
    -androidapi 26 \
    -o ../app/libs/mobile.aar \
    ./mobile

echo "=== mobile.aar built successfully ==="