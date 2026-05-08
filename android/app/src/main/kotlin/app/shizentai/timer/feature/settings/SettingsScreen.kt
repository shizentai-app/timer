package app.shizentai.timer.feature.settings

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import android.os.LocaleList
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import app.shizentai.timer.R
import app.shizentai.timer.core.theme.Shizentai

@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    val s by viewModel.settings.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Shizentai.colors.paper)
            .statusBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            stringResource(R.string.settings_title),
            style = Shizentai.typography.screenTitle,
            color = Shizentai.colors.ink,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        SettingRow(
            label = stringResource(R.string.settings_sound),
            description = stringResource(R.string.settings_sound_desc),
            checked = s.soundEnabled,
            onChange = viewModel::setSound,
        )
        SettingRow(
            label = stringResource(R.string.settings_vibration),
            description = stringResource(R.string.settings_vibration_desc),
            checked = s.hapticsEnabled,
            onChange = viewModel::setHaptics,
        )
        SettingRow(
            label = stringResource(R.string.settings_keep_screen_on),
            description = stringResource(R.string.settings_keep_screen_on_desc),
            checked = s.keepScreenOn,
            onChange = viewModel::setKeepScreenOn,
        )
        LanguageSetting()
    }
}

@Composable
private fun SettingRow(
    label: String,
    description: String,
    checked: Boolean,
    onChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Shizentai.colors.paper2, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Shizentai.colors.ink)
            Text(description, fontSize = 12.sp, color = Shizentai.colors.ink3)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Shizentai.colors.accent,
                uncheckedThumbColor = Shizentai.colors.paper,
                uncheckedTrackColor = Shizentai.colors.line2,
            ),
        )
    }
}

@Composable
private fun LanguageSetting() {
    val context = LocalContext.current
    // Reading LocalConfiguration ties this composable to config-change
    // recompositions, so the highlighted segment updates after the activity
    // recreates with the newly-selected locale.
    val configuration = LocalConfiguration.current
    val current = remember(configuration) { readCurrentLanguage(context) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Shizentai.colors.paper2, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column {
            Text(
                stringResource(R.string.settings_language),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Shizentai.colors.ink,
            )
            Text(
                stringResource(R.string.settings_language_desc),
                fontSize = 12.sp,
                color = Shizentai.colors.ink3,
            )
        }
        val options = listOf(
            "" to stringResource(R.string.lang_system),
            "en" to stringResource(R.string.lang_en),
            "uk" to stringResource(R.string.lang_uk),
            "ja" to stringResource(R.string.lang_ja),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Shizentai.colors.paper, RoundedCornerShape(8.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            options.forEach { (value, label) ->
                val active = value == current
                Button(
                    onClick = { applyLanguage(context, value) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (active) Shizentai.colors.accent else Color.Transparent,
                        contentColor = if (active) Color.White else Shizentai.colors.ink2,
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp, pressedElevation = 0.dp),
                    contentPadding = PaddingValues(vertical = 10.dp, horizontal = 4.dp),
                ) {
                    Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                }
            }
        }
    }
}

/**
 * Reads the currently-applied per-app locale tag. Empty string means the user
 * hasn't overridden — the app follows the system locale.
 *
 * On API 33+ the canonical source is [LocaleManager]; AppCompat's mirror can
 * lag behind a system-side change. On older devices AppCompat is authoritative.
 */
private fun readCurrentLanguage(context: Context): String {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val lm = context.getSystemService(LocaleManager::class.java)
        val list = lm?.applicationLocales
        if (list == null || list.isEmpty) return ""
        return list[0].language
    }
    val list = AppCompatDelegate.getApplicationLocales()
    return if (list.isEmpty) "" else list[0]?.language ?: ""
}

/**
 * Applies a per-app locale. On API 33+ goes straight to [LocaleManager] —
 * AppCompat's [setApplicationLocales] is asynchronous on Tiramisu+ and was
 * not propagating reliably in this app, so we bypass it for the modern path.
 * On older devices we fall back to AppCompat (which handles persistence + a
 * synchronous activity recreate).
 */
private fun applyLanguage(context: Context, tag: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val lm = context.getSystemService(LocaleManager::class.java) ?: return
        lm.applicationLocales = if (tag.isEmpty()) LocaleList.getEmptyLocaleList()
            else LocaleList.forLanguageTags(tag)
        // Don't call recreate() ourselves — the system schedules a config
        // change as a result of the locale write, and our extra recreate
        // collides with it producing a black flash between two restarts.
        return
    }
    val locales = if (tag.isEmpty()) LocaleListCompat.getEmptyLocaleList()
        else LocaleListCompat.forLanguageTags(tag)
    AppCompatDelegate.setApplicationLocales(locales)
}
