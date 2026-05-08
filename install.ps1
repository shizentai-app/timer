# Build a debug APK and install it on the connected device, then launch.
#
# Usage (from the timer/ directory, with a device attached and USB debugging on):
#   .\install.ps1

$ErrorActionPreference = "Stop"

$androidDir = Join-Path $PSScriptRoot "android"

if (-not (Test-Path $androidDir)) {
    Write-Error "android/ not found at $androidDir - run this from the timer/ directory."
    exit 1
}

# Locate adb up-front so the launch step at the end has it. Gradle's
# installDebug doesn't need adb on PATH, but the launcher intent does.
$adbCandidates = @(
    "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe",
    "$env:ANDROID_HOME\platform-tools\adb.exe",
    "$env:ANDROID_SDK_ROOT\platform-tools\adb.exe",
    "C:\Android\Sdk\platform-tools\adb.exe",
    "C:\Program Files\Android\Sdk\platform-tools\adb.exe"
)
$adb = $null
foreach ($c in $adbCandidates) {
    if ($c -and (Test-Path $c)) { $adb = $c; break }
}

# Quick device check so the user gets a clear failure rather than waiting for
# a 30-second build only to find no device.
if ($adb) {
    $devices = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match "\sdevice$" }
    if (-not $devices) {
        Write-Error "No device detected by adb. Plug in your phone and enable USB debugging."
        exit 1
    }
}

Write-Host "Building + installing debug APK..."
Push-Location $androidDir
try {
    & .\gradlew.bat :app:installDebug --console=plain --no-daemon
    if ($LASTEXITCODE -ne 0) {
        Write-Error "gradlew :app:installDebug failed (exit $LASTEXITCODE)."
        exit $LASTEXITCODE
    }
} finally {
    Pop-Location
}

if ($adb) {
    # PowerShell 5.1 turns redirected stderr from native exes into
    # NativeCommandError ErrorRecords, which trip ErrorActionPreference=Stop
    # even when the exe returns 0. Relax the preference around the call so
    # `2>&1 | Out-Null` can silence monkey's debug chatter without aborting.
    $prev = $ErrorActionPreference
    $ErrorActionPreference = "Continue"
    & $adb shell monkey -p app.shizentai.timer.debug -c android.intent.category.LAUNCHER 1 2>&1 | Out-Null
    $ErrorActionPreference = $prev
    Write-Host ""
    Write-Host "OK  installed and launched app.shizentai.timer.debug"
} else {
    Write-Host ""
    Write-Host "OK  installed app.shizentai.timer.debug"
    Write-Host "    (adb not found; open the app from the launcher manually)"
}
