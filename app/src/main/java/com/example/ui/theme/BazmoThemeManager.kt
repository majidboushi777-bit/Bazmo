package com.example.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class BazmoThemeMode(val prefValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromPref(value: String?): BazmoThemeMode = when (value) {
            LIGHT.prefValue -> LIGHT
            DARK.prefValue -> DARK
            else -> SYSTEM
        }
    }
}

object BazmoThemeManager {
    private const val PREFS_NAME = "bazmo_theme_prefs"
    private const val KEY_MODE = "theme_mode"

    @Volatile
    private var loaded = false

    private val _mode = MutableStateFlow(BazmoThemeMode.SYSTEM)

    fun modeFlow(context: Context): StateFlow<BazmoThemeMode> {
        ensureLoaded(context)
        return _mode.asStateFlow()
    }

    fun currentMode(context: Context): BazmoThemeMode {
        ensureLoaded(context)
        return _mode.value
    }

    fun resolveDarkTheme(mode: BazmoThemeMode, systemInDarkTheme: Boolean): Boolean = when (mode) {
        BazmoThemeMode.DARK -> true
        BazmoThemeMode.LIGHT -> false
        BazmoThemeMode.SYSTEM -> systemInDarkTheme
    }

    fun setMode(context: Context, mode: BazmoThemeMode) {
        _mode.value = mode
        loaded = true
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_MODE, mode.prefValue)
            .apply()
    }

    fun toggleDark(context: Context, currentlyDark: Boolean) {
        setMode(context, if (currentlyDark) BazmoThemeMode.LIGHT else BazmoThemeMode.DARK)
    }

    fun resetInMemoryForTest() {
        loaded = false
        _mode.value = BazmoThemeMode.SYSTEM
    }

    private fun ensureLoaded(context: Context) {
        if (loaded) return
        synchronized(this) {
            if (loaded) return
            val saved = context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_MODE, BazmoThemeMode.SYSTEM.prefValue)
            _mode.value = BazmoThemeMode.fromPref(saved)
            loaded = true
        }
    }
}
