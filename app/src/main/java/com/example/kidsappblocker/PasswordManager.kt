package com.example.kidsappblocker

import android.content.Context
import android.util.Log

class PasswordManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_PASSWORD = "Password"
        private const val TAG = "PasswordManager"
    }

    // Save the password to SharedPreferences
    fun setPassword(password: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putString(KEY_PASSWORD, password)
            apply()
        }
    }

    // Get the saved password from SharedPreferences
    fun getPassword(): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_PASSWORD, null)
    }

    // Verify the entered password
    fun verifyPassword(inputPassword: String): Boolean {
        val savedPassword = getPassword()
        val isPasswordCorrect = savedPassword == inputPassword
        Log.d(TAG, "Password verification: $isPasswordCorrect")
        return isPasswordCorrect
    }

    // Check if a password is set
    fun isPasswordSet(): Boolean {
        return getPassword() != null
    }
}