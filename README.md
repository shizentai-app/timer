# Shizentai Timer

> Інтервал-таймер для тренувань. Кармін, кандзі 自, без облікового запису, без аналітики, без реклами.

🇺🇦 Українська · [🇬🇧 English](#english)

---

## Що це

Локальний інтервал-таймер з раундами, паузами, попередженнями і звуковими сигналами. Без сервера, без логіну — відкрив і користуєшся. Створений як друга програма сім'ї **Shizentai** (див. [`../.github/FAMILY.md`](../.github/FAMILY.md)).

### Можливості v1

- Раунди, тривалість роботи й відпочинку, час підготовки, попередження перед кінцем раунду
- Сигнал перед кінцем відпочинку («залишилось 10 секунд»)
- Вбудовані пресети: Кумітé 30с/1хв/2хв/3хв, бокс класичний, бокс аматорський, MMA, Tabata
- Власні пресети — створення, видалення, локальне збереження
- 4 вбудовані звуки (gong, gong_twice, rest_end, alert) — обираються окремо для кожної події
- Власні кольори фази, темна/світла тема
- Англійська, українська, японська локалізація
- Японські команди як стандарт: «Hajime!», «Yame!», «Yoi!», «Kiai!»
- Вібрація, утримання екрана увімкненим (Android)

### Чого нема (і не буде у v1)

- Сторонні аудіозаписи (мікрофон), голосові пакети
- Хмарна синхронізація, обліковий запис
- iOS-клієнт
- Сповіщення у фоні

## Встановлення з вихідного коду

### Android

```bash
cd android
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Потрібно: JDK 17, Android SDK 35.

### Веб

```bash
cd web/app
npm install
npm run dev   # http://localhost:5174
```

Потрібно: Node 20.

## Документація

- [`docs/architecture.md`](docs/architecture.md) — як влаштований движок таймера
- [`docs/dev-setup.md`](docs/dev-setup.md) — детальніша інструкція для розробника
- [`CLAUDE.md`](CLAUDE.md) — повний контекст проєкту (англ.)

---

## English

Interval timer for training. Carmine, 自 kanji, no account, no analytics, no ads.

### What it is

A local-only interval timer with rounds, rest periods, warnings, and audio cues. No server, no login — open and use. Built as the second product in the **Shizentai** family (see [`../.github/FAMILY.md`](../.github/FAMILY.md)).

### v1 features

- Rounds, work/rest durations, prepare time, warning before round end
- "10 seconds left" cue before rest ends
- Built-in presets: Kumite 30s/1m/2m/3m, classic boxing, amateur boxing, MMA, Tabata
- Custom presets — create, delete, persist locally
- Four bundled sounds (gong, gong_twice, rest_end, alert), pickable per event
- Custom phase colors, light/dark theme
- English, Ukrainian, Japanese localization
- Japanese commands by default: "Hajime!", "Yame!", "Yoi!", "Kiai!"
- Vibration, keep-screen-on (Android)

### Out of scope for v1

- Audio recording (microphone), voice packs
- Cloud sync, account
- iOS client
- Background notifications

### Build from source

**Android** — JDK 17, Android SDK 35:

```bash
cd android
./gradlew :app:assembleDebug
```

**Web** — Node 20:

```bash
cd web/app
npm install
npm run dev   # http://localhost:5174
```

### Docs

- [`docs/architecture.md`](docs/architecture.md) — timer engine internals
- [`docs/dev-setup.md`](docs/dev-setup.md) — full dev environment setup
- [`CLAUDE.md`](CLAUDE.md) — full project context

## License

MIT — see [`LICENSE`](LICENSE).
