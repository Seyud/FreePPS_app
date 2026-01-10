package me.freepps.tile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun FreePPSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorSchemeMode = if (darkTheme) ColorSchemeMode.Dark else ColorSchemeMode.Light
    val controller = ThemeController(colorSchemeMode)
    val view = LocalView.current

    SideEffect {
        val window = (view.context as? android.app.Activity)?.window
        window?.let {
            WindowCompat.getInsetsController(it, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(it, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MiuixTheme(
        controller = controller
    ) {
        content()
    }
}
