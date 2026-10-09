package com.example.horario.data

import android.content.Context

/** Guarda las preferencias de apariencia (tema y modo oscuro) en SharedPreferences. */
class SettingsStore(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** "SYSTEM" | "LIGHT" | "DARK" */
    var themeMode: String
        get() = prefs.getString(KEY_MODE, "SYSTEM") ?: "SYSTEM"
        set(value) = prefs.edit().putString(KEY_MODE, value).apply()

    /** Nombre de la paleta (ThemePalette.name). */
    var paletteName: String
        get() = prefs.getString(KEY_PALETTE, "MORADO") ?: "MORADO"
        set(value) = prefs.edit().putString(KEY_PALETTE, value).apply()

    /** "NORMAL" | "GLASS" — estilo visual de las tarjetas. */
    var cardStyle: String
        get() = prefs.getString(KEY_CARD, "NORMAL") ?: "NORMAL"
        set(value) = prefs.edit().putString(KEY_CARD, value).apply()

    companion object {
        private const val KEY_MODE = "theme_mode"
        private const val KEY_PALETTE = "palette"
        private const val KEY_CARD = "card_style"
    }
}
