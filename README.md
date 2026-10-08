
App de **horario de clases** para Android

-  Horario semanal (Lunes a Domingo) con selector de día.
-  Agregar / editar / borrar clases: materia, aula/nota, día, hora de inicio y fin, color.
-  **Notificación una vez antes de cada clase** (configurable: 5, 10, 15 o 30 min antes, o sin aviso). Se repite cada semana.
-  **Modo claro y oscuro** (sigue el sistema y se puede alternar con un botón).
-  Guardado local con **Room** (base de datos en el teléfono). Todo offline.
-  Los recordatorios se reprograman solos tras reiniciar el teléfono.

## Cómo compilar y ejecutar

**Requisito:** [Android Studio](https://developer.android.com/studio) (trae el SDK y Gradle; descarga las dependencias automáticamente la primera vez).

1. Abre **Android Studio**.
2. **Open** → elige la carpeta `horario`.
3. Deja que Gradle sincronice y descargue dependencias (necesita internet solo la primera vez).
4. Dale a **Run** ▶️ en un emulador o en tu teléfono.
5. En Android 13+ acepta el permiso de **notificaciones** cuando lo pida.

> Nota: no hay nada de Gemini ni archivos `.env` / claves de API. La app funciona 100 % offline.

## Permisos usados

- `POST_NOTIFICATIONS` — mostrar los avisos (Android 13+).
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — avisar a la hora exacta.
- `RECEIVE_BOOT_COMPLETED` — reprogramar recordatorios tras reiniciar.
