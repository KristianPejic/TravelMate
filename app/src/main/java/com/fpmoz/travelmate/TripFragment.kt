package com.fpmoz.travelmate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.fpmoz.travelmate.databinding.FragmentTripBinding
import com.fpmoz.travelmate.model.Trip
import com.fpmoz.travelmate.model.TripStatus
import com.fpmoz.travelmate.repository.TripRepository
import com.fpmoz.travelmate.utils.TripCalculator
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TripFragment : Fragment() {

    private var _binding: FragmentTripBinding? = null
    private val binding get() = _binding!!

    private lateinit var tripRepository: TripRepository
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)


    private lateinit var originSpinner: Spinner
    private lateinit var destinationSpinner: Spinner
    private lateinit var transportSpinner: Spinner
    private lateinit var helperText: TextView
    private lateinit var previewCard: CardView
    private lateinit var previewRoute: TextView
    private lateinit var previewDistance: TextView
    private lateinit var previewCost: TextView
    private lateinit var saveTripBtn: Button
    private lateinit var departureDateBtn: Button
    private lateinit var returnDateBtn: Button

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
        initViews(view)
        setupSpinners()
        setupDatePickers()
        setupSaveButton()
    }

    private fun initViews(view: View) {
        originSpinner = view.findViewById(R.id.originSpinner)
        destinationSpinner = view.findViewById(R.id.destinationSpinner)
        transportSpinner = view.findViewById(R.id.transportSpinner)
        helperText = view.findViewById(R.id.helperText)
        previewCard = view.findViewById(R.id.previewCard)
        previewRoute = view.findViewById(R.id.previewRoute)
        previewDistance = view.findViewById(R.id.previewDistance)
        previewCost = view.findViewById(R.id.previewCost)
        saveTripBtn = view.findViewById(R.id.saveTripBtn)
        departureDateBtn = view.findViewById(R.id.departureDateBtn)
        returnDateBtn = view.findViewById(R.id.returnDateBtn)
    }

    private fun setupSpinners() {

        val allCities = listOf("Select Origin City") + TripCalculator.getAvailableCities()
        val originAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, allCities)
        originSpinner.adapter = originAdapter


        val transports = TripCalculator.getAvailableTransports()
        val transportAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, transports)
        transportSpinner.adapter = transportAdapter


        originSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position > 0) { // Not "Select Origin City"
                    val selectedOrigin = allCities[position]
                    setupDestinationSpinner(selectedOrigin)
                    helperText.text = "Now select your destination"
                    helperText.setTextColor(resources.getColor(android.R.color.holo_green_dark, null))
                } else {
                    resetDestinationSpinner()
                    helperText.text = "Please select origin city first"
                    helperText.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
                    hidePreview()
                    updateSaveButtonState()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        destinationSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (destinationSpinner.isEnabled && position > 0) {
                    updateTripPreview()
                    helperText.visibility = View.GONE
                    updateSaveButtonState()
                } else {
                    hidePreview()
                    updateSaveButtonState()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }


        transportSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (isValidSelection()) {
                    updateTripPreview()
                    updateSaveButtonState()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupDestinationSpinner(originCity: String) {
        val availableDestinations = listOf("Select Destination") + TripCalculator.getDestinationCitiesFor(originCity)
        val destinationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, availableDestinations)
        destinationSpinner.adapter = destinationAdapter
        destinationSpinner.isEnabled = true
        destinationSpinner.setSelection(0)
    }

    private fun resetDestinationSpinner() {
        val emptyList = listOf("Select origin first")
        val destinationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, emptyList)
        destinationSpinner.adapter = destinationAdapter
        destinationSpinner.isEnabled = false
    }

    private fun updateTripPreview() {
        if (!isValidSelection()) return

        val origin = originSpinner.selectedItem.toString()
        val destination = destinationSpinner.selectedItem.toString()
        val transport = transportSpinner.selectedItem.toString()

        val calculation = TripCalculator.calculateTrip(origin, destination, transport)

        if (calculation.isRouteFound) {
            previewRoute.text = "$origin → $destination"
            previewDistance.text = "${transport} • ${calculation.distance} km"
            previewCost.text = "Estimated cost: €%.2f".format(calculation.estimatedCost)
            previewCard.visibility = View.VISIBLE
        } else {
            hidePreview()
            Toast.makeText(requireContext(), "Route not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hidePreview() {
        previewCard.visibility = View.GONE
    }

    private fun isValidSelection(): Boolean {
        return originSpinner.selectedItemPosition > 0 &&
                destinationSpinner.selectedItemPosition > 0 &&
                destinationSpinner.isEnabled
    }

    private fun updateSaveButtonState() {
        val hasValidCities = isValidSelection()
        val hasDates = departureDateBtn.text != "📅 Select Departure Date" &&
                returnDateBtn.text != "📅 Select Return Date"

        saveTripBtn.isEnabled = hasValidCities && hasDates
        saveTripBtn.alpha = if (saveTripBtn.isEnabled) 1.0f else 0.5f
    }

    private fun setupDatePickers() {
        val calendar = Calendar.getInstance()

        departureDateBtn.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    departureDateBtn.text = "📅 ${dateFormat.format(calendar.time)}"
                    updateSaveButtonState()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        returnDateBtn.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    returnDateBtn.text = "📅 ${dateFormat.format(calendar.time)}"
                    updateSaveButtonState()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupSaveButton() {
        saveTripBtn.setOnClickListener {
            saveTrip()
        }
    }

    private fun saveTrip() {
        val origin = originSpinner.selectedItem.toString()
        val destination = destinationSpinner.selectedItem.toString()
        val departureDate = departureDateBtn.text.toString().replace("📅 ", "")
        val returnDate = returnDateBtn.text.toString().replace("📅 ", "")
        val transport = transportSpinner.selectedItem.toString()

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

        lifecycleScope.launch {
            saveTripBtn.isEnabled = false
            saveTripBtn.text = "Creating Trip..."

            try {
                tripRepository.saveTrip(trip).fold(
                    onSuccess = { tripId ->
                        Toast.makeText(requireContext(), "Trip created successfully!", Toast.LENGTH_SHORT).show()
                        clearForm()
                        showSuccessMessage(trip)
                    },
                    onFailure = { error ->
                        Toast.makeText(requireContext(), "Error creating trip: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Unexpected error: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                saveTripBtn.isEnabled = true
                saveTripBtn.text = "Create Trip"
                updateSaveButtonState()
            }
        }
    }

    private fun clearForm() {
        originSpinner.setSelection(0)
        destinationSpinner.setSelection(0)
        transportSpinner.setSelection(0)
        departureDateBtn.text = "📅 Select Departure Date"
        returnDateBtn.text = "📅 Select Return Date"
        hidePreview()
        helperText.visibility = View.VISIBLE
        helperText.text = "Please select origin city first"
        helperText.setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
        resetDestinationSpinner()
        updateSaveButtonState()
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

            }
            .setNegativeButton("Create Another", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}