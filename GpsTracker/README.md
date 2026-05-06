# 📱 GpsTracker — Aplicación Android

<div align="center">

<img src="../imgs/apk.png" width="300"/>

*Interfaz de la aplicación*

</div>

---

## 👨‍💻 Autor

**Denis Jair Cancinas Cardenas**

---

## 📖 Descripción

Aplicación Android desarrollada en **Kotlin** con **Jetpack Compose** que captura y envía ubicaciones GPS en tiempo real hacia un servidor backend.

La app funciona como un servicio en segundo plano (Foreground Service), garantizando que el rastreo continúe incluso cuando la aplicación no está visible.

---

## ✨ Características

| Característica | Descripción |
|---------------|-------------|
| 📍 **GPS de alta precisión** | Usa FusedLocationProvider de Google |
| 🔄 **Envío automático** | Coordenadas cada 10 segundos |
| 🔋 **Monitoreo de batería** | Registra nivel de batería en cada lectura |
| 🖥️ **UI con Compose** | Interfaz moderna y responsiva |
| 💾 **Almacenamiento local** | Room DB para sincronización offline |
| 🔐 **Permisos dinámicos** | Solicita permisos en tiempo de ejecución |
| 📶 **Reintento automático** | Sincroniza datos cuando hay conexión |

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────┐
│              MainActivity.kt                │
│  - UI con Jetpack Compose                   │
│  - Manejo de permisos                       │
└─────────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────┐
│            LocationService.kt               │
│  - Foreground Service                       │
│  - Captura GPS continua                     │
│  - Sincronización con servidor              │
└─────────────────────────────────────────────┘
                     │
           ┌─────────┴─────────┐
           ▼                   ▼
┌──────────────────┐  ┌──────────────────┐
│   AppDatabase     │  │  RetrofitClient │
│   (Room)          │  │  (API REST)     │
│   Almacenamiento  │  │  Envío datos    │
│   local           │  │  al servidor    │
└──────────────────┘  └──────────────────┘
```

---

## 📂 Estructura del Proyecto

```
GpsTracker/
├── app/
│   └── src/main/
│       ├── java/com/embebidos/gpstracker/
│       │   ├── MainActivity.kt          # UI principal
│       │   ├── service/
│       │   │   ├── LocationService.kt   # Servicio GPS
│       │   │   ├── BootReceiver.kt      # Inicio automático
│       │   │   └── RestartReceiver.kt   # Reinicio automático
│       │   ├── network/
│       │   │   ├── RetrofitClient.kt    # Cliente HTTP
│       │   │   └── ApiService.kt        # Definición API
│       │   ├── data/
│       │   │   ├── AppDatabase.kt       # Base de datos Room
│       │   │   ├── LocationDao.kt       # DAO de ubicaciones
│       │   │   └── LocationEntity.kt    # Modelo de datos
│       │   └── ui/theme/                 # Temas Compose
│       ├── res/                          # Recursos
│       └── AndroidManifest.xml
├── build.gradle.kts
└── settings.gradle.kts
```

---

## ⚙️ Requisitos

- **Android Studio** (Arctic Fox o superior)
- **SDK mínimo**: API 21 (Android 5.0)
- **SDK objetivo**: API 34

### 📌 Permisos requeridos

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/>
<uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION"/>
<uses-permission android:name="android.permission.INTERNET"/>
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/>
<uses-permission android:name="android.permission.WAKE_LOCK"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE"/>
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

---

## 🔧 Configuración

### 🌐 URL del Servidor

En `MainActivity.kt` (línea 134):
```kotlin
Text(text = "Servidor: 18.190.159.143", ...)
```

### 🔑 API Key

En `network/RetrofitClient.kt`:
```kotlin
const val API_KEY = "clave_secreta_gps_2024"
```

### ⏱️ Intervalo de Actualización

En `service/LocationService.kt`:
```kotlin
const val INTERVAL_MS = 10000L  // 10 segundos
```

---

## 🚀 Construcción y Ejecución

```bash
# 1. Abrir proyecto en Android Studio

# 2. Sincronizar Gradle
./gradlew sync

# 3. Compilar
./gradlew assembleDebug

# 4. El APK se genera en:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## ▶️ Uso de la Aplicación

1. **Iniciar rastreo**: Toca el botón verde "▶ Iniciar Rastreo"
2. **Permisos**: Acepta los permisos de ubicación (ubicación precisa + fondo)
3. **Funcionamiento**: La app mostrará "Estado: Rastreando ✅"
4. **Notificación**: Una notificación persistente indica que el GPS está activo
5. **Detener**: Toca el botón rojo "⏹ Detener Rastreo"

---

## ⚠️ Notas Importantes

- **Consumo de batería**: El rastreo continuo consume batería. Ajusta el intervalo según necesidades.
- **Optimizaciones de fabricantes**: Algunos dispositivos (Xiaomi, Huawei, Samsung) pueden limitar servicios en segundo plano. Desactiva las optimizaciones de batería para esta app.
- **Precisión GPS**: El servicio descartará lecturas con precisión mayor a 100 metros.

---

## 📄 Licencia

MIT License — © 2026 Denis Jair Cancinas Cardenas