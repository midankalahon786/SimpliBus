package com.example.simplibus.data.passenger.model

data class BusRouteInfo(
    val name: String,
    val stops: List<Station>,
    val stopsReturn: List<Station>
)