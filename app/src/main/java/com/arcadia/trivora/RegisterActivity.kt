package com.arcadia.trivora

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.arcadia.trivora.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        binding.textView.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.registerButton.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val pass = binding.passET.text.toString()
            val confirmPass = binding.confirmPassEt.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    registerUser(email, pass)
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Empty Fields Are not Allowed !!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    fetchTokenAndRegisterWithBackend()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchTokenAndRegisterWithBackend() {
        val user = firebaseAuth.currentUser
        user?.getIdToken(true)?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val idToken = task.result.token
                if (idToken != null) {
                    registerWithBackend(idToken)
                } else {
                    Toast.makeText(this, "Failed to get authentication token", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Token fetch failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerWithBackend(idToken: String) {
        SharedPrefs.saveAuthToken(idToken)
        SharedPrefs.saveUserInfo(
            firebaseAuth.currentUser?.email ?: "",
            firebaseAuth.currentUser?.uid ?: ""
        )

        firebaseAuth.currentUser?.email?.let { email ->
            SharedPrefs.saveBiometricEmail(email)
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = RetrofitClient.instance.getProfile()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        sendFCMTokenToBackend()
                        showBiometricSetupDialog()
                    } else {
                        sendFCMTokenToBackend()
                        showBiometricSetupDialog()
                    }
                } else {
                    when (response.code()) {
                        404 -> {
                            sendFCMTokenToBackend()
                            showBiometricSetupDialog()
                        }
                        401 -> {
                            Toast.makeText(this@RegisterActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                            navigateToMainActivity()
                        }
                        else -> {
                            // For other errors, still show biometric setup since Firebase auth worked
                            sendFCMTokenToBackend()
                            showBiometricSetupDialog()
                        }
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                showBiometricSetupDialog()
            }
        }
    }

    // Send FCM token to backend
    private fun sendFCMTokenToBackend() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "Sending FCM token to backend after registration: $token")

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.instance.updateFCMToken(UpdateFCMTokenRequest(token))
                        if (response.isSuccessful) {
                            Log.d("FCM", "FCM token saved to backend after registration")
                        } else {
                            Log.e("FCM", "Failed to save FCM token after registration: ${response.code()}")
                        }
                    } catch (e: Exception) {
                        Log.e("FCM", "Error saving FCM token after registration", e)
                    }
                }
            } else {
                Log.e("FCM", "FCM token fetch failed after registration", task.exception)
            }
        }
    }

    // Show biometric setup dialog after successful registration
    private fun showBiometricSetupDialog() {
        if (BiometricLogin.isBiometricAvailable(this)) {
            AlertDialog.Builder(this)
                .setTitle("Registration Successful!")
                .setMessage("For more secure access, would you like to enable biometric login?\n\n• If enabled: You'll use fingerprint to login\n• If disabled: You'll auto-login without biometrics")
                .setPositiveButton("Enable Biometrics") { dialog, which ->
                    SharedPrefs.setBiometricEnabled(true)
                    Toast.makeText(this, "Biometric login enabled!", Toast.LENGTH_LONG).show()
                    navigateToMainActivity()
                }
                .setNegativeButton("Auto-Login Instead") { dialog, which ->
                    SharedPrefs.setBiometricEnabled(false)
                    Toast.makeText(this, "Biometric login disabled!", Toast.LENGTH_LONG).show()
                    navigateToMainActivity()
                }
                .setCancelable(false)
                .show()
        } else {
            // Biometrics not available
            AlertDialog.Builder(this)
                .setTitle("Registration Successful!")
                .setPositiveButton("Continue") { dialog, which ->
                    navigateToMainActivity()
                }
                .setCancelable(false)
                .show()
        }
    }

    // Helper function to navigate to MainActivity
    private fun navigateToMainActivity() {
        val intent = Intent(this@RegisterActivity, MainActivity::class.java)
        intent.putExtra("username", firebaseAuth.currentUser?.email ?: "Player")
        startActivity(intent)
        finish()
    }
}