package app.shizentai.timer.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.shizentai.timer.core.audio.SoundPlayer
import app.shizentai.timer.core.haptics.Haptics
import app.shizentai.timer.data.presets.Preset
import app.shizentai.timer.data.presets.PresetRepository
import app.shizentai.timer.data.settings.SettingsRepository
import app.shizentai.timer.data.settings.TimerSettings
import app.shizentai.timer.engine.AudioCue
import app.shizentai.timer.engine.Phase
import app.shizentai.timer.engine.TimerEngine
import app.shizentai.timer.engine.TimerState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@HiltViewModel
class TimerViewModel @Inject constructor(
    presetRepo: PresetRepository,
    settingsRepo: SettingsRepository,
    private val soundPlayer: SoundPlayer,
    private val haptics: Haptics,
) : ViewModel() {

    private val engine = TimerEngine()
    private var tickJob: Job? = null

    val state: StateFlow<TimerState> = engine.state
    val activePreset: StateFlow<Preset> = presetRepo.activePreset
    val settings: StateFlow<TimerSettings> = settingsRepo.settings

    init {
        viewModelScope.launch {
            engine.state
                .map { it.isRunning }
                .distinctUntilChanged()
                .collect { running ->
                    if (running) startTickLoop() else stopTickLoop()
                }
        }
        // Audio cues — gated by user settings.
        viewModelScope.launch {
            engine.cues.collect { cue ->
                if (settings.value.soundEnabled) soundPlayer.play(cue)
                if (settings.value.hapticsEnabled) haptics.forPhaseChange(phaseFor(cue))
            }
        }
    }

    fun start() = engine.start(activePreset.value.config, System.currentTimeMillis())
    fun pause() = engine.pause(System.currentTimeMillis())
    fun resume() = engine.resume(System.currentTimeMillis())
    fun reset() = engine.reset()
    fun skip() = engine.skip(System.currentTimeMillis())

    private fun startTickLoop() {
        if (tickJob?.isActive == true) return
        tickJob = viewModelScope.launch {
            while (true) {
                delay(TimerEngine.TIMER_TICK_MS)
                engine.tick(System.currentTimeMillis())
            }
        }
    }

    private fun stopTickLoop() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun phaseFor(cue: AudioCue): Phase = when (cue) {
        AudioCue.WORK_START -> Phase.WORK
        AudioCue.WORK_WARNING -> Phase.WARNING
        AudioCue.REST_START -> Phase.REST
        AudioCue.REST_ENDING -> Phase.WARNING
        AudioCue.FINISHED -> Phase.FINISHED
    }

    override fun onCleared() {
        super.onCleared()
        stopTickLoop()
    }
}
