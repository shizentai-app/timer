package app.shizentai.timer.data.presets

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.presetsDataStore by preferencesDataStore(name = "timer_presets")
private val CUSTOM_KEY = stringPreferencesKey("custom_presets_json")
private val ACTIVE_ID_KEY = stringPreferencesKey("active_preset_id")
private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

@Singleton
class PresetRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val customPresets: StateFlow<List<Preset>> = context.presetsDataStore.data
        .map { prefs ->
            val raw = prefs[CUSTOM_KEY] ?: return@map emptyList()
            runCatching { json.decodeFromString(ListSerializer(Preset.serializer()), raw) }
                .getOrDefault(emptyList())
        }
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val activePresetId: StateFlow<String> = context.presetsDataStore.data
        .map { it[ACTIVE_ID_KEY] ?: DEFAULT_PRESET.id }
        .stateIn(scope, SharingStarted.Eagerly, DEFAULT_PRESET.id)

    val allPresets: StateFlow<List<Preset>> = customPresets
        .map { customs -> BUILT_IN_PRESETS + customs }
        .stateIn(scope, SharingStarted.Eagerly, BUILT_IN_PRESETS)

    val activePreset: StateFlow<Preset> = combine(allPresets, activePresetId) { all, id ->
        all.firstOrNull { it.id == id } ?: DEFAULT_PRESET
    }.stateIn(scope, SharingStarted.Eagerly, DEFAULT_PRESET)

    suspend fun selectActive(id: String) {
        context.presetsDataStore.edit { it[ACTIVE_ID_KEY] = id }
    }

    suspend fun addCustom(preset: Preset) {
        require(!preset.isBuiltIn) { "Built-in presets cannot be added via addCustom" }
        context.presetsDataStore.edit { prefs ->
            val current = prefs[CUSTOM_KEY]
                ?.let { runCatching { json.decodeFromString(ListSerializer(Preset.serializer()), it) }.getOrNull() }
                ?: emptyList()
            val next = current.filterNot { it.id == preset.id } + preset
            prefs[CUSTOM_KEY] = json.encodeToString(ListSerializer(Preset.serializer()), next)
        }
    }

    suspend fun deleteCustom(id: String) {
        context.presetsDataStore.edit { prefs ->
            val current = prefs[CUSTOM_KEY]
                ?.let { runCatching { json.decodeFromString(ListSerializer(Preset.serializer()), it) }.getOrNull() }
                ?: return@edit
            val next = current.filterNot { it.id == id }
            prefs[CUSTOM_KEY] = json.encodeToString(ListSerializer(Preset.serializer()), next)
            if (prefs[ACTIVE_ID_KEY] == id) prefs[ACTIVE_ID_KEY] = DEFAULT_PRESET.id
        }
    }

    fun newCustomId(): String = "custom-${System.currentTimeMillis()}"
}
