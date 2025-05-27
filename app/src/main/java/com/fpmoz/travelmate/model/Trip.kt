package com.fpmoz.travelmate.model

import com.google.firebase.firestore.DocumentId
import java.util.*

data class Trip(
    @DocumentId
    val id: String = "",
    val origin: String = "",
    val destination: String = "",
    val departureDate: String = "",
    val returnDate: String = "",
    val transport: String = "",
    val estimatedCost: Double = 0.0,
    val actualCost: Double = 0.0,
    val distance: Int = 0,
    val status: TripStatus = TripStatus.PLANNED,
    val createdAt: Long = System.currentTimeMillis(),
    val userId: String = ""
)

enum class TripStatus {
    PLANNED,
    ONGOING,
    COMPLETED,
    CANCELLED
}