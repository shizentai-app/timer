package app.shizentai.timer.data.presets

import app.shizentai.timer.engine.TimerConfig
import kotlinx.serialization.Serializable

@Serializable
data class Preset(
    val id: String,
    val name: String,
    val config: TimerConfig,
    val isBuiltIn: Boolean = false,
)
