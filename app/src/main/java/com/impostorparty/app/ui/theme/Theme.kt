package com.impostorparty.app.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.impostorparty.domain.model.ThemeMode

private val LightColors = lightColorScheme(
    primary = RoyalBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE5FF),
    onPrimaryContainer = InkBlue,
    secondary = TealSignal,
    onSecondary = Color.White,
    tertiary = AmberHint,
    onTertiary = Color.White,
    error = DangerRose,
    onError = Color.White,
    surface = SurfaceLight,
    onSurface = InkBlue,
    surfaceContainer = SurfaceLightElevated,
    surfaceContainerHighest = Color(0xFFE1E8F6),
    onSurfaceVariant = Color(0xFF51607A),
    outline = Color(0xFF7786A0),
    outlineVariant = Color(0xFFC3CCDC),
    background = SurfaceLight,
    onBackground = InkBlue,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFA8BEFF),
    onPrimary = Color(0xFF0F275F),
    primaryContainer = Color(0xFF2E4A99),
    onPrimaryContainer = Color(0xFFDCE5FF),
    secondary = Color(0xFF72E2D7),
    onSecondary = Color(0xFF053D38),
    tertiary = Color(0xFFFFC77A),
    onTertiary = Color(0xFF4A2D00),
    error = Color(0xFFFFB3C5),
    onError = Color(0xFF5B1224),
    surface = Color(0xFF0A1224),
    onSurface = Color(0xFFE7ECF7),
    surfaceContainer = Color(0xFF14213B),
    surfaceContainerHighest = Color(0xFF213353),
    onSurfaceVariant = Color(0xFFB7C4DF),
    outline = Color(0xFF8B9AB8),
    outlineVariant = Color(0xFF41506E),
    background = Color(0xFF08101E),
    onBackground = Color(0xFFE7ECF7),
)

private val AppShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(PartyDimens.RadiusSm),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(PartyDimens.RadiusMd),
    large = androidx.compose.foundation.shape.RoundedCornerShape(PartyDimens.RadiusLg),
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
        shapes = AppShapes,
        content = content,
    )
}
