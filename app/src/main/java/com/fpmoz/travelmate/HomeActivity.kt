package com.fpmoz.travelmate

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private val pricePerKm = mapOf(
        "Car" to 0.5,
        "Bus" to 0.2,
        "Train" to 0.15,
        "Plane" to 0.8
    )

    private val distanceMap = mapOf(
        Pair("Zagreb", "Split") to 410,
        Pair("Zagreb", "Rijeka") to 160,
        Pair("Split", "Dubrovnik") to 220,
        Pair("Rijeka", "Osijek") to 520,
        Pair("Zagreb", "Osijek") to 280,
        Pair("Split", "Zagreb") to 410,
        Pair("Osijek", "Zagreb") to 280
        // Add more routes as needed
    )

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val currentLocation = findViewById<EditText>(R.id.currentLocationEditText)
        val destination = findViewById<EditText>(R.id.destinationEditText)
        val transportSpinner = findViewById<Spinner>(R.id.transportSpinner)
        val calculateButton = findViewById<Button>(R.id.calculateButton)
        val resultTextView = findViewById<TextView>(R.id.resultTextView)

        val transportOptions = pricePerKm.keys.toList()
        transportSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            transportOptions
        )

        calculateButton.setOnClickListener {
            val from = currentLocation.text.toString().trim().capitalize()
            val to = destination.text.toString().trim().capitalize()
            val transport = transportSpinner.selectedItem.toString()

            val distance = distanceMap[Pair(from, to)] ?: distanceMap[Pair(to, from)]

            if (distance != null) {
                val cost = distance * (pricePerKm[transport] ?: 0.0)
                resultTextView.text = "Distance: $distance km\nTransport: $transport\nEstimated Cost: $%.2f".format(cost)
            } else {
                resultTextView.text = "Route not found. Try another city combination."
            }
        }
    }
}
