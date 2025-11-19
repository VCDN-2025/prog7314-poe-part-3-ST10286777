package com.arcadia.trivora

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.arcadia.trivora.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var offlineQuizRepository: OfflineQuizRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val languageCode = LocaleHelper.getPersistedLanguage(this)
        val context = LocaleHelper.setLocale(this, languageCode)
        resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        val database = TrivoraDatabase.getInstance(this)
        offlineQuizRepository = OfflineQuizRepository(database)
        enableEdgeToEdge()
        setupWindowInsets()
        setupRecyclerView()
        setupObservers()
        setupUsername()
        setupOfflineSection()
        setupOfflineDemoControls()

        // Load categories when activity starts
        viewModel.loadCategories()

        // Handle navigation item clicks
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // Already on home, do nothing or refresh
                    true
                }
                R.id.nav_random_quiz -> {
                    val intent = Intent(this@MainActivity, RandomQuizActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_settings -> {
                    val intent = Intent(this@MainActivity, SettingsActivity::class.java)
                    startActivity(intent)
                    true
                }
                R.id.nav_profile -> {
                    val intent = Intent(this@MainActivity, ProfileActivity::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }

        setupFCMTokenManagement()
        val token = SharedPrefs.getAuthToken()
        Log.d("POSTMAN_TEST", "Stored Token: $token")

        if (NetworkUtils.isOnline(this)) {
            SyncService.startSyncService(this)
        }

        testDatabase() // Added: call testDatabase
    }

    private fun testDatabase() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Use local repository instead of application repository
                // Test initialization
                offlineQuizRepository.initializeSyncStatus()

                // Test getting available quizzes (should be empty initially)
                val count = offlineQuizRepository.getAvailableQuizCount()
                Log.d("DATABASE_TEST", "Available quizzes: $count")

                // Test sync status
                val syncStatus = offlineQuizRepository.getSyncStatus()
                Log.d("DATABASE_TEST", "Sync status: $syncStatus")

            } catch (e: Exception) {
                Log.e("DATABASE_TEST", "Database test failed", e)
            }
        }
    }

    private fun setupOfflineSection() {
        // Update offline status
        updateOfflineStatus()

        // Setup offline play button
        binding.btnOfflinePlay.setOnClickListener {
            openOfflineQuizzes()
        }

        // Setup download all button (only show when online)
        binding.btnDownloadAll.setOnClickListener {
            downloadAllCategoriesForOffline()
        }
    }

    private fun setupOfflineDemoControls() {
        // Show/hide offline section based on network
        val isOnline = NetworkUtils.isOnline(this)
        if (isOnline) {
            binding.offlineSection.visibility = View.VISIBLE
            binding.btnDownloadAll.visibility = View.VISIBLE
        } else {
            binding.offlineSection.visibility = View.VISIBLE
            binding.btnDownloadAll.visibility = View.GONE
        }
    }

    private fun updateOfflineStatus() {
        lifecycleScope.launch {
            // Get count of available offline quizzes
            val availableCount = offlineQuizRepository.getAvailableQuizCount()
            binding.tvOfflineStatus.text = getString(R.string.offline_quizzes_available, availableCount)

            // Show/hide offline section based on whether we have offline content
            if (availableCount > 0) {
                binding.offlineSection.visibility = View.VISIBLE
                binding.btnOfflinePlay.isEnabled = true
                binding.btnOfflinePlay.text = getString(R.string.play_downloaded_quizzes)
            } else {
                // Still show but disable the button if no content
                binding.offlineSection.visibility = View.VISIBLE
                binding.btnOfflinePlay.isEnabled = false
                binding.btnOfflinePlay.text = getString(R.string.no_offline_quizzes)
            }
        }
    }

    private fun openOfflineQuizzes() {
        val intent = Intent(this, OfflineQuizzesActivity::class.java)
        startActivity(intent)
    }

    private fun downloadAllCategoriesForOffline() {
        // This will download questions for all available categories
        lifecycleScope.launch {
            try {
                // Get all categories first
                val categories = viewModel.categories.value
                if (categories.isNotEmpty()) {
                    Toast.makeText(this@MainActivity, "Downloading all categories for offline use...", Toast.LENGTH_SHORT).show()

                    // Download each category using repository directly
                    categories.forEach { category ->
                        // Create a temporary QuizViewModel to download questions
                        val quizViewModel = QuizViewModel()
                        quizViewModel.initialize(this@MainActivity, category)
                        quizViewModel.downloadQuestionsForOffline(category)
                        delay(500) // Small delay between downloads
                    }

                    // Update status after download
                    delay(2000)
                    updateOfflineStatus()
                    Toast.makeText(this@MainActivity, "All categories downloaded for offline use!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this@MainActivity, "No categories available to download", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Failed to download categories: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setupFCMTokenManagement() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d("FCM", "FCM Token in MainActivity: $token")
                if (firebaseAuth.currentUser != null) {
                    sendFCMTokenToBackend(token)
                }
            }
        }
    }

    private fun sendFCMTokenToBackend(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.updateFCMToken(UpdateFCMTokenRequest(token))
                if (response.isSuccessful) {
                    Log.d("FCM", "FCM token refreshed in backend")
                } else {
                    Log.e("FCM", "Failed to refresh FCM token: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("FCM", "Error refreshing FCM token", e)
            }
        }
    }

    private fun setupUsername() {
        // Get user data from SharedPrefs
        val userEmail = SharedPrefs.getUserEmail() ?: "Not available"

        binding.tvUsername.text = userEmail

        binding.tvUsername.setOnClickListener {
            val intent = Intent(this@MainActivity, ProfileActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter { category ->
            val intent = Intent(this, QuizActivity::class.java).apply {
                putExtra("SELECTED_CATEGORY", category)
            }
            startActivity(intent)
        }

        binding.categoriesRecyclerView.apply {
            layoutManager = GridLayoutManager(this@MainActivity, 2)
            adapter = categoryAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupObservers() {
        // Observe categories with StateFlow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.categories.collect { categories ->
                    categoryAdapter.submitList(categories)
                    if (categories.isNotEmpty()) {
                        binding.emptyState.visibility = View.GONE
                    } else {
                        binding.emptyState.visibility = View.VISIBLE
                    }
                }
            }
        }

        // Observe loading state with StateFlow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.isLoading.collect { isLoading ->
                    binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                    if (isLoading) {
                        binding.emptyState.visibility = View.GONE
                    }
                }
            }
        }

        // Observe errors with StateFlow
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.error.collect { error ->
                    error?.let {
                        Toast.makeText(this@MainActivity, "Error: $it", Toast.LENGTH_LONG).show()
                        viewModel.clearError()
                        binding.emptyState.visibility = View.VISIBLE
                        binding.emptyState.text = "${getString(R.string.error)}\n${getString(R.string.tap_to_retry)}"
                    }
                }
            }
        }

        // Setup retry on empty state click
        binding.emptyState.setOnClickListener {
            viewModel.loadCategories()
        }
    }
}