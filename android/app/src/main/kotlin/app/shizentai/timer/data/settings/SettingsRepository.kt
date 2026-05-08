package app.shizentai.timer.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

private val Context.settingsDataStore by preferencesDataStore(name = "timer_settings")
private val KEY_SOUND = booleanPreferencesKey("sound_enabled")
private val KEY_HAPTICS = booleanPreferencesKey("haptics_enabled")
private val KEY_KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")

data class TimerSettings(
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val keepScreenOn: Boolean = true,
)

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    val settings: StateFlow<TimerSettings> = context.settingsDataStore.data
        .map {
            TimerSettings(
                soundEnabled = it[KEY_SOUND] ?: true,
                hapticsEnabled = it[KEY_HAPTICS] ?: true,
                keepScreenOn = it[KEY_KEEP_SCREEN_ON] ?: true,
            )
        }
        .stateIn(scope, SharingStarted.Eagerly, TimerSettings())

    suspend fun setSoundEnabled(on: Boolean) {
        context.settingsDataStore.edit { it[KEY_SOUND] = on }
    }
    suspend fun setHapticsEnabled(on: Boolean) {
        context.settingsDataStore.edit { it[KEY_HAPTICS] = on }
    }
    suspend fun setKeepScreenOn(on: Boolean) {
        context.settingsDataStore.edit { it[KEY_KEEP_SCREEN_ON] = on }
    }
}
