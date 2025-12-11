package com.example.simplibus.data

import com.example.simplibus.data.passenger.model.Station
import com.google.android.gms.maps.model.LatLng

object RouteData {
    val route1Stops: List<Station> = listOf(
        Station("High Court", 26.19175, 91.75100),
        Station("Panbazar", 26.18785, 91.74210),
        Station("Fancy Bazar", 26.18180, 91.73750),
        Station("Bharalumukh", 26.17050, 91.72380),
        Station("Santipur", 26.16700, 91.72050),
        Station("Kamakhya Gate", 26.16230, 91.71300),
        Station("Maligaon Chariali", 26.15850, 91.69850),
        Station("Adabari", 26.15750, 91.68500),
        Station("Jalukbari", 26.15650, 91.66950),
        Station("GU Main Gate", 26.15500, 91.66280),
        Station("Satmile", 26.15220, 91.65580),
        Station("Forest School", 26.14620, 91.64600),
        Station("Dharapur", 26.13850, 91.64050)
    )

    val route1RoadPath: List<LatLng> = listOf(
        LatLng(26.19175, 91.75100), LatLng(26.18950, 91.74800), LatLng(26.18785, 91.74210),
        LatLng(26.18500, 91.74100), LatLng(26.18180, 91.73750), LatLng(26.17800, 91.73200),
        LatLng(26.17500, 91.72900), LatLng(26.17200, 91.72600), LatLng(26.17050, 91.72380),
        LatLng(26.16900, 91.72250), LatLng(26.16700, 91.72050), LatLng(26.16500, 91.71800),
        LatLng(26.16230, 91.71300), LatLng(26.16000, 91.70500), LatLng(26.15850, 91.69850),
        LatLng(26.15800, 91.69000), LatLng(26.15750, 91.68500), LatLng(26.15700, 91.67800),
        LatLng(26.15650, 91.66950), LatLng(26.15580, 91.66600), LatLng(26.15500, 91.66280),
        LatLng(26.15220, 91.65580), LatLng(26.14900, 91.65100), LatLng(26.14620, 91.64600),
        LatLng(26.14100, 91.63970), LatLng(26.13850, 91.64050)
    )
    val route2Stops: List<Station> = listOf(
        Station("Basistha Chariali", 26.10850, 91.78850),
        Station("Lokhora", 26.11250, 91.75050),
        Station("ISBT", 26.11550, 91.72050),
        Station("Garchuk", 26.11650, 91.70050),
        Station("Boragaon", 26.12050, 91.69050),
        Station("Tetelia", 26.13550, 91.67050),
        Station("Jalukbari", 26.15650, 91.66950),
        Station("GU Main Gate", 26.15500, 91.66280),
        Station("AT-7 Boys Hall", 26.15450, 91.66050)
    )

    val route2RoadPath: List<LatLng> = listOf(
        LatLng(26.10850, 91.78850), // Basistha
        LatLng(26.11050, 91.77000), // NH27
        LatLng(26.11250, 91.75050), // Lokhora
        LatLng(26.11400, 91.73500),
        LatLng(26.11550, 91.72050), // ISBT
        LatLng(26.11650, 91.70050), // Garchuk
        LatLng(26.11800, 91.69500),
        LatLng(26.12050, 91.69050), // Boragaon
        LatLng(26.12800, 91.68000),
        LatLng(26.13550, 91.67050), // Tetelia
        LatLng(26.14500, 91.66800), // Highway Approach
        LatLng(26.15650, 91.66950), // Jalukbari
        LatLng(26.15500, 91.66280), // GU Main Gate
        LatLng(26.15450, 91.66050)  // AT-7 Boys Hall
    )

    fun getStopsForBus(busId: String): List<Station> {
        return if (busId.uppercase().contains("R2")) route2Stops else route1Stops
    }

    fun getPathForBus(busId: String): List<LatLng> {
        return if (busId.uppercase().contains("R2")) route2RoadPath else route1RoadPath
    }
}