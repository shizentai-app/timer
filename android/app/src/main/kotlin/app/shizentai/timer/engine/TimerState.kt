package app.shizentai.timer.engine

data class TimerState(
    val phase: Phase = Phase.IDLE,
    val isRunning: Boolean = false,
    val round: Int = 0,
    val totalRounds: Int = 0,
    val remainingMs: Long = 0L,
    val totalMs: Long = 0L,
)
