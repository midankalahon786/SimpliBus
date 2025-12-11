package com.example.simplibus.data.passenger

import android.util.Log
import com.example.simplibus.data.passenger.model.BusLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import org.json.JSONArray
import java.io.IOException

//private const val SERVER_IP = "10.0.2.2:5008"
private const val SERVER_IP = "backend.r786.me"
private const val WS_URL = "wss://$SERVER_IP"
private const val HTTP_URL = "https://$SERVER_IP"
private const val TAG = "BusRepository"

class BusRepository(private val client: OkHttpClient) {
    private val repositoryScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val _busUpdates = MutableSharedFlow<BusLocation>()
    val busUpdates = _busUpdates.asSharedFlow()

    private var webSocket: WebSocket? = null
    private val listener = BusWebSocketListener(repositoryScope, _busUpdates)

    fun startListening() {
        if (webSocket != null) {
            Log.i(TAG, "startListening: Already listening, skipping.")
            return
        }
        Log.i(TAG, "startListening: Attempting new WebSocket connection to $WS_URL")
        try {
            val request = Request.Builder().url(WS_URL).build()
            webSocket = client.newWebSocket(request, listener)
        } catch (e: Exception) {
            Log.e(TAG, "startListening: Failed to *initiate* WebSocket request", e)
        }
    }

    fun stopListening() {
        Log.i(TAG, "stopListening: Closing WebSocket connection.")
        webSocket?.close(1000, "Client stopped listening")
        webSocket = null
    }

    suspend fun getAvailableBuses(): List<String> {
        val url = "$HTTP_URL/buses"

        // [LOG 6] Added log for start of fetch
        Log.d(TAG, "getAvailableBuses: Attempting to fetch from $url")

        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(url)
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.w(TAG, "getAvailableBuses: Server returned unsuccessful code: ${response.code} ${response.message}")
                    throw IOException("Unexpected code $response")
                }
                val responseBody = response.body?.string() ?: "[]"
                Log.i(TAG, "getAvailableBuses: Successfully fetched. Body size: ${responseBody.length}")

                val jsonArray = JSONArray(responseBody)
                val busList = mutableListOf<String>()
                for (i in 0 until jsonArray.length()) {
                    busList.add(jsonArray.getString(i))
                }
                busList
            } catch (e: Exception) {
                Log.e(TAG, "getAvailableBuses: Failed to fetch bus list from $url", e)
                emptyList()
            }
        }
    }

    suspend fun getScheduleJson(): String? {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url("$HTTP_URL/schedule").get().build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.string()
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to fetch schedule", e)
                null
            }
        }
    }
}