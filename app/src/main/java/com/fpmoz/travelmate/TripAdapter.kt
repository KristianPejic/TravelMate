package com.fpmoz.travelmate.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.fpmoz.travelmate.databinding.ItemTripBinding
import com.fpmoz.travelmate.model.Trip

class TripAdapter(
    private val items: List<Trip>
) : RecyclerView.Adapter<TripAdapter.TripViewHolder>() {

    inner class TripViewHolder(val binding: ItemTripBinding)
        : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val binding = ItemTripBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return TripViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        val trip = items[position]
        with(holder.binding) {
            originText.text = trip.origin
            destinationText.text = trip.destination
            datesText.text = "${trip.departureDate} → ${trip.returnDate}"
            transportText.text = trip.transport
            costText.text = "$${"%.2f".format(trip.estimatedCost)}"
        }
    }

    override fun getItemCount() = items.size
}
