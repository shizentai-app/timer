package app.shizentai.timer.core.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color

@Composable
fun ShizentaiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = if (darkTheme) DarkShizentaiColors else LightShizentaiColors
    val shapes = ShizentaiShapes()
    val typography = ShizentaiTypography()
    val spacing = ShizentaiSpacing()

    CompositionLocalProvider(
        LocalShizentaiColors provides colors,
        LocalShizentaiTypography provides typography,
        LocalShizentaiShapes provides shapes,
        LocalShizentaiSpacing provides spacing,
        LocalContentColor provides colors.ink,
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialScheme(),
            typography = typography.toMaterial(),
            shapes = androidx.compose.material3.Shapes(
                small = shapes.sm,
                medium = shapes.md,
                large = shapes.lg,
                extraLarge = shapes.xl,
            ),
            content = content,
        )
    }
}

object Shizentai {
    val colors: ShizentaiColors
        @Composable @ReadOnlyComposable
        get() = LocalShizentaiColors.current
    val typography: ShizentaiTypography
        @Composable @ReadOnlyComposable
        get() = LocalShizentaiTypography.current
    val shapes: ShizentaiShapes
        @Composable @ReadOnlyComposable
        get() = LocalShizentaiShapes.current
    val spacing: ShizentaiSpacing
        @Composable @ReadOnlyComposable
        get() = LocalShizentaiSpacing.current
}

private fun ShizentaiColors.toMaterialScheme() = if (isDark) {
    darkColorScheme(
        primary = ink,
        onPrimary = paper,
        secondary = accent,
        onSecondary = Color.White,
        tertiary = gold,
        onTertiary = ink,
        background = paper,
        onBackground = ink,
        surface = paper,
        onSurface = ink,
        surfaceVariant = paper2,
        onSurfaceVariant = ink2,
        outline = line,
        outlineVariant = line2,
        error = accent,
        onError = Color.White,
    )
} else {
    lightColorScheme(
        primary = ink,
        onPrimary = paper,
        secondary = accent,
        onSecondary = Color.White,
        tertiary = gold,
        onTertiary = ink,
        background = paper,
        onBackground = ink,
        surface = paper,
        onSurface = ink,
        surfaceVariant = paper2,
        onSurfaceVariant = ink2,
        outline = line,
        outlineVariant = line2,
        error = accent,
        onError = Color.White,
    )
}

private fun ShizentaiTypography.toMaterial() = Typography(
    displayLarge = timerDigit,
    displayMedium = heroNumeral,
    headlineLarge = screenTitle,
    headlineMedium = screenTitle,
    headlineSmall = cardTitleLg,
    titleLarge = cardTitleLg,
    titleMedium = cardTitle,
    titleSmall = cardTitleSm,
    bodyLarge = body,
    bodyMedium = body,
    bodySmall = caption,
    labelLarge = button,
    labelMedium = caption,
    labelSmall = microLabel,
)
