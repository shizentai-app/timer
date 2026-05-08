package app.shizentai.timer.core.screen

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Paints the status bar and navigation bar with [color] for as long as the
 * caller is composed; restores the previous values on dispose. Icon contrast
 * is auto-derived from the bg luminance.
 *
 * Used by the timer screen to make the active-phase color extend edge-to-edge
 * (the screen is the flag, not a card on top of system chrome).
 */
@Composable
fun SystemBarsColor(color: Color) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(color) {
        val window = view.context.findActivity()?.window ?: return@DisposableEffect onDispose { }
        val originalStatus = window.statusBarColor
        val originalNav = window.navigationBarColor
        val controller = WindowCompat.getInsetsController(window, view)
        val originalLightStatus = controller.isAppearanceLightStatusBars
        val originalLightNav = controller.isAppearanceLightNavigationBars

        val argb = color.toArgb()
        val isDarkBg = color.luminance() < 0.5f
        window.statusBarColor = argb
        window.navigationBarColor = argb
        controller.isAppearanceLightStatusBars = !isDarkBg
        controller.isAppearanceLightNavigationBars = !isDarkBg

        onDispose {
            window.statusBarColor = originalStatus
            window.navigationBarColor = originalNav
            controller.isAppearanceLightStatusBars = originalLightStatus
            controller.isAppearanceLightNavigationBars = originalLightNav
        }
    }
}

private fun Context.findActivity(): Activity? {
    var ctx: Context? = this
    while (ctx is ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}
