package com.arcadia.trivora

import android.Manifest
import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.arcadia.trivora.databinding.ActivitySettingsBinding

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var toneGenerator: ToneGenerator
    private lateinit var vibrator: Vibrator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Apply the saved locale before setting content view
        val languageCode = LocaleHelper.getPersistedLanguage(this)
        val context = LocaleHelper.setLocale(this, languageCode)
        resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize tone generator for system beep
        toneGenerator = ToneGenerator(AudioManager.STREAM_SYSTEM, 100)

        // Initialize vibrator with API check
        vibrator = getSystemService(Vibrator::class.java)

        loadCurrentSettings()
        setupClickListeners()
        updateLanguageUI()

        binding.backButton.setOnClickListener {
            val intent = Intent(this@SettingsActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun updateLanguageUI() {
        // Update the current language display
        binding.currentLanguageText.text = getString(R.string.current_language,
            LocaleHelper.getCurrentLanguageName(this))
    }

    private fun loadCurrentSettings() {
        val settings = SharedPrefs.getSettings()

        binding.soundSwitch.isChecked = settings.soundEnabled
        binding.vibrationSwitch.isChecked = settings.vibrationEnabled

        binding.easyCheckbox.isChecked = settings.selectedDifficulties.contains("Easy")
        binding.mediumCheckbox.isChecked = settings.selectedDifficulties.contains("Medium")
        binding.hardCheckbox.isChecked = settings.selectedDifficulties.contains("Hard")

        // Load biometric setting
        binding.biometricSwitch.isChecked = SharedPrefs.isBiometricEnabled()

        // Check biometric status
        checkBiometricStatus()
    }

    private fun setupClickListeners() {
        binding.saveButton.setOnClickListener {
            saveSettings()
            playFeedback()
        }

        // Language change button
        binding.languageButton.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Test sound when sound switch is toggled
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                playSound()
            }
        }

        // Test vibration when vibration switch is toggled
        binding.vibrationSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                vibrate()
            }
        }

        // Handle biometric switch changes
        binding.biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            onBiometricSettingChanged(isChecked)
        }
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf(
            getString(R.string.english),
            getString(R.string.afrikaans)
        )

        val languageCodes = arrayOf("en", "af")
        val currentLanguage = LocaleHelper.getPersistedLanguage(this)
        val currentIndex = languageCodes.indexOf(currentLanguage)

        AlertDialog.Builder(this)
            .setTitle(R.string.select_language)
            .setSingleChoiceItems(languages, currentIndex) { dialog, which ->
                val selectedLanguageCode = languageCodes[which]
                changeLanguage(selectedLanguageCode)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun changeLanguage(languageCode: String) {
        // Save the selected language
        LocaleHelper.setLocale(this, languageCode)

        // Show confirmation message
        val languageName = when (languageCode) {
            "en" -> getString(R.string.english)
            "af" -> getString(R.string.afrikaans)
            else -> getString(R.string.english)
        }

        Toast.makeText(this, getString(R.string.language_changed_message, languageName), Toast.LENGTH_SHORT).show()

        // Restart the activity to apply language changes
        val intent = Intent(this, SettingsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    private fun onBiometricSettingChanged(isEnabled: Boolean) {
        if (isEnabled) {
            // User wants to enable biometrics - check if available
            if (BiometricLogin.isBiometricAvailable(this)) {
                SharedPrefs.setBiometricEnabled(true)
                updateBiometricStatus(getString(R.string.biometric_login_enabled))
                Toast.makeText(this, R.string.biometric_login_enabled, Toast.LENGTH_SHORT).show()
            } else {
                // Biometrics not available - revert the switch
                binding.biometricSwitch.isChecked = false
                updateBiometricStatus(getString(R.string.biometric_hardware_not_available))
                Toast.makeText(this, R.string.biometric_not_available_message, Toast.LENGTH_LONG).show()
            }
        } else {
            // User wants to disable biometrics
            SharedPrefs.setBiometricEnabled(false)
            updateBiometricStatus(getString(R.string.biometric_login_disabled))
            Toast.makeText(this, R.string.biometric_login_disabled, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkBiometricStatus() {
        val isBiometricAvailable = BiometricLogin.isBiometricAvailable(this)
        val isBiometricEnabled = SharedPrefs.isBiometricEnabled()

        val statusText = when {
            !isBiometricAvailable -> getString(R.string.biometric_hardware_not_available)
            isBiometricEnabled -> getString(R.string.biometric_login_enabled)
            else -> getString(R.string.biometric_login_disabled)
        }

        updateBiometricStatus(statusText)

        // Disable the switch if biometrics aren't available
        binding.biometricSwitch.isEnabled = isBiometricAvailable
    }

    private fun updateBiometricStatus(status: String) {
        binding.biometricStatusText.text = status
    }

    private fun saveSettings() {
        val selectedDifficulties = mutableSetOf<String>()
        if (binding.easyCheckbox.isChecked) selectedDifficulties.add("Easy")
        if (binding.mediumCheckbox.isChecked) selectedDifficulties.add("Medium")
        if (binding.hardCheckbox.isChecked) selectedDifficulties.add("Hard")

        if (selectedDifficulties.isEmpty()) {
            Toast.makeText(this, R.string.select_difficulty_message, Toast.LENGTH_SHORT).show()
            return
        }

        val settings = Settings(
            soundEnabled = binding.soundSwitch.isChecked,
            selectedDifficulties = selectedDifficulties,
            vibrationEnabled = binding.vibrationSwitch.isChecked,
        )

        SharedPrefs.saveSettings(settings)
        Toast.makeText(this, R.string.settings_saved_message, Toast.LENGTH_SHORT).show()
    }

    private fun playFeedback() {
        playSound()
        vibrate()
    }

    private fun playSound() {
        if (binding.soundSwitch.isChecked) {
            toneGenerator.startTone(ToneGenerator.TONE_DTMF_1, 200)
        }
    }

    private fun vibrate() {
        if (binding.vibrationSwitch.isChecked && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // API 26+ - use VibrationEffect
                val vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
                vibrator.vibrate(vibrationEffect)
            } else {
                // API 25 and below
                @Suppress("DEPRECATION")
                vibrator.vibrate(100)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        toneGenerator.release()
    }
}