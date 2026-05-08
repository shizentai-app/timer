package app.shizentai.timer.engine

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Pure interval-timer engine. No coroutines, no Android. The consumer drives
 * ticks via tick(now). Mirrors web/app/src/lib/timer.ts line-for-line.
 *
 * State updates are exposed via StateFlow; one-shot audio cues via SharedFlow.
 */
class TimerEngine {

    private val _state = MutableStateFlow(TimerState())
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _cues = MutableSharedFlow<AudioCue>(
        replay = 0,
        extraBufferCapacity = 8,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val cues: SharedFlow<AudioCue> = _cues.asSharedFlow()

    private var sequence: List<PhaseStep> = emptyList()
    private var seqIndex: Int = 0
    private var phaseStartedAt: Long = 0L
    private var elapsedAtPause: Long = 0L
    private var restEndCueFired: Boolean = false
    private var warningEntered: Boolean = false
    private var config: TimerConfig? = null

    fun start(config: TimerConfig, now: Long) {
        this.config = config
        sequence = buildSequence(config)
        seqIndex = 0
        phaseStartedAt = now
        elapsedAtPause = 0L
        restEndCueFired = false
        warningEntered = false

        val first = sequence.firstOrNull()
        if (first == null) {
            emitState(TimerState(phase = Phase.FINISHED, totalRounds = config.rounds))
            tryEmitCue(AudioCue.FINISHED)
            return
        }

        emitState(
            TimerState(
                phase = first.phase,
                isRunning = true,
                round = first.round,
                totalRounds = config.rounds,
                remainingMs = first.durationMs,
                totalMs = first.durationMs,
            )
        )
        when (first.phase) {
            Phase.WORK -> tryEmitCue(AudioCue.WORK_START)
            Phase.REST -> tryEmitCue(AudioCue.REST_START)
            else -> Unit
        }
    }

    fun pause(now: Long) {
        if (!_state.value.isRunning) return
        elapsedAtPause += now - phaseStartedAt
        emitState(_state.value.copy(isRunning = false))
    }

    fun resume(now: Long) {
        val s = _state.value
        if (s.isRunning) return
        if (s.phase == Phase.IDLE || s.phase == Phase.FINISHED) return
        phaseStartedAt = now
        emitState(s.copy(isRunning = true))
    }

    fun reset() {
        sequence = emptyList()
        seqIndex = 0
        phaseStartedAt = 0L
        elapsedAtPause = 0L
        restEndCueFired = false
        warningEntered = false
        config = null
        emitState(TimerState())
    }

    fun skip(now: Long) {
        if (config == null) return
        advanceToPhase(seqIndex + 1, now)
    }

    fun tick(now: Long) {
        val current = _state.value
        if (!current.isRunning) return
        val cfg = config ?: return
        val step = sequence.getOrNull(seqIndex) ?: return

        val phaseElapsed = elapsedAtPause + (now - phaseStartedAt)
        val remaining = (step.durationMs - phaseElapsed).coerceAtLeast(0L)

        if (remaining <= 0L) {
            advanceToPhase(seqIndex + 1, now)
            return
        }

        var displayPhase: Phase = step.phase
        var warningJustEntered = false
        if (step.phase == Phase.WORK && cfg.warningSeconds > 0) {
            if (remaining <= cfg.warningSeconds * 1000L) {
                displayPhase = Phase.WARNING
                if (!warningEntered) {
                    warningJustEntered = true
                    warningEntered = true
                }
            }
        }

        var restEndingJustFired = false
        if (
            step.phase == Phase.REST &&
            cfg.signalEndOfRest &&
            !restEndCueFired &&
            remaining <= REST_END_CUE_MS
        ) {
            restEndingJustFired = true
            restEndCueFired = true
        }

        emitState(
            TimerState(
                phase = displayPhase,
                isRunning = true,
                round = if (step.round != 0) step.round else current.round,
                totalRounds = current.totalRounds,
                remainingMs = remaining,
                totalMs = step.durationMs,
            )
        )
        if (warningJustEntered) tryEmitCue(AudioCue.WORK_WARNING)
        if (restEndingJustFired) tryEmitCue(AudioCue.REST_ENDING)
    }

    private fun advanceToPhase(nextIdx: Int, now: Long) {
        val current = _state.value
        if (nextIdx >= sequence.size) {
            emitState(
                current.copy(
                    phase = Phase.FINISHED,
                    isRunning = false,
                    remainingMs = 0L,
                    totalMs = 0L,
                )
            )
            tryEmitCue(AudioCue.FINISHED)
            return
        }

        seqIndex = nextIdx
        phaseStartedAt = now
        elapsedAtPause = 0L
        restEndCueFired = false
        warningEntered = false

        val next = sequence[nextIdx]
        emitState(
            TimerState(
                phase = next.phase,
                isRunning = true,
                round = if (next.round != 0) next.round else current.round,
                totalRounds = current.totalRounds,
                remainingMs = next.durationMs,
                totalMs = next.durationMs,
            )
        )
        when (next.phase) {
            Phase.WORK -> tryEmitCue(AudioCue.WORK_START)
            Phase.REST -> tryEmitCue(AudioCue.REST_START)
            else -> Unit
        }
    }

    private fun emitState(next: TimerState) {
        _state.value = next
    }

    private fun tryEmitCue(cue: AudioCue) {
        _cues.tryEmit(cue)
    }

    companion object {
        const val TIMER_TICK_MS: Long = 100L
        const val REST_END_CUE_MS: Long = 10_000L
    }
}

internal data class PhaseStep(
    val phase: Phase, // PREPARE | WORK | REST
    val durationMs: Long,
    val round: Int,
)

internal fun buildSequence(config: TimerConfig): List<PhaseStep> {
    val seq = mutableListOf<PhaseStep>()
    if (config.prepSeconds > 0) {
        seq.add(PhaseStep(Phase.PREPARE, config.prepSeconds * 1000L, 0))
    }
    for (r in 1..config.rounds) {
        seq.add(PhaseStep(Phase.WORK, config.workSeconds * 1000L, r))
        if (r < config.rounds && config.restSeconds > 0) {
            seq.add(PhaseStep(Phase.REST, config.restSeconds * 1000L, r))
        }
    }
    return seq
}

fun formatMmSs(ms: Long): String {
    val totalSec = ((ms + 999L) / 1000L).coerceAtLeast(0L)
    val m = totalSec / 60L
    val s = totalSec % 60L
    return "%02d:%02d".format(m, s)
}
