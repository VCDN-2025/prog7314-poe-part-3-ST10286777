package com.arcadia.trivora

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.arcadia.trivora.databinding.ActivityProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        setupUserInfo()
        setupLogoutButton()
        setupEditNameButton()
        loadUserStats()

        // Handle navigation item clicks
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    val intent = Intent(this@ProfileActivity, MainActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_random_quiz -> {
                    val intent = Intent(this@ProfileActivity, RandomQuizActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this@ProfileActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    // Already on profile page
                    true
                }
                else -> false
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to profile
        loadUserStats()
    }

    private fun setupUserInfo() {
        // Get user data from SharedPrefs
        val userEmail = SharedPrefs.getUserEmail() ?: "Not available"

        binding.tvUserEmail.text = userEmail

        // Initially set display name to email
        binding.tvDisplayName.text = userEmail
    }

    private fun setupEditNameButton() {
        binding.btnEditName.setOnClickListener {
            showEditNameDialog()
        }
    }

    private fun showEditNameDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_name, null)
        val etDisplayName = dialogView.findViewById<TextInputEditText>(R.id.etDisplayName)
        // Get user data from SharedPrefs
        val userEmail = SharedPrefs.getUserEmail() ?: "Not available"

        // Pre-fill with current display name if available
        val currentUser = firebaseAuth.currentUser
        currentUser?.displayName?.let { name ->
            if (name.isNotEmpty() && name != userEmail) {
                etDisplayName.setText(name)
            }
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Edit Display Name")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, which ->
                val newName = etDisplayName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    updateDisplayName(newName)
                } else {
                    Toast.makeText(this, "Display name cannot be empty", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun updateDisplayName(newName: String) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val request = UpdateDisplayNameRequest(newName)
                val response = RetrofitClient.instance.updateDisplayName(request)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        // Update local display
                        binding.tvDisplayName.text = newName

                        // Update Firebase auth display name
                        val user = firebaseAuth.currentUser
                        user?.let {
                            val profileUpdates = com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                .setDisplayName(newName)
                                .build()

                            it.updateProfile(profileUpdates)
                        }

                        Toast.makeText(this@ProfileActivity, "Display name updated successfully!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to update display name: ${apiResponse?.message}", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Server error: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun loadUserStats() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Load user profile first to get display name
                val profileResponse = RetrofitClient.instance.getProfile()
                if (profileResponse.isSuccessful) {
                    val profileApiResponse = profileResponse.body()
                    if (profileApiResponse?.success == true) {
                        // Update display name from profile
                        val displayName = profileApiResponse.data?.user?.displayName
                        if (!displayName.isNullOrEmpty()) {
                            binding.tvDisplayName.text = displayName
                        } else {
                            binding.tvDisplayName.text = "Set Display Name"
                        }
                    }
                }

                // Load user stats
                val statsResponse = RetrofitClient.instance.getUserStats()
                if (statsResponse.isSuccessful) {
                    val apiResponse = statsResponse.body()
                    if (apiResponse?.success == true) {
                        val stats = apiResponse.data
                        updateStatsUI(stats)
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to load stats: ${apiResponse?.message}", Toast.LENGTH_SHORT).show()
                        setDefaultStats()
                    }
                } else {
                    Toast.makeText(this@ProfileActivity, "Server error: ${statsResponse.code()}", Toast.LENGTH_SHORT).show()
                    setDefaultStats()
                }
            } catch (e: Exception) {
                Toast.makeText(this@ProfileActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
                setDefaultStats()
            }
        }
    }

    private fun updateStatsUI(stats: UserStatsResponse?) {
        stats?.let {
            binding.tvTotalScore.text = it.totalScore.toString()
            binding.tvTotalQuizzes.text = it.totalQuizzes.toString()
            binding.tvCorrectAnswers.text = it.correctAnswers.toString()
            binding.tvTotalQuestions.text = it.totalQuestions.toString()
            binding.tvAverageScore.text = "${String.format("%.1f", it.averageScore)}%"
            binding.tvBestCategory.text = it.bestCategory.ifEmpty { "None" }
        }
    }

    private fun setDefaultStats() {
        binding.tvTotalScore.text = "0"
        binding.tvTotalQuizzes.text = "0"
        binding.tvCorrectAnswers.text = "0"
        binding.tvTotalQuestions.text = "0"
        binding.tvAverageScore.text = "0%"
        binding.tvBestCategory.text = "None"
        binding.tvDisplayName.text = "Set Display Name"
    }

    private fun setupLogoutButton() {
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes, Logout") { dialog, which ->
                performLogout()
            }
            .setNegativeButton("Cancel") { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performLogout() {
        try {
            // Clear SharedPrefs data
            SharedPrefs.clearUserData()

            // Sign out from Firebase
            firebaseAuth.signOut()

            // Sign out from Google
            GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN).signOut()

            // Navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()

            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Toast.makeText(this, "Logout failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}