package app.shizentai.timer.nav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.shizentai.timer.core.ui.BottomNav
import app.shizentai.timer.core.ui.NavTab
import app.shizentai.timer.feature.presets.PresetsScreen
import app.shizentai.timer.feature.settings.SettingsScreen
import app.shizentai.timer.feature.timer.TimerScreen
import kotlinx.serialization.Serializable

@Serializable object TimerRoute
@Serializable object PresetsRoute
@Serializable object SettingsRoute

@Composable
fun TimerNavGraph() {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val selected = when {
        currentRoute?.endsWith("PresetsRoute") == true -> NavTab.Presets
        currentRoute?.endsWith("SettingsRoute") == true -> NavTab.Settings
        else -> NavTab.Timer
    }

    Scaffold(
        bottomBar = {
            // Always visible — hiding it during an active phase made the
            // layout jump on every Start/Pause and obscured the user's mental
            // map of "where is the menu". The phase color stops at the top of
            // the nav rather than bleeding behind it.
            BottomNav(
                selected = selected,
                onSelect = { tab ->
                    val target: Any = when (tab) {
                        NavTab.Timer -> TimerRoute
                        NavTab.Presets -> PresetsRoute
                        NavTab.Settings -> SettingsRoute
                    }
                    navController.navigate(target) {
                        popUpTo(TimerRoute) { inclusive = false; saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier.navigationBarsPadding(),
            )
        },
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            NavHost(
                navController = navController,
                startDestination = TimerRoute,
            ) {
                composable<TimerRoute> { TimerScreen() }
                composable<PresetsRoute> { PresetsScreen() }
                composable<SettingsRoute> { SettingsScreen() }
            }
        }
    }
}
