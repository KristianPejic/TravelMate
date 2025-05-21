package com.fpmoz.travelmate

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.DatePicker
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.fpmoz.travelmate.R
import com.fpmoz.travelmate.databinding.FragmentTripBinding
import com.fpmoz.travelmate.model.Trip
import com.fpmoz.travelmate.ui.adapter.TripAdapter
import java.text.SimpleDateFormat
import java.util.*

class TripFragment : Fragment() {

    private var _binding: FragmentTripBinding? = null
    private val binding get() = _binding!!
    private val trips = mutableListOf<Trip>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = FragmentTripBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup transport spinner
        val transports = listOf("Car", "Bus", "Train", "Flight")
        binding.transportSpinner.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            transports
        )

        // Date pickers
        val cal = Calendar.getInstance()
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        binding.departureDateBtn.setOnClickListener {
            DatePickerDialog(requireContext(), { _: DatePicker, y, m, d ->
                cal.set(y, m, d); binding.departureDateBtn.text = fmt.format(cal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show()
        }
        binding.returnDateBtn.setOnClickListener {
            DatePickerDialog(requireContext(), { _: DatePicker, y, m, d ->
                cal.set(y, m, d); binding.returnDateBtn.text = fmt.format(cal.time)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
                .show()
        }

        // RecyclerView
        binding.tripsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.tripsRecyclerView.adapter = TripAdapter(trips)

        // Save trip
        binding.saveTripBtn.setOnClickListener {
            val origin = binding.originInput.text.toString().trim()
            val destination = binding.destinationInput.text.toString().trim()
            val dep = binding.departureDateBtn.text.toString()
            val ret = binding.returnDateBtn.text.toString()
            val transport = binding.transportSpinner.selectedItem.toString()

            // Fake distance & cost
            val distance = 100
            val costPerKm = when (transport) {
                "Car" -> 0.2; "Bus" -> 0.1; "Train" -> 0.15; "Flight" -> 0.5; else -> 0.0
            }
            val estimatedCost = distance * costPerKm

            val trip = Trip(origin, destination, dep, ret, transport, estimatedCost)
            trips.add(0, trip)
            binding.tripsRecyclerView.adapter?.notifyItemInserted(0)
            binding.tripsRecyclerView.scrollToPosition(0)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
