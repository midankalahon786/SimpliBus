package com.example.simplibus.ui.passenger

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.simplibus.data.passenger.BusRepository
import com.example.simplibus.data.passenger.model.BusRouteInfo
import com.example.simplibus.data.passenger.model.Station
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import androidx.core.content.edit

data class ScheduleUiState(
    val isLoading: Boolean = true,
    val routes: List<BusRouteInfo> = emptyList(),
    val error: String? = null,
)

class ScheduleViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext
    private val prefs = context.getSharedPreferences("app_cache", Context.MODE_PRIVATE)
    private val repository = BusRepository(OkHttpClient())
    private val gson = Gson()

    private val _uiState = MutableStateFlow(ScheduleUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val networkJson = repository.getScheduleJson()
            if (networkJson != null) {
                prefs.edit { putString("cached_schedule", networkJson) }
                parseAndEmit(networkJson)
            } else {
                val cachedJson = prefs.getString("cached_schedule", null)
                if (cachedJson != null) {
                    parseAndEmit(cachedJson)
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, error = "No internet and no offline schedule found.")
                    }
                }
            }
        }
    }

    private fun parseAndEmit(jsonString: String) {
        try {
            val root = JSONObject(jsonString)
            val routesList = mutableListOf<BusRouteInfo>()
            fun parseRoute(key: String): BusRouteInfo? {
                if (!root.has(key)) return null
                val rObj = root.getJSONObject(key)
                val name = rObj.getString("name")

                val stopsList = mutableListOf<Station>()
                val sArray = rObj.getJSONArray("stops")
                for (i in 0 until sArray.length()) {
                    val s = sArray.getJSONObject(i)
                    stopsList.add(Station(s.getString("name"), s.getDouble("lat"), s.getDouble("lng")))
                }

                val stopsReturnList = mutableListOf<Station>()
                if (rObj.has("stopsReturn")) {
                    val srArray = rObj.getJSONArray("stopsReturn")
                    for (i in 0 until srArray.length()) {
                        val s = srArray.getJSONObject(i)
                        stopsReturnList.add(Station(s.getString("name"), s.getDouble("lat"), s.getDouble("lng")))
                    }
                }

                return BusRouteInfo(name, stopsList, stopsReturnList)
            }
            parseRoute("route1")?.let { routesList.add(it) }
            parseRoute("route2")?.let { routesList.add(it) }

            _uiState.update {
                it.copy(isLoading = false, routes = routesList)
            }

        } catch (e: Exception) {
            _uiState.update {
                it.copy(isLoading = false, error = "Failed to parse schedule data.")
            }
        }
    }
}