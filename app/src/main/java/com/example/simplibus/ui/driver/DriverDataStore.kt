package com.example.simplibus.ui.driver

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class DriverDataStore(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("DriverPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_DRIVER_ID = "driver_id"
        private const val KEY_BUS_ID = "bus_id"
        private const val KEY_DRIVER_NAME = "driver_name"
    }
    fun saveDriverSession(driverId: String, busId: String, driverName: String) {
        prefs.edit().apply {
            putString(KEY_DRIVER_ID, driverId)
            putString(KEY_BUS_ID, busId)
            putString(KEY_DRIVER_NAME, driverName)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }
    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }
    fun getBusId(): String? {
        return prefs.getString(KEY_BUS_ID, null)
    }
    fun getDriverName(): String? {
        return prefs.getString(KEY_DRIVER_NAME, null)
    }
    fun clearSession() {
        prefs.edit { clear() }
    }
}