package com.fpmoz.travelmate.model

data class Trip(
    val origin: String,
    val destination: String,
    val departureDate: String,
    val returnDate: String,
    val transport: String,
    val estimatedCost: Double
)
