package com.example.horario.ui

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.horario.data.ClassItem
import com.example.horario.share.QrGenerator
import com.example.horario.share.ScheduleCodec
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

@Composable
fun ShareScreen(
    classes: List<ClassItem>,
    onClose: () -> Unit,
    onImported: (List<ClassItem>) -> Unit
) {
    val context = LocalContext.current

    val qrBitmap: Bitmap? = remember(classes) {
        if (classes.isEmpty()) null
        else runCatching { QrGenerator.generate(ScheduleCodec.encode(classes)) }.getOrNull()
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Compartir horario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Muestra este código a la otra persona para que lo escanee, " +
                "o escanea el de alguien más para copiar su horario.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
        Spacer(Modifier.height(24.dp))

        if (qrBitmap != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = "Código QR del horario",
                modifier = Modifier
                    .size(260.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .padding(12.dp)
            )
        } else {
            Text(
                "Aún no tienes clases para compartir.",
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                val scanner = GmsBarcodeScanning.getClient(context)
                scanner.startScan()
                    .addOnSuccessListener { barcode ->
                        val raw = barcode.rawValue.orEmpty()
                        val imported = ScheduleCodec.decode(raw)
                        if (imported.isNotEmpty()) {
                            onImported(imported)
                            Toast.makeText(
                                context,
                                "Importadas ${imported.size} clases",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Ese código no es un horario válido",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "No se pudo escanear", Toast.LENGTH_SHORT).show()
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Escanear un código") }

        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar")
        }
    }
}
