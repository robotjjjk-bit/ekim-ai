package net.ekmai.android.`in`.ui.theme

import android.app.Activity
import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import net.ekmai.android.`in`.components.applyLiquidGlassWindowBlur

enum class ThemeOption {
    DARK,
    LIGHT,
    SYSTEM_DEFAULT;
}

private const val PREFS_NAME = "theme_prefs"
private const val KEY_THEME_MODE = "theme_mode"

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

fun setTheme(
    context: Context,
    option: ThemeOption
) {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    when (option) {
        ThemeOption.DARK -> sharedPreferences.edit { putString(KEY_THEME_MODE, "dark") }
        ThemeOption.LIGHT -> sharedPreferences.edit { putString(KEY_THEME_MODE, "light") }
        ThemeOption.SYSTEM_DEFAULT -> sharedPreferences.edit { putString(KEY_THEME_MODE, "system") }
    }
}

fun getTheme(context: Context): ThemeOption {
    val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return when (sharedPreferences.getString(KEY_THEME_MODE, "system")) {
        "dark" -> ThemeOption.DARK
        "light" -> ThemeOption.LIGHT
        else -> ThemeOption.SYSTEM_DEFAULT
    }
}

@Composable
fun EkmAITheme(
    themeOption: ThemeOption,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeOption) {
        ThemeOption.DARK -> true
        ThemeOption.LIGHT -> false
        ThemeOption.SYSTEM_DEFAULT -> isSystemInDarkTheme()
    }

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            // Liquid Glass: window blur API 31+, no-op + fallback translusen di API lama.
            applyLiquidGlassWindowBlur(view.context as Activity)
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}