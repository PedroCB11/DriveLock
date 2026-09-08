package com.drivelock.app.ui.settings

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

class SettingsPreferences(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    private val mutableThemeMode = MutableStateFlow(readThemeMode())
    val themeMode: StateFlow<ThemeMode> = mutableThemeMode

    fun setThemeMode(mode: ThemeMode) {
        preferences.edit().putString(KEY_THEME_MODE, mode.name).apply()
        mutableThemeMode.value = mode
    }

    private fun readThemeMode(): ThemeMode = runCatching {
        ThemeMode.valueOf(preferences.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name)!!)
    }.getOrDefault(ThemeMode.SYSTEM)

    private companion object {
        const val PREFERENCES_NAME = "drivelock_settings"
        const val KEY_THEME_MODE = "theme_mode"
    }
}
