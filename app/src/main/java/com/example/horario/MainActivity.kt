package com.example.horario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.horario.ui.MainScreen
import com.example.horario.ui.ThemeMode
import com.example.horario.ui.ThemeViewModel
import com.example.horario.ui.theme.DarkFlavor
import com.example.horario.ui.theme.HorarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Pasamos del tema de arranque (splash oscuro con logo) al tema normal.
        setTheme(R.style.Theme_Horario)
        super.onCreate(savedInstanceState)

        setContent {
            val themeVm: ThemeViewModel = viewModel()
            val systemDark = isSystemInDarkTheme()

            // El tema por defecto sigue al del celular (SYSTEM), con opción de cambiarlo.
            val flavor = when (themeVm.mode) {
                ThemeMode.SYSTEM -> if (systemDark) DarkFlavor.DARK else DarkFlavor.LIGHT
                ThemeMode.LIGHT -> DarkFlavor.LIGHT
                ThemeMode.DARK -> DarkFlavor.DARK
                ThemeMode.AMOLED -> DarkFlavor.AMOLED
            }

            HorarioTheme(flavor = flavor, accentArgb = themeVm.accentColor) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        themeMode = themeVm.mode,
                        onSelectMode = themeVm::updateMode,
                        accentColor = themeVm.accentColor,
                        onSelectAccent = themeVm::updateAccent
                    )
                }
            }
        }
    }
}
