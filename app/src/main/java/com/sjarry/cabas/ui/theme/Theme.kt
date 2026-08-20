package com.sjarry.cabas.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val Green = Color(0xFF2E7D5B)
private val GreenDark = Color(0xFF8FD6B4)

private val LightColors = lightColorScheme(
    primary = Green,
    secondary = Color(0xFF52634F),
    tertiary = Color(0xFF7C5800),
)

private val DarkColors = darkColorScheme(
    primary = GreenDark,
    secondary = Color(0xFFB9CCB3),
    tertiary = Color(0xFFF2BF48),
)

@Composable
fun CabasTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colors = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(colorScheme = colors, content = content)
}
