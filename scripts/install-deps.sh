#!/usr/bin/env bash
# Installs build dependencies for BullseyesTracker on Linux (and macOS, best-effort):
# JDK 17, Android SDK cmdline-tools + platform-tools + build-tools;34.0.0 + platforms;android-34,
# license acceptance, and the Gradle wrapper.
#
# Idempotent: safe to re-run. Does not touch anything outside $HOME and the chosen SDK dir.
# Requires: curl or wget, unzip, sudo (only if using a system package manager for the JDK).
#
# Usage: ./scripts/install-deps.sh
# Env overrides: ANDROID_SDK_ROOT (default: $HOME/Android/Sdk)

set -euo pipefail

CMDLINE_TOOLS_BUILD="15859902"
BUILD_TOOLS_VERSION="34.0.0"
PLATFORM_VERSION="android-34"
GRADLE_WRAPPER_VERSION="8.9"

ANDROID_SDK_ROOT="${ANDROID_SDK_ROOT:-$HOME/Android/Sdk}"

log() { printf '\n== %s ==\n' "$1"; }
have() { command -v "$1" >/dev/null 2>&1; }

download() {
  local url="$1" dest="$2"
  if have curl; then
    curl -fL --retry 3 -o "$dest" "$url"
  elif have wget; then
    wget -O "$dest" "$url"
  else
    echo "Need curl or wget to download files." >&2
    exit 1
  fi
}

install_jdk17() {
  log "JDK 17"
  if have java && java -version 2>&1 | grep -q '"17'; then
    echo "JDK 17 already active: $(command -v java)"
    return
  fi

  if have apt-get; then
    sudo apt-get update -y && sudo apt-get install -y openjdk-17-jdk
  elif have dnf; then
    sudo dnf install -y java-17-openjdk-devel
  elif have pacman; then
    sudo pacman -Sy --noconfirm jdk17-openjdk
  elif have brew; then
    brew install openjdk@17
  else
    log "No known package manager found - installing Eclipse Temurin 17 manually"
    local jdk_dir="$HOME/.local/opt/jdk-17"
    mkdir -p "$jdk_dir"
    local tarball
    tarball="$(mktemp).tar.gz"
    local os arch
    os="$(uname -s | tr '[:upper:]' '[:lower:]')"
    arch="$(uname -m)"
    case "$arch" in
      x86_64) arch="x64" ;;
      aarch64|arm64) arch="aarch64" ;;
      *) echo "Unsupported arch: $arch" >&2; exit 1 ;;
    esac
    download "https://api.adoptium.net/v3/binary/latest/17/ga/${os}/${arch}/jdk/hotspot/normal/eclipse" "$tarball"
    tar -xzf "$tarball" -C "$jdk_dir" --strip-components=1
    rm -f "$tarball"
    echo "export JAVA_HOME=\"$jdk_dir\"" >> "$HOME/.profile_bullseyestracker"
    echo "export PATH=\"\$JAVA_HOME/bin:\$PATH\"" >> "$HOME/.profile_bullseyestracker"
    echo "Installed to $jdk_dir. Source ~/.profile_bullseyestracker (see summary at the end)."
  fi
}

install_android_sdk() {
  log "Android SDK (\$ANDROID_SDK_ROOT=$ANDROID_SDK_ROOT)"
  mkdir -p "$ANDROID_SDK_ROOT/cmdline-tools"

  local sdkmanager="$ANDROID_SDK_ROOT/cmdline-tools/latest/bin/sdkmanager"
  if [ ! -x "$sdkmanager" ]; then
    log "Downloading cmdline-tools (build $CMDLINE_TOOLS_BUILD)"
    local zip
    zip="$(mktemp).zip"
    download "https://dl.google.com/android/repository/commandlinetools-linux-${CMDLINE_TOOLS_BUILD}_latest.zip" "$zip"
    local extract_dir
    extract_dir="$(mktemp -d)"
    unzip -q "$zip" -d "$extract_dir"
    rm -rf "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    mv "$extract_dir/cmdline-tools" "$ANDROID_SDK_ROOT/cmdline-tools/latest"
    rm -rf "$zip" "$extract_dir"
  else
    echo "cmdline-tools already installed at $sdkmanager"
  fi

  log "Accepting SDK licenses"
  yes | "$sdkmanager" --licenses >/dev/null || true

  log "Installing platform-tools, platforms;$PLATFORM_VERSION, build-tools;$BUILD_TOOLS_VERSION"
  "$sdkmanager" "platform-tools" "platforms;$PLATFORM_VERSION" "build-tools;$BUILD_TOOLS_VERSION"
}

setup_gradle_wrapper() {
  log "Gradle wrapper"
  if [ -x "./gradlew" ]; then
    echo "./gradlew already present."
  elif have gradle; then
    gradle wrapper --gradle-version "$GRADLE_WRAPPER_VERSION"
  else
    echo "No system 'gradle' found - skipping wrapper generation."
    echo "Install gradle (e.g. 'devbox shell', sdkman, or your package manager) and re-run this script, or run 'make wrapper' once gradle is available."
  fi
}

write_env_profile() {
  local profile="$HOME/.profile_bullseyestracker"
  {
    echo "export ANDROID_SDK_ROOT=\"$ANDROID_SDK_ROOT\""
    echo "export ANDROID_HOME=\"$ANDROID_SDK_ROOT\""
    echo "export PATH=\"\$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:\$ANDROID_SDK_ROOT/platform-tools:\$PATH\""
  } > "$profile"

  for rc in "$HOME/.bashrc" "$HOME/.zshrc"; do
    if [ -f "$rc" ] && ! grep -q "profile_bullseyestracker" "$rc"; then
      echo ". \"$profile\"" >> "$rc"
    fi
  done

  echo "Wrote $profile and sourced it from ~/.bashrc / ~/.zshrc if present."
}

install_jdk17
install_android_sdk
setup_gradle_wrapper
write_env_profile

log "Done"
echo "Restart your shell (or 'source ~/.profile_bullseyestracker') then run: make doctor"
