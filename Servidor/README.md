# 🌐 Servidor GPS Tracker — Backend + Frontend

<div align="center">

<img src="../imgs/mapa.png" width="600"/>

*Dashboard de visualización de recorridos*

</div>

---

## 👨‍💻 Autor

**Denis Jair Cancinas Cardenas**

---

## 📖 Descripción

Servidor backend desarrollado en **Python** con **FastAPI** que recibe, almacena y sirve ubicaciones GPS. Incluye un frontend web con mapa interactivo para visualizar recorridos en tiempo real.

---

## ✨ Características

| Característica | Descripción |
|---------------|-------------|
| 📍 **API REST** | Endpoints para recibir y consultar ubicaciones |
| 🗄️ **Almacenamiento** | Base de datos PostgreSQL |
| 🗺️ **Dashboard web** | Mapa interactivo con Leaflet + OpenStreetMap |
| 🔐 **Autenticación** | API Key para todos los endpoints |
| 📊 **Consulta avanzada** | Filtrado por dispositivo, fecha y hora |
| 🔄 **Tiempo real** | Actualización cada 10 segundos |

---

## 🏗️ Arquitectura

```
                    ┌─────────────────┐
                    │   Dispositivo   │
                    │   Android       │
                    └────────┬────────┘
                             │ POST /api/location
                             ▼
┌────────────┐      ┌─────────────────┐      ┌─────────────────┐
│  Navegador │ ◄──► │   FastAPI       │ ◄──► │  PostgreSQL     │
│  (Mapa)    │      │   (Python)      │      │  (Database)     │
└────────────┘      └─────────────────┘      └─────────────────┘
```

---

## 📂 Estructura del Proyecto

```
Servidor/
├── main.py                  # Aplicación FastAPI
├── gps_api.conf             # Configuración
├── gps_api.service          # Servicio systemd
├── .env                     # Variables de entorno
└── web/
    ├── index.html           # Página de estado
    └── mapa/
        └── index.html       # Dashboard de mapas
```

---

## 🔗 Endpoints de la API

### 📍 Recibir ubicación
```http
POST /api/location
Headers: x-api-key: clave_secreta_gps_2024
Body: {
  "device_id": "Samsung_Galaxy_S21",
  "latitude": -13.5319,
  "longitude": -71.9675,
  "accuracy": 5.2,
  "speed": 2.5,
  "Battery": 85
}
```

### 📋 Obtener ubicaciones por dispositivo
```http
GET /api/locations/{device_id}
Headers: x-api-key: clave_secreta_gps_2024
```

### 📋 Obtener última ubicación
```http
GET /api/locations/{device_id}/latest
Headers: x-api-key: clave_secreta_gps_2024
```

### 📅 Obtener ubicaciones por fecha
```http
GET /api/locations/{device_id}/date/{fecha}?hora_inicio=00:00&hora_fin=23:59
Headers: x-api-key: clave_secreta_gps_2024
```

### 📱 Listar dispositivos
```http
GET /api/devices
Headers: x-api-key: clave_secreta_gps_2024
```

### 🗑️ Limpiar datos
```http
DELETE /api/locations/clear/{device_id}
Headers: x-api-key: clave_secreta_gps_2024
```

---

## 🚀 Instalación y Ejecución

### Requisitos

- Python 3.8+
- PostgreSQL

### Paso 1: Crear entorno virtual

```bash
cd Servidor
python -m venv venv
source venv/bin/activate  # Linux/Mac
# venv\Scripts\activate   # Windows
```

### Paso 2: Instalar dependencias

```bash
pip install fastapi uvicorn sqlalchemy pydantic python-dotenv
```

### Paso 3: Configurar variables de entorno

Crear archivo `.env`:
```env
DATABASE_URL=postgresql://user:password@localhost/gps_tracker
SECRET_KEY=clave_secreta_gps_2024
```

### Paso 4: Ejecutar servidor

```bash
# Modo desarrollo
uvicorn main:app --host 0.0.0.0 --port 8000 --reload

# Producción (producción con systemctl)
sudo cp gps_api.service /etc/systemd/system/
sudo systemctl enable gps_api
sudo systemctl start gps_api
```

---

## 🗺️ Acceso al Dashboard

Una vez iniciado el servidor:

| Servicio | URL |
|----------|-----|
| **Estado** | `http://localhost:8000/` |
| **Mapa** | `http://localhost:8000/mapa/` |

---

## ⚙️ Configuración del Frontend

En `web/mapa/index.html` (líneas 549-550):

```javascript
const API = 'http://18.190.159.143';        // URL del servidor
const API_KEY = 'clave_secreta_gps_2024';   // Tu API Key
```

---

## 🛠️ Funcionalidades del Dashboard

- 📍 **Selección de dispositivo** — Elige qué dispositivo visualizar
- 📅 **Filtrado por fecha** — Consulta rutas históricas
- ⏰ **Rango de tiempo** — Define hora inicio y fin
- 🔴 **Modo tiempo real** — Actualización automática cada 10s
- 🗺️ **Visualización de ruta** — Línea de recorrido con marcadores
- 📊 **Barra de información** — Muestra batería, velocidad, precisión
- 🗑️ **Limpiar datos** — Elimina registros de la base de datos

---

## 📄 Licencia

MIT License — © 2026 Denis Jair Cancinas Cardenas