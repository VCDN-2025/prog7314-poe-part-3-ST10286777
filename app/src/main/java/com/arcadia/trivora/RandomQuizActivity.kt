package com.arcadia.trivora

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.arcadia.trivora.databinding.ActivityRandomQuizBinding
import java.io.Serializable

class RandomQuizActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRandomQuizBinding
    private val viewModel: RandomQuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRandomQuizBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        setupObservers()

    }

    private fun setupClickListeners() {
        binding.singleQuestionBtn.setOnClickListener {
            viewModel.loadRandomQuestion()
        }

        binding.quizOfFiveBtn.setOnClickListener {
            viewModel.loadRandomQuestions(5)
        }

        binding.backButton.setOnClickListener {
            finish()
        }
    }

    private fun setupObservers() {
        viewModel.randomQuestion.observe(this) { question ->
            question?.let {
                val intent = Intent(this, QuizActivity::class.java).apply {
                    putExtra("QUESTIONS", arrayListOf(it) as Serializable)
                    putExtra("QUIZ_MODE", "RANDOM_SINGLE")
                }
                startActivity(intent)
            }
        }

        viewModel.randomQuestions.observe(this) { questions ->
            if (questions.isNotEmpty()) {
                val intent = Intent(this, QuizActivity::class.java).apply {
                    putExtra("QUESTIONS", ArrayList(questions) as Serializable)
                    putExtra("QUIZ_MODE", "RANDOM_QUIZ")
                }
                startActivity(intent)
            }
        }

        viewModel.isLoading.observe(this) { isLoading ->
            binding.progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        }

        viewModel.error.observe(this) { error ->
            error?.let {
                Toast.makeText(this, "Error: $it", Toast.LENGTH_SHORT).show()
            }
        }
    }
}