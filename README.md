# Horario 📅

App de **horario de clases** para Android, minimalista e intuitiva. **Sin IA, sin internet, sin API keys.**

Reescrita desde cero en Kotlin + Jetpack Compose, reemplazando por completo la dependencia de la API de Gemini del proyecto original.

## Funciones

Dos pestañas: **📅 Horario** y **📝 Notas**.

### Horario
- 🗓️ Horario semanal (Lunes a Domingo) con selector de día.
- ➕ Agregar / editar / borrar clases: materia, aula/nota, día, hora de inicio y fin, color.
- 🔔 **Notificación una vez antes de cada clase** (configurable: 5, 10, 15 o 30 min antes, o sin aviso). Se repite cada semana.

### Notas (calculadora de calificaciones)
- 📚 Crea materias (ej. "Cálculo Diferencial") como **lista desplegable**.
- Cada materia muestra su **nota/promedio** con color según el estado.
- Al desplegar, agregas **actividades** con **nombre + nota + porcentaje editable**.
- Calcula automáticamente la **nota ponderada** = Σ(nota × %/100).
- **Colores (escala 1.0–5.0, aprueba en 3.0):**
  - 🔴 Rojo: 1.0 – 2.9 (perdiendo)
  - 🟠 Naranja: 3.0 – 3.8 (en riesgo)
  - 🟢 Verde: 3.9 – 5.0 (bien)

### General
- 🌙☀️ **Modo claro y oscuro** (sigue el sistema y se puede alternar con un botón).
- 💾 Guardado local con **Room** (base de datos en el teléfono). Todo offline.
- 🔁 Los recordatorios se reprograman solos tras reiniciar el teléfono.

## Tecnología

- Kotlin + Jetpack Compose (Material 3)
- Room (persistencia local)
- AlarmManager + NotificationManager (recordatorios)
- minSdk 24 · targetSdk 34

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
