package com.fpmoz.travelmate.repository

import com.fpmoz.travelmate.model.Trip
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class TripRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val tripsCollection = firestore.collection("trips")

    suspend fun saveTrip(trip: Trip): Result<String> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")
            val tripWithUser = trip.copy(userId = userId)

            val documentRef = tripsCollection.add(tripWithUser).await()
            Result.success(documentRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserTrips(): Result<List<Trip>> {
        return try {
            val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")


            val querySnapshot = tripsCollection
                .whereEqualTo("userId", userId)
                .get()
                .await()

            val trips = querySnapshot.documents.mapNotNull { document ->
                document.toObject(Trip::class.java)?.copy(id = document.id)
            }.sortedByDescending { it.createdAt }

            Result.success(trips)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateTrip(tripId: String, trip: Trip): Result<Unit> {
        return try {
            tripsCollection.document(tripId).set(trip).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteTrip(tripId: String): Result<Unit> {
        return try {
            tripsCollection.document(tripId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}