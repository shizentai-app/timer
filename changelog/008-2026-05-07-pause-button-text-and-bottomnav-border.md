# Pause-button text invisible in dark mode + bottom-nav side borders

**Date:** 2026-05-07 · **Files:** `feature/timer/TimerScreen.kt`, `web/app/src/features/timer/TimerScreen.tsx`, `core/ui/BottomNav.kt`

## Why

After deploying the dark-theme update from `006`, the Pixel reported two regressions:

1. The pause/start/resume button (a white pill) sat with **invisible text** during PREPARE and other phases — `colors.ink` is now `#F4EFE3` (cream) in dark mode, and that's the colour the button used for its label. Cream text on a white pill is functionally blank.
2. The bottom navigation showed thin **left and right border lines** at the screen edges. Cosmetic but ugly — the eye reads them as "the nav is a card with a frame", which it isn't.

The first one was introduced indirectly by `006`: the `isWhiteInk` branch of `PrimaryButton` used `Shizentai.colors.ink` for the text, which in light mode happens to be `#1A1A1A` (dark) so it worked, but in dark mode flips to cream. The bug was always latent; the dark palette change exposed it.

The second was a Compose-vs-CSS mismatch: the web `BottomNav.tsx` already used `borderTop` (single edge), but `BottomNav.kt` used `Modifier.border(BorderStroke(...))` which paints all four sides.

## What changed

**`PrimaryButton` (Android + Web): tone-locked text colour for the white pill.**

The pill is white regardless of theme, so its text colour also has to be theme-independent. Hardcoded `#1A1A1A` (the same value `colors.ink` carries in light mode). Added a one-line comment explaining the trap so the next person doesn't accidentally swap it back to `colors.ink`.

```kotlin
contentColor = if (isWhiteInk) Color(0xFF1A1A1A) else Shizentai.colors.paper
```

```ts
color: isWhiteInk ? "#1A1A1A" : "var(--paper)"
```

**`BottomNav.kt` (Android): top-only separator via `drawBehind`.**

Replaced `.border(BorderStroke(1.dp, colors.line))` with a `drawBehind` that draws a single line at `y=0` only. Removed the now-unused `BorderStroke` and `border` imports. Added `drawBehind` and `geometry.Offset` imports.

```kotlin
.drawBehind {
    drawLine(
        color = lineColor,
        start = Offset(0f, 0f),
        end = Offset(size.width, 0f),
        strokeWidth = 1.dp.toPx(),
    )
}
```

The web side already had this right (`borderTop: "1px solid var(--line)"`), so no change there.

## How it was decided

- **Tone-lock to a literal hex rather than introducing a new "always-dark" token.** A token would communicate intent better but adds a vocabulary item used in exactly one place. If a third place ever needs the same value, promote then. For now, the inline comment carries the intent.
- **`drawBehind` rather than wrapping the Row in a Column with a 1dp Box.** `drawBehind` doesn't change the layout tree (no extra parent), keeps the `modifier` chain readable, and renders identically. The Box approach would have meant restructuring `paddingNavigationBars` and `weight` distribution.
- **Did not introduce a `Modifier.borderTop` extension.** Tempting, but only one caller; the inline `drawBehind` is shorter than the helper would be.

## Known follow-ups

- Reset/Skip secondary buttons (visible only when paused) still use `var(--ink)` / `Shizentai.colors.ink` for their text via the phase-ink prop. They sit on the **phase-coloured background**, so the cream-on-dark contrast is fine — but worth eyeballing once a Ukrainian Pixel screenshot in WORK/REST mode shows up.
- If a future palette change introduces a third "phase ink" colour besides white and dark, the `isWhiteInk == Color.White` branch logic needs revisiting.
