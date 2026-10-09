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
 * Parser por reglas (sin IA) y TOLERANTE: la materia, la hora y el aula
 * pueden venir en líneas separadas (como suele pasar en una tabla).
 */
object ScheduleParser {

    // Rango de horas: "12-14", "12:00-14:00", "8 a 10", "08:00 - 10:00", "8–10"
    private val timeRange = Regex(
        """(\d{1,2})(?::(\d{2}))?\s*(?:-|–|—|a|hasta|/)\s*(\d{1,2})(?::(\d{2}))?"""
    )
    // Una sola hora suelta: "12:00", "8", "08"
    private val singleTime = Regex("""\b(\d{1,2})(?::(\d{2}))?\b""")

    private val roomRegex = Regex(
        """(?i)\b(aula|sal[oó]n|sala|edificio|bloque|lab|laboratorio)\b[\s:.\-]*([\wáéíóúñ°\-]+(?:\s+[\w°\-]+){0,2})"""
    )

    // Palabras que NO son nombres de materia (ruido típico del OCR).
    private val noiseWords = setOf(
        "lun", "mar", "mie", "mié", "jue", "vie", "sab", "sáb", "dom",
        "lunes", "martes", "miercoles", "miércoles", "jueves", "viernes", "sabado", "sábado", "domingo",
        "grupo", "codigo", "código", "creditos", "créditos", "clasificacion", "clasificación",
        "sede", "ftte", "techne", "horario", "clases", "nota", "notas", "promedio", "parcial"
    )

    fun parse(rawText: String): List<DetectedClass> {
        val lines = rawText.split("\n")
            .map { it.trim() }
            .filter { it.isNotBlank() }

        val result = mutableListOf<DetectedClass>()

        for ((index, line) in lines.withIndex()) {
            val range = timeRange.find(line) ?: continue

            val startH = range.groupValues[1].toIntOrNull() ?: continue
            val startM = range.groupValues[2].toIntOrNull() ?: 0
            val endH = range.groupValues[3].toIntOrNull() ?: continue
            val endM = range.groupValues[4].toIntOrNull() ?: 0

            val start = startH.coerceIn(0, 23) * 60 + startM.coerceIn(0, 59)
            val end = endH.coerceIn(0, 23) * 60 + endM.coerceIn(0, 59)

            // 1) Lo que queda de ESTA línea sin la hora.
            var rest = line.replace(range.value, " ")

            // 2) Aula: la buscamos en esta línea y en la siguiente.
            val around = listOf(line, lines.getOrNull(index + 1) ?: "").joinToString(" ")
            val roomMatch = roomRegex.find(around)
            val room = roomMatch?.value?.trim()?.replace(Regex("\\s+"), " ") ?: ""
            if (roomMatch != null) rest = rest.replace(roomMatch.value, " ")

            // 3) Nombre: lo que quede en esta línea; si no hay, miramos la anterior.
            var name = cleanName(rest)
            if (name.isBlank()) {
                name = cleanName(lines.getOrNull(index - 1) ?: "")
            }

            result.add(
                DetectedClass(
                    name = name.ifBlank { "Clase" },
                    startMinutes = start,
                    endMinutes = if (end > start) end else null,
                    room = room
                )
            )
        }

        // Si no encontramos rangos, intento más agresivo: líneas con una sola hora.
        if (result.isEmpty()) {
            for ((index, line) in lines.withIndex()) {
                val st = singleTime.find(line) ?: continue
                val h = st.groupValues[1].toIntOrNull() ?: continue
                if (h !in 5..22) continue // horas razonables de clase
                val m = st.groupValues[2].toIntOrNull() ?: 0
                val start = h * 60 + m
                val name = cleanName(line.replace(st.value, " "))
                    .ifBlank { cleanName(lines.getOrNull(index - 1) ?: "") }
                if (name.isNotBlank() && name != "Clase") {
                    result.add(
                        DetectedClass(
                            name = name,
                            startMinutes = start,
                            endMinutes = start + 120, // suponemos 2h, el usuario corrige
                            room = ""
                        )
                    )
                }
            }
        }

        return result
    }

    private fun cleanName(text: String): String {
        val cleaned = text
            .replace(Regex("""[|•·:,/]+"""), " ")
            .replace(Regex("""\b\d+\b"""), " ")      // quita números sueltos
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
        // Si lo que queda es solo ruido (día, "grupo", etc.), lo descartamos.
        val lower = cleaned.lowercase()
        if (cleaned.length < 3) return ""
        if (noiseWords.any { lower == it }) return ""
        return cleaned.take(60)
    }
}
