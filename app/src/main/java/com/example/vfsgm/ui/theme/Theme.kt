package com.example.vfsgm.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DashboardSky,
    onPrimary = Color(0xFF04111B),
    secondary = DashboardTeal,
    onSecondary = Color(0xFF04111B),
    tertiary = DashboardCyan,
    onTertiary = Color(0xFF04111B),
    background = DashboardNavy,
    onBackground = AppDarkText,
    surface = DashboardCard,
    onSurface = AppDarkText,
    surfaceVariant = Color(0xFF111B30),
    onSurfaceVariant = DashboardMuted,
    outline = DashboardCardBorder,
    error = Color(0xFFFF7E8A),
    onError = Color(0xFF17070A)
)

private val LightColorScheme = lightColorScheme(
    primary = MidnightBlue,
    onPrimary = Mist,
    secondary = Aqua,
    onSecondary = InkBlue,
    tertiary = Coral,
    onTertiary = Mist,
    background = AppLightBackground,
    onBackground = AppLightText,
    surface = AppLightSurface,
    onSurface = AppLightText,
    surfaceVariant = AppLightSurfaceAlt,
    onSurfaceVariant = AppLightMuted,
    outline = Color(0x40FFFFFF),
    error = Rose,
    onError = Mist
)

@Composable
fun VFSGMTheme(
    darkTheme: Boolean = true,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
