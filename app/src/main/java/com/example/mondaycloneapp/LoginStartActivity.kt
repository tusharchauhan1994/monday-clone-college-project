package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.util.Log // NEW: For logging errors
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn // NEW: Google Sign-In
import com.google.android.gms.auth.api.signin.GoogleSignInClient // NEW: Google Sign-In Client
import com.google.android.gms.auth.api.signin.GoogleSignInOptions // NEW: Google Sign-In Options
import com.google.android.gms.common.api.ApiException // NEW: For handling Google sign-in errors
import com.google.android.material.button.MaterialButton // NEW: Import for the Google button
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider // NEW: Firebase Google Provider

class LoginStartActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonContinue: Button
    private lateinit var btnBack: ImageView
    private lateinit var btnGoogleSignIn: MaterialButton // NEW: Google Button Declaration

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient // NEW: Google Sign-In Client Declaration

    // NEW: Activity Result Launcher for Google Sign-In
    private val googleSignInLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                try {
                    val account = task.getResult(ApiException::class.java)!!
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: ApiException) {
                    Log.w("GoogleSignIn", "Google sign in failed", e)
                    Toast.makeText(this, "Google sign in failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_start)

        auth = FirebaseAuth.getInstance()

        // 1. Initialize Google Sign-In Options and Client
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Use the Web Client ID (audience) from google-services.json if you had one,
            // or use the R.string.default_web_client_id provided by Firebase
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Initialize UI elements
        editTextEmail = findViewById(R.id.et_email_address)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonContinue = findViewById(R.id.btn_create_account)
        btnBack = findViewById(R.id.btn_back)
        btnGoogleSignIn = findViewById(R.id.btn_google_signup) // Initialize Google Button

        // 2. Set Click Listener for Google Button
        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        // Set the click listener for Email/Password (Existing Logic)
        buttonContinue.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                firebaseLogin(email, password)
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    // 3. Helper function to launch the Google Sign-In Intent
    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    // 4. Function to exchange Google ID Token for a Firebase Credential
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Google Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Firebase Google Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    // Existing firebaseLogin remains here (for email/password)
    private fun firebaseLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
}