package com.example.horario.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.horario.data.SettingsStore
import com.example.horario.ui.theme.ThemePalette

enum class ThemeMode { SYSTEM, LIGHT, DARK }
enum class CardStyle { NORMAL, GLASS }

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SettingsStore(app.applicationContext)

    var mode by mutableStateOf(
        runCatching { ThemeMode.valueOf(store.themeMode) }.getOrDefault(ThemeMode.SYSTEM)
    )
        private set

    var palette by mutableStateOf(ThemePalette.fromName(store.paletteName))
        private set

    var cardStyle by mutableStateOf(
        runCatching { CardStyle.valueOf(store.cardStyle) }.getOrDefault(CardStyle.NORMAL)
    )
        private set

    // Nombres con prefijo "update..." para no chocar con los setters
    // generados por las propiedades (set-mode / setCardStyle / setPalette).
    fun updateMode(newMode: ThemeMode) {
        mode = newMode
        store.themeMode = newMode.name
    }

    /** Alterna rápido entre claro/oscuro (para el botón del top bar). */
    fun toggleDark(currentlyDark: Boolean) {
        updateMode(if (currentlyDark) ThemeMode.LIGHT else ThemeMode.DARK)
    }

    fun updatePalette(newPalette: ThemePalette) {
        palette = newPalette
        store.paletteName = newPalette.name
    }

    fun updateCardStyle(newStyle: CardStyle) {
        cardStyle = newStyle
        store.cardStyle = newStyle.name
    }
}
