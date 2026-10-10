package com.example.horario.ocr

/**
 * Resultado detectado al analizar el texto de un horario.
 * @param day 1=Lun ... 7=Dom ; 0 si no se detectó el día.
 */
data class DetectedClass(
    val name: String,
    val startMinutes: Int?,
    val endMinutes: Int?,
    val room: String,
    val day: Int = 0
)

/**
 * Convierte texto de un horario en una lista de clases, por reglas (sin IA).
 * Detecta día(s), hora y aula. Si una línea tiene varios días, crea una clase
 * por cada día. Tolerante: datos pueden venir en líneas separadas.
 */
object ScheduleParser {

    private val timeRange = Regex(
        """(\d{1,2})(?::(\d{2}))?\s*(?:-|–|—|a|hasta|/)\s*(\d{1,2})(?::(\d{2}))?"""
    )
    private val singleTime = Regex("""\b(\d{1,2})(?::(\d{2}))?\b""")

    private val roomRegex = Regex(
        """(?i)\b(aula|sal[oó]n|sala|edificio|bloque|lab|laboratorio)\b[\s:.\-]*([\wáéíóúñ°\-]+(?:\s+[\w°\-]+){0,2})"""
    )

    // Mapa de palabras de día -> número (1..7). Incluye abreviaturas y sin tildes.
    private val dayWords: List<Pair<Regex, Int>> = listOf(
        Regex("""(?i)\blunes\b|\blun\b|\blu\b""") to 1,
        Regex("""(?i)\bmartes\b|\bmar\b|\bma\b""") to 2,
        Regex("""(?i)\bmi[eé]rcoles\b|\bmi[eé]\b|\bmier\b|\bmc\b""") to 3,
        Regex("""(?i)\bjueves\b|\bjue\b|\bju\b""") to 4,
        Regex("""(?i)\bviernes\b|\bvie\b|\bvi\b""") to 5,
        Regex("""(?i)\bs[aá]bado\b|\bs[aá]b\b|\bsa\b""") to 6,
        Regex("""(?i)\bdomingo\b|\bdom\b|\bdo\b""") to 7
    )

    private val noiseWords = setOf(
        "lun", "mar", "mie", "mié", "jue", "vie", "sab", "sáb", "dom",
        "lunes", "martes", "miercoles", "miércoles", "jueves", "viernes", "sabado", "sábado", "domingo",
        "grupo", "codigo", "código", "creditos", "créditos", "clasificacion", "clasificación",
        "sede", "ftte", "techne", "horario", "clases", "nota", "notas", "promedio", "parcial", "y"
    )

    fun parse(rawText: String): List<DetectedClass> {
        val lines = rawText.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        val result = mutableListOf<DetectedClass>()

        for ((index, line) in lines.withIndex()) {
            val range = timeRange.find(line) ?: continue

            val startH = range.groupValues[1].toIntOrNull() ?: continue
            val startM = range.groupValues[2].toIntOrNull() ?: 0
            val endH = range.groupValues[3].toIntOrNull() ?: continue
            val endM = range.groupValues[4].toIntOrNull() ?: 0

            val start = startH.coerceIn(0, 23) * 60 + startM.coerceIn(0, 59)
            val end = endH.coerceIn(0, 23) * 60 + endM.coerceIn(0, 59)

            var rest = line.replace(range.value, " ")

            // Aula (en esta línea o la siguiente).
            val around = listOf(line, lines.getOrNull(index + 1) ?: "").joinToString(" ")
            val roomMatch = roomRegex.find(around)
            val room = roomMatch?.value?.trim()?.replace(Regex("\\s+"), " ") ?: ""
            if (roomMatch != null) rest = rest.replace(roomMatch.value, " ")

            // Día(s) detectados en la línea (puede haber varios: "martes y jueves").
            val days = detectDays(line)
            // Quitar las palabras de día del texto para que no ensucien el nombre.
            days.forEach { _ -> }
            var cleaned = rest
            dayWords.forEach { (rx, _) -> cleaned = rx.replace(cleaned, " ") }

            var name = cleanName(cleaned)
            if (name.isBlank()) name = cleanName(lines.getOrNull(index - 1) ?: "")

            val base = DetectedClass(
                name = name.ifBlank { "Clase" },
                startMinutes = start,
                endMinutes = if (end > start) end else null,
                room = room
            )

            if (days.isEmpty()) {
                result.add(base) // día 0 = sin asignar
            } else {
                days.forEach { d -> result.add(base.copy(day = d)) }
            }
        }

        return result
    }

    /** Devuelve los días (1..7) mencionados en la línea, sin repetir. */
    private fun detectDays(line: String): List<Int> {
        val found = linkedSetOf<Int>()
        for ((rx, num) in dayWords) {
            if (rx.containsMatchIn(line)) found.add(num)
        }
        return found.toList()
    }

    private fun cleanName(text: String): String {
        val cleaned = text
            .replace(Regex("""[|•·:,/]+"""), " ")
            .replace(Regex("""\b\d+\b"""), " ")
            .replace(Regex("""\s{2,}"""), " ")
            .trim()
        val lower = cleaned.lowercase()
        if (cleaned.length < 3) return ""
        if (noiseWords.any { lower == it }) return ""
        return cleaned.take(60)
    }
}
