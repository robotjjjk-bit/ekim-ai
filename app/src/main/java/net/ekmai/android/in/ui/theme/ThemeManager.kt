package net.ekmai.android.`in`.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow

object ThemeManager {
    val themeState = MutableStateFlow<ThemeOption>(ThemeOption.SYSTEM_DEFAULT)
}