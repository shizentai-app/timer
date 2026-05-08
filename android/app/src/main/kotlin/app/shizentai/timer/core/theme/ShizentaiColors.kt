package app.shizentai.timer.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TimerPhaseColors(
    val work: Color,
    val warning: Color,
    val rest: Color,
    val prepare: Color,
    val finished: Color,
)

@Immutable
data class ShizentaiColors(
    val paperBasement: Color,
    val paper: Color,
    val paper2: Color,
    val paper3: Color,
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
    val line: Color,
    val line2: Color,
    val hairline: Color,
    val accent: Color,
    val accentSoft: Color,
    val accentInk: Color,
    val gold: Color,
    val goldSoft: Color,
    val phases: TimerPhaseColors,
    val isDark: Boolean,
)

internal val LightShizentaiColors = ShizentaiColors(
    paperBasement = Color(0xFFFFFFFF),
    paper = Color(0xFFFAF7F2),
    paper2 = Color(0xFFF2EDE4),
    paper3 = Color(0xFFE8E1D3),
    ink = Color(0xFF1A1A1A),
    ink2 = Color(0xFF4A4A48),
    ink3 = Color(0xFF8A867E),
    line = Color(0xFFDCD4C2),
    line2 = Color(0xFFC8BFA8),
    hairline = Color(0x141A1A1A),
    accent = Color(0xFF8B1818),
    accentSoft = Color(0xFFF4E2E2),
    accentInk = Color(0xFF5C1010),
    gold = Color(0xFFC8A24B),
    goldSoft = Color(0xFFF5EBD2),
    phases = TimerPhaseColors(
        // Dojo emerald — disciplined, not playground-Material green.
        work = Color(0xFF1B5E20),
        // Brand gold — same as DM coin, cohesive with carmine.
        warning = Color(0xFFC8A24B),
        // Brand carmine — same #8B1818 as the launcher.
        rest = Color(0xFF8B1818),
        prepare = Color(0xFF1A1A1A),
        finished = Color(0xFF1A1A1A),
    ),
    isDark = false,
)

// Dark palette per shizentai/docs/THEME.md — carmine-tinted, not neutral black.
// Shifted from warm-neutral (#14130F/#1C1B17) to a red-shifted hierarchy so the
// brand is present even on empty screens, and surface levels read as depth
// instead of all blending into one near-black.
internal val DarkShizentaiColors = ShizentaiColors(
    paperBasement = Color(0xFF0F0A0A),
    paper = Color(0xFF1A1212),
    paper2 = Color(0xFF241818),
    paper3 = Color(0xFF2E1F1F),
    ink = Color(0xFFF4EFE3),
    ink2 = Color(0xFFB8B2A2),
    ink3 = Color(0xFF807B6E),
    line = Color(0xFF4A3434),
    line2 = Color(0xFF5A4040),
    hairline = Color(0x14F4EFE3),
    accent = Color(0xFFA52828),
    accentSoft = Color(0xFF2E1414),
    accentInk = Color(0xFFE8B5B5),
    gold = Color(0xFFD4B45F),
    goldSoft = Color(0xFF2E2613),
    phases = TimerPhaseColors(
        // Slightly brighter emerald in dark mode so the color reads against
        // the near-black system bars without fighting the carmine in REST.
        work = Color(0xFF2E7D32),
        warning = Color(0xFFD4B45F),
        rest = Color(0xFFA52828),
        prepare = Color(0xFF1A1212),
        finished = Color(0xFF1A1212),
    ),
    isDark = true,
)

val LocalShizentaiColors = staticCompositionLocalOf<ShizentaiColors> {
    error("ShizentaiColors not provided. Wrap content in ShizentaiTheme.")
}
