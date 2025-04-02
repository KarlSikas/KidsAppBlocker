package com.example.kidsappblocker

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText

class PasswordPromptActivity : Activity() {

    private lateinit var passwordManager: PasswordManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_prompt)

        passwordManager = PasswordManager(this)

        val passwordInput = findViewById<TextInputEditText>(R.id.passwordInput)
        val submitButton = findViewById<Button>(R.id.submitButton)

        submitButton.setOnClickListener {
            val inputPassword = passwordInput.text.toString()
            if (passwordManager.verifyPassword(inputPassword)) {
                Toast.makeText(this, "Access granted", Toast.LENGTH_SHORT).show()
                finish() // Finish the activity and allow access
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                // Relaunch Home Screen to block access
                launchHomeScreen()
            }
        }
    }

    override fun onBackPressed() {
        // Prevent back button from closing the activity
    }

    private fun launchHomeScreen() {
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
    }

    companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PasswordPromptActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
    }
}