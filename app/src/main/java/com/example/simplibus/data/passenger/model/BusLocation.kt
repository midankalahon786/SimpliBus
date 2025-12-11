package com.example.simplibus.data.passenger.model

import com.google.gson.annotations.SerializedName

data class BusLocation(
    @SerializedName("busId") val busId: String,
    @SerializedName("lat") val lat: Double,
    @SerializedName("lng") val lng: Double,
    @SerializedName("seatStatus")val seatStatus: String? = "Unknown",

    // These annotations force Gson to look for these exact keys
    @SerializedName("nextStop") val nextStop: String? = null,
    @SerializedName("nextStopIndex") val nextStopIndex: Int = -1,
    @SerializedName("distance") val distance: String? = null,
    @SerializedName("eta") val eta: Int? = null
)