package app.shizentai.timer.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import app.shizentai.timer.R

private val GoogleFontsProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs,
)

private val ManropeName = GoogleFont("Manrope")

val Manrope: FontFamily = FontFamily(
    Font(googleFont = ManropeName, fontProvider = GoogleFontsProvider, weight = FontWeight.Medium),
    Font(googleFont = ManropeName, fontProvider = GoogleFontsProvider, weight = FontWeight.SemiBold),
    Font(googleFont = ManropeName, fontProvider = GoogleFontsProvider, weight = FontWeight.Bold),
    Font(googleFont = ManropeName, fontProvider = GoogleFontsProvider, weight = FontWeight.ExtraBold),
)

private val UkrainianLocales = LocaleList(Locale("uk"), Locale("en"))

private fun base(
    size: Int,
    weight: FontWeight,
    letterSpacingEm: Float = 0f,
    lineHeightMultiplier: Float = 1.4f,
): TextStyle = TextStyle(
    fontFamily = Manrope,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = (size * lineHeightMultiplier).sp,
    letterSpacing = letterSpacingEm.em,
    localeList = UkrainianLocales,
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)

@Immutable
data class ShizentaiTypography(
    // Bumped from 120 to 168 — reviewer wanted the digit ~30-35% of screen
    // height so the timer reads from across the dojo, not just up close.
    val timerDigit: TextStyle = base(168, FontWeight.ExtraBold, letterSpacingEm = -0.030f, lineHeightMultiplier = 1.0f),
    // Phase command ("Hajime!", "Yame!"). It's a vocal cue, not a caption —
    // weight + size make it carry the same authority as a sensei calling it.
    val phaseCommand: TextStyle = base(34, FontWeight.ExtraBold, letterSpacingEm = 0.06f, lineHeightMultiplier = 1.1f),
    val heroNumeral: TextStyle = base(38, FontWeight.ExtraBold, letterSpacingEm = -0.026f, lineHeightMultiplier = 1.05f),
    val screenTitle: TextStyle = base(24, FontWeight.ExtraBold, lineHeightMultiplier = 1.2f),
    val cardTitleLg: TextStyle = base(17, FontWeight.ExtraBold, lineHeightMultiplier = 1.3f),
    val cardTitle: TextStyle = base(15, FontWeight.Bold, lineHeightMultiplier = 1.35f),
    val cardTitleSm: TextStyle = base(14, FontWeight.Bold, lineHeightMultiplier = 1.35f),
    val body: TextStyle = base(14, FontWeight.SemiBold, lineHeightMultiplier = 1.5f),
    val bodyLong: TextStyle = base(14, FontWeight.SemiBold, lineHeightMultiplier = 1.55f),
    val caption: TextStyle = base(12, FontWeight.SemiBold, lineHeightMultiplier = 1.4f),
    val microLabel: TextStyle = base(11, FontWeight.Bold, letterSpacingEm = 0.11f, lineHeightMultiplier = 1.4f),
    val tabular: TextStyle = base(12, FontWeight.Bold, lineHeightMultiplier = 1.3f),
    val button: TextStyle = base(15, FontWeight.Bold, letterSpacingEm = 0.007f, lineHeightMultiplier = 1.2f),
)

val LocalShizentaiTypography = staticCompositionLocalOf { ShizentaiTypography() }
