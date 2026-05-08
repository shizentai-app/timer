# Cleanup: delete unused timer-running plumbing + add dark splash

**Date:** 2026-05-07 · **Files:** `feature/timer/TimerActiveState.kt` (deleted), `feature/timer/TimerViewModel.kt`, `web/app/src/state/timerActive.ts` (deleted), `web/app/src/features/timer/useTimerEngine.ts`, `android/app/src/main/res/values-night/colors.xml` (new), `engine/TimerEngineTest.kt`

## Why

Two open follow-ups from `002` were "decide next session whether to keep this":

1. The `timerIsRunning` state on Android (and its mirror `setTimerRunning` / `useTimerRunning` on web) was wired to support hiding the bottom nav during an active phase. That feature was reverted — the user pushed back ("keep the menu all the time") because the slide animation made the layout jump on every Start/Pause. The plumbing was left in place "in case we revisit". Confirmed today: we are staying with always-visible nav. So the writes/reads were dead code.
2. `windowBackground` on the splash theme was hard-pinned to `@color/paper` (light cream). In dark mode, any splash flash would render light-on-dark for one frame. We eliminated activity recreates already (see `003`), but cold-launch splash still uses this resource — visible inconsistency.

## What changed

**Android:**
- Deleted `feature/timer/TimerActiveState.kt`.
- Removed the single `timerIsRunning.value = running` write in `TimerViewModel.init {}`. The collector still runs (it drives `startTickLoop` / `stopTickLoop`), so behavior is unchanged.
- Added `res/values-night/colors.xml` overriding `paper` → `#14130F` (the same hex that was already defined as `paper_dark` in the light values). The splash theme references `@color/paper`, so resource resolution does the right thing in dark mode without touching `themes.xml`.
- Fixed pre-existing bug in `TimerEngineTest > does not emit rest_ending when signalEndOfRest is false` — the Kotlin port had an extra `WORK_WARNING` assertion at `tick=15_000` that doesn't exist in the parallel TS spec, and would never fire (tabata WORK starts at 10s, ends at 30s, `warningSeconds=5` → WARNING needs `tick ≥ 25_000`). Removed the bogus line so the test mirrors `web/app/src/lib/timer.test.ts` exactly. 25/25 tests now pass.

**Web:**
- Deleted `src/state/timerActive.ts` (and the now-empty `src/state/` directory).
- Removed the `setTimerRunning` import + 3 call sites in `useTimerEngine.ts` (the running-state mirror `useEffect` and the unmount-cleanup `useEffect`). Tick-loop `useEffect` is unchanged.

## How it was decided

- **Override `@color/paper` in `values-night/`** rather than touching `themes.xml`. There's only one consumer of `@color/paper` in resource land (the splash `windowBackground`), and Compose theme tokens read directly from `ShizentaiColors.kt`, not from XML. So the override is surgical: it changes only the splash background, and only in dark mode. Adding a separate `Theme.ShizentaiTimer.Splash` override in `values-night/themes.xml` would be more typing for the same effect.
- **Fixed the broken Kotlin test** rather than skipping it or filing a follow-up. The TS test file has been the canonical spec for engine behavior since `003` (Phase 3 explicitly says "translate `lib/timer.ts` line-for-line"). The Kotlin test diverging from the spec is a regression in testing discipline, not a real engine question. One-line fix; better caught now than left as a `@Ignore`.
- **Did not migrate `setTimerRunning` to a no-op shim** for callers — there were no external callers (the writes were inside the file we owned), so a clean delete was correct.

## Known follow-ups

None for this thread. The two items left open from `002` are both closed.
