package com.example.horario.ui

import androidx.compose.ui.graphics.Color

/**
 * Estado de una nota según la escala colombiana (1.0–5.0, aprueba en 3.0).
 *  🔴 1.0–2.9  perdiendo
 *  🟠 3.0–3.8  en riesgo / medio
 *  🟢 3.9–5.0  bien
 */
enum class GradeStatus(val color: Color, val label: String) {
    LOSING(Color(0xFFE53935), "Perdiendo"),
    WARNING(Color(0xFFFB8C00), "En riesgo"),
    PASSING(Color(0xFF2E9E6B), "Bien");

    companion object {
        fun of(grade: Double): GradeStatus = when {
            grade < 3.0 -> LOSING
            grade < 3.9 -> WARNING
            else -> PASSING
        }
    }
}
