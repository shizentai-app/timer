# Helper script fixes: em-dash encoding + stderr redirect on PS 5.1

**Date:** 2026-05-07 · **Files:** `build-apk.ps1`, `install.ps1`

## Why

Tried to invoke `.\build-apk.ps1` and `.\install.ps1` (added in `004`) for the first time after the dark-theme update. Both blew up — the first with a misleading parser error pointing at line 42, the second with a `NativeCommandError` after a successful install. Two separate Windows PowerShell 5.1 quirks, both worth recording so a future session doesn't waste time re-debugging.

## What changed

### 1. Em-dash bytes broke the script's string parsing

`build-apk.ps1` line 16 had a literal em-dash (`—`) inside a double-quoted error message: `"android/ not found at $androidDir — run from..."`. The Write tool that created the file encoded as UTF-8 without BOM. PowerShell 5.1 (`powershell.exe`) reads scripts as the system ANSI codepage when there's no BOM, so the em-dash bytes (`E2 80 94`) decoded as `â€"` in Windows-1252 — and `0x94` (the third byte) is the right curly quote `"`, which prematurely terminated the string literal. The parser then misreported the failure point at line 42 inside an unrelated `Write-Host` call.

`install.ps1` had the same issue on its line 11.

**Fix:** replaced both em-dashes with ASCII hyphens. The scripts are now pure ASCII, so encoding ambiguity can't bite them again.

### 2. `*> $null` on a native exe trips ErrorActionPreference=Stop

`install.ps1` ended with `& $adb shell monkey -p ... 1 *> $null` to silence monkey's debug chatter. Under PowerShell 5.1, redirecting a native exe's stderr (which `*>` does — it merges all streams into one and then redirects) wraps each stderr line in a `NativeCommandError` ErrorRecord. With `$ErrorActionPreference = "Stop"` set at the top of the script, that record terminates the script — even though `monkey` actually returned exit 0 and the launch succeeded.

**Fix:** wrap the call in a temporary `$ErrorActionPreference = "Continue"` block and use `2>&1 | Out-Null` to silence both streams. The launch noise is gone, the script exits cleanly, and `Stop` semantics are preserved everywhere else.

```powershell
$prev = $ErrorActionPreference
$ErrorActionPreference = "Continue"
& $adb shell monkey -p ... 2>&1 | Out-Null
$ErrorActionPreference = $prev
```

## How it was decided

- **Replaced em-dashes with hyphens rather than writing files with a UTF-8 BOM.** The scripts are short tooling messages — typographic punctuation isn't load-bearing. ASCII-only is the most defensive choice and means the next agent can edit these scripts with any tool without having to remember encoding rules.
- **Local `ErrorActionPreference` toggle rather than removing `Stop` from the script.** The `Stop` preference at the top is what gives the script its fail-fast behavior on a build error or a missing APK. Killing it globally would mask real problems. Toggling around the one known-noisy native call keeps the rest of the script strict.
- **Did not switch to `Start-Process -RedirectStandardOutput`** for the monkey call. Heavyweight for a 1ms launcher intent; the temp-preference pattern is idiomatic enough.

## Known follow-ups

- If `release` build script gets added later (per `004`'s open follow-up), keep it ASCII-only by default.
- If anyone changes the Write tool's default encoding to add a BOM, the em-dash workaround becomes unnecessary — but ASCII is still safer.
