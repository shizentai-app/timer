# CLAUDE.md — Shizentai Timer

> Per-product orientation. For family-level context (brand, palette, naming conventions), see `../.github/FAMILY.md`. For workspace orientation across all sibling repos, see `../CLAUDE.md`.

## What this is

Interval timer for training. Two physically separate clients sharing brand and behavior:

- `android/` — Kotlin + Jetpack Compose + Material 3, package `app.shizentai.timer`. Single `:app` module.
- `web/app/` — Vite 5 + React 18 + TypeScript 5.6 + React Router 6.

**Local-only**: no Supabase, no accounts, no auth gate. Opens straight to the timer screen. Custom presets persist via DataStore (Android) and localStorage (Web).

Source of features: ported from the prior Expo React Native app `C:\repository\dyi\fudokan-countdown\`. **All features ported except sound recording / voice packs** — Shizentai Timer ships only the four bundled defaults (gong, gong_twice, rest_end, alert).

## Status (2026-05-06)

| Phase | What | State |
|---|---|---|
| 1 | Scaffold both clients (Gradle, Vite, theme, 3-tab nav, hello stubs) | ✓ |
| 2 | Web timer engine + visual (pure TS engine, Vitest, real preset config wired in) | ✓ |
| 3 | Port engine to Android (Kotlin StateFlow + JUnit/Turbine, mirrors Vitest cases) | ✓ |
| 4 | Presets persistence (8 built-ins + DataStore/localStorage + selectable list + create/delete UI) | ✓ |
| 5 | Audio + haptics + screen-on (SoundPool / HTMLAudioElement + Vibrator / navigator.vibrate + FLAG_KEEP_SCREEN_ON / Wake Lock API) | ✓ |
| 6 | i18n (en/uk/ja) wired across both clients; theme + language pickers on web Settings | ✓ |

**Out-of-scope-for-v1 polish that did not land:**
- Per-event sound choice (the original allows picking which of the 4 cues plays for each event). Defaults applied uniformly: `gong_twice` for WORK start, `alert` for WARNING, `gong` for REST start + FINISHED, `rest_end` for the 10s-before-rest cue.
- Custom phase color picker (palette is fixed to family green / gold / carmine on both clients).
- Android in-app language switcher. Android 13+ users change the language via system Settings → Apps → Shizentai Timer → Language; the `localeConfig` declares en / uk / ja so the OS picker shows them. Older devices fall back to system locale only.
- Animated progress ring around the digit (the static MM:SS display is what ships in v1; the ring concept lives only on the launcher icon for now).

## Build & test

```bash
# Android
cd android
./gradlew :app:assembleDebug          # builds APK
./gradlew :app:testDebugUnitTest      # runs unit tests (TimerEngine)

# Web
cd web/app
npm install
npm run typecheck
npm test
npm run build
npm run dev                           # Vite on http://localhost:5174
```

## Conventions

- **Theme tokens** live in `android/app/src/main/kotlin/app/shizentai/timer/core/theme/` and `web/app/src/styles/tokens.css`. Carmine `#8B1818` accent and Manrope typography are family-shared with main `shizentai`. Phase colors (work green, warning gold, rest carmine) are timer-only and live alongside the family tokens.
- **Package naming**: Android sources under `app.shizentai.timer.*`. Web sources under `src/{features,components,lib,data,theme,styles,i18n}`.
- **Engine logic** is pure (no Compose, no DOM): `engine/TimerEngine.kt` and `lib/timer.ts`. Both expose a deterministic phase machine over an injectable clock so unit tests run synchronously without sleeps.
- **Persistence**: DataStore<Preferences> on Android (`data/presets/PresetRepository.kt`), localStorage on Web (`data/presets.ts`). JSON shape mirrors across both via kotlinx-serialization & TS types.
- **No backend**, no Supabase, no Hilt Supabase modules. Future Shizentai↔Timer integration goes through the `shizentai` API, not a shared schema.

## Adding a built-in preset

1. Append to `android/app/src/main/kotlin/app/shizentai/timer/data/presets/BuiltInPresets.kt`.
2. Append the matching entry to `web/app/src/data/presets.ts` `BUILT_INS`.
3. No persistence migration required — built-ins are constants, not stored.

## Adding a sound cue

1. Drop the WAV into `android/app/src/main/res/raw/<name>.wav` AND `web/app/public/sounds/<name>.wav` (snake_case required by Android `aapt`).
2. Register the new event in `core/audio/SoundPlayer.kt` (Android) and `lib/audio.ts` (Web).
3. Map it from a phase transition in the engine consumer.

## Brand assets

Live in `brand/` at the repo root (one level up from `android/` and `web/app/`). The launcher icon (Android `mipmap-anydpi-v26/`) and web favicon both originate from `brand/shizentai_timer.svg` — kanji 自 + 270° progress arc on carmine.

## Changelog

`changelog/` is the running log of meaningful changes since v1 shipped — read the latest few entries before making decisions about already-touched areas. The "How it was decided" section in each entry records what was tried and rejected; that's the part that's hard to recover from git history. Convention is documented in `changelog/README.md`.

## Helper scripts (PowerShell)

Run from the `timer/` directory:

- `.\build-apk.ps1` — assembles a debug APK and copies it to `android/dist/shizentai-timer-debug.apk` for sharing.
- `.\install.ps1` — assembles + installs the debug APK on the connected device, then launches it (auto-detects `adb` from the local Android SDK).

## Out of scope

- iOS / Expo / React Native — this is native Android + Web only.
- Voice recording, voice packs, microphone permissions, custom-recording playback.
- Workout history log (was never implemented in fudokan-countdown either).
- Push notifications.
- Account sync, cloud presets.

If a request lands in this list, point at this section and ask whether it's a v2 ask.
