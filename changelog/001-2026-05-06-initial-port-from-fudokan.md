# Initial port from fudokan-countdown to native Android + Web

**Date:** 2026-05-06 · **Files:** entire `timer/` tree

## Why

The previous interval-timer app — the prior `fudokan-countdown` repo (Expo / React Native) — was a single-codebase that the family no longer uses. The Shizentai family standardizes on **native Android (Kotlin/Compose) + native web (Vite/React/TS)** — the same shape as the main `shizentai/` app. Goal: rebuild as Shizentai Timer, two physically separate codebases, sharing brand and product behavior, dropping voice-recording (use bundled defaults only).

## What changed

Created `timer/` from scratch — mirrors `shizentai/`'s structure minus everything backend-related:

```
timer/
  brand/                  — pre-existing icons (kanji 自 + 270° progress arc)
  android/                — Kotlin + Jetpack Compose, package app.shizentai.timer
  web/app/                — Vite 5 + React 18 + TypeScript 5.6
  docs/                   — architecture.md, dev-setup.md
  CLAUDE.md, README.md (uk + en), LICENSE, .gitignore
```

### Phases delivered

| # | What | Notes |
|---|---|---|
| 1 | Scaffold (Gradle wrapper, libs.versions.toml stripped of Supabase/Room/Coil/Roborazzi/Kotest, Vite config, theme tokens, 3-tab nav, hello stubs, brand-tinted launcher icon) | Built ~50 files |
| 2 | Pure TypeScript engine `web/app/src/lib/timer.ts` + 28 Vitest cases | Pure logic, no React |
| 3 | Kotlin port `android/.../engine/TimerEngine.kt` + JUnit/Turbine tests mirroring the Vitest cases line-for-line | StateFlow + SharedFlow output |
| 4 | 8 built-in presets (Kumite 30s/1m/2m/3m, Classic Boxing, Amateur Boxing, MMA, Tabata) + DataStore (Android) / localStorage (Web) for customs + create/delete UI | |
| 5 | Audio (SoundPool / HTMLAudioElement), haptics (Vibrator / `navigator.vibrate`), keep-screen-on (FLAG_KEEP_SCREEN_ON / Wake Lock API), Settings toggles | |
| 6 | i18n en/uk/ja — Android `res/values{,-uk,-ja}/strings.xml`, web hand-rolled `useTranslation` hook (no `react-i18next` dep) | |

### Locked product decisions

1. **Local-only.** No Supabase, no auth gate, opens straight to timer. Future Shizentai↔Timer integration goes through the `shizentai` API, not a shared schema.
2. **Light + dark themes** wired on both clients (web has a picker; Android follows system).
3. **All eight built-in presets** ship — Kumite ones included for brand fit.
4. **Japanese commands as default** — "Hajime!", "Yame!", "Yoi!", "Kiai!" — kept across all three locales (transliterated in uk, kanji in ja).
5. **Android + Web only** — no iOS, no Expo.

### Architectural notes

- **Engine** is callback-free pure logic. The consumer drives ticks via `tick(now)`. No `setInterval` inside the engine itself — makes it deterministic and synchronously testable. Both clients exposed Flow/listener wrappers around the same shape.
- **No shared package.** Two codebases that look alike. The TS engine is the spec; the Kotlin engine is the parallel reimplementation. Tests in both languages cover the same scenarios so divergence is caught.
- **Brand assets** live in `timer/brand/` (one level up from both clients). The Android adaptive icon and the web favicon both originate from `brand/shizentai_timer.svg`.
- **i18n on web** is a hand-rolled `useTranslation` hook with three flat dicts inline — `react-i18next` (≈5 MB gzipped + setup ceremony) was overkill for ~30 strings.

## How it was decided

Up-front planning notes (kept locally) called out these tradeoffs:

- **Custom presets persistence** — DataStore + JSON over Room because the schema is one flat list. Room would mean migration ceremony for zero benefit at this surface.
- **`SoundPool` over `MediaPlayer` on Android** — short cues, low-latency requirement, no state-machine churn.
- **WAV files kept as WAV** (not converted to OGG) — total payload is small; conversion saves maybe 200–400 KB and risks subtle latency / encoder artifacts on percussive cues.
- **No iOS** — the family standard is "Android + Web", not "everything everywhere". Adding iOS would mean a third engine port + a third design QA cycle.

## Known follow-ups (still open)

- **Per-event sound choice** — original allowed picking which of the 4 cues plays per event. We ship uniform defaults: `gong_twice` for WORK start, `alert` for WARNING, `gong` for REST start + FINISHED, `rest_end` for the 10s-before-rest cue.
- **Custom phase color picker** — reviewer wanted this; out of v1 scope.
- **Animated progress ring around the digit** — only on the launcher icon for now.
- **`values-night/colors.xml`** — `windowBackground` is fixed to light paper. In dark mode, the area behind the translucent system bars during a brief moment of activity recreate shows the light value. Trivial to fix when needed.
