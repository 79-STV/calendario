package com.example.horario.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.example.horario.data.SettingsStore

enum class ThemeMode { SYSTEM, LIGHT, DARK, AMOLED }

class ThemeViewModel(app: Application) : AndroidViewModel(app) {

    private val store = SettingsStore(app.applicationContext)

    var mode by mutableStateOf(
        runCatching { ThemeMode.valueOf(store.themeMode) }.getOrDefault(ThemeMode.SYSTEM)
    )
        private set

    var accentColor by mutableIntStateOf(store.accentColor)
        private set

    fun updateMode(newMode: ThemeMode) {
        mode = newMode
        store.themeMode = newMode.name
    }

    fun updateAccent(color: Int) {
        accentColor = color
        store.accentColor = color
    }
}
