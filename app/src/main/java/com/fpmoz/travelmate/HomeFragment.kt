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
            }
        )

        tripsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = tripAdapter
        }
    }

    private fun loadTripHistory() {
        // Check if view is still attached
        if (!isAdded || view == null) return

        lifecycleScope.launch {
            try {
                loadingProgressBar?.visibility = View.VISIBLE
                emptyStateText?.visibility = View.GONE

                tripRepository.getUserTrips().fold(
                    onSuccess = { trips ->
                        // Check if view is still attached before updating UI
                        if (!isAdded || view == null) return@fold

                        loadingProgressBar?.visibility = View.GONE

                        if (trips.isEmpty()) {
                            emptyStateText?.visibility = View.VISIBLE
                            tripsRecyclerView?.visibility = View.GONE
                            statsCard?.visibility = View.GONE
                        } else {
                            emptyStateText?.visibility = View.GONE
                            tripsRecyclerView?.visibility = View.VISIBLE
                            statsCard?.visibility = View.VISIBLE

                            tripAdapter.submitList(trips)
                            updateStatistics(trips)
                        }
                    },
                    onFailure = { error ->
                        // Check if view is still attached before updating UI
                        if (!isAdded || view == null) return@fold

                        loadingProgressBar?.visibility = View.GONE
                        emptyStateText?.text = "Error loading trips: ${error.message}"
                        emptyStateText?.visibility = View.VISIBLE
                    }
                )
            } catch (e: Exception) {
                // Check if view is still attached before updating UI
                if (!isAdded || view == null) return@launch

                loadingProgressBar?.visibility = View.GONE
                emptyStateText?.text = "Unexpected error: ${e.message}"
                emptyStateText?.visibility = View.VISIBLE
            }
        }
    }

    private fun updateStatistics(trips: List<Trip>) {
        // Check if view is still attached
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

        // Calculate average cost per trip
        val avgCost = if (totalTrips > 0) {
            if (totalActualCost > 0) totalActualCost / totalTrips else totalEstimatedCost / totalTrips
        } else 0.0
        avgCostText?.text = "€%.2f avg per trip".format(avgCost)
    }

    private fun showTripDetails(trip: Trip) {
        // Check if fragment is still attached
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
        loadTripHistory() // Refresh data when returning to this fragment
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear all view references to prevent memory leaks
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