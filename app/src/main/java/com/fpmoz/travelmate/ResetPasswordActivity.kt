package com.fpmoz.travelmate

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class ResetPasswordActivity : AppCompatActivity() {

    private lateinit var resetEmailTxt: EditText
    private lateinit var resetPasswordBtn: Button
    private lateinit var backButton: ImageButton
    private lateinit var logInTxt: TextView
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reset_password)

        resetEmailTxt = findViewById(R.id.resetEmailTxt)
        resetPasswordBtn = findViewById(R.id.resetPasswordBtn)
        backButton = findViewById(R.id.backButton)
        logInTxt = findViewById(R.id.logInTxt)

        auth = FirebaseAuth.getInstance()

        resetPasswordBtn.setOnClickListener {
            val email = resetEmailTxt.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Reset link sent to your email", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }


        backButton.setOnClickListener {
            finish()
        }


        logInTxt.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}