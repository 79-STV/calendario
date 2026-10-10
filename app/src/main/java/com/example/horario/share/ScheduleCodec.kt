package com.example.horario.share

import android.util.Base64
import com.example.horario.data.ClassItem
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

/**
 * Convierte el horario a/desde un texto para meterlo en un QR.
 *
 * Para que quepan muchas materias, el texto se COMPRIME (GZIP) y se codifica
 * en Base64. El contenido empieza con el prefijo "HZ1:" para reconocerlo.
 *
 * Formato interno (una clase por línea): nombre|día|inicio|fin|aula
 * (el color no se comparte: es irrelevante y ocupa espacio).
 */
object ScheduleCodec {

    private const val PREFIX = "HZ1:"       // horario comprimido v1
    private const val PLAIN_HEADER = "HORARIOv1" // compatibilidad con QR antiguos
    private const val SEP = "|"

    fun encode(classes: List<ClassItem>): String {
        val body = classes.joinToString("\n") { c ->
            listOf(
                c.name.sanitize(),
                c.dayOfWeek.toString(),
                c.startMinutes.toString(),
                c.endMinutes.toString(),
                c.room.sanitize()
            ).joinToString(SEP)
        }
        val plain = "$PLAIN_HEADER\n$body"
        return PREFIX + gzipBase64(plain)
    }

    /** Devuelve las clases, o lista vacía si el texto no es un horario válido. */
    fun decode(text: String): List<ClassItem> {
        val trimmed = text.trim()
        val plain = when {
            trimmed.startsWith(PREFIX) ->
                runCatching { ungzipBase64(trimmed.removePrefix(PREFIX)) }.getOrNull() ?: return emptyList()
            trimmed.startsWith(PLAIN_HEADER) -> trimmed // QR antiguo sin comprimir
            else -> return emptyList()
        }

        val lines = plain.split("\n").map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty() || lines.first() != PLAIN_HEADER) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val parts = line.split(SEP)
            if (parts.size < 4) return@mapNotNull null
            val name = parts[0]
            val day = parts[1].toIntOrNull() ?: return@mapNotNull null
            val start = parts[2].toIntOrNull() ?: return@mapNotNull null
            val end = parts[3].toIntOrNull() ?: return@mapNotNull null
            val room = parts.getOrNull(4).orEmpty()
            if (day !in 1..7 || end <= start) return@mapNotNull null
            ClassItem(
                name = name.ifBlank { "Clase" },
                room = room,
                dayOfWeek = day,
                startMinutes = start,
                endMinutes = end
            )
        }
    }

    private fun gzipBase64(text: String): String {
        val out = ByteArrayOutputStream()
        GZIPOutputStream(out).use { it.write(text.toByteArray(Charsets.UTF_8)) }
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    private fun ungzipBase64(b64: String): String {
        val bytes = Base64.decode(b64, Base64.NO_WRAP)
        GZIPInputStream(ByteArrayInputStream(bytes)).use { gz ->
            return gz.readBytes().toString(Charsets.UTF_8)
        }
    }

    private fun String.sanitize(): String =
        replace("|", " ").replace("\n", " ").trim()
}
