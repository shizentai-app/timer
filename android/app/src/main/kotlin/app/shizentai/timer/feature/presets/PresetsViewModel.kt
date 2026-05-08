package app.shizentai.timer.feature.presets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.shizentai.timer.data.presets.Preset
import app.shizentai.timer.data.presets.PresetRepository
import app.shizentai.timer.engine.TimerConfig
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PresetsViewModel @Inject constructor(
    private val repo: PresetRepository,
) : ViewModel() {

    val presets: StateFlow<List<Preset>> = repo.allPresets
    val activeId: StateFlow<String> = repo.activePresetId

    fun select(id: String) = viewModelScope.launch { repo.selectActive(id) }

    fun delete(id: String) = viewModelScope.launch { repo.deleteCustom(id) }

    fun add(name: String, config: TimerConfig) = viewModelScope.launch {
        val id = repo.newCustomId()
        repo.addCustom(Preset(id = id, name = name, config = config, isBuiltIn = false))
    }
}
