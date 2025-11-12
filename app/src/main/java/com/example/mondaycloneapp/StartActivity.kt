package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class StartActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnCreateAccount: Button = findViewById(R.id.btn_create_new_account)

        btnLogin.setOnClickListener { navigateToLoginScreen() }
        btnCreateAccount.setOnClickListener {
            val intent = Intent(this, CreateAccountActivity::class.java)
            startActivity(intent)
        }
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginStartActivity::class.java)
        startActivity(intent)
    }
}