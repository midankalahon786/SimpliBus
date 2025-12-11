package com.example.simplibus.data.driver

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

//private const val SERVER_IP = "10.0.2.2:5008"
private const val SERVER_IP = "backend.r786.me"
private const val HTTP_URL = "https://$SERVER_IP/update"
private val JSON = "application/json; charset=utf-8".toMediaType()

class BusApiClient {
    private val client = OkHttpClient()
    private val TAG = "BusApiClient"
    fun sendLocationUpdate(busId: String, lat: Double, lng: Double,seatStatus: String) {
        val payload = JSONObject()
        payload.put("busId", busId)
        payload.put("lat", lat)
        payload.put("lng", lng)
        payload.put("seatStatus", seatStatus)
        payload.put("timestamp", System.currentTimeMillis())

        val body = payload.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(HTTP_URL)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Failed to send update for $busId", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    Log.i(TAG, "Update sent successfully for $busId")
                } else {
                    Log.w(TAG, "Server error: ${response.code}")
                }
                response.close()
            }
        })
    }
}
