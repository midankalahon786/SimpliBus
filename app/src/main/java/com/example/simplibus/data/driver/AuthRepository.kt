package com.example.simplibus.data.driver

import android.util.Log
import com.example.simplibus.ui.driver.model.LoginResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

//private const val SERVER_IP = "10.0.2.2:5008"
private const val SERVER_IP = "backend.r786.me"
private const val HTTP_URL = "https://$SERVER_IP"
private val JSON = "application/json; charset=utf-8".toMediaType()
private const val TAG = "AuthRepository"

class AuthRepository(private val client: OkHttpClient) {
    suspend fun loginDriver(driverId: String, password: String): LoginResponse {
        val url = "$HTTP_URL/driver/login"
        Log.d(TAG, "Attempting login to $url")

        val payload = JSONObject()
        payload.put("driverId", driverId)
        payload.put("password", password)

        val body = payload.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()

            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "Unknown error"
                Log.w(TAG, "Login failed: ${response.code} $errorBody")
                val errorMsg = try {
                    JSONObject(errorBody).getString("message")
                } catch (_: Exception) {
                    "Invalid response from server"
                }
                throw IOException(errorMsg)
            }
            val responseBody = response.body?.string()
            if (responseBody == null) {
                Log.e(TAG, "Login failed: Empty response body")
                throw IOException("Empty response from server")
            }

            Log.i(TAG, "Login successful: $responseBody")

            val json = JSONObject(responseBody)
            LoginResponse(
                message = json.getString("message"),
                name = json.getString("name"),
                busId = json.getString("busId")
            )
        }
    }
    suspend fun requestPasswordReset(driverId: String): String {
        val url = "$HTTP_URL/driver/forgot-password"
        Log.d(TAG, "Requesting password reset for $driverId")

        val payload = JSONObject()
        payload.put("driverId", driverId)

        val body = payload.toString().toRequestBody(JSON)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody ?: "").getString("message")
                } catch (_: Exception) {
                    "Failed to reset password. Please try again."
                }
                throw IOException(errorMsg)
            }

            try {
                JSONObject(responseBody ?: "").getString("message")
            } catch (_: Exception) {
                "Reset link sent successfully"
            }
        }
    }

    suspend fun verifyOtpAndResetPassword(driverId: String, otp: String, newPass: String): String {
        val url = "$HTTP_URL/driver/reset-password"
        Log.d(TAG, "Verifying OTP for $driverId")

        val payload = JSONObject()
        payload.put("driverId", driverId)
        payload.put("otp", otp)
        payload.put("newPassword", newPass)

        val body = payload.toString().toRequestBody(JSON)
        val request = Request.Builder().url(url).post(body).build()

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful) {
                val errorMsg = try {
                    JSONObject(responseBody ?: "").getString("message")
                } catch (_: Exception) {
                    "Invalid OTP or Error"
                }
                throw IOException(errorMsg)
            }
            try {
                JSONObject(responseBody ?: "").getString("message")
            } catch (_: Exception) {
                "Password reset successful"
            }
        }
    }
}