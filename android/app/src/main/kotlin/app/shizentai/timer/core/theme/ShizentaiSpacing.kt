package app.shizentai.timer.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class ShizentaiSpacing(
    val s4: Dp = 4.dp,
    val s6: Dp = 6.dp,
    val s8: Dp = 8.dp,
    val s10: Dp = 10.dp,
    val s12: Dp = 12.dp,
    val s14: Dp = 14.dp,
    val s16: Dp = 16.dp,
    val s18: Dp = 18.dp,
    val s22: Dp = 22.dp,
    val s32: Dp = 32.dp,
    val s44: Dp = 44.dp,
)

val LocalShizentaiSpacing = staticCompositionLocalOf { ShizentaiSpacing() }
