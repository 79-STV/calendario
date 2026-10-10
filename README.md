# Horario 📅

App de **horario de clases** para Android, minimalista e intuitiva. **Sin IA en la nube, sin internet, sin API keys.**

Reescrita desde cero en Kotlin + Jetpack Compose, reemplazando por completo la dependencia de la API de Gemini del proyecto original.

## Funciones

Dos pestañas: **📅 Horario** y **📝 Notas**.

### Horario
- 🗓️ Horario semanal (Lunes a Domingo) con selector de día.
- ➕ Agregar / editar / borrar clases: materia, aula/nota, día, hora de inicio y fin, color.
- 📋 **Pegar horario (texto):** pega tu horario y la app detecta **día, materia, hora y aula automáticamente**. Incluye un botón **"Copiar prompt para mi IA"**: copias el prompt, se lo das a tu IA (ChatGPT, Gemini...) junto con la foto/texto de tu horario, y pegas aquí el resultado ya ordenado.

### Notas (calculadora de calificaciones)
- 📚 Crea materias como **lista desplegable** que muestra su **nota/promedio** con color.
- Al desplegar, agregas **actividades** con **nombre + nota + porcentaje editable**.
- Botón **"Usar plantilla"** predefinida (Taller/Quiz/Parcial/Final), editable.
- Calcula la **nota ponderada** = Σ(nota × %/100).
- **Colores (escala 1.0–5.0, aprueba en 3.0):** 🔴 1.0–2.9 · 🟠 3.0–3.8 · 🟢 3.9–5.0.

### Apariencia
- 🎨 **Tema:** sigue al del teléfono por defecto, con opción de forzar **Claro / Oscuro / Negro (AMOLED)**.
- 🌈 **Color de acento** elegible (12 colores).
- 📱 Barra de navegación flotante y formas redondeadas.

### General
- 💾 Guardado local con **Room**. Todo offline.

## Tecnología

- Kotlin + Jetpack Compose (Material 3)
- Room (persistencia local)
- minSdk 24 · targetSdk 34

## Cómo compilar y ejecutar

**Requisito:** [Android Studio](https://developer.android.com/studio).

1. Abre **Android Studio** → **Open** → elige la carpeta del proyecto.
2. Deja que Gradle sincronice (descarga dependencias la primera vez).
3. **Run** ▶️ en un emulador o tu teléfono.

> No hay nada de Gemini ni archivos `.env` / claves de API. La app funciona 100 % offline y no pide permisos especiales.
