package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkCookieColorScheme = darkColorScheme(
  primary = Cyan400,
  onPrimary = Slate950,
  primaryContainer = Slate800,
  onPrimaryContainer = Cyan400,
  secondary = Emerald400,
  onSecondary = Slate950,
  secondaryContainer = Slate800,
  onSecondaryContainer = Emerald400,
  tertiary = Indigo400,
  onTertiary = Slate950,
  background = Slate950,
  onBackground = Slate100,
  surface = Slate900,
  onSurface = Slate100,
  surfaceVariant = Slate800,
  onSurfaceVariant = Slate300,
  outline = Slate700,
  outlineVariant = Slate800,
  error = Rose400,
  onError = Slate950,
  errorContainer = Color(0xFF451A1A),
  onErrorContainer = Rose400
)

val LightCookieColorScheme = lightColorScheme(
  primary = Cyan600,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0F2FE),
  onPrimaryContainer = Color(0xFF0369A1),
  secondary = Emerald600,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFD1FAE5),
  onSecondaryContainer = Color(0xFF065F46),
  tertiary = Indigo500,
  onTertiary = Color.White,
  background = Slate50,
  onBackground = Slate900,
  surface = Color.White,
  onSurface = Slate900,
  surfaceVariant = Slate100,
  onSurfaceVariant = Slate700,
  outline = Slate300,
  outlineVariant = Slate200,
  error = Rose500,
  onError = Color.White,
  errorContainer = Color(0xFFFEE2E2),
  onErrorContainer = Color(0xFF991B1B)
)

@Composable
fun CookieManagerTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkCookieColorScheme else LightCookieColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
