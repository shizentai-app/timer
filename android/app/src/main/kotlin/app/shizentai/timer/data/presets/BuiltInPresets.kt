package app.shizentai.timer.data.presets

import app.shizentai.timer.engine.TimerConfig

val BUILT_IN_PRESETS: List<Preset> = listOf(
    Preset(
        id = "kumite-30s",
        name = "Kumite 30 sec",
        config = TimerConfig(workSeconds = 30, restSeconds = 30, rounds = 3, prepSeconds = 10, warningSeconds = 10, signalEndOfRest = true),
        isBuiltIn = true,
    ),
    Preset(
        id = "kumite-1",
        name = "Kumite 1 min",
        config = TimerConfig(workSeconds = 60, restSeconds = 30, rounds = 3, prepSeconds = 10, warningSeconds = 10, signalEndOfRest = true),
        isBuiltIn = true,
    ),
    Preset(
        id = "kumite-2",
        name = "Kumite 2 min",
        config = TimerConfig(workSeconds = 120, restSeconds = 60, rounds = 3, prepSeconds = 10, warningSeconds = 10, signalEndOfRest = true),
        isBuiltIn = true,
    ),
    Preset(
        id = "kumite-3",
        name = "Kumite 3 min",
        config = TimerConfig(workSeconds = 180, restSeconds = 60, rounds = 3, prepSeconds = 10, warningSeconds = 10, signalEndOfRest = true),
        isBuiltIn = true,
    ),
    Preset(
        id = "classic-boxing",
        name = "Classic boxing",
        config = TimerConfig(workSeconds = 180, restSeconds = 60, rounds = 12, prepSeconds = 5, warningSeconds = 10, signalEndOfRest = true),
        isBuiltIn = true,
    ),
    Preset(
        id = "amateur-boxing",
        name = "Amateur boxing",
        config = TimerConfig(workSeconds = 120, restSeconds = 60, rounds = 4, prepSeconds = 5, warningSeconds = 10, signalEndOfRest = true),
        isBuiltIn = true,
    ),
    Preset(
        id = "mma",
        name = "MMA",
        config = TimerConfig(workSeconds = 300, restSeconds = 60, rounds = 5, prepSeconds = 5, warningSeconds = 10, signalEndOfRest = true),
        isBuiltIn = true,
    ),
    Preset(
        id = "tabata",
        name = "Tabata",
        config = TimerConfig(workSeconds = 20, restSeconds = 10, rounds = 8, prepSeconds = 10, warningSeconds = 5, signalEndOfRest = false),
        isBuiltIn = true,
    ),
)

val DEFAULT_PRESET: Preset = BUILT_IN_PRESETS.first()
