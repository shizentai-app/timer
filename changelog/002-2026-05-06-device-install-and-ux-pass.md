# First device install + UX pass (emerald, edge-to-edge, larger digit)

**Date:** 2026-05-06 · **Files:** `core/theme/`, `core/screen/SystemBars.kt`, `feature/timer/TimerScreen.kt`, `nav/TimerNavGraph.kt`, web counterparts

## Why

Built v1 from `001`. Got the APK onto a Pixel 4 XL (`adb install`). Did a real user pass with a sensei-aware reviewer. Found one functional bug, one icon-rendering bug, and a list of tonal/UX issues that wanted addressing before further polish.

## What changed

### Bugs fixed

1. **PREPARE / FINISHED phase had invisible text.** Phase palette returned `(colors.phases.prepare, colors.ink)` — but `phases.prepare` and `colors.ink` are both `#1A1A1A` in light mode. Dark text on dark bg → user only saw the "Pause" button (white-on-dark via `PrimaryButton`'s contrast logic). Fix: PREPARE/FINISHED ink is now `Color.White`. Same fix on web.
2. **Launcher icon arc invisible.** The 270° arc was scaled too aggressively into the adaptive-icon safe zone (uniform 0.78 around 1024 viewport with stroke width 22 → ~1.5 dp on a launcher tile, basically a hairline). Fix: dropped scale to 0.65 and bumped stroke to 56. Now reads clearly at 48 dp launcher size.

### UX overhaul (per reviewer feedback)

Reviewer prioritized 6 items in two tiers. Implemented all six:

| # | Change | Files |
|---|---|---|
| 1 | **Edge-to-edge phase color.** Status bar + nav bar adopt the phase tint via `SystemBarsColor` (new file `core/screen/SystemBars.kt`). The timer's `Box` lost its rounded corners + outer 16dp padding so the color is the *flag*, not a card. | `SystemBars.kt`, `TimerScreen.kt`, `TimerNavGraph.kt` |
| 2 | **Brand-coherent palette.** WORK was Material green-500 (`#4F8F4A`) → dojo emerald `#1B5E20` light / `#2E7D32` dark. REST stayed at brand carmine `#8B1818` (already correct). | `ShizentaiColors.kt`, `tokens.css` |
| 3 | **Larger digit.** `timerDigit` style 120 sp → 168 sp baseline. Then auto-shrunk to fit available width via `BoxWithConstraints` + a per-frame computed font size (`widthSp / 2.8f`) clamped 72–200 sp. | `ShizentaiTypography.kt`, `TimerScreen.kt` |
| 4 | **Phase command typography.** "Hajime!" was 13 sp body caption — reviewer noted "voiced commands deserve the weight of a sensei calling them". New `phaseCommand` style: 34 sp ExtraBold + 0.06em letter-spacing. | `ShizentaiTypography.kt`, `TimerScreen.kt` |
| 5 | **Skip/Reset visible only when paused.** During an active phase the only action is the big Pause button — eliminates the foot-tap-Reset risk when the phone is on the floor. | `TimerScreen.kt` |
| 6 | **Bottom-nav auto-hide while running** (initially shipped). | `TimerNavGraph.kt` |

### Reverted on user push-back

Item 6 — auto-hiding the bottom nav — was rolled back the same day. The slide-in/out animation made the entire layout jump on every Start/Pause and obscured the user's mental map of "where is the menu". Now the nav is **always visible**; the phase color stops at the top of the nav rather than bleeding behind it.

The unused state plumbing (`feature/timer/TimerActiveState.kt` + the `setTimerRunning` writes from `useTimerEngine`) is left in place — it costs nothing, and we may want it later if we revisit option B (nav matches phase color instead of disappearing).

### Layout follow-up: button row overflow in Ukrainian

Once language switching worked (`003`), Ukrainian "Скинути" / "Продовжити" overflowed the bottom action row — three buttons of fixed width didn't fit on Pixel 4 XL. Restructured: primary action always on its own row; Skip + Reset on a second row below, each with `Modifier.weight(1f)` so they share the available width equally. Robust across all locales.

## How it was decided

- **System-bar tinting via `SideEffect` + `DisposableEffect(Unit)`** — `SideEffect` re-applies on every recomposition (no flicker between phase changes), `DisposableEffect(Unit)` only restores originals on full leave (not on color change). Captured originals via a one-shot `remember` so what gets restored is the activity defaults, not whatever phase color we last set.
- **Auto-fit digit via `BoxWithConstraints`** — no library needed. Manrope ExtraBold tabular numerals are ~0.55em per glyph, "MM:SS" is 5 glyphs ≈ 2.75em, so font size = available width / 2.8 fits with margin. Tried fixed 168 sp first → wrapped on Pixel 4 XL.
- **Centering the auto-fit digit** — `BoxWithConstraints` does NOT auto-center children. Added `contentAlignment = Alignment.Center` after the digit shipped left-aligned in the first attempt.
- **Bottom nav: "always visible" beat "matches phase color"** — the user's literal request was "keep the menu all the time". The phase color stopping at the nav line is a small visual seam but removes all ambiguity about where they can navigate.

## Known follow-ups

- The unused `timerIsRunning` state (`feature/timer/TimerActiveState.kt`) and its companion `web/app/src/state/timerActive.ts` — keep or delete next session.
- The `windowBackground` is set to `@color/paper` (light cream) in `Theme.ShizentaiTimer.Splash`. In dark mode it'd flash light during any future activity recreate (but we've eliminated recreates from the language switch — see `003`).
