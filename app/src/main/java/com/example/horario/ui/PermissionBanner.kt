package com.example.horario.ui

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.runtime.DisposableEffect
import com.example.horario.notifications.PermissionHelper
import com.example.horario.notifications.TestNotifier

/**
 * Banner que aparece arriba del horario SOLO si falta algún permiso para que
 * lleguen las notificaciones. Permite concederlos y probar que funcionan.
 */
@Composable
fun PermissionBanner() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Recalcula el estado de permisos cada vez que la pantalla vuelve a primer plano
    // (p. ej. al volver de los ajustes del sistema).
    var refresh by remember { mutableIntStateOf(0) }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refresh++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // 'refresh' fuerza la recomposición y la relectura de los permisos.
    @Suppress("UNUSED_EXPRESSION") refresh
    val hasNotif = PermissionHelper.hasNotificationPermission(context)
    val canExact = PermissionHelper.canScheduleExactAlarms(context)

    val notifLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            // Si lo niega, lo mandamos a ajustes para activarlo manualmente.
            PermissionHelper.openNotificationSettings(context)
        }
        refresh++
    }

    if (hasNotif && canExact) {
        // Todo OK: no mostramos banner (pero dejamos probar desde ajustes).
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(14.dp)
    ) {
        Text(
            "🔔 Activa las notificaciones",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Para que te avise antes de cada clase, concede estos permisos:",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(Modifier.height(10.dp))

        if (!hasNotif) {
            Button(
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        PermissionHelper.openNotificationSettings(context)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Permitir notificaciones") }
            Spacer(Modifier.height(6.dp))
        }

        if (!canExact) {
            OutlinedButton(
                onClick = { PermissionHelper.openExactAlarmSettings(context) },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Permitir alarmas exactas") }
        }
    }
}

/** Botón para enviar una notificación de prueba al instante. */
@Composable
fun TestNotificationButton() {
    val context = LocalContext.current
    OutlinedButton(
        onClick = {
            TestNotifier.send(context)
            Toast.makeText(context, "Enviada: revisa tus notificaciones", Toast.LENGTH_SHORT).show()
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) { Text("Probar notificación") }
}
