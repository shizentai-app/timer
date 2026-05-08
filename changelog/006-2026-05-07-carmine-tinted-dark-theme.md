# Carmine-tinted dark theme + paperBasement surface level

**Date:** 2026-05-07 · **Files:** `core/theme/ShizentaiColors.kt`, `core/ui/BottomNav.kt`, `res/values-night/colors.xml`, `web/app/src/styles/tokens.css`, `web/app/src/components/BottomNav.tsx`

## Why

User feedback: dark theme felt "too dark" on the Pixel 4 XL. Surfaces all blended into a single near-black slab — no visible difference between the screen background and a card on it. Also: the tone was warm-neutral (`#14130F`/`#1C1B17`), which read as "default Material dark" rather than as a Shizentai screen.

The main `shizentai/` app had already worked through this in `shizentai/docs/THEME.md` (a living spec doc). It defines a **carmine-tinted, level-numbered dark hierarchy** — not "black", but "deep brand-tinted" with five distinct surface elevations. Adopting that spec in `timer/` gives us:

1. The brand is present on empty screens (no need to wait for content to recognise the app — same pattern as Monobank's blue).
2. Surfaces of different depths are visually distinct (shadows don't render on near-black, so we rely on tonal contrast instead).
3. Family coherence — when both apps end up on the same device, they look like siblings rather than two unrelated training tools.

## What changed

**Palette shift (dark only — light is untouched):**

| Token | Before | After | Role |
|---|---|---|---|
| `paperBasement` (new) | — | `#0F0A0A` | Bottom navigation bar |
| `paper` | `#14130F` | `#1A1212` | Screen background |
| `paper2` | `#1C1B17` | `#241818` | Cards, list items, sheets |
| `paper3` | `#25231D` | `#2E1F1F` | Input fields, picker chips |
| `line` | `#2E2B23` | `#4A3434` | Tonal separators |
| `line2` | `#3D3A30` | `#5A4040` | Heavier dividers |
| `phases.prepare/finished` | `#14130F` | `#1A1212` | Match new screen bg |

**Code changes:**

- `ShizentaiColors.kt` (Android): added `paperBasement: Color` to the data class; populated for both Light (`#FFFFFF`) and Dark (`#0F0A0A`) palettes; updated all dark-mode colors per the table.
- `tokens.css` (Web): added `--paper-basement` to `:root` (white) and `[data-theme="dark"]` (`#0F0A0A`); updated dark-block paper levels per the table.
- `BottomNav.kt` (Android): switched background from `Shizentai.colors.paper` to `Shizentai.colors.paperBasement`. The bar is now darker than the screen — the eye reads "this is below the content".
- `BottomNav.tsx` (Web): same — `var(--paper)` → `var(--paper-basement)`.
- `values-night/colors.xml`: synced `paper` to `#1A1212` so the splash window background continues to match the in-app `paper` token in dark mode.

**Built and tested:**

- Android: `:app:assembleDebug` + `:app:testDebugUnitTest` ✓ (25/25)
- Web: `npm run typecheck` ✓ · `npm test` ✓ (22/22)

## How it was decided

- **Adopted THEME.md's exact hex values** rather than picking close-but-different shades — the whole point of the spec is to give the family one consistent palette. Drift between `shizentai` and `timer` in dark mode would be the visible kind: cards don't quite match across the two apps. Aligning means a future shared design-system extraction is a copy, not a merge.
- **Added `paperBasement` rather than darkening `paper`.** Nav bar needs to be *below* content. Two options: (1) keep paper as is, lower the nav explicitly via a new token; (2) make paper itself darker. Picked (1) because Level 0 is conceptually different from Level 1 — selecting a token should mean something. With (2), every nav-related component would need a custom shadow or border to pop, which is exactly what the spec rejects ("ієрархія через рівні, не через тіні").
- **Did not introduce `paperHighlight` (Level 4)** despite it being in the spec. The timer doesn't currently have any UI that distinguishes "selected" from "elevated" — the preset list uses `paper2` for cards, `accent` for the active item. If a future feature needs the distinction (e.g. a row that's both raised and selected), add it then.
- **Light mode untouched.** The spec also defines a light hierarchy, but the light tokens already match it well enough (`paper3 #E8E1D3` ≈ spec `#F5F0E8`, etc.) and the user only complained about dark. Premature change.
- **`line` jumped two steps lighter** (`#2E2B23` → `#4A3434`) — this looks aggressive in a diff but matches the spec's `BgOutline` value. The reason: on `#1A1212` paper, a `#2E2B23` line is barely a 0.5-stop separation; testers reported "I can't see where the card ends". `#4A3434` reads as a real edge while still feeling tonal rather than harsh.

## Known follow-ups

- **Light-mode parity sweep** — only worth doing if light dark-tones drift in a future change. Right now the light values are close enough to the spec that re-shifting them would just be churn.
- **`paperHighlight` (Level 4)** — add when there's an actual UI need, not preemptively.
- **Revisit `line2`** — extrapolated to `#5A4040` (one step above `line`); not in the spec table. If the spec adds it later, sync.
- **Validate on a real device.** The next install is the first chance to confirm the new palette reads as we expect on the Pixel 4 XL OLED panel — OLED can render `#0F0A0A` and `#1A1212` more distinct than they look in mockups.
