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
        Pair("Zagreb", "Osijek") to 280,
        Pair("Zagreb", "Dubrovnik") to 600,
        Pair("Zagreb", "Pula") to 260,
        Pair("Zagreb", "Zadar") to 290,
        Pair("Zagreb", "Šibenik") to 350,
        Pair("Zagreb", "Karlovac") to 55,
        Pair("Zagreb", "Varaždin") to 81,
        Pair("Zagreb", "Slavonski Brod") to 200,
        Pair("Zagreb", "Vukovar") to 320,
        Pair("Zagreb", "Požega") to 160,
        Pair("Zagreb", "Sisak") to 57,
        Pair("Zagreb", "Čakovec") to 90,
        Pair("Zagreb", "Bjelovar") to 80,
        Pair("Zagreb", "Koprivnica") to 70,
        Pair("Zagreb", "Kutina") to 80,
        Pair("Zagreb", "Velika Gorica") to 16,
        Pair("Zagreb", "Samobor") to 20,


        Pair("Split", "Zagreb") to 410,
        Pair("Split", "Dubrovnik") to 220,
        Pair("Split", "Zadar") to 160,
        Pair("Split", "Šibenik") to 85,
        Pair("Split", "Rijeka") to 350,
        Pair("Split", "Osijek") to 450,
        Pair("Split", "Pula") to 420,
        Pair("Split", "Makarska") to 65,
        Pair("Split", "Trogir") to 25,
        Pair("Split", "Omiš") to 25,
        Pair("Split", "Sinj") to 35,
        Pair("Split", "Imotski") to 80,
        Pair("Split", "Kaštela") to 15,
        Pair("Split", "Hvar") to 55,
        Pair("Split", "Brač") to 45,
        Pair("Split", "Vis") to 85,
        Pair("Split", "Korčula") to 145,


        Pair("Rijeka", "Zagreb") to 160,
        Pair("Rijeka", "Split") to 350,
        Pair("Rijeka", "Pula") to 105,
        Pair("Rijeka", "Osijek") to 520,
        Pair("Rijeka", "Zadar") to 240,
        Pair("Rijeka", "Opatija") to 15,
        Pair("Rijeka", "Krk") to 50,
        Pair("Rijeka", "Crikvenica") to 35,
        Pair("Rijeka", "Senj") to 65,
        Pair("Rijeka", "Delnice") to 45,
        Pair("Rijeka", "Cres") to 95,
        Pair("Rijeka", "Mali Lošinj") to 120,


        Pair("Osijek", "Zagreb") to 280,
        Pair("Osijek", "Split") to 450,
        Pair("Osijek", "Rijeka") to 520,
        Pair("Osijek", "Vukovar") to 35,
        Pair("Osijek", "Slavonski Brod") to 110,
        Pair("Osijek", "Požega") to 130,
        Pair("Osijek", "Đakovo") to 35,
        Pair("Osijek", "Vinkovci") to 25,
        Pair("Osijek", "Našice") to 50,
        Pair("Osijek", "Beli Manastir") to 45,


        Pair("Dubrovnik", "Split") to 220,
        Pair("Dubrovnik", "Zagreb") to 600,
        Pair("Dubrovnik", "Zagreb") to 600,
        Pair("Pula", "Zagreb") to 260,
        Pair("Pula", "Rijeka") to 105,
        Pair("Pula", "Rovinj") to 35,
        Pair("Pula", "Poreč") to 60,
        Pair("Zadar", "Zagreb") to 290,
        Pair("Zadar", "Split") to 160,
        Pair("Zadar", "Rijeka") to 240,


    )

    data class TripCalculation(
        val distance: Int,
        val estimatedCost: Double,
        val isRouteFound: Boolean
    )

    fun calculateTrip(origin: String, destination: String, transport: String): TripCalculation {
        val distance = distanceMap[Pair(origin, destination)]
            ?: distanceMap[Pair(destination, origin)]

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
        return distanceMap.keys.flatMap { listOf(it.first, it.second) }
            .distinct()
            .sorted()
    }

    fun getDestinationCitiesFor(originCity: String): List<String> {
        return distanceMap.keys
            .filter { it.first == originCity || it.second == originCity }
            .flatMap {
                if (it.first == originCity) listOf(it.second)
                else listOf(it.first)
            }
            .distinct()
            .sorted()
    }
}