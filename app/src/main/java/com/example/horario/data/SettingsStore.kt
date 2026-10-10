package com.example.horario.data

import android.content.Context

/** Guarda las preferencias de apariencia (modo y color de acento). */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** "SYSTEM" | "LIGHT" | "DARK" | "AMOLED" */
    var themeMode: String
        get() = prefs.getString(KEY_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_MODE, value).apply()

    /** Color de acento como entero ARGB. Por defecto morado. */
    var accentColor: Int
        get() = prefs.getInt(KEY_ACCENT, DEFAULT_ACCENT)
        set(value) = prefs.edit().putInt(KEY_ACCENT, value).apply()

    companion object {
        const val DEFAULT_ACCENT = 0xFF7C4DFF.toInt() // Morado
        private const val KEY_MODE = "theme_mode"
        private const val KEY_ACCENT = "accent_color"
    }
}
