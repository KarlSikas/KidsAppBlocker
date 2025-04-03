package com.example.kidsappblocker

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_PASSWORD_SET = "PasswordSet"
        private const val KEY_FIRST_LAUNCH = "FirstLaunch"
        private const val TAG = "MainActivity"
    }

    private lateinit var usagePermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var notificationPermissionLauncher: ActivityResultLauncher<Intent>
    private lateinit var overlayPermissionLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        Log.d(TAG, "onCreate called")

        // Capture and log device information
        logDeviceInfo()

        // Initialize the Activity Result Launcher for requesting permissions
        usagePermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "Usage permission result received: $result")
            if (isUsageAccessGranted()) {
                Log.d(TAG, "Usage access granted after result")
                checkAllPermissionsAndProceed()
            } else {
                Log.d(TAG, "Usage access not granted after result")
                showPermissionDeniedDialog()
            }
        }

        notificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "Notification permission result received: $result")
            if (isNotificationAccessGranted()) {
                Log.d(TAG, "Notification access granted after result")
                checkAllPermissionsAndProceed()
            } else {
                Log.d(TAG, "Notification access not granted after result")
                showPermissionDeniedDialog()
            }
        }

        overlayPermissionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "Overlay permission result received: $result")
            if (isOverlayPermissionGranted()) {
                Log.d(TAG, "Overlay access granted after result")
                checkAllPermissionsAndProceed()
            } else {
                Log.d(TAG, "Overlay access not granted after result")
                showPermissionDeniedDialog()
            }
        }

        // Check if it's the first time the app is launched
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)

        if (isFirstLaunch) {
            Log.d(TAG, "First launch detected")
            // If it's the first launch, prompt for usage access and notification access permissions
            requestUsageAccessPermission()
            requestNotificationAccessPermission()
            requestOverlayPermission()
            // Mark as not first launch
            prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
        } else {
            Log.d(TAG, "Not the first launch")
            // Check if the permissions to access usage stats and notification listener are granted
            checkAllPermissionsAndProceed()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        // Recheck if the permissions are granted when returning from settings
        checkAllPermissionsAndProceed()
    }

    private fun checkAllPermissionsAndProceed() {
        Log.d(TAG, "Checking all permissions")
        if (isUsageAccessGranted() && isNotificationAccessGranted() && isOverlayPermissionGranted()) {
            Log.d(TAG, "All permissions granted")
            proceedAfterPermissionCheck()
        } else {
            Log.d(TAG, "Permissions not granted, requesting permissions")
            requestUsageAccessPermission()
            requestNotificationAccessPermission()
            requestOverlayPermission()
        }
    }

    private fun requestUsageAccessPermission() {
        Log.d(TAG, "Requesting usage access permission")
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        usagePermissionLauncher.launch(intent)
    }

    private fun isUsageAccessGranted(): Boolean {
        val appOps = getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName)
        val granted = mode == AppOpsManager.MODE_ALLOWED
        Log.d(TAG, "Usage access granted: $granted")
        return granted
    }

    private fun requestNotificationAccessPermission() {
        Log.d(TAG, "Requesting notification access permission")
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        notificationPermissionLauncher.launch(intent)
    }

    private fun isNotificationAccessGranted(): Boolean {
        val contentResolver = contentResolver
        val enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val granted = enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName)
        Log.d(TAG, "Notification access granted: $granted")
        return granted
    }

    private fun requestOverlayPermission() {
        Log.d(TAG, "Requesting overlay permission")
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        overlayPermissionLauncher.launch(intent)
    }

    private fun isOverlayPermissionGranted(): Boolean {
        val granted = Settings.canDrawOverlays(this)
        Log.d(TAG, "Overlay permission granted: $granted")
        return granted
    }

    private fun proceedAfterPermissionCheck() {
        Log.d(TAG, "Proceeding after permission check")
        // Start the AppBlockerService
        startAppBlockerService()

        // Proceed with checking if the password is set
        if (isPasswordSet()) {
            Log.d(TAG, "Password is set, starting LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            Log.d(TAG, "Password is not set, starting SetupPasswordActivity")
            startActivity(Intent(this, SetupPasswordActivity::class.java))
        }
        finish() // Close this activity so the user can't go back here
    }

    // Function to check if the password is set
    private fun isPasswordSet(): Boolean {
        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isSet = prefs.getBoolean(KEY_PASSWORD_SET, false)
        Log.d(TAG, "Password set: $isSet")
        return isSet
    }

    // Function to start the AppBlockerService
    private fun startAppBlockerService() {
        Log.d(TAG, "Starting AppBlockerService")
        val intent = Intent(this, AppBlockerService::class.java)
        startService(intent)
        Log.d(TAG, "AppBlockerService started")
    }

    // Function to log device information
    private fun logDeviceInfo() {
        val deviceModel = Build.MODEL
        val deviceManufacturer = Build.MANUFACTURER
        val apiLevel = Build.VERSION.SDK_INT
        Log.d(TAG, "Device Info: Model: $deviceModel, Manufacturer: $deviceManufacturer, API Level: $apiLevel")
    }

    // Function to open app settings
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.parse("package:$packageName")
        startActivity(intent)
    }

    // Function to show permission denied dialog
    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("This app needs certain permissions to function properly. Please grant the required permissions in the settings.")
            .setPositiveButton("Go to Settings") { _, _ ->
                openAppSettings()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}