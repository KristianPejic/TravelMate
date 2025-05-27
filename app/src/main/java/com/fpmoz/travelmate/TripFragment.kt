package com.fpmoz.travelmate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fpmoz.travelmate.databinding.FragmentTripBinding
import com.fpmoz.travelmate.model.Trip
import com.fpmoz.travelmate.model.TripStatus
import com.fpmoz.travelmate.repository.TripRepository
import com.fpmoz.travelmate.utils.TripCalculator
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

class TripFragment : Fragment() {

    private var _binding: FragmentTripBinding? = null
    private val binding get() = _binding!!

    private lateinit var tripRepository: TripRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTripBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tripRepository = TripRepository()
        setupUI()
    }

    private fun setupUI() {
        // Setup transport spinner
        val transports = TripCalculator.getAvailableTransports()
        binding.transportSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            transports
        )

        // Setup date pickers
        val calendar = Calendar.getInstance()

        binding.departureDateBtn.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.departureDateBtn.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        binding.returnDateBtn.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    binding.returnDateBtn.text = dateFormat.format(calendar.time)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        // Save trip button
        binding.saveTripBtn.setOnClickListener {
            saveTrip()
        }
    }

    private fun saveTrip() {
        val origin = binding.originInput.text.toString().trim()
        val destination = binding.destinationInput.text.toString().trim()
        val departureDate = binding.departureDateBtn.text.toString()
        val returnDate = binding.returnDateBtn.text.toString()
        val transport = binding.transportSpinner.selectedItem.toString()

        // Validation
        if (origin.isEmpty() || destination.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in origin and destination", Toast.LENGTH_SHORT).show()
            return
        }

        if (departureDate == "Select Departure Date" || returnDate == "Select Return Date") {
            Toast.makeText(requireContext(), "Please select both dates", Toast.LENGTH_SHORT).show()
            return
        }

        // Calculate trip details
        val calculation = TripCalculator.calculateTrip(origin, destination, transport)

        if (!calculation.isRouteFound) {
            Toast.makeText(requireContext(), "Route not found. Please try different cities.", Toast.LENGTH_SHORT).show()
            return
        }

        val trip = Trip(
            origin = origin,
            destination = destination,
            departureDate = departureDate,
            returnDate = returnDate,
            transport = transport,
            estimatedCost = calculation.estimatedCost,
            distance = calculation.distance,
            status = TripStatus.PLANNED
        )

        // Save to Firebase
        lifecycleScope.launch {
            binding.saveTripBtn.isEnabled = false
            binding.saveTripBtn.text = "Saving..."

            try {
                tripRepository.saveTrip(trip).fold(
                    onSuccess = { tripId ->
                        Toast.makeText(requireContext(), "Trip saved successfully!", Toast.LENGTH_SHORT).show()
                        clearForm()

                        // Show success message with trip details
                        showSuccessMessage(trip)
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), "Error saving trip: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                binding.saveTripBtn.isEnabled = true
                binding.saveTripBtn.text = "Save Trip"
            }
        }
    }

    private fun clearForm() {
        binding.originInput.text.clear()
        binding.destinationInput.text.clear()
        binding.departureDateBtn.text = "Select Departure Date"
        binding.returnDateBtn.text = "Select Return Date"
        binding.transportSpinner.setSelection(0)
    }

    private fun showSuccessMessage(trip: Trip) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Trip Created! 🎉")
            .setMessage("""
                Your trip has been saved successfully!
                
                ${trip.origin} → ${trip.destination}
                ${trip.departureDate} - ${trip.returnDate}
                ${trip.transport} • ${trip.distance} km
                Estimated cost: €%.2f
                
                You can view and manage your trips in the Home tab.
            """.trimIndent().format(trip.estimatedCost))
            .setPositiveButton("View Trips") { _, _ ->
                // Switch to home tab to show the trip
                // You might need to implement this based on your MainActivity structure
            }
            .setNegativeButton("Create Another", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}