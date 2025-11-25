package com.example.mondaycloneapp

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mondaycloneapp.models.ContactUs
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class ContactUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        val etName = findViewById<TextInputEditText>(R.id.et_name)
        val etEmail = findViewById<TextInputEditText>(R.id.et_email)
        val etPhone = findViewById<TextInputEditText>(R.id.et_phone)
        val etCompany = findViewById<TextInputEditText>(R.id.et_company)
        val etWebsite = findViewById<TextInputEditText>(R.id.et_website)
        val etSubject = findViewById<TextInputEditText>(R.id.et_subject)
        val etMessage = findViewById<TextInputEditText>(R.id.et_message)
        val btnSend = findViewById<Button>(R.id.btn_send)

        btnSend.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val company = etCompany.text.toString().trim()
            val website = etWebsite.text.toString().trim()
            val subject = etSubject.text.toString().trim()
            val message = etMessage.text.toString().trim()
            val user = FirebaseAuth.getInstance().currentUser
            val loginGmail = user?.email

            if (name.isEmpty()) {
                etName.error = "Name is required"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                etEmail.error = "Please enter a valid email"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (phone.isNotEmpty() && !Patterns.PHONE.matcher(phone).matches()) {
                etPhone.error = "Please enter a valid phone number"
                etPhone.requestFocus()
                return@setOnClickListener
            }

            if (subject.isEmpty()) {
                etSubject.error = "Subject is required"
                etSubject.requestFocus()
                return@setOnClickListener
            }

            if (message.isEmpty()) {
                etMessage.error = "Message is required"
                etMessage.requestFocus()
                return@setOnClickListener
            }

            val contactUs = ContactUs(name, email, phone, company, website, subject, message, loginGmail)
            FirebaseDatabase.getInstance().getReference("ContactUs")
                .push()
                .setValue(contactUs)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Your message has been sent.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}