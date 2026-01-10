package me.freepps.tile.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun FreePPSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorSchemeMode = if (darkTheme) ColorSchemeMode.Dark else ColorSchemeMode.Light
    val controller = remember(colorSchemeMode) {
        ThemeController(colorSchemeMode)
    }

    MiuixTheme(
        controller = controller
    ) {
        content()
    }
}
