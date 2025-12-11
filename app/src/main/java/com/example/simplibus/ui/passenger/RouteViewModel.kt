package com.example.simplibus.ui.passenger

import android.app.Application // [FIX] Import Application
import android.location.Location
import android.util.Log
import androidx.lifecycle.AndroidViewModel // [FIX] Import AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplibus.data.RouteData
import com.example.simplibus.data.passenger.BusRepository
import com.example.simplibus.data.passenger.model.BusLocation
import com.example.simplibus.data.passenger.model.Station
import com.example.simplibus.di.AppModule
import com.example.simplibus.utils.NotificationHelper // Import NotificationHelper
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RouteUiState(
    val availableBuses: List<String> = emptyList(),
    val selectedBusId: String? = null,
    val isLoadingBuses: Boolean = true,
    val stations: List<Station> = emptyList(),
    val statusMessage: String = "Loading buses...",
    val currentSegmentIndex: Int = -1,
    val segmentProgress: Float = 0.5f,
    val eta: String? = null,
    val distance: String? = null,
    val stationEstimates: Map<Int, Pair<String, String>> = emptyMap(),
    val busLat: Double = 0.0,
    val busLng: Double = 0.0,
    val seatStatus: String? = "Unknown",
    val routePath: List<LatLng> = emptyList(),
    val currentTotalDistance: Double = 0.0
)

class RouteViewModel(application: Application) : AndroidViewModel(application) {

    private val TAG = "RouteViewModel"
    private var trackingJob: Job? = null
    private val AVERAGE_SPEED_METERS_PER_MIN = 333.0
    private val repository = BusRepository(AppModule.provideOkHttpClient())

    private var lastNotifiedStopIndex = -1
    private val NOTIFICATION_THRESHOLD_METERS = 1000.0 // 1 km

    private val TOTAL_ROUTE_1_DISTANCE_METERS = 14802.7
    private val TOTAL_ROUTE_2_DISTANCE_METERS = 16500.0

    private val _uiState = MutableStateFlow(RouteUiState())
    val uiState: StateFlow<RouteUiState> = _uiState.asStateFlow()

    init {
        fetchAvailableBuses()
    }

    private fun fetchAvailableBuses() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingBuses = true, statusMessage = "Finding buses...") }
            try {
                val buses = repository.getAvailableBuses()
                if (buses.isNotEmpty()) {
                    _uiState.update { it.copy(availableBuses = buses, isLoadingBuses = false, statusMessage = "Select a bus.") }
                } else {
                    _uiState.update { it.copy(isLoadingBuses = false, statusMessage = "No buses found.") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoadingBuses = false, statusMessage = "Error loading bus list.") }
            }
        }
    }

    fun selectBusAndStartTracking(busId: String) {
        trackingJob?.cancel()

        val selectedRouteStops = RouteData.getStopsForBus(busId)
        val selectedPath = RouteData.getPathForBus(busId)
        lastNotifiedStopIndex = -1

        val isRoute2 = busId.uppercase().contains("R2")
        val selectedTotalDistance = if (isRoute2) TOTAL_ROUTE_2_DISTANCE_METERS else TOTAL_ROUTE_1_DISTANCE_METERS

        _uiState.update {
            it.copy(
                selectedBusId = busId,
                stations = selectedRouteStops,
                routePath = selectedPath,
                currentTotalDistance = selectedTotalDistance,
                statusMessage = "Connecting to $busId...",
                currentSegmentIndex = -1,
                eta = null,
                distance = null,
                stationEstimates = emptyMap(),
                seatStatus = "Unknown",
                busLat = 0.0,
                busLng = 0.0
            )
        }

        trackingJob = viewModelScope.launch {
            repository.startListening()
            repository.busUpdates
                .catch { e ->
                    Log.e(TAG, "Bus update error: ${e.message}")
                    _uiState.update { it.copy(statusMessage = "Connection Error") }
                }
                .collect { busLocation ->
                    if (busLocation.busId == _uiState.value.selectedBusId) {
                        processBusLocation(busLocation)
                    }
                }
        }
    }

    fun deselectBus() {
        trackingJob?.cancel()
        repository.stopListening()
        _uiState.update {
            it.copy(
                selectedBusId = null,
                currentSegmentIndex = -1,
                statusMessage = "Select a bus.",
                eta = null,
                distance = null,
                stationEstimates = emptyMap()
            )
        }
    }

    private fun findNearestPointOnPath(busLat: Double, busLng: Double, routePath: List<LatLng>): Pair<Int, Double> {
        if (routePath.isEmpty()) return Pair(-1, 0.0)

        val busLocation = Location("bus").apply {
            latitude = busLat
            longitude = busLng
        }

        var minDistance = Float.MAX_VALUE
        var nearestIndex = -1

        routePath.forEachIndexed { index, pathPoint ->
            val pathLocation = Location("path").apply {
                latitude = pathPoint.latitude
                longitude = pathPoint.longitude
            }
            val distanceToBus = busLocation.distanceTo(pathLocation)

            if (distanceToBus < minDistance) {
                minDistance = distanceToBus
                nearestIndex = index
            }
        }

        var progressDistance = 0.0
        for(i in 1..nearestIndex) {
            val prevPoint = routePath[i - 1]
            val currPoint = routePath[i]
            val prevLoc = Location("prev").apply { latitude = prevPoint.latitude; longitude = prevPoint.longitude }
            val currLoc = Location("curr").apply { latitude = currPoint.latitude; longitude = currPoint.longitude }
            progressDistance += prevLoc.distanceTo(currLoc)
        }

        return Pair(nearestIndex, progressDistance)
    }

    private fun processBusLocation(busLocation: BusLocation) {
        val nextIndex = busLocation.nextStopIndex
        val safeNextIndex = nextIndex.coerceIn(0, _uiState.value.stations.size - 1)
        val newCurrentSegmentIndex = (nextIndex - 1).coerceAtLeast(0)

        val nextStationName = if (_uiState.value.stations.isNotEmpty()) {
            _uiState.value.stations[safeNextIndex].name
        } else "Unknown"

        val selectedPath = _uiState.value.routePath
        val totalDistance = _uiState.value.currentTotalDistance

        val newEstimates = calculateFutureEstimates(
            busLocation.lat,
            busLocation.lng,
            _uiState.value.stations,
            safeNextIndex
        )

        val nextStopLoc = if (_uiState.value.stations.isNotEmpty()) {
            _uiState.value.stations[safeNextIndex].toLocation()
        } else null
        val busLoc = Location("").apply { latitude=busLocation.lat; longitude=busLocation.lng }
        var distToNextStop = 0f
        if (nextStopLoc != null) {
            distToNextStop = busLoc.distanceTo(nextStopLoc)
            if (distToNextStop < NOTIFICATION_THRESHOLD_METERS && safeNextIndex != lastNotifiedStopIndex) {
                val etaMins = (distToNextStop / AVERAGE_SPEED_METERS_PER_MIN).toInt()
                val timeText = if(etaMins == 0) "Arriving now" else "$etaMins mins"
                NotificationHelper.showArrivalNotification(
                    getApplication<Application>(),
                    busLocation.busId,
                    nextStationName,
                    timeText
                )

                lastNotifiedStopIndex = safeNextIndex
            }
        }
        val distText = if(distToNextStop < 1000) "${distToNextStop.toInt()}m" else String.format("%.1fkm", distToNextStop/1000)
        if (selectedPath.isNotEmpty()) {
            val (nearestIndex, progressDistance) = findNearestPointOnPath(
                busLocation.lat,
                busLocation.lng,
                selectedPath
            )
            val newSegmentProgress = (progressDistance / totalDistance).toFloat().coerceIn(0f, 1f)
            val newEtaMins = (distToNextStop / AVERAGE_SPEED_METERS_PER_MIN).toInt()

            _uiState.update {
                it.copy(
                    currentSegmentIndex = newCurrentSegmentIndex,
                    segmentProgress = newSegmentProgress,
                    eta = "$newEtaMins mins",
                    distance = distText,
                    statusMessage = "En route to $nextStationName: $distText remaining",
                    stationEstimates = newEstimates,
                    busLat = busLocation.lat,
                    busLng = busLocation.lng,
                    seatStatus = busLocation.seatStatus ?: "Unknown"
                )
            }
        } else {
            val etaText = busLocation.eta?.let { "$it mins" } ?: "Calculating..."
            val apiDistText = busLocation.distance ?: "Unknown"

            _uiState.update {
                it.copy(
                    currentSegmentIndex = newCurrentSegmentIndex,
                    eta = etaText,
                    distance = apiDistText,
                    segmentProgress = 0.5f,
                    statusMessage = "En route to ${busLocation.nextStop ?: "Unknown"}",
                    stationEstimates = newEstimates,
                    busLat = busLocation.lat,
                    busLng = busLocation.lng,
                    seatStatus = busLocation.seatStatus ?: "Unknown"
                )
            }
        }
    }

    private fun calculateFutureEstimates(
        busLat: Double,
        busLng: Double,
        stations: List<Station>,
        nextStopIndex: Int
    ): Map<Int, Pair<String, String>> {
        val estimates = mutableMapOf<Int, Pair<String, String>>()
        val busLoc = Location("bus").apply { latitude = busLat; longitude = busLng }

        for (i in nextStopIndex until stations.size) {
            val station = stations[i]
            val stationLoc = Location("station").apply { latitude = station.lat; longitude = station.lng }
            val distanceMeters = busLoc.distanceTo(stationLoc)
            val distStr = if (distanceMeters < 1000) "${distanceMeters.toInt()}m" else String.format("%.1fkm", distanceMeters / 1000)
            val etaMins = (distanceMeters / AVERAGE_SPEED_METERS_PER_MIN).toInt()
            estimates[i] = Pair(distStr, "$etaMins mins")
        }
        return estimates
    }
}