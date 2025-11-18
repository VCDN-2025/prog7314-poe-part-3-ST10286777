package com.arcadia.trivora

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.arcadia.trivora.databinding.ActivityOfflineQuizzesBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class OfflineQuizzesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOfflineQuizzesBinding
    private val viewModel: QuizViewModel by viewModels()
    private lateinit var offlineQuizRepository: OfflineQuizRepository
    private lateinit var offlineCategoriesAdapter: OfflineCategoriesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOfflineQuizzesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        val database = TrivoraDatabase.getInstance(this)
        offlineQuizRepository = OfflineQuizRepository(database)

        setupRecyclerView()
        loadOfflineCategories()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        offlineCategoriesAdapter = OfflineCategoriesAdapter { category ->
            startOfflineQuiz(category)
        }

        binding.rvOfflineCategories.apply {
            layoutManager = LinearLayoutManager(this@OfflineQuizzesActivity)
            adapter = offlineCategoriesAdapter
        }
    }

    private fun loadOfflineCategories() {
        lifecycleScope.launch {
            // Show loading
            binding.progressBar.visibility = View.VISIBLE
            binding.emptyState.visibility = View.GONE

            try {
                // Get all available quizzes and extract unique categories
                val availableQuizzes = offlineQuizRepository.getAvailableQuizzes().first()
                val categories = availableQuizzes.map { it.category }.distinct()

                if (categories.isNotEmpty()) {
                    offlineCategoriesAdapter.submitList(categories)
                    binding.emptyState.visibility = View.GONE
                } else {
                    binding.emptyState.visibility = View.VISIBLE
                    binding.emptyState.text = "No offline quizzes available. Please download quizzes when online."
                }
            } catch (e: Exception) {
                binding.emptyState.visibility = View.VISIBLE
                binding.emptyState.text = "Error loading offline quizzes: ${e.message}"
            } finally {
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private fun startOfflineQuiz(category: String) {
        val intent = Intent(this, QuizActivity::class.java).apply {
            putExtra("SELECTED_CATEGORY", category)
            putExtra("FORCE_OFFLINE", true) // Add flag to force offline mode
        }
        startActivity(intent)
    }

    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.emptyState.setOnClickListener {
            loadOfflineCategories() // Retry loading
        }
    }
}