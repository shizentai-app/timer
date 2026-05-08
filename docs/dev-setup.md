# Dev setup

## Prerequisites

| Tool | Version | Why |
|---|---|---|
| JDK | 17 | Gradle + Kotlin 2.1 toolchain |
| Android SDK | 35 (compileSdk + targetSdk) | Compose / Material 3 |
| Android command-line tools | latest | for sdkmanager + avdmanager |
| Node | 20 LTS | Vite + Vitest |
| npm | bundled with Node | dependency install |

Set `ANDROID_HOME` (or `ANDROID_SDK_ROOT`) so Gradle can find the SDK. The Gradle wrapper handles the Gradle version itself.

## First run

```bash
git clone https://github.com/shizentai-app/timer.git
cd timer
```

### Android

```bash
cd android
./gradlew :app:assembleDebug
./gradlew :app:installDebug    # to a connected device or running emulator
./gradlew :app:testDebugUnitTest
```

The first build will download Compose BOM, Hilt, Navigation Compose, DataStore, kotlinx-serialization, and test deps — expect a few minutes.

Open in Android Studio (`File → Open` on the `android/` directory) for a friendlier dev loop.

### Web

```bash
cd web/app
npm install
npm run dev          # Vite at http://localhost:5174
npm run typecheck    # tsc --noEmit
npm test             # Vitest run
npm run build        # tsc + vite build → dist/
```

## Project layout

```
timer/
  brand/                         icon SVG + PNGs (1024 / 512 / 256) + favicon
  android/                       Kotlin / Compose client
    app/                         single :app module
    gradle/, gradlew*            wrapper + version catalog
  web/app/                       Vite / React client
    public/                      brand assets, sounds, manifest
    src/                         app code
  docs/                          this folder
  CLAUDE.md, README.md, LICENSE
```

## What you don't need

- A Supabase project. The timer is local-only.
- Firebase / FCM. No push notifications in v1.
- A signing key. Debug builds are signed by the default Android debug keystore.
- An Apple developer account. No iOS client.

## Verifying a green build

End-to-end smoke after each phase:

```bash
# Android — must succeed
cd android && ./gradlew :app:assembleDebug :app:testDebugUnitTest

# Web — must succeed
cd web/app && npm run typecheck && npm test && npm run build
```

## Manual functional smoke (per platform, post Phase 5)

Load Kumite-30s preset → start → verify in order:

1. 10 s PREPARE countdown
2. `gong_twice` plays at WORK start
3. `alert` plays when WARNING phase begins (last `warningSeconds` of work)
4. `gong` plays at REST start
5. `rest_end` plays 10 s before REST ends (if `signalEndOfRest` is on)
6. Phase background colors transition correctly
7. Screen stays on during the workout
8. Android vibrates per phase: heavy on WORK/REST/FINISHED, medium on PREPARE/WARNING
9. Pause/resume preserves remaining ms
10. Round counter advances 1/3 → 2/3 → 3/3 → FINISHED with closing `gong`
