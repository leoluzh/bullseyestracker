#Requires -Version 5.1
<#
.SYNOPSIS
  Installs build dependencies for BullseyesTracker on Windows: JDK 17, Android SDK
  cmdline-tools + platform-tools + build-tools;34.0.0 + platforms;android-34, license
  acceptance, and the Gradle wrapper.

.DESCRIPTION
  Idempotent: safe to re-run. Persists ANDROID_HOME/ANDROID_SDK_ROOT and PATH additions to
  the current User environment (no admin rights required; does not touch machine-wide state).

.PARAMETER AndroidSdkRoot
  Where to install the Android SDK. Defaults to $env:ANDROID_SDK_ROOT if already set, else
  "$env:LOCALAPPDATA\Android\Sdk" (Android Studio's own default on Windows).

.EXAMPLE
  .\scripts\install-deps.ps1
#>

[CmdletBinding()]
param(
    [string]$AndroidSdkRoot = $(if ($env:ANDROID_SDK_ROOT) { $env:ANDROID_SDK_ROOT } else { "$env:LOCALAPPDATA\Android\Sdk" })
)

$ErrorActionPreference = 'Stop'

$CmdlineToolsBuild    = '15859902'
$BuildToolsVersion    = '34.0.0'
$PlatformVersion      = 'android-34'
$GradleWrapperVersion = '8.9'

function Write-Section([string]$Title) {
    Write-Host ""
    Write-Host "== $Title ==" -ForegroundColor Cyan
}

function Test-Command([string]$Name) {
    return [bool](Get-Command $Name -ErrorAction SilentlyContinue)
}

function Install-Jdk17 {
    Write-Section "JDK 17"

    $javaOk = $false
    if (Test-Command 'java') {
        $versionOutput = (& java -version 2>&1 | Out-String)
        if ($versionOutput -match '"17') { $javaOk = $true }
    }
    if ($javaOk) {
        Write-Host "JDK 17 already active: $((Get-Command java).Source)"
        return
    }

    if (Test-Command 'winget') {
        Write-Host "Installing Temurin 17 via winget..."
        & winget install --id EclipseAdoptium.Temurin.17.JDK -e --silent --accept-package-agreements --accept-source-agreements
        return
    }
    if (Test-Command 'choco') {
        Write-Host "Installing Temurin 17 via Chocolatey..."
        & choco install temurin17 -y
        return
    }

    Write-Host "No winget/choco found - downloading Eclipse Temurin 17 manually."
    $jdkDir = Join-Path $env:LOCALAPPDATA 'BullseyesTracker\jdk-17'
    New-Item -ItemType Directory -Force -Path $jdkDir | Out-Null
    $zipPath = Join-Path $env:TEMP 'temurin17.zip'
    Invoke-WebRequest -Uri 'https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile $zipPath
    Expand-Archive -Path $zipPath -DestinationPath $jdkDir -Force
    Remove-Item $zipPath -Force
    # Adoptium zips extract to a single nested folder (e.g. jdk-17.0.x+y); flatten it.
    $nested = Get-ChildItem -Path $jdkDir -Directory | Select-Object -First 1
    if ($nested) {
        Get-ChildItem -Path $nested.FullName | Move-Item -Destination $jdkDir -Force
        Remove-Item $nested.FullName -Recurse -Force
    }
    [Environment]::SetEnvironmentVariable('JAVA_HOME', $jdkDir, 'User')
    Add-UserPathEntry (Join-Path $jdkDir 'bin')
    Write-Host "Installed JDK 17 to $jdkDir"
}

function Add-UserPathEntry([string]$Entry) {
    $userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
    $parts = @()
    if ($userPath) { $parts = $userPath -split ';' | Where-Object { $_ -ne '' } }
    if ($parts -notcontains $Entry) {
        $newPath = if ($userPath) { "$userPath;$Entry" } else { $Entry }
        [Environment]::SetEnvironmentVariable('Path', $newPath, 'User')
    }
    if (($env:Path -split ';') -notcontains $Entry) {
        $env:Path = "$env:Path;$Entry"
    }
}

function Install-AndroidSdk {
    Write-Section "Android SDK (ANDROID_SDK_ROOT=$AndroidSdkRoot)"

    New-Item -ItemType Directory -Force -Path (Join-Path $AndroidSdkRoot 'cmdline-tools') | Out-Null
    $sdkmanager = Join-Path $AndroidSdkRoot 'cmdline-tools\latest\bin\sdkmanager.bat'

    if (-not (Test-Path $sdkmanager)) {
        Write-Host "Downloading cmdline-tools (build $CmdlineToolsBuild)..."
        $zipPath = Join-Path $env:TEMP 'android-cmdline-tools.zip'
        Invoke-WebRequest -Uri "https://dl.google.com/android/repository/commandlinetools-win-${CmdlineToolsBuild}_latest.zip" -OutFile $zipPath

        $extractDir = Join-Path $env:TEMP "android-cmdline-tools-extract"
        if (Test-Path $extractDir) { Remove-Item $extractDir -Recurse -Force }
        Expand-Archive -Path $zipPath -DestinationPath $extractDir -Force

        $latestDir = Join-Path $AndroidSdkRoot 'cmdline-tools\latest'
        if (Test-Path $latestDir) { Remove-Item $latestDir -Recurse -Force }
        # The zip contains a top-level "cmdline-tools" folder; sdkmanager expects its
        # *contents* directly under cmdline-tools\latest\.
        Move-Item -Path (Join-Path $extractDir 'cmdline-tools') -Destination $latestDir

        Remove-Item $zipPath -Force
        Remove-Item $extractDir -Recurse -Force
    } else {
        Write-Host "cmdline-tools already installed at $sdkmanager"
    }

    Write-Section "Accepting SDK licenses"
    try {
        1..12 | ForEach-Object { 'y' } | & $sdkmanager --licenses --sdk_root="$AndroidSdkRoot" | Out-Null
    } catch {
        Write-Warning "License acceptance step reported an error (often harmless if licenses were already accepted): $_"
    }

    Write-Section "Installing platform-tools, platforms;$PlatformVersion, build-tools;$BuildToolsVersion"
    & $sdkmanager "platform-tools" "platforms;$PlatformVersion" "build-tools;$BuildToolsVersion" --sdk_root="$AndroidSdkRoot"

    [Environment]::SetEnvironmentVariable('ANDROID_SDK_ROOT', $AndroidSdkRoot, 'User')
    [Environment]::SetEnvironmentVariable('ANDROID_HOME', $AndroidSdkRoot, 'User')
    $env:ANDROID_SDK_ROOT = $AndroidSdkRoot
    $env:ANDROID_HOME = $AndroidSdkRoot
    Add-UserPathEntry (Join-Path $AndroidSdkRoot 'cmdline-tools\latest\bin')
    Add-UserPathEntry (Join-Path $AndroidSdkRoot 'platform-tools')
}

function Set-GradleWrapper {
    Write-Section "Gradle wrapper"
    if (Test-Path '.\gradlew.bat') {
        Write-Host "gradlew.bat already present."
        return
    }
    if (Test-Command 'gradle') {
        & gradle wrapper --gradle-version $GradleWrapperVersion
    } else {
        Write-Host "No system 'gradle' found - skipping wrapper generation."
        Write-Host "Install gradle (e.g. 'devbox shell', scoop, or choco) and re-run this script, or run 'make wrapper' once gradle is available."
    }
}

Install-Jdk17
Install-AndroidSdk
Set-GradleWrapper

Write-Section "Done"
Write-Host "Open a new terminal (so the updated User PATH/env vars take effect), then run: make doctor"
