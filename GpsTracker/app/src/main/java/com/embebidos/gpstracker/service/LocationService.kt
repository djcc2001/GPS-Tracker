package com.embebidos.gpstracker.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.embebidos.gpstracker.data.AppDatabase
import com.embebidos.gpstracker.data.LocationEntity
import com.embebidos.gpstracker.network.LocationRequest
import com.embebidos.gpstracker.network.RetrofitClient
import kotlinx.coroutines.*

class LocationService : Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var database: AppDatabase

    companion object {
        const val CHANNEL_ID = "gps_tracker_channel"
        const val NOTIFICATION_ID = 1
        const val INTERVAL_MS = 10000L
    }

    private fun getDeviceName(): String {
        val bluetoothName = android.provider.Settings.Secure.getString(
            contentResolver,
            "bluetooth_name"
        )
        return (bluetoothName ?: android.os.Build.MODEL)
            .replace(" ", "_")
            .take(30)
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        database = AppDatabase.getDatabase(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(
            NOTIFICATION_ID,
            createNotification("Rastreando ubicación..."),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
        )
        startLocationUpdates()
        return START_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    // Ignorar si la precisión es muy mala
                    if (location.accuracy > 100f) return

                    serviceScope.launch {
                        processLocation(
                            lat = location.latitude,
                            lon = location.longitude,
                            accuracy = location.accuracy,
                            speed = location.speed
                        )
                    }

                    // Actualizar notificación con precisión actual
                    val notifManager = getSystemService(NotificationManager::class.java)
                    notifManager.notify(
                        NOTIFICATION_ID,
                        createNotification("GPS: ±${location.accuracy.toInt()}m | ${getDeviceName()}")
                    )
                }
            }
        }
    }

    private suspend fun processLocation(
        lat: Double,
        lon: Double,
        accuracy: Float,
        speed: Float
    ) {
        // Ignorar lecturas con precisión peor a 100 metros
        if (accuracy > 100f) {
            android.util.Log.d("LocationService", "Punto descartado: accuracy=$accuracy")
            return
        }

        val battery = getBatteryLevel()
        val entity = LocationEntity(
            deviceId = getDeviceName(),
            latitude = lat,
            longitude = lon,
            accuracy = accuracy,
            speed = speed,
            battery = battery
        )
        database.locationDao().insert(entity)
        syncPendingLocations()
    }

    private suspend fun syncPendingLocations() {
        val pending = database.locationDao().getPendingLocations()
        pending.forEach { location ->
            try {
                val request = LocationRequest(
                    device_id = location.deviceId,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    speed = location.speed,
                    battery = location.battery
                )
                RetrofitClient.apiService.sendLocation(RetrofitClient.API_KEY, request)
                database.locationDao().markAsSynced(location.id)
            } catch (e: Exception) {
                // Sin internet, se reintentará después
            }
        }
        database.locationDao().deleteSynced()
    }

    private fun getBatteryLevel(): Int {
        val bm = getSystemService(BATTERY_SERVICE) as android.os.BatteryManager
        return bm.getIntProperty(android.os.BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    @Suppress("MissingPermission")
    private fun startLocationUpdates() {
        val request = com.google.android.gms.location.LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            INTERVAL_MS
        )
            .setMinUpdateIntervalMillis(5000)        // mínimo 5s entre updates
            .setMaxUpdateDelayMillis(15000)          // máximo esperar 15s
            .setMinUpdateDistanceMeters(5f)          // solo si se movió 5 metros
            .setWaitForAccurateLocation(true)        // esperar lectura precisa
            .build()

        fusedLocationClient.requestLocationUpdates(
            request,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "GPS Tracker",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(text: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("GPS Tracker")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Enviar broadcast para reiniciar el servicio
        val restartIntent = Intent("com.embebidos.gpstracker.RESTART_SERVICE")
        restartIntent.setPackage(packageName)
        sendBroadcast(restartIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        serviceScope.cancel()
    }
}