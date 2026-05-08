package app.shizentai.timer

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import app.shizentai.timer.core.theme.ShizentaiTheme
import app.shizentai.timer.nav.TimerNavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Default system-bar icon contrast = inverse of theme. The TimerScreen
        // overrides this per phase via SystemBarsColor; this default is what
        // gets restored when the user navigates back to Presets / Settings.
        val nightMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = !nightMode
            isAppearanceLightNavigationBars = !nightMode
        }
        setContent {
            ShizentaiTheme {
                TimerNavGraph()
            }
        }
    }
}
