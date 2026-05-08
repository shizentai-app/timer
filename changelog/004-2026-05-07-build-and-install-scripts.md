# PowerShell helper scripts: build-apk + install

**Date:** 2026-05-07 · **Files:** `build-apk.ps1`, `install.ps1`, `CLAUDE.md`

## Why

Across the prior sessions, every device install was a hand-typed `cd timer\android; .\gradlew.bat :app:installDebug` plus a follow-up `adb shell monkey ... LAUNCHER 1` to actually open the app, plus a one-time `$env:ANDROID_HOME=...` to find adb. Two small scripts capture the dev loop so it survives across sessions and is one command from a fresh PowerShell.

## What landed

Two scripts at the `timer/` root, runnable directly without `cd android`:

- **`build-apk.ps1`** — `gradlew :app:assembleDebug`, then copies the resulting APK to `android/dist/shizentai-timer-debug.apk` and prints its size. For sharing the build with someone who isn't connected over USB.
- **`install.ps1`** — `gradlew :app:installDebug`, then `adb shell monkey ... LAUNCHER 1` to launch the freshly-installed app. Auto-detects `adb` from common Android SDK locations so it works without `ANDROID_HOME` set in the user's PowerShell. Bails out early with a clear message if no device is connected.

Both echo a single `OK ...` line on success so they're scriptable from CI later if needed.

CLAUDE.md gained a short "Helper scripts" section pointing at them.

## How it was decided

- **Debug APKs only.** A `release` variant requires a signing keystore + ProGuard verification. For v1 distribution and dev install, debug is the right default. When release is needed, add a `build-release.ps1` rather than overloading `build-apk.ps1` with a flag.
- **Plain PowerShell, no Bash equivalent.** The user's environment is Windows + PowerShell exclusively; cross-platform shells would just bit-rot.
- **`--no-daemon`.** Two parallel script invocations have collided before in this project (file lock on `build.log`). `--no-daemon` keeps each invocation isolated; the small startup-cost penalty is fine for these.
- **Auto-launch via `monkey`** rather than `am start` — works without knowing the activity's component name and survives renames of `MainActivity`.

## Known follow-ups

- No `release` build script. Add one if/when the app gets a real signing config.
- No equivalent web-side scripts (`npm run build`, `npm run dev` are the conventions there). Add later if the workflow gets more elaborate.
