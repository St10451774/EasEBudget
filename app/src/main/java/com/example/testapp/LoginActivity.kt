package com.example.testapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.EditText
import android.widget.Button
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var database: AppDatabase
    private lateinit var userDao: UserDao
    private lateinit var biometricHelper: BiometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        database = DatabaseProvider.getInstance(this)
        userDao = database.userDao()
        biometricHelper = BiometricHelper(this)

        val username = findViewById<EditText>(R.id.usernameInput)
        val password = findViewById<EditText>(R.id.passwordInput)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val registerBtn = findViewById<Button>(R.id.goToRegisterBtn)
        val biometricBtn = findViewById<Button>(R.id.biometricBtn)

        loginBtn.setOnClickListener {
            val usernameText = username.text.toString().trim()
            val passwordText = password.text.toString()

            if (usernameText.isEmpty() || passwordText.isEmpty()) {
                Toast.makeText(this, "Please enter username and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            performLogin(usernameText, passwordText)
        }

        biometricBtn.setOnClickListener {
            performBiometricLogin()
        }

        registerBtn.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun performLogin(username: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userDao.login(username, password)
                
                withContext(Dispatchers.Main) {
                    if (user != null) {
                        Toast.makeText(this@LoginActivity, "Login Successful", Toast.LENGTH_SHORT).show()
                        
                        // Apply user's theme preference
                        if (user.darkMode) {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                        } else {
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                        }
                        
                        val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                        intent.putExtra("USER_ID", user.id)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@LoginActivity, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun performBiometricLogin() {
        biometricHelper.authenticate(
            activity = this,
            title = "EasEBudget Login",
            subtitle = "Use your fingerprint or face to login",
            negativeButtonText = "Cancel",
            onSuccess = {
                // Biometric success - get the user with biometric enabled
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        // For demo purposes, we'll get the first user with biometric enabled
                        // In a real app, you'd have a more sophisticated way to identify the user
                        val users = userDao.getAllUsers()
                        val biometricUser = users.find { it.biometricEnabled }
                        
                        withContext(Dispatchers.Main) {
                            if (biometricUser != null) {
                                Toast.makeText(this@LoginActivity, "Biometric Login Successful", Toast.LENGTH_SHORT).show()
                                
                                if (biometricUser.darkMode) {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                                } else {
                                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                                }
                                
                                val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                                intent.putExtra("USER_ID", biometricUser.id)
                                startActivity(intent)
                                finish()
                            } else {
                                Toast.makeText(this@LoginActivity, "No user with biometric login enabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@LoginActivity, "Biometric login error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onFailure = { errorMessage ->
                Toast.makeText(this@LoginActivity, "Biometric authentication failed: $errorMessage", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
