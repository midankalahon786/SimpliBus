package com.example.simplibus.data.passenger.model

data class Station(
    val name: String,
    val lat: Double,
    val lng: Double
) {
    fun toLocation(): android.location.Location {
        val loc = android.location.Location("")
        loc.latitude = lat
        loc.longitude = lng
        return loc
    }
}