package com.example.kidsappblocker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_PASSWORD = "Password"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val passwordInput = findViewById<EditText>(R.id.passwordInput)
        val loginButton = findViewById<Button>(R.id.loginButton)

        loginButton.setOnClickListener {
            val enteredPassword = passwordInput.text.toString()
            if (validatePassword(enteredPassword)) {
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                // Redirect to the app selection activity instead of the main screen
                startActivity(Intent(this, AppBlockSettingsActivity::class.java)) // Redirect to app selection screen
                finish()
            } else {
                Toast.makeText(this, "Incorrect Password", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validatePassword(enteredPassword: String): Boolean {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedPassword = prefs.getString(KEY_PASSWORD, "")
        return enteredPassword == savedPassword
    }
}
