package com.example.testapp

import android.os.Bundle
import android.content.Intent
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var biometricHelper: BiometricHelper
    private var currentUserId: Int = -1
    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        currentUserId = intent.getIntExtra("USER_ID", -1)
        if (currentUserId == -1) {
            finish()
            return
        }

        database = DatabaseProvider.getInstance(this)
        userDao = database.userDao()
        biometricHelper = BiometricHelper(this)

        initializeViews()
        loadUserSettings()
    }

    private fun initializeViews() {
        // Language settings
        val languageSpinner = findViewById<Spinner>(R.id.languageSpinner)
        val languages = arrayOf("English", "Spanish", "French", "German", "Chinese", "Japanese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        // Dark mode toggle
        val darkModeSwitch = findViewById<Switch>(R.id.darkModeSwitch)
        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateDarkMode(isChecked)
        }

        // Biometric toggle
        val biometricSwitch = findViewById<Switch>(R.id.biometricSwitch)
        biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateBiometricSetting(isChecked)
        }

        // Notification settings
        val emailNotificationSwitch = findViewById<Switch>(R.id.emailNotificationSwitch)
        val smsNotificationSwitch = findViewById<Switch>(R.id.smsNotificationSwitch)

        emailNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationSettings(isChecked, smsNotificationSwitch.isChecked)
        }

        smsNotificationSwitch.setOnCheckedChangeListener { _, isChecked ->
            updateNotificationSettings(emailNotificationSwitch.isChecked, isChecked)
        }

        // Action buttons
        val saveBtn = findViewById<Button>(R.id.saveSettingsBtn)
        val logoutBtn = findViewById<Button>(R.id.logoutBtn)

        saveBtn.setOnClickListener {
            saveSettings()
        }

        logoutBtn.setOnClickListener {
            logout()
        }

        // Additional settings buttons
        val sharedAccountsBtn = findViewById<Button>(R.id.sharedAccountsBtn)
        val exportDataBtn = findViewById<Button>(R.id.exportDataBtn)
        val aboutBtn = findViewById<Button>(R.id.aboutBtn)

        sharedAccountsBtn.setOnClickListener {
            val intent = Intent(this, SharedAccountsActivity::class.java)
            intent.putExtra("USER_ID", currentUserId)
            startActivity(intent)
        }

        exportDataBtn.setOnClickListener {
            showExportOptions()
        }

        aboutBtn.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun loadUserSettings() {
        CoroutineScope(Dispatchers.IO).launch {
            currentUser = userDao.getUserById(currentUserId)
            
            withContext(Dispatchers.Main) {
                currentUser?.let { user ->
                    // Set language
                    val languageSpinner = findViewById<Spinner>(R.id.languageSpinner)
                    val languageIndex = when (user.language) {
                        "es" -> 1
                        "fr" -> 2
                        "de" -> 3
                        "zh" -> 4
                        "ja" -> 5
                        else -> 0
                    }
                    languageSpinner.setSelection(languageIndex)

                    // Set dark mode
                    val darkModeSwitch = findViewById<Switch>(R.id.darkModeSwitch)
                    darkModeSwitch.isChecked = user.darkMode

                    // Set biometric
                    val biometricSwitch = findViewById<Switch>(R.id.biometricSwitch)
                    biometricSwitch.isChecked = user.biometricEnabled
                    if (!biometricHelper.canAuthenticate()) {
                        biometricSwitch.isEnabled = false
                        biometricSwitch.text = "Biometrics not available"
                    }

                    // Set notifications
                    val emailNotificationSwitch = findViewById<Switch>(R.id.emailNotificationSwitch)
                    val smsNotificationSwitch = findViewById<Switch>(R.id.smsNotificationSwitch)
                    emailNotificationSwitch.isChecked = user.notificationEmail
                    smsNotificationSwitch.isChecked = user.notificationSMS

                    // Set user info
                    val usernameTextView = findViewById<TextView>(R.id.usernameTextView)
                    val emailTextView = findViewById<TextView>(R.id.emailInput)
                    usernameTextView.text = "Username: ${user.username}"
                    emailTextView.text = "Email: ${user.email}"
                }
            }
        }
    }

    private fun updateDarkMode(enableDarkMode: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enableDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun updateBiometricSetting(enable: Boolean) {
        if (enable && !biometricHelper.canAuthenticate()) {
            Toast.makeText(this, "Biometric authentication not available on this device", Toast.LENGTH_LONG).show()
            findViewById<Switch>(R.id.biometricSwitch).isChecked = false
            return
        }

        if (enable) {
            // Test biometric authentication
            biometricHelper.authenticate(
                activity = this,
                title = "Enable Biometric Login",
                subtitle = "Confirm your identity to enable biometric login",
                onSuccess = {
                // Success - keep switch enabled
                CoroutineScope(Dispatchers.IO).launch {
                    userDao.updateBiometricSetting(currentUserId, true)
                }
            }
        }
        } else {
            CoroutineScope(Dispatchers.IO).launch {
                userDao.updateBiometricSetting(currentUserId, false)
            }
        }
    }

    private fun updateNotificationSettings(emailEnabled: Boolean, smsEnabled: Boolean) {
        CoroutineScope(Dispatchers.IO).launch {
            userDao.updateNotificationSettings(currentUserId, emailEnabled, smsEnabled)
        }
    }

    private fun saveSettings() {
        val languageSpinner = findViewById<Spinner>(R.id.languageSpinner)
        val emailInput = findViewById<EditText>(R.id.emailInput)
        val phoneInput = findViewById<EditText>(R.id.phoneInput)

        val languageCode = when (languageSpinner.selectedItemPosition) {
            1 -> "es"
            2 -> "fr"
            3 -> "de"
            4 -> "zh"
            5 -> "ja"
            else -> "en"
        }

        CoroutineScope(Dispatchers.IO).launch {
            currentUser?.let { user ->
                val updatedUser = user.copy(
                    language = languageCode,
                    email = emailInput.text.toString().trim(),
                    phone = phoneInput.text.toString().trim()
                )
                userDao.updateUser(updatedUser)
                
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SettingsActivity, "Settings saved successfully", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showExportOptions() {
        val options = arrayOf("Export as CSV", "Export as PDF", "Export to Cloud")
        
        AlertDialog.Builder(this)
            .setTitle("Export Data")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportAsCSV()
                    1 -> exportAsPDF()
                    2 -> exportToCloud()
                }
            }
            .show()
    }

    private fun exportAsCSV() {
        Toast.makeText(this, "CSV export feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun exportAsPDF() {
        Toast.makeText(this, "PDF export feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun exportToCloud() {
        Toast.makeText(this, "Cloud export feature coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle("About EasEBudget")
            .setMessage("EasEBudget v1.0\n\nSmart Budget Tracking Made Easy\n\nFeatures:\n• Biometric Authentication\n• Expense Categorization\n• Shared Accounts\n• Gamification\n• Reports & Analytics\n\n© 2024 EasEBudget")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
