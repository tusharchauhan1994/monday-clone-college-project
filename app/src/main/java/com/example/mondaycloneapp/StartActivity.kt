package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start) // We will create this layout next

        val btnLogin: Button = findViewById(R.id.btn_login)
        val btnCreateAccount: Button = findViewById(R.id.btn_create_new_account)

        // Both buttons navigate to the LoginStartActivity
        btnLogin.setOnClickListener { navigateToLoginScreen() }
        btnCreateAccount.setOnClickListener { navigateToLoginScreen() }
    }

    private fun navigateToLoginScreen() {
        val intent = Intent(this, LoginStartActivity::class.java)
        startActivity(intent)
    }
}