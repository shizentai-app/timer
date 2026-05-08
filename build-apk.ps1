# Build a debug APK and copy it to android/dist/ for sharing.
#
# Usage (from the timer/ directory):
#   .\build-apk.ps1
#
# Output: android/dist/shizentai-timer-debug.apk

$ErrorActionPreference = "Stop"

$androidDir = Join-Path $PSScriptRoot "android"
$distDir    = Join-Path $androidDir "dist"
$apkSrc     = Join-Path $androidDir "app\build\outputs\apk\debug\app-debug.apk"
$apkDest    = Join-Path $distDir    "shizentai-timer-debug.apk"

if (-not (Test-Path $androidDir)) {
    Write-Error "android/ not found at $androidDir - run this from the timer/ directory."
    exit 1
}

Write-Host "Assembling debug APK..."
Push-Location $androidDir
try {
    & .\gradlew.bat :app:assembleDebug --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) {
        Write-Error "gradlew :app:assembleDebug failed (exit $LASTEXITCODE)."
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}

if (-not (Test-Path $apkSrc)) {
    Write-Error "Build reported success but APK is missing: $apkSrc"
    exit 1
}

New-Item -ItemType Directory -Force -Path $distDir | Out-Null
Copy-Item -Path $apkSrc -Destination $apkDest -Force

$sizeMb = [math]::Round((Get-Item $apkDest).Length / 1MB, 2)
Write-Host ""
Write-Host "OK  $apkDest ($sizeMb MB)"
