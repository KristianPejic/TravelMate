package com.fpmoz.travelmate.utils

object TripCalculator {

    private val pricePerKm = mapOf(
        "Car" to 0.5,
        "Bus" to 0.2,
        "Train" to 0.15,
        "Plane" to 0.8
    )

    private val distanceMap = mapOf(
        Pair("Zagreb", "Split") to 410,
        Pair("Zagreb", "Rijeka") to 160,
        Pair("Split", "Dubrovnik") to 220,
        Pair("Rijeka", "Osijek") to 520,
        Pair("Zagreb", "Osijek") to 280,
        Pair("Split", "Zagreb") to 410,
        Pair("Osijek", "Zagreb") to 280,
        Pair("Rijeka", "Zagreb") to 160,
        Pair("Dubrovnik", "Split") to 220,
        Pair("Osijek", "Rijeka") to 520
    )

    data class TripCalculation(
        val distance: Int,
        val estimatedCost: Double,
        val isRouteFound: Boolean
    )

    fun calculateTrip(origin: String, destination: String, transport: String): TripCalculation {
        val originCapitalized = origin.trim().replaceFirstChar { it.uppercase() }
        val destinationCapitalized = destination.trim().replaceFirstChar { it.uppercase() }

        val distance = distanceMap[Pair(originCapitalized, destinationCapitalized)]
            ?: distanceMap[Pair(destinationCapitalized, originCapitalized)]

        return if (distance != null) {
            val costPerKm = pricePerKm[transport] ?: 0.0
            val estimatedCost = distance * costPerKm
            TripCalculation(distance, estimatedCost, true)
        } else {
            TripCalculation(0, 0.0, false)
        }
    }

    fun getAvailableTransports(): List<String> = pricePerKm.keys.toList()

    fun getAvailableCities(): List<String> {
        return distanceMap.keys.flatMap { listOf(it.first, it.second) }.distinct().sorted()
    }
}