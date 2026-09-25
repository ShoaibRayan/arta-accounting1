package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

private val LightColorScheme = lightColorScheme(
    primary = PrimaryDark,
    onPrimary = Color.White,
    secondary = PrimaryTeal,
    onSecondary = Color.White,
    tertiary = AccentLime,
    background = BackgroundCanvas,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = ChipInactive,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryTeal,
    onPrimary = PrimaryDark,
    secondary = AccentLime,
    onSecondary = PrimaryDark,
    tertiary = PrimaryDark,
    background = Color(0xFF0F1316),
    onBackground = Color(0xFFF1F3F5),
    surface = Color(0xFF171D23),
    onSurface = Color(0xFFF1F3F5),
    surfaceVariant = Color(0xFF232B33),
    onSurfaceVariant = Color(0xFF9AA4B2),
    outline = Color(0xFF2A343E)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Preserve bespoke Bento luxury styling
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            content()
        }
    }
}
