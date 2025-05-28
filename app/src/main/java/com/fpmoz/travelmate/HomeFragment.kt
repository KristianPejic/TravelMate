package com.fpmoz.travelmate

import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fpmoz.travelmate.model.Trip
import com.fpmoz.travelmate.model.TripStatus
import com.fpmoz.travelmate.repository.TripRepository
import com.fpmoz.travelmate.ui.adapter.TripAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var tripRepository: TripRepository
    private lateinit var tripAdapter: TripAdapter

    // Views - using findViewById to avoid binding issues
    private var loadingProgressBar: ProgressBar? = null
    private var emptyStateText: TextView? = null
    private var tripsRecyclerView: RecyclerView? = null
    private var statsCard: androidx.cardview.widget.CardView? = null
    private var totalTripsText: TextView? = null
    private var completedTripsText: TextView? = null
    private var plannedTripsText: TextView? = null
    private var totalDistanceText: TextView? = null
    private var totalCostText: TextView? = null
    private var avgCostText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripRepository = TripRepository()
        initViews(view)
        setupRecyclerView()
        loadTripHistory()
    }

    private fun initViews(view: View) {
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar)
        emptyStateText = view.findViewById(R.id.emptyStateText)
        tripsRecyclerView = view.findViewById(R.id.tripsRecyclerView)
        statsCard = view.findViewById(R.id.statsCard)
        totalTripsText = view.findViewById(R.id.totalTripsText)
        completedTripsText = view.findViewById(R.id.completedTripsText)
        plannedTripsText = view.findViewById(R.id.plannedTripsText)
        totalDistanceText = view.findViewById(R.id.totalDistanceText)
        totalCostText = view.findViewById(R.id.totalCostText)
        avgCostText = view.findViewById(R.id.avgCostText)
    }

    private fun setupRecyclerView() {
        tripAdapter = TripAdapter(
            onTripClick = { trip ->
                showTripDetails(trip)
            },
            onDeleteClick = { trip ->
                confirmDeleteTrip(trip)
            }
        )

        tripsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tripAdapter
        }
    }

    private fun confirmDeleteTrip(trip: Trip) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Delete Trip")
            .setMessage("Are you sure you want to delete this trip?\n\n${trip.origin} → ${trip.destination}")
            .setPositiveButton("Delete") { _, _ ->
                deleteTrip(trip)
            }
            .setNegativeButton("Cancel", null)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show()
    }

    private fun deleteTrip(trip: Trip) {
        lifecycleScope.launch {
            try {
                tripRepository.deleteTrip(trip.id).fold(
                    onSuccess = {
                        Toast.makeText(requireContext(), "Trip deleted successfully!", Toast.LENGTH_SHORT).show()

                        // Remove from current list immediately for better UX
                        val currentList = tripAdapter.currentList.toMutableList()
                        currentList.remove(trip)
                        tripAdapter.submitList(currentList)

                        // Update statistics immediately
                        updateStatistics(currentList)

                        // Check if list is empty after deletion
                        if (currentList.isEmpty()) {
                            showEmptyState()
                        }

                        // Refresh from server to ensure consistency
                        loadTripHistoryQuietly()
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), "Error deleting trip: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadTripHistory() {

        if (!isAdded || view == null) return

        lifecycleScope.launch {
            try {
                loadingProgressBar?.visibility = View.VISIBLE
                emptyStateText?.visibility = View.GONE

                tripRepository.getUserTrips().fold(
                    onSuccess = { trips ->

                        if (!isAdded || view == null) return@fold

                        loadingProgressBar?.visibility = View.GONE

                        if (trips.isEmpty()) {
                            showEmptyState()
                        } else {
                            showTripsState()
                            tripAdapter.submitList(trips)
                            updateStatistics(trips)
                        }
                    },
                    onFailure = { error ->

                        if (!isAdded || view == null) return@fold

                        loadingProgressBar?.visibility = View.GONE
                        emptyStateText?.text = "Error loading trips: ${error.message}"
                        emptyStateText?.visibility = View.VISIBLE
                    }
                )
            } catch (e: Exception) {

                if (!isAdded || view == null) return@launch

                loadingProgressBar?.visibility = View.GONE
                emptyStateText?.text = "Unexpected error: ${e.message}"
                emptyStateText?.visibility = View.VISIBLE
            }
        }
    }


    private fun loadTripHistoryQuietly() {
        if (!isAdded || view == null) return

        lifecycleScope.launch {
            try {
                tripRepository.getUserTrips().fold(
                    onSuccess = { trips ->
                        if (!isAdded || view == null) return@fold

                        if (trips.isEmpty()) {
                            showEmptyState()
                        } else {
                            showTripsState()
                            tripAdapter.submitList(trips)
                            updateStatistics(trips)
                        }
                    },
                    onFailure = {  }
                )
            } catch (e: Exception) {

            }
        }
    }

    private fun showEmptyState() {
        emptyStateText?.visibility = View.VISIBLE
        tripsRecyclerView?.visibility = View.GONE
        statsCard?.visibility = View.GONE
    }

    private fun showTripsState() {
        emptyStateText?.visibility = View.GONE
        tripsRecyclerView?.visibility = View.VISIBLE
        statsCard?.visibility = View.VISIBLE
    }

    private fun updateStatistics(trips: List<Trip>) {

        if (!isAdded || view == null) return

        val totalTrips = trips.size
        val completedTrips = trips.count { it.status == TripStatus.COMPLETED }
        val plannedTrips = trips.count { it.status == TripStatus.PLANNED }
        val totalEstimatedCost = trips.sumOf { it.estimatedCost }
        val totalActualCost = trips.sumOf { it.actualCost }
        val totalDistance = trips.sumOf { it.distance }

        totalTripsText?.text = totalTrips.toString()
        completedTripsText?.text = completedTrips.toString()
        plannedTripsText?.text = plannedTrips.toString()
        totalDistanceText?.text = "$totalDistance km"

        if (totalActualCost > 0) {
            totalCostText?.text = "€%.2f (actual)".format(totalActualCost)
        } else {
            totalCostText?.text = "€%.2f (estimated)".format(totalEstimatedCost)
        }


        val avgCost = if (totalTrips > 0) {
            if (totalActualCost > 0) totalActualCost / totalTrips else totalEstimatedCost / totalTrips
        } else 0.0
        avgCostText?.text = "€%.2f avg per trip".format(avgCost)
    }

    private fun showTripDetails(trip: Trip) {

        if (!isAdded) return

        val message = """
            Origin: ${trip.origin}
            Destination: ${trip.destination}
            Departure: ${trip.departureDate}
            Return: ${trip.returnDate}
            Transport: ${trip.transport}
            Distance: ${trip.distance} km
            Estimated Cost: €%.2f
            Status: ${trip.status.name}
        """.trimIndent().format(trip.estimatedCost)

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Trip Details")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadTripHistory()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        loadingProgressBar = null
        emptyStateText = null
        tripsRecyclerView = null
        statsCard = null
        totalTripsText = null
        completedTripsText = null
        plannedTripsText = null
        totalDistanceText = null
        totalCostText = null
        avgCostText = null
    }
}