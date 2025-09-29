package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginStartActivity : AppCompatActivity() {

    // Declare the UI components we need to interact with
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonCreateAccount: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_start)

        // Initialize UI elements using their IDs from the XML
        // Note: et_email_address is used for the email input
        editTextEmail = findViewById(R.id.et_email_address)
        // Note: editTextPassword is used for the password input
        editTextPassword = findViewById(R.id.editTextPassword)
        // Note: btn_create_account is used as the trigger button
        buttonCreateAccount = findViewById(R.id.btn_create_account)

        // Set the click listener for the "Create account" button
        buttonCreateAccount.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()

            // Input validation
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                // Use the static authentication logic
                authenticateUser(email, password)
            }
        }
    }

    /**
     * Implements static user authentication logic for demonstration.
     * On success, navigates to HomeActivity. On failure, shows a Toast.
     */
    private fun authenticateUser(email: String, password: String) {
        // Hardcoded credentials for static check
        val validEmail = "tushar@gmail.com"
        val validPassword = "123456"

        // Check credentials
        if (email == validEmail && password == validPassword) {
            // Success: Treat as successful sign-up/login and navigate
            Toast.makeText(this, "Account created/Login successful!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Close the current screen
        } else {
            // Failure
            Toast.makeText(this, "Authentication failed. Use email: tushar@gmail.com and pass: 123456", Toast.LENGTH_LONG).show()
        }
    }
}
