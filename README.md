# Horario 📅

App de **horario de clases** para Android, minimalista e intuitiva. **Sin IA en la nube, sin internet, sin API keys.**

Reescrita desde cero en Kotlin + Jetpack Compose, reemplazando por completo la dependencia de la API de Gemini del proyecto original.

## Funciones

Dos pestañas: **📅 Horario** y **📝 Notas**.

### Horario
- 🗓️ Horario semanal (Lunes a Domingo) con selector de día.
- ➕ Agregar / editar / borrar clases: materia, aula/nota, día, hora de inicio y fin, color.
- 🔔 **Notificación una vez antes de cada clase** (5, 10, 15 o 30 min antes, o sin aviso). Se repite cada semana.
- 🌙 **Resumen diario a las 8 PM** con las clases de mañana.
- 📸 **Importar desde foto (OCR offline):** toma o elige una foto de tu horario; la app detecta materias, horas y aulas con **ML Kit** (en el dispositivo, sin internet) y tú eliges el día de cada clase antes de guardar.
- 🛎️ **Banner de permisos**: si faltan permisos para que lleguen las notificaciones, la app te guía para activarlos, con botón de **"Probar notificación"**.

### Notas (calculadora de calificaciones)
- 📚 Crea materias como **lista desplegable** que muestra su **nota/promedio** con color.
- Al desplegar, agregas **actividades** con **nombre + nota + porcentaje editable**.
- Botón **"Usar plantilla"** predefinida (Taller/Quiz/Parcial/Final), editable.
- Calcula la **nota ponderada** = Σ(nota × %/100).
- **Colores (escala 1.0–5.0, aprueba en 3.0):** 🔴 1.0–2.9 · 🟠 3.0–3.8 · 🟢 3.9–5.0.

### General
- 🌙☀️ **Modo claro y oscuro** (sigue el sistema y se puede alternar con un botón).
- 💾 Guardado local con **Room**. Todo offline.
- 🔁 Los recordatorios se reprograman solos tras reiniciar el teléfono.

## Tecnología

- Kotlin + Jetpack Compose (Material 3)
- Room (persistencia local)
- AlarmManager + NotificationManager (recordatorios)
- ML Kit Text Recognition (OCR en el dispositivo)
- minSdk 24 · targetSdk 34

## Cómo compilar y ejecutar

**Requisito:** [Android Studio](https://developer.android.com/studio).

1. Abre **Android Studio** → **Open** → elige la carpeta del proyecto.
2. Deja que Gradle sincronice (descarga dependencias la primera vez).
3. **Run** ▶️ en un emulador o tu teléfono.
4. Acepta el permiso de **notificaciones** y de **alarmas exactas** cuando la app lo pida (banner superior).

> No hay nada de Gemini ni archivos `.env` / claves de API. El OCR de ML Kit funciona en el propio teléfono.

## Permisos usados

- `POST_NOTIFICATIONS` — mostrar los avisos (Android 13+).
- `SCHEDULE_EXACT_ALARM` / `USE_EXACT_ALARM` — avisar a la hora exacta.
- `RECEIVE_BOOT_COMPLETED` — reprogramar recordatorios tras reiniciar.
- `CAMERA` — fotografiar el horario para importarlo (opcional; también puedes usar la galería).
