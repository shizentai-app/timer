# In-app language picker (and a debugging journey)

**Date:** 2026-05-07 · **Files:** `AndroidManifest.xml`, `feature/settings/SettingsScreen.kt`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `res/values{,-uk,-ja}/strings.xml`, `themes.xml`

## Why

We had en/uk/ja translations from day one (`values-uk/strings.xml`, etc.) but no way to pick a language inside the app — users had to dig into system Settings → Apps → Shizentai Timer → Language. The user asked for an in-app picker on the Settings screen.

This entry documents the debugging journey because the API surface is genuinely confusing: there are three layers (system `LocaleManager`, AppCompat's wrapper, and manifest `configChanges`) and only one combination produces a clean experience on Android 13+.

## What landed (the working solution)

1. **Manifest:** declared `android:configChanges="locale|layoutDirection|uiMode|fontScale|orientation|screenSize|smallestScreenSize|screenLayout|keyboardHidden"` on `MainActivity`. With this, the OS does *not* recreate the activity when the locale changes — it calls `onConfigurationChanged` instead, the activity stays alive, Compose's `LocalConfiguration` updates, and every `stringResource` re-evaluates. **No flash**, no restart.
2. **Picker UI** in `SettingsScreen.kt` — a fourth setting row with four Material `Button` chips: System / English / Українська / 日本語.
3. **Locale write:** on Tiramisu+ (API 33), goes straight to `LocaleManager.setApplicationLocales(LocaleList.forLanguageTags(tag))`. On older devices, falls back to `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(tag))`.
4. **Locale read** for "what's currently selected" comes from the same source — `LocaleManager.applicationLocales` on API 33+, `AppCompatDelegate.getApplicationLocales()` below.
5. **Persistence** — `LocaleManager` persists per-app locale natively on API 33+. For older devices, AppCompat persists via the `<service android:name="androidx.appcompat.app.AppLocalesMetadataHolderService">` + `meta-data autoStoreLocales=true` declared in the manifest.
6. **Strings** — added `settings_language`, `settings_language_desc`, `lang_system`, `lang_en`, `lang_uk`, `lang_ja` to all three `strings.xml` files.

Dependency added: `androidx.appcompat:appcompat:1.7.0` — used only for the legacy-API path. The activity stays a plain `ComponentActivity`; we never extend `AppCompatActivity`.

## How it was decided (what didn't work, and why)

This took five iterations on-device. The key failure modes:

### Attempt 1 — Custom `Box.clickable` segments
Built the picker as a `Row` of `Box(Modifier.weight(1f).clickable {…})`. **Failed silently** — the user reported "buttons are not clickable, no visual feedback". Root cause never fully nailed down (likely a clickable-region issue with `weight` + `background(Transparent)`), but the fix was to switch to Material `Button` components which guarantee click handling and ripple. **Lesson: don't roll your own click target inside a Row of weighted Boxes.**

### Attempt 2 — `AppCompatDelegate.setApplicationLocales` + `recreate()`
Click fired (ripple visible), but the locale never changed. Root cause: `AppCompatDelegate.setApplicationLocales` is async on API 33+ — it dispatches to `LocaleManager` under the hood, but the explicit `recreate()` we called immediately afterwards preempted the async write. The activity restarted with the *old* locale, and the new write landed too late to matter. **Lesson: don't combine AppCompat's async locale wrapper with an explicit recreate. Use one or the other.**

### Attempt 3 — `LocaleManager` direct + explicit `recreate()`
Skipped AppCompat entirely on API 33+, called `LocaleManager.applicationLocales = …` (synchronous), then `activity.recreate()`. **Locale changed correctly** — but produced a jarring black flash. The OS also schedules its own config-change-driven recreate after the locale write; ours plus the system's collided, leaving a blank window between them.

### Attempt 4 — `LocaleManager` direct, no explicit recreate
Removed our `recreate()` call, relied on the system. **Still flashed** — even a single OS-driven recreate shows a black moment between the destroyed and re-created activity. Tried mitigations on the splash theme: `windowDisablePreview="true"` and `windowAnimationStyle="@null"`. Neither addresses the flash, because the flash is not the splash window or the activity animation — it's the empty window between the two activity instances. **Both flags reverted.**

### Attempt 5 (working) — `configChanges` so the activity never restarts
Added `android:configChanges="locale|…"` to the activity. The OS hands the live activity a new `Configuration` instead of destroying + recreating it. Compose's `AndroidComposeView` listens to `onConfigurationChanged` and updates `LocalConfiguration`, which makes every `stringResource` re-evaluate against the new locale. No window destruction, no flash. This is what shipped.

## Known follow-ups

- **Configuration changes are now app-handled across the board** (we listed a wide set: locale, layoutDirection, uiMode, fontScale, orientation, screenSize, smallestScreenSize, screenLayout, keyboardHidden). For a portrait-locked timer this is fine, but if anyone adds landscape later they'll need to verify nothing breaks on rotation.
- **The legacy AppCompat path** (API < 33) was not tested on a real pre-Tiramisu device. Pixel 4 XL is API 33; that's the path we exercised.
- **Web client** uses its own `useTranslation` hook with localStorage persistence and a segmented theme/language picker on the Settings screen — see `001`. Not affected by this entry.
- **The Skip/Reset button row overflow** discovered in Ukrainian (`Скинути` is wider than `Reset`) was fixed in the same session and documented at the end of `002` rather than here.
