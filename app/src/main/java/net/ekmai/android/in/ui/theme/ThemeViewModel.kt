package net.ekmai.android.`in`.ui.theme

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ThemeViewModel(
    private val context: Context
): ViewModel() {
    private val _themeModel = MutableStateFlow(getTheme(context))
    val theme = _themeModel.asStateFlow()

    fun getTheme(): ThemeOption{
        return theme.value
    }

    fun setTheme(mode: ThemeOption) {
        _themeModel.value = mode
        setTheme(context, mode)
    }
}