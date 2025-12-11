package com.example.simplibus.ui.driver

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplibus.data.driver.service.LocationService
import com.example.simplibus.data.passenger.BusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.core.content.edit
import com.example.simplibus.data.RouteData
import com.example.simplibus.data.passenger.model.Station
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import okhttp3.OkHttpClient

data class DriverUiState(
    val isLoading: Boolean = true,
    val availableBuses: List<String> = emptyList(),
    val selectedBusId: String = "",
    val currentLocation: String = "Waiting for GPS..."

)

class DriverViewModel(application: Application) : AndroidViewModel(application){
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("driver_prefs", Context.MODE_PRIVATE)
    private val TAG = "DriverViewModel"

    private val driverDataStore = DriverDataStore(context)
    private val busRepository = BusRepository(OkHttpClient())
    private val _uiState = MutableStateFlow(DriverUiState())
    val uiState = _uiState.asStateFlow()
    private val _logoutSuccess = MutableStateFlow(false)
    val logoutSuccess = _logoutSuccess.asStateFlow()
    val isTracking = LocationService.isRunning

    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            for (location in locationResult.locations) {
                val statusText = calculateLocationStatus(location, _uiState.value.selectedBusId)
                _uiState.update { it.copy(currentLocation = statusText) }
            }
        }
    }
    init {
        fetchAvailableBuses()
        loadSavedBus()
        if (isTracking.value) {
            startLocationUpdates()
        }
    }
    @SuppressLint("DefaultLocale")
    private fun calculateLocationStatus(currentLoc: Location, busId: String): String {
        val routeStops = RouteData.getStopsForBus(busId)
        var nearestStation: Station? = null
        var minDistance = Float.MAX_VALUE

        for (station in routeStops) {
            val stationLoc = Location("").apply {
                latitude = station.lat
                longitude = station.lng
            }
            val distance = currentLoc.distanceTo(stationLoc)
            if (distance < minDistance) {
                minDistance = distance
                nearestStation = station
            }
        }
        return if (nearestStation != null) {
            if (minDistance < 70) {
                "At ${nearestStation.name}"
            } else {
                val distStr = if (minDistance >= 1000) String.format("%.1fkm", minDistance / 1000) else "${minDistance.toInt()}m"
                "Near ${nearestStation.name} ($distStr away)"
            }
        } else {
            "Unknown Location"
        }
    }
    @SuppressLint("DefaultLocale")
    private fun updateLocationUi(location: Location) {
        val lat = String.format("%.5f", location.latitude)
        val lng = String.format("%.5f", location.longitude)
        _uiState.update { it.copy(currentLocation = "Lat: $lat, Lng: $lng") }
    }
    private fun loadSavedBus() {
        val savedBus = prefs.getString("saved_bus_id", "") ?: ""
        if (savedBus.isNotEmpty()) {
            _uiState.update { it.copy(selectedBusId = savedBus) }
        }
    }
    private fun fetchAvailableBuses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val buses = try {
                busRepository.getAvailableBuses()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch bus list", e)
                emptyList()
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    availableBuses = buses
                )
            }
        }
    }
    fun onBusSelected(busId: String) {
        prefs.edit { putString("saved_bus_id", busId) }
        _uiState.update { it.copy(selectedBusId = busId) }
    }
    @SuppressLint("MissingPermission")
    fun startTracking(context: Context, busId: String) {
        val serviceIntent = Intent(context, LocationService::class.java).apply {
            putExtra("BUS_ID", busId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 3000)
            .setMinUpdateDistanceMeters(5f)
            .build()
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    fun stopTracking(context: Context) {
        context.stopService(Intent(context, LocationService::class.java))
    }
    fun logout() {
        Log.i(TAG, "Attempting driver logout.")
        viewModelScope.launch {
            if (isTracking.value) {
                stopTracking(context)
            }
            driverDataStore.clearSession()
            prefs.edit { remove("saved_bus_id") }
            _logoutSuccess.value = true
            Log.i(TAG, "Driver logout complete.")
        }
    }
    fun resetLogoutState() {
        _logoutSuccess.value = false
    }
    override fun onCleared() {
        super.onCleared()
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}