package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginStartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_start)

        // Find the "Create account" button from the XML
        val createAccountButton: Button = findViewById(R.id.btn_create_account)

        // Set the listener: when this button is clicked, execute the code inside
        createAccountButton.setOnClickListener {
            // For a simple project, we'll treat this as a "continue" button for now,
            // bypassing complex sign-up and jumping to the next screen.

            // This is equivalent to starting a new Activity using Intent [cite: 37, 38]
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)

            // Optional: Close the login screen after successful entry
            // finish()
        }
    }
}