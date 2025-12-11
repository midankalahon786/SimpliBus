package com.example.simplibus.data.passenger

import android.util.Log
import com.example.simplibus.data.passenger.model.BusLocation
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject

class BusWebSocketListener(
    private val scope: CoroutineScope,
    private val busUpdatesFlow: MutableSharedFlow<BusLocation>
) : WebSocketListener() {
    private val TAG = "BusWebSocketListener"
    private val gson = Gson()
    override fun onOpen(webSocket: WebSocket, response: Response) {
        Log.i(TAG, "WebSocket Connection Opened")
        webSocket.send(
            JSONObject().put("type", "greeting").put("message", "Android Tracker connected").toString()
        )
    }
    override fun onMessage(webSocket: WebSocket, text: String) {
        Log.d(TAG, "Received message: $text")
        try {
            val jsonObject = JsonParser.parseString(text).asJsonObject
            if (jsonObject.has("type") && jsonObject.get("type").asString == "location_update") {
                val payload = jsonObject.get("payload")
                val location = gson.fromJson(payload, BusLocation::class.java)
                scope.launch(Dispatchers.Default) {
                    busUpdatesFlow.emit(location)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing message: $text", e)
        }
    }
    override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "WebSocket Closing: $reason")
        webSocket.close(1000, null)
    }
    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        Log.i(TAG, "WebSocket Closed: $reason")
    }
    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
        Log.e(TAG, "WebSocket Failure: ${t.message}", t)
    }
}