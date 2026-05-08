package app.shizentai.timer.engine

import kotlinx.serialization.Serializable

@Serializable
data class TimerConfig(
    val workSeconds: Int,
    val restSeconds: Int,
    val rounds: Int,
    val prepSeconds: Int,
    val warningSeconds: Int,
    val signalEndOfRest: Boolean,
)
