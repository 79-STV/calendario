package com.example.horario

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.horario.ui.MainScreen
import com.example.horario.ui.ThemeMode
import com.example.horario.ui.ThemeViewModel
import com.example.horario.ui.theme.HorarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Pasamos del tema de arranque (splash oscuro con logo) al tema normal.
        setTheme(R.style.Theme_Horario)
        super.onCreate(savedInstanceState)

        // Programa el resumen diario de "mañana tienes..." (8 PM).
        com.example.horario.notifications.DailySummaryScheduler.schedule(this)

        setContent {
            val themeVm: ThemeViewModel = viewModel()

            val systemDark = isSystemInDarkTheme()
            val isDark = when (themeVm.mode) {
                ThemeMode.SYSTEM -> systemDark
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            // Pide permiso de notificaciones en Android 13+
            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { /* el usuario decide */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }

            HorarioTheme(darkTheme = isDark, palette = themeVm.palette) {
                com.example.horario.ui.ProvideAppStyle(
                    cardStyle = themeVm.cardStyle,
                    corner = themeVm.corner,
                    density = themeVm.density
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainScreen(
                            isDark = isDark,
                            onToggleTheme = { themeVm.toggleDark(isDark) },
                            selectedPalette = themeVm.palette,
                            onSelectPalette = themeVm::updatePalette,
                            themeMode = themeVm.mode,
                            onSelectMode = themeVm::updateMode,
                            cardStyle = themeVm.cardStyle,
                            onSelectCardStyle = themeVm::updateCardStyle,
                            corner = themeVm.corner,
                            onSelectCorner = themeVm::updateCorner,
                            density = themeVm.density,
                            onSelectDensity = themeVm::updateDensity
                        )
                    }
                }
            }
        }
    }
}
