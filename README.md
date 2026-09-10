# FitVive

FitVive es una aplicación multiplataforma de salud, alimentación y actividad física desarrollada con Kotlin Multiplatform para Android e iOS. El proyecto comparte la interfaz y la lógica principal entre ambas plataformas mediante Compose Multiplatform.

## Funcionalidades principales

- Registro, inicio de sesión y persistencia de la sesión del usuario.
- Configuración inicial y edición del perfil: datos físicos, objetivo, días de entrenamiento y grupo muscular prioritario.
- Planes de entrenamiento organizados por días, con detalle de ejercicios y registro de entrenamientos completados.
- Planificación de dieta adaptada al objetivo y a las necesidades calóricas, con información nutricional y seguimiento de comidas completadas.
- Registro del peso y visualización de estadísticas de progreso, gráficas y continuidad de entrenamiento.
- Configuración de recordatorios de entrenamiento, dieta y progreso.
- Asistente conversacional de salud y actividad física integrado con la API de Google Gemini.
- Persistencia local de los datos y sincronización con Firebase Firestore en Android.

## Tecnologías principales

- Kotlin Multiplatform y Kotlin Coroutines.
- Compose Multiplatform y Material 3.
- SQLDelight para la base de datos local y sus migraciones.
- Ktor Client y Kotlinx Serialization para las comunicaciones HTTP y JSON.
- Google Gemini API para el asistente.
- Firebase Firestore para la sincronización de datos en Android.
- Gradle, Android Gradle Plugin y Xcode para la compilación de las aplicaciones.

## Requisitos

- JDK 21.
- Android Studio con Android SDK 36 instalado.
- Para Android, un dispositivo o emulador con Android 7.0 (API 24) o posterior.
- Para iOS, macOS, Xcode compatible con el objetivo iOS 18.2 y un simulador Apple Silicon.
- Conexión a Internet para descargar dependencias y utilizar los servicios externos.
- Una clave propia de Google Gemini API para utilizar el asistente.

## Configuración local

Copie `local.properties.example` como `local.properties` en la raíz del proyecto y sustituya los valores de ejemplo:

```properties
sdk.dir=RUTA_AL_ANDROID_SDK
GEMINI_API_KEY=TU_CLAVE
```

`GEMINI_API_KEY` debe ser una clave propia y habilitada para Google Gemini API. Durante la compilación, Gradle genera la configuración Kotlin utilizada por el código compartido.

`local.properties` no se versiona porque contiene rutas específicas del equipo y credenciales locales. No debe añadirse al repositorio.

## Ejecución en Android

Abra el proyecto raíz en Android Studio, espere a que finalice la sincronización de Gradle y ejecute la configuración de `composeApp` en un dispositivo o emulador.

También puede generar el APK de depuración desde la terminal:

```shell
./gradlew :composeApp:assembleDebug
```

En Windows:

```powershell
.\gradlew.bat :composeApp:assembleDebug
```

El APK se genera en `composeApp/build/outputs/apk/debug/composeApp-debug.apk`.

## Ejecución en iOS

En macOS, abra `iosApp/iosApp.xcodeproj` con Xcode, seleccione el esquema `iosApp` y un simulador compatible, y ejecute la aplicación. Xcode invoca la compilación del framework compartido de Kotlin, por lo que también deben estar configurados JDK 21, el Android SDK y `local.properties`.

## Compilación y pruebas

```shell
# Compilar Android
./gradlew :composeApp:compileDebugKotlinAndroid

# Ejecutar todos los tests
./gradlew :composeApp:allTests

# Compilar Kotlin para iOS Simulator (Apple Silicon)
./gradlew :composeApp:compileKotlinIosSimulatorArm64
```
