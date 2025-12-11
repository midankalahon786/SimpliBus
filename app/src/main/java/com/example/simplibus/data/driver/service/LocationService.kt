package com.example.simplibus.data.driver.service

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.simplibus.R // Make sure to import your app's R file
import com.example.simplibus.data.driver.BusApiClient
import com.example.simplibus.ui.role.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var busApiClient: BusApiClient

    private var busId: String = "UNKNOWN_BUS"
    private val TAG = "LocationService"
    private val CHANNEL_ID = "LocationServiceChannel"
    private val NOTIFICATION_ID = 12345

    companion object {
        val isRunning = mutableStateOf(false)
        var currentSeatStatus = "Seats Available"
    }

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "Service Created")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        busApiClient = BusApiClient()
        createNotificationChannel()
        isRunning.value = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service Started")
        busId = intent?.getStringExtra("BUS_ID") ?: "DEFAULT_BUS_ID"
        startForeground(NOTIFICATION_ID, createNotification("Tracking Bus: $busId"))
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).setMinUpdateIntervalMillis(3000)
            .build()
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                locationResult.lastLocation?.let { location ->
                    Log.i(TAG, "New Location: ${location.latitude}, ${location.longitude}")
                    busApiClient.sendLocationUpdate(
                        busId = busId,
                        lat = location.latitude,
                        lng = location.longitude,
                        seatStatus = currentSeatStatus
                    )
                }
            }
        }
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(TAG, "Location permission not granted. Stopping service.")
            stopSelf()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        Log.i(TAG, "Location updates started for $busId")
    }
    override fun onDestroy() {
        Log.i(TAG, "Service Destroyed")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        isRunning.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(text: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Bus Tracking Active")
            .setContentText(text)
            .setSmallIcon(R.drawable.baseline_directions_bus_24)
            .setContentIntent(pendingIntent)
            .build()
    }
}