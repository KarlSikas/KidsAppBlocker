package com.example.kidsappblocker

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.provider.Settings
import android.app.AppOpsManager
import android.os.Process
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import android.util.Log

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_PASSWORD_SET = "PasswordSet"
        private const val KEY_FIRST_LAUNCH = "FirstLaunch"
    }

    private lateinit var usagePermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize the Activity Result Launcher for requesting permission
        usagePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (isUsageAccessGranted()) {
                proceedAfterPermissionCheck()
            } else {
                Toast.makeText(this, "Permission not granted. The app will not function as intended.", Toast.LENGTH_LONG).show()
            }
        }

        // Check if it's the first time the app is launched
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            // If it's the first launch, prompt for usage access permission
            requestUsageAccessPermission()
            // Mark as not first launch
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        } else {
            // Check if the permission to access usage stats is granted
            if (!isUsageAccessGranted()) {
                // If permission is not granted, open the settings to request it
                requestUsageAccessPermission()
            } else {
                // If permission is already granted, check if the password is set
                proceedAfterPermissionCheck()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Recheck if the permission is granted when returning from settings
        if (isUsageAccessGranted()) {
            proceedAfterPermissionCheck()
        }
    }

    private fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        usagePermissionLauncher.launch(intent)
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        return mode == AppOpsManager.MODE_ALLOWED
    }

    private fun proceedAfterPermissionCheck() {
        // Start the AppBlockerService
        startAppBlockerService()

        // Proceed with checking if the password is set
        if (isPasswordSet()) {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, SetupPasswordActivity::class.java))
        }
        finish() // Close this activity so the user can't go back here
    }

    // Function to check if the password is set
    private fun isPasswordSet(): Boolean {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_PASSWORD_SET, false)
    }

    // Function to start the AppBlockerService
    private fun startAppBlockerService() {
        val intent = Intent(this, AppBlockerService::class.java)
        startService(intent)
        Log.d("MainActivity", "AppBlockerService started")
    }
}