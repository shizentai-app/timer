package app.shizentai.timer.engine

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Mirrors web/app/src/lib/timer.test.ts. Same scenarios, same numbers — if a
 * case lands here, it should land there too. The engine is pure, so tests run
 * synchronously without a real clock.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TimerEngineTest {

    private val kumite30 = TimerConfig(
        workSeconds = 30,
        restSeconds = 30,
        rounds = 3,
        prepSeconds = 10,
        warningSeconds = 10,
        signalEndOfRest = true,
    )

    private val tabata = TimerConfig(
        workSeconds = 20,
        restSeconds = 10,
        rounds = 8,
        prepSeconds = 10,
        warningSeconds = 5,
        signalEndOfRest = false,
    )

    // ---------- buildSequence ----------

    @Test
    fun `buildSequence includes PREPARE when prepSeconds gt 0`() {
        val seq = buildSequence(kumite30)
        assertEquals(Phase.PREPARE, seq[0].phase)
        assertEquals(10_000L, seq[0].durationMs)
        assertEquals(0, seq[0].round)
    }

    @Test
    fun `buildSequence skips PREPARE when prepSeconds is zero`() {
        val seq = buildSequence(kumite30.copy(prepSeconds = 0))
        assertEquals(Phase.WORK, seq[0].phase)
        assertEquals(1, seq[0].round)
    }

    @Test
    fun `buildSequence alternates WORK and REST and omits REST after last round`() {
        val seq = buildSequence(kumite30)
        assertEquals(6, seq.size)
        assertEquals(Phase.WORK, seq[1].phase); assertEquals(1, seq[1].round)
        assertEquals(Phase.REST, seq[2].phase); assertEquals(1, seq[2].round)
        assertEquals(Phase.WORK, seq[3].phase); assertEquals(2, seq[3].round)
        assertEquals(Phase.REST, seq[4].phase); assertEquals(2, seq[4].round)
        assertEquals(Phase.WORK, seq[5].phase); assertEquals(3, seq[5].round)
    }

    @Test
    fun `buildSequence omits REST when restSeconds is zero`() {
        val seq = buildSequence(kumite30.copy(restSeconds = 0))
        assertEquals(0, seq.count { it.phase == Phase.REST })
    }

    // ---------- start ----------

    @Test
    fun `start emits initial PREPARE state with round 0`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30, 0L)
            val s = engine.state.value
            assertEquals(Phase.PREPARE, s.phase)
            assertTrue(s.isRunning)
            assertEquals(0, s.round)
            assertEquals(3, s.totalRounds)
            assertEquals(10_000L, s.remainingMs)
            assertEquals(10_000L, s.totalMs)
            // PREPARE start emits no cue
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `start emits WORK_START cue when prep is skipped`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30.copy(prepSeconds = 0), 0L)
            assertEquals(AudioCue.WORK_START, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(Phase.WORK, engine.state.value.phase)
    }

    // ---------- tick & transitions ----------

    @Test
    fun `tick decrements remainingMs`() {
        val engine = TimerEngine()
        engine.start(kumite30, 0L)
        engine.tick(2_500L)
        assertEquals(7_500L, engine.state.value.remainingMs)
    }

    @Test
    fun `transitions PREPARE to WORK and emits work_start`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30, 0L)
            engine.tick(10_000L)
            assertEquals(AudioCue.WORK_START, awaitItem())
            val s = engine.state.value
            assertEquals(Phase.WORK, s.phase)
            assertEquals(1, s.round)
            assertEquals(30_000L, s.remainingMs)
            assertEquals(30_000L, s.totalMs)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `enters WARNING in last warningSeconds and emits work_warning once`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30, 0L)
            engine.tick(10_000L)              // → WORK r1, emits WORK_START
            assertEquals(AudioCue.WORK_START, awaitItem())
            engine.tick(30_000L)              // remaining 10_000 → WARNING
            assertEquals(AudioCue.WORK_WARNING, awaitItem())
            assertEquals(Phase.WARNING, engine.state.value.phase)
            engine.tick(35_000L)
            engine.tick(38_000L)
            expectNoEvents()                  // no duplicate WORK_WARNING
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `transitions WARNING to REST and emits rest_start`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30, 0L)
            engine.tick(10_000L); assertEquals(AudioCue.WORK_START, awaitItem())
            engine.tick(30_000L); assertEquals(AudioCue.WORK_WARNING, awaitItem())
            engine.tick(40_000L); assertEquals(AudioCue.REST_START, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits rest_ending exactly once when remaining le 10s and signalEndOfRest is true`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30, 0L)
            engine.tick(10_000L); assertEquals(AudioCue.WORK_START, awaitItem())
            engine.tick(30_000L); assertEquals(AudioCue.WORK_WARNING, awaitItem())
            engine.tick(40_000L); assertEquals(AudioCue.REST_START, awaitItem())
            engine.tick(55_000L); expectNoEvents()              // remaining 15s — too early
            engine.tick(60_500L); assertEquals(AudioCue.REST_ENDING, awaitItem())
            engine.tick(65_000L); expectNoEvents()              // no re-fire
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not emit rest_ending when signalEndOfRest is false`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(tabata, 0L)
            engine.tick(10_000L); assertEquals(AudioCue.WORK_START, awaitItem())
            engine.tick(30_000L); assertEquals(AudioCue.REST_START, awaitItem())
            engine.tick(31_000L); expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `does not enter WARNING when warningSeconds is zero`() {
        val engine = TimerEngine()
        engine.start(kumite30.copy(warningSeconds = 0), 0L)
        engine.tick(10_000L)
        engine.tick(35_000L)
        assertEquals(Phase.WORK, engine.state.value.phase)
    }

    @Test
    fun `transitions to FINISHED after last WORK and emits finished`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(
                TimerConfig(
                    workSeconds = 10, restSeconds = 5, rounds = 2,
                    prepSeconds = 0, warningSeconds = 0, signalEndOfRest = false,
                ),
                0L,
            )
            assertEquals(AudioCue.WORK_START, awaitItem()) // r1
            engine.tick(10_000L); assertEquals(AudioCue.REST_START, awaitItem()) // r1 rest
            engine.tick(15_000L); assertEquals(AudioCue.WORK_START, awaitItem()) // r2
            engine.tick(25_000L); assertEquals(AudioCue.FINISHED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        val s = engine.state.value
        assertEquals(Phase.FINISHED, s.phase)
        assertFalse(s.isRunning)
        assertEquals(0L, s.remainingMs)
    }

    // ---------- pause / resume ----------

    @Test
    fun `pause and resume preserve remaining ms`() {
        val engine = TimerEngine()
        engine.start(kumite30, 0L)
        engine.tick(3_000L)
        engine.pause(3_000L)
        assertFalse(engine.state.value.isRunning)
        assertEquals(7_000L, engine.state.value.remainingMs)
        engine.resume(8_000L) // 5s of paused real time
        engine.tick(9_000L)
        assertEquals(6_000L, engine.state.value.remainingMs)
    }

    @Test
    fun `pause is a no-op when not running`() {
        val engine = TimerEngine()
        val before = engine.state.value
        engine.pause(0L)
        assertEquals(before, engine.state.value)
    }

    @Test
    fun `resume is a no-op in IDLE`() {
        val engine = TimerEngine()
        engine.resume(0L)
        assertFalse(engine.state.value.isRunning)
    }

    // ---------- reset ----------

    @Test
    fun `reset returns to initial state`() {
        val engine = TimerEngine()
        engine.start(kumite30, 0L)
        engine.tick(5_000L)
        engine.reset()
        val s = engine.state.value
        assertEquals(Phase.IDLE, s.phase)
        assertFalse(s.isRunning)
        assertEquals(0, s.round)
        assertEquals(0, s.totalRounds)
        assertEquals(0L, s.remainingMs)
        assertEquals(0L, s.totalMs)
    }

    // ---------- skip ----------

    @Test
    fun `skip advances to next sequence step and emits its cue`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30, 0L)
            assertEquals(Phase.PREPARE, engine.state.value.phase)
            engine.skip(1_000L)
            assertEquals(AudioCue.WORK_START, awaitItem())
            assertEquals(Phase.WORK, engine.state.value.phase)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ---------- formatMmSs ----------

    @Test
    fun `formatMmSs handles whole seconds`() {
        assertEquals("00:30", formatMmSs(30_000L))
        assertEquals("01:00", formatMmSs(60_000L))
        assertEquals("02:05", formatMmSs(125_000L))
    }

    @Test
    fun `formatMmSs rounds up partial seconds`() {
        assertEquals("00:30", formatMmSs(29_500L))
        assertEquals("00:01", formatMmSs(1L))
        assertEquals("00:00", formatMmSs(0L))
    }

    // ---------- companion constants ----------

    @Test
    fun `REST_END_CUE_MS is 10 seconds`() {
        assertEquals(10_000L, TimerEngine.REST_END_CUE_MS)
    }

    @Test
    fun `TIMER_TICK_MS is 100ms`() {
        assertEquals(100L, TimerEngine.TIMER_TICK_MS)
    }

    @Test
    fun `start with rounds 0 and no prep finishes immediately`() = runTest {
        val engine = TimerEngine()
        engine.cues.test {
            engine.start(kumite30.copy(rounds = 0, prepSeconds = 0), 0L)
            assertEquals(AudioCue.FINISHED, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(Phase.FINISHED, engine.state.value.phase)
    }

    @Test
    fun `consecutive rounds advance round counter`() {
        val engine = TimerEngine()
        engine.start(
            TimerConfig(
                workSeconds = 5, restSeconds = 5, rounds = 3,
                prepSeconds = 0, warningSeconds = 0, signalEndOfRest = false,
            ),
            0L,
        )
        assertEquals(1, engine.state.value.round) // WORK r1
        engine.tick(5_000L); assertEquals(1, engine.state.value.round) // REST r1 (round stays at 1)
        engine.tick(10_000L); assertEquals(2, engine.state.value.round) // WORK r2
        engine.tick(15_000L); engine.tick(20_000L); assertEquals(3, engine.state.value.round) // WORK r3
        assertNotEquals(1, engine.state.value.round)
    }
}
