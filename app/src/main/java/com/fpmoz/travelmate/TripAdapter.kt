package com.fpmoz.travelmate.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.fpmoz.travelmate.R
import com.fpmoz.travelmate.model.Trip
import com.fpmoz.travelmate.model.TripStatus

class TripAdapter(
    private val onTripClick: (Trip) -> Unit = {},
    private val onDeleteClick: (Trip) -> Unit = {}
) : ListAdapter<Trip, TripAdapter.TripViewHolder>(TripDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_trip, parent, false)
        return TripViewHolder(view)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TripViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val originText: TextView = itemView.findViewById(R.id.originText)
        private val destinationText: TextView = itemView.findViewById(R.id.destinationText)
        private val datesText: TextView = itemView.findViewById(R.id.datesText)
        private val transportText: TextView = itemView.findViewById(R.id.transportText)
        private val costText: TextView = itemView.findViewById(R.id.costText)
        private val deleteButton: Button = itemView.findViewById(R.id.deleteButton)

        fun bind(trip: Trip) {
            // Route information
            originText.text = "${trip.origin} → ${trip.destination}"

            // Status badge with dynamic colors
            destinationText.text = trip.status.name
            val statusColor = when (trip.status) {
                TripStatus.PLANNED -> "#2196F3"  // Blue
                TripStatus.ONGOING -> "#FF9800"  // Orange
                TripStatus.COMPLETED -> "#4CAF50" // Green
                TripStatus.CANCELLED -> "#F44336" // Red
            }

            // Set status badge background color
            val statusDrawable = ContextCompat.getDrawable(itemView.context, R.drawable.status_badge_background)
            statusDrawable?.setTint(android.graphics.Color.parseColor(statusColor))
            destinationText.background = statusDrawable

            // Date information
            datesText.text = "${trip.departureDate} - ${trip.returnDate}"

            // Transport and distance
            transportText.text = if (trip.distance > 0) {
                "${trip.transport} • ${trip.distance} km"
            } else {
                trip.transport
            }

            // Cost information
            if (trip.actualCost > 0.0) {
                costText.text = "€%.2f".format(trip.actualCost)
            } else {
                costText.text = "€%.2f".format(trip.estimatedCost)
            }

            // Click listeners
            itemView.setOnClickListener { onTripClick(trip) }
            deleteButton.setOnClickListener { onDeleteClick(trip) }

            // Add subtle animation on card click
            itemView.setOnClickListener {
                itemView.animate()
                    .scaleX(0.98f)
                    .scaleY(0.98f)
                    .setDuration(100)
                    .withEndAction {
                        itemView.animate()
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(100)
                            .start()
                        onTripClick(trip)
                    }
                    .start()
            }
        }
    }

    class TripDiffCallback : DiffUtil.ItemCallback<Trip>() {
        override fun areItemsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Trip, newItem: Trip): Boolean {
            return oldItem == newItem
        }
    }
}