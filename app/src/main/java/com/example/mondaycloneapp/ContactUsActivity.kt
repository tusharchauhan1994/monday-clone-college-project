package com.example.mondaycloneapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mondaycloneapp.models.ContactUs
import com.google.firebase.database.FirebaseDatabase

class ContactUsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_us)

        val etName = findViewById<EditText>(R.id.et_name)
        val etEmail = findViewById<EditText>(R.id.et_email)
        val etContactNumber = findViewById<EditText>(R.id.et_contact_number)
        val etMessage = findViewById<EditText>(R.id.et_message)
        val btnSubmit = findViewById<Button>(R.id.btn_submit)

        btnSubmit.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val contactNumber = etContactNumber.text.toString().trim()
            val message = etMessage.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && contactNumber.isNotEmpty() && message.isNotEmpty()) {
                val contactUs = ContactUs(name, email, contactNumber, message)
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
            } else {
                Toast.makeText(this, "Please fill all the fields.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}