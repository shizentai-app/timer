package app.shizentai.timer.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.shizentai.timer.data.settings.SettingsRepository
import app.shizentai.timer.data.settings.TimerSettings
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repo: SettingsRepository,
) : ViewModel() {

    val settings: StateFlow<TimerSettings> = repo.settings

    fun setSound(on: Boolean) = viewModelScope.launch { repo.setSoundEnabled(on) }
    fun setHaptics(on: Boolean) = viewModelScope.launch { repo.setHapticsEnabled(on) }
    fun setKeepScreenOn(on: Boolean) = viewModelScope.launch { repo.setKeepScreenOn(on) }
}
