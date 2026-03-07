package com.impostorparty.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.impostorparty.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = PartyBlue,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    secondary = PartyCyan,
    tertiary = PartyAmber,
    surface = PartySurfaceLight,
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFFE9ECFA),
    error = PartyRose,
)

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF8AB4FF),
    secondary = androidx.compose.ui.graphics.Color(0xFF5DE9DB),
    tertiary = androidx.compose.ui.graphics.Color(0xFFFFC75C),
    surface = PartySurfaceDark,
    surfaceContainerHighest = androidx.compose.ui.graphics.Color(0xFF1A2333),
    error = androidx.compose.ui.graphics.Color(0xFFFF8A8A),
)

@Composable
fun ImpostorPartyTheme(
    themeMode: ThemeMode,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColors
        else -> LightColors
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}