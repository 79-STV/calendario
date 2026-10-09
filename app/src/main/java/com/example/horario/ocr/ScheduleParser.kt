package com.example.horario.ocr

/**
 * Resultado detectado por el OCR para una posible clase.
 * El DÍA no se detecta (es poco fiable en tablas) → lo elige el usuario después.
 */
data class DetectedClass(
    val name: String,
    val startMinutes: Int?,
    val endMinutes: Int?,
    val room: String
)

/**
 * Convierte el texto crudo del OCR en una lista de posibles clases.
 * Es un parser por reglas (sin IA): busca líneas con horas tipo "12-14" o
 * "12:00 - 14:00", nombres de materia y aulas/salones.
 */
object ScheduleParser {

    // Horas: "12-14", "12:00-14:00", "8 - 10", "08:00 a 10:00"
    private val timeRange = Regex(
        """(\d{1,2})(?::(\d{2}))?\s*(?:-|a|–|—|hasta)\s*(\d{1,2})(?::(\d{2}))?"""
    )
    // Una sola hora: "12:00", "8"
    private val singleTime = Regex("""\b(\d{1,2})(?::(\d{2}))?\b""")
    private val roomRegex = Regex(
        """(?i)\b(aula|sal[oó]n|sala|edificio|bloque)\b[\s:.-]*([\wáéíóú°\-]+(?:\s+\d+)?)"""
    )

    fun parse(rawText: String): List<DetectedClass> {
        val lines = rawText.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val result = mutableListOf<DetectedClass>()

        for (line in lines) {
            val range = timeRange.find(line)
            // Nos interesan sobre todo las líneas que tienen una hora.
            if (range == null) continue

            val startH = range.groupValues[1].toIntOrNull() ?: continue
            val startM = range.groupValues[2].toIntOrNull() ?: 0
            val endH = range.groupValues[3].toIntOrNull() ?: continue
            val endM = range.groupValues[4].toIntOrNull() ?: 0

            val start = (startH.coerceIn(0, 23)) * 60 + startM.coerceIn(0, 59)
            val end = (endH.coerceIn(0, 23)) * 60 + endM.coerceIn(0, 59)

            // El nombre: lo que queda de la línea sin la hora ni el aula.
            var rest = line.replace(range.value, " ")
            val roomMatch = roomRegex.find(rest)
            val room = roomMatch?.value?.trim()?.replace(Regex("\\s+"), " ") ?: ""
            if (roomMatch != null) rest = rest.replace(roomMatch.value, " ")

            val name = cleanName(rest)
            if (name.isNotBlank() || room.isNotBlank()) {
                result.add(
                    DetectedClass(
                        name = name.ifBlank { "Clase" },
                        startMinutes = start,
                        endMinutes = if (end > start) end else null,
                        room = room
                    )
                )
            }
        }
        return result
    }

    private fun cleanName(text: String): String {
        return text
            .replace(Regex("""[|•·:]+"""), " ")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
            .take(60)
    }
}
