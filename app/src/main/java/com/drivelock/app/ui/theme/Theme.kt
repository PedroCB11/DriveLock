package com.drivelock.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.drivelock.app.ui.settings.ThemeMode

private val LightColors = lightColorScheme(
    primary = Color(0xFF087F6D),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD5F6EF),
    onPrimaryContainer = Color(0xFF00382F),
    secondary = Color(0xFF2F6671),
    background = Color(0xFFF7F9FA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EEF0),
    onSurface = Color(0xFF102A33),
    onSurfaceVariant = Color(0xFF53666D),
    outlineVariant = Color(0xFFD1DCE0),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF67DBC3),
    onPrimary = Color(0xFF00382F),
    primaryContainer = Color(0xFF075C50),
    onPrimaryContainer = Color(0xFFD5F6EF),
    background = Color(0xFF071B22),
    surface = Color(0xFF10272F),
    surfaceVariant = Color(0xFF233A42),
    onSurface = Color(0xFFE7F2F5),
    onSurfaceVariant = Color(0xFFB8C9CE),
)

private val DriveLockTypography = Typography(
    displayMedium = Typography().displayMedium.copy(fontWeight = FontWeight.Bold),
    headlineLarge = Typography().headlineLarge.copy(fontSize = 34.sp, lineHeight = 40.sp, fontWeight = FontWeight.Bold),
    headlineSmall = Typography().headlineSmall.copy(fontWeight = FontWeight.SemiBold),
    titleLarge = Typography().titleLarge.copy(fontWeight = FontWeight.SemiBold),
    titleMedium = Typography().titleMedium.copy(fontWeight = FontWeight.SemiBold),
)

private val DriveLockShapes = Shapes(
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(22.dp),
)

@Composable
fun DriveLockTheme(themeMode: ThemeMode = ThemeMode.SYSTEM, content: @Composable () -> Unit) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = DriveLockTypography,
        shapes = DriveLockShapes,
        content = content,
    )
}
