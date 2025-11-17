package com.example.mondaycloneapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.mondaycloneapp.models.User
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class LoginStartActivity : AppCompatActivity() {

    private lateinit var editTextEmail: EditText
    private lateinit var editTextPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var createAccountTextView: TextView
    private lateinit var btnBack: ImageView
    private lateinit var btnGoogleSignIn: MaterialButton
    private lateinit var loadingAnimation: LottieAnimationView
    private lateinit var contentScrollView: ScrollView

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

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
                    hideLoading()
                }
            } else {
                hideLoading()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_start)

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        editTextEmail = findViewById(R.id.et_email_address)
        editTextPassword = findViewById(R.id.editTextPassword)
        buttonLogin = findViewById(R.id.btn_login)
        createAccountTextView = findViewById(R.id.tv_create_account)
        btnBack = findViewById(R.id.btn_back)
        btnGoogleSignIn = findViewById(R.id.btn_google_signup)
        loadingAnimation = findViewById(R.id.loading_animation)
        contentScrollView = findViewById(R.id.scroll_view)

        btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        buttonLogin.setOnClickListener {
            val email = editTextEmail.text.toString()
            val password = editTextPassword.text.toString()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                showLoading()
                firebaseLogin(email, password)
            }
        }

        createAccountTextView.setOnClickListener {
            startActivity(Intent(this, CreateAccountActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun showLoading() {
        contentScrollView.visibility = View.GONE
        loadingAnimation.visibility = View.VISIBLE
    }

    private fun hideLoading() {
        contentScrollView.visibility = View.VISIBLE
        loadingAnimation.visibility = View.GONE
    }

    private fun signInWithGoogle() {
        showLoading()
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                hideLoading()
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val user = User(id = firebaseUser!!.uid, name = firebaseUser.displayName ?: "", email = firebaseUser.email!!)

                    val database = FirebaseDatabase.getInstance().getReference("users")
                    database.child(firebaseUser.uid).setValue(user).addOnCompleteListener { 
                        saveUserFcmToken()
                        Toast.makeText(this, "Google Login successful!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this, "Firebase Google Auth failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun firebaseLogin(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                hideLoading()
                if (task.isSuccessful) {
                    saveUserFcmToken()
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Authentication failed. ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveUserFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

            // Save token to Realtime Database
            val database = FirebaseDatabase.getInstance().getReference("users")
            database.child(userId).child("fcmToken").setValue(token)
        }
    }
}