package com.example.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import android.content.Intent
import android.widget.ArrayAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        database = DatabaseProvider.getInstance(this)
        userDao = database.userDao()
        biometricHelper = BiometricHelper(this)

        val username = findViewById<EditText>(R.id.regUsername)
        val email = findViewById<EditText>(R.id.regEmail)
        val phone = findViewById<EditText>(R.id.regPhone)
        val password = findViewById<EditText>(R.id.regPassword)
        val confirmPassword = findViewById<EditText>(R.id.regConfirmPassword)
        val languageSpinner = findViewById<Spinner>(R.id.languageSpinner)
        val registerBtn = findViewById<Button>(R.id.registerBtn)
        val backToLoginBtn = findViewById<Button>(R.id.backToLoginBtn)

        // Setup language spinner
        val languages = arrayOf("English", "Spanish", "French", "German", "Chinese", "Japanese")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner.adapter = adapter

        registerBtn.setOnClickListener {
            val usernameText = username.text.toString().trim()
            val emailText = email.text.toString().trim()
            val phoneText = phone.text.toString().trim()
            val passwordText = password.text.toString()
            val confirmPasswordText = confirmPassword.text.toString()

            if (usernameText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText != confirmPasswordText) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (passwordText.length < 4) {
                Toast.makeText(this, "Password must be at least 4 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (emailText.isNotEmpty() && !isValidEmail(emailText)) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val languageCode = when (languageSpinner.selectedItemPosition) {
                1 -> "es"
                2 -> "fr"
                3 -> "de"
                4 -> "zh"
                5 -> "ja"
                else -> "en"
            }

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val existingUser = userDao.getUserByUsername(usernameText)
                    
                    withContext(Dispatchers.Main) {
                        if (existingUser != null) {
                            Toast.makeText(this@RegisterActivity, "Username already exists", Toast.LENGTH_SHORT).show()
                            return@withContext
                        }
                    }

                    val newUser = User(
                        username = usernameText,
                        password = passwordText,
                        email = emailText,
                        phone = phoneText,
                        language = languageCode,
                        biometricEnabled = biometricHelper.canAuthenticate(),
                        darkMode = false,
                        notificationEmail = emailText.isNotEmpty(),
                        notificationSMS = phoneText.isNotEmpty()
                    )
                    
                    userDao.registerUser(newUser)
                    
                    // Initialize user points
                    val gamificationDao = database.gamificationDao()
                    val initialPoints = UserPoints(userId = newUser.id)
                    gamificationDao.insertUserPoints(initialPoints)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                        
                        val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@RegisterActivity, "Registration failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        backToLoginBtn.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
