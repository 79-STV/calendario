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

    /** "NORMAL" | "GLASS" | "FLAT" | "OUTLINE" | "NEON" — estilo visual de las tarjetas. */
    var cardStyle: String
        get() = prefs.getString(KEY_CARD, "NORMAL") ?: "NORMAL"
        set(value) = prefs.edit().putString(KEY_CARD, value).apply()

    /** "SQUARE" | "SOFT" | "ROUND" | "PILL" — redondez de las esquinas. */
    var cornerShape: String
        get() = prefs.getString(KEY_CORNER, "ROUND") ?: "ROUND"
        set(value) = prefs.edit().putString(KEY_CORNER, value).apply()

    /** "COMFY" | "COMPACT" — densidad / espaciado. */
    var density: String
        get() = prefs.getString(KEY_DENSITY, "COMFY") ?: "COMFY"
        set(value) = prefs.edit().putString(KEY_DENSITY, value).apply()

    companion object {
        private const val KEY_MODE = "theme_mode"
        private const val KEY_PALETTE = "palette"
        private const val KEY_CARD = "card_style"
        private const val KEY_CORNER = "corner_shape"
        private const val KEY_DENSITY = "density"
    }
}
