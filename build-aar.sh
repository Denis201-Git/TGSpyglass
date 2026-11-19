#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
GO_DIR="$ROOT_DIR/go-tdlib"
AAR_PATH="$ROOT_DIR/app/libs/mobile.aar"

if ! command -v go >/dev/null 2>&1; then
    echo "Go toolchain not found" >&2
    exit 1
fi

if [[ -z "${ANDROID_HOME:-}" && -z "${ANDROID_SDK_ROOT:-}" && -f "$ROOT_DIR/local.properties" ]]; then
    SDK_DIR="$(grep -m1 '^sdk.dir=' "$ROOT_DIR/local.properties" | cut -d= -f2-)"
    if [[ -n "$SDK_DIR" ]]; then
        export ANDROID_HOME="$SDK_DIR"
    fi
fi

if [[ -z "${ANDROID_HOME:-}" && -z "${ANDROID_SDK_ROOT:-}" ]]; then
    echo "ANDROID_HOME or ANDROID_SDK_ROOT is required" >&2
    exit 1
fi

cd "$GO_DIR"

MOBILE_MODULE="$(go list -m -f '{{.Path}}' golang.org/x/mobile)"
GOMOBILE_VERSION="$(go list -m -f '{{.Version}}' golang.org/x/mobile)"
if [[ -z "$MOBILE_MODULE" || -z "$GOMOBILE_VERSION" ]]; then
    echo "golang.org/x/mobile version not found in go.mod" >&2
    exit 1
fi

GOBIN_DIR="${GOBIN:-$(go env GOPATH)/bin}"
mkdir -p "$GOBIN_DIR" "$(dirname "$AAR_PATH")"
export PATH="$GOBIN_DIR:$PATH"

build_pinned_tool() {
    local tool="$1"
    local bin="$GOBIN_DIR/$(basename "$tool")"
    if [[ -x "$bin" ]] && go version -m "$bin" 2>/dev/null | awk -v m="$MOBILE_MODULE" -v v="$GOMOBILE_VERSION" '$1 == "mod" && $2 == m && $3 == v { found = 1 } END { exit !found }'; then
        echo "Using pinned $bin"
        return
    fi
    echo "Building $tool@$GOMOBILE_VERSION"
    GOWORK=off go build -o "$bin" "$tool"
}

build_pinned_tool golang.org/x/mobile/cmd/gomobile

echo "=== Initializing gomobile ==="
gomobile init

build_pinned_tool golang.org/x/mobile/cmd/gobind

echo "=== Building mobile.aar for 4 ABIs ==="
gomobile bind \
    -target=android/arm,android/arm64,android/386,android/amd64 \
    -androidapi 26 \
    -trimpath \
    -ldflags "-s -w -buildid=" \
    -o "$AAR_PATH" \
    ./mobile

echo "=== mobile.aar built successfully: $AAR_PATH ==="
