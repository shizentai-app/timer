# Architecture

## Two clients, one shape

`shizentai-timer` ships as two independent codebases that mirror each other in feature surface and data shape:

```
Android (Kotlin + Compose + Hilt)  ←→  Web (React + TS + React Router)
                  ↓                              ↓
            DataStore<Preferences>          localStorage
            res/raw/*.wav                   public/sounds/*.wav
```

There is no shared backend. Both clients are self-contained.

## Timer engine

The engine is the only piece of logic that's worth porting carefully — everything else is glue. It's intentionally pure (no Compose, no DOM), so the same test cases run on both sides.

### Inputs

```
TimerConfig {
  workSeconds: Int
  restSeconds: Int
  rounds: Int
  prepSeconds: Int
  warningSeconds: Int            // yellow countdown at end of work
  signalEndOfRest: Boolean       // emit "10s before rest ends" cue
}
```

### Phase machine

```
IDLE  →  PREPARE
            ↓
        ┌─→ WORK
        │     ↓ (last warningSeconds)
        │   WARNING
        │     ↓
        │   REST
        │     ↓ (when signalEndOfRest && remaining ≤ 10s)
        │   REST_ENDING_CUE (one-shot flag, not a phase)
        │     ↓
        └── (if more rounds) WORK ... else FINISHED
```

### Outputs

```
TimerState {
  phase: Phase
  round: Int                     // 1-indexed; 0 during PREPARE/IDLE
  remainingMs: Long
  totalMs: Long                  // duration of the current phase
  cue: AudioCue?                 // one-shot, set on phase transition
}
```

### Tick

100 ms internal tick. The engine takes an injectable Clock + tick scheduler so tests run synchronously (`StandardTestDispatcher` on Android, `vi.useFakeTimers()` on Web) without real waits.

### Pause / resume

Pause snapshots `remainingMs`. Resume re-anchors the clock against that snapshot. The same engine instance survives pause/resume — the Compose `ViewModel` and React hook each hold a single instance for the lifetime of the screen.

## Persistence

| What | Android | Web |
|---|---|---|
| Custom presets | DataStore<Preferences> key `timer.presets.custom` (JSON via kotlinx-serialization) | localStorage `shizentai-timer.presets.custom` |
| Settings (vibration, keep-screen-on, language, per-event sound, custom colors) | DataStore<Preferences> key `timer.settings` | localStorage `shizentai-timer.settings` |
| Theme mode | (mirrors web pattern via DataStore key `timer.theme`) | localStorage `shizentai-timer.theme` (system / light / dark) |

Built-in presets are **constants in source**, not stored. Adding a new built-in is a code change in both clients.

## Audio

- **Android** — `SoundPool` for all four cues. Loaded once on `SoundPlayer` init from `res/raw/`. Low-latency playback, no MediaPlayer state churn.
- **Web** — `HTMLAudioElement` pool keyed by event name. Preloaded on first user gesture (any tap on the Timer screen) to satisfy autoplay policies. Files served from `public/sounds/`.

Both expose the same API: `play(event: AudioEvent)` where `AudioEvent` ∈ {`work_start`, `work_warning`, `rest_start`, `rest_ending`, `finished`}.

## Haptics

- **Android** — `VibratorManager` with phase-specific intensities (heavy on WORK/REST/FINISHED, medium on PREPARE/WARNING). Gated by user toggle in Settings.
- **Web** — `navigator.vibrate(pattern)` with graceful no-op when unsupported.

## Screen wake

- **Android** — `WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON` set on the activity Window via a `DisposableEffect` while the timer is running.
- **Web** — `navigator.wakeLock.request('screen')`. Released on unmount and on `visibilitychange`-hidden, re-acquired on visible.

## i18n

- **Android** — native `res/values{,-uk,-ja}/strings.xml`. The locale follows the system per-app language (Android 13+) via `xml/locales_config.xml`; no in-app picker.
- **Web** — a single hand-written hook in `src/i18n/index.ts` exposes `useTranslation()` returning `{ locale, setLocale, t }`. Three flat dictionaries (`en`, `uk`, `ja`) live inline; default locale detected from `navigator.language`; user override persists in `localStorage["shizentai-timer.locale"]`. The Settings screen has a segmented language picker.

Keys and parameter names are kept identical across the two clients so translations stay in sync when scanned side-by-side.

## What lives where

```
android/app/src/main/kotlin/app/shizentai/timer/
  ShizentaiTimerApp.kt            @HiltAndroidApp
  MainActivity.kt                 single activity
  nav/TimerNavGraph.kt            @Serializable routes
  core/
    theme/                        ShizentaiColors, Typography, Theme, Shapes, Spacing, ReducedMotion
    ui/                           BottomNav, (Btn, Card, Sheet, etc. ported as features land)
    audio/SoundPlayer.kt          [Phase 5]
    haptics/Haptics.kt            [Phase 5]
    screen/KeepScreenOn.kt        [Phase 5]
  engine/                         TimerEngine, TimerConfig, Phase, TimerState — pure Kotlin [Phase 3]
  data/
    presets/                      PresetRepository, BuiltInPresets [Phase 4]
    settings/                     SettingsRepository [Phase 5/6]
  feature/
    timer/                        TimerScreen, TimerViewModel, TimerRing
    presets/                      PresetsScreen, PresetsViewModel, PresetEditorSheet
    settings/                     SettingsScreen, SettingsViewModel, ColorPickerSheet
  di/AppModule.kt                 Hilt — DataStore, SoundPlayer, Haptics

web/app/src/
  main.tsx, App.tsx
  styles/{tokens.css, global.css}
  theme/ThemeProvider.tsx
  components/{Shell, BottomNav}
  lib/
    timer.ts                      pure-TS engine [Phase 2]
    audio.ts                      [Phase 5]
    haptics.ts, wakeLock.ts       [Phase 5]
  data/{presets.ts, settings.ts}  [Phase 4/5]
  features/{timer, presets, settings}/
  i18n/{index.ts, en.json, uk.json, ja.json}    [Phase 6]
  test/setup.ts
```
