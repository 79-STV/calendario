package com.example.horario.share

import com.example.horario.data.ClassItem

/**
 * Convierte el horario a/desde un texto compacto para meterlo en un QR.
 *
 * Formato (una clase por línea):
 *   nombre|día|inicio|fin|aula|colorHex
 * Prefijo de cabecera: "HORARIOv1"
 */
object ScheduleCodec {

    private const val HEADER = "HORARIOv1"
    private const val SEP = "|"

    fun encode(classes: List<ClassItem>): String {
        val body = classes.joinToString("\n") { c ->
            listOf(
                c.name.sanitize(),
                c.dayOfWeek.toString(),
                c.startMinutes.toString(),
                c.endMinutes.toString(),
                c.room.sanitize(),
                c.colorHex
            ).joinToString(SEP)
        }
        return "$HEADER\n$body"
    }

    /** Devuelve las clases, o lista vacía si el texto no es un horario válido. */
    fun decode(text: String): List<ClassItem> {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty() || lines.first() != HEADER) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val parts = line.split(SEP)
            if (parts.size < 4) return@mapNotNull null
            val name = parts[0]
            val day = parts[1].toIntOrNull() ?: return@mapNotNull null
            val start = parts[2].toIntOrNull() ?: return@mapNotNull null
            val end = parts[3].toIntOrNull() ?: return@mapNotNull null
            val room = parts.getOrNull(4).orEmpty()
            val color = parts.getOrNull(5)?.takeIf { it.startsWith("#") } ?: "#7C4DFF"
            if (day !in 1..7 || end <= start) return@mapNotNull null
            ClassItem(
                name = name.ifBlank { "Clase" },
                room = room,
                dayOfWeek = day,
                startMinutes = start,
                endMinutes = end,
                colorHex = color
            )
        }
    }

    /** Evita que los separadores rompan el formato. */
    private fun String.sanitize(): String =
        replace("|", " ").replace("\n", " ").trim()
}
