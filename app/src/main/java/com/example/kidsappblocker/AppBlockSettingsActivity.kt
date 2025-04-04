package com.example.kidsappblocker

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.edit
import com.google.android.material.textfield.TextInputEditText
import android.content.ContentValues
import android.os.Environment
import android.provider.MediaStore
import android.database.Cursor

class AppBlockSettingsActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "AppBlockSettingsActivity"
    }

    private lateinit var blockedApps: MutableSet<String>
    private lateinit var appListRecyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private var blockGooglePlay: Boolean = true // Default true to include Google Play
    private lateinit var passwordManager: PasswordManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_block_settings)
        Log.d(TAG, "onCreate called")

        passwordManager = PasswordManager(this)

        // Check if a password is already set, if not prompt for password creation
        if (!passwordManager.isPasswordSet()) {
            Log.d(TAG, "Password not set, prompting for password creation")
            promptForPasswordCreation()
        } else {
            Log.d(TAG, "Password already set")
        }

        // Initialize SharedPreferences to store blocked apps
        blockedApps = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getStringSet("BlockedApps", mutableSetOf()) ?: mutableSetOf()
        Log.d(TAG, "Blocked apps loaded: $blockedApps")

        // Initialize RecyclerView
        appListRecyclerView = findViewById(R.id.appListRecyclerView)
        appListRecyclerView.layoutManager = LinearLayoutManager(this)
        Log.d(TAG, "RecyclerView initialized")

        // Initialize the adapter for the RecyclerView
        appListAdapter = AppListAdapter(getInstalledApps(), BlockedAppsManager(this)) { appPackageName, isChecked ->
            if (isChecked) {
                Log.d(TAG, "App checked for blocking: $appPackageName")
                blockedApps.add(appPackageName)
            } else {
                Log.d(TAG, "App unchecked for blocking: $appPackageName")
                blockedApps.remove(appPackageName)
            }
        }
        appListRecyclerView.adapter = appListAdapter
        Log.d(TAG, "Adapter set for RecyclerView")

        // Get the list of installed apps based on the current filter settings
        updateAppList()

        // Button to save the selected blocked apps
        val saveButton = findViewById<Button>(R.id.saveBlockedAppsButton)
        saveButton.setOnClickListener {
            Log.d(TAG, "Save button clicked")
            saveBlockedApps()
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()

            // Optionally, show the saved message for a few seconds and then close the activity
            Handler(Looper.getMainLooper()).postDelayed({
                Log.d(TAG, "Finishing activity after save")
                finish()  // Close the activity after a few seconds
            }, 3000)
        }

        // Example code to interact with MediaProvider
        val contentUri = MediaStore.Files.getContentUri("external_primary")
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "example.txt")
            // Ensure primary directory is set correctly
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // or Environment.DIRECTORY_DOCUMENTS
        }

        // Check if the file already exists
        val cursor: Cursor? = contentResolver.query(
            contentUri,
            arrayOf(MediaStore.MediaColumns._ID),
            "${MediaStore.MediaColumns.DISPLAY_NAME}=? AND ${MediaStore.MediaColumns.RELATIVE_PATH}=?",
            arrayOf("example.txt", Environment.DIRECTORY_DOWNLOADS),
            null
        )

        if (cursor != null && cursor.moveToFirst()) {
            Log.d(TAG, "File already exists, skipping insertion")
        } else {
            val uri = contentResolver.insert(contentUri, values)
            Log.d(TAG, "Inserted new file with URI: $uri")
        }

        cursor?.close()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop called")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
    }

    private fun promptForPasswordCreation() {
        Log.d(TAG, "Prompting for password creation")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Password")
        val input = TextInputEditText(this)
        input.hint = "Enter password"
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val password = input.text.toString()
            if (password.isNotEmpty()) {
                Log.d(TAG, "Password entered: $password")
                passwordManager.setPassword(password)
                Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Log.d(TAG, "Empty password entered, prompting again")
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                promptForPasswordCreation()
            }
        }
        builder.setCancelable(false)
        builder.show()
    }

    // Function to get the list of installed apps based on the Google Play exclusion
    private fun updateAppList() {
        Log.d(TAG, "Updating app list with blockGooglePlay: $blockGooglePlay")
        val apps = BlockedAppsManager(this).getInstalledApps(blockGooglePlay = blockGooglePlay)
        appListAdapter.updateApps(apps)
        Log.d(TAG, "App list updated: ${apps.size} apps found")
    }

    // Function to save the blocked apps to SharedPreferences
    private fun saveBlockedApps() {
        Log.d(TAG, "Saving blocked apps: $blockedApps")
        getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit {
            putStringSet("BlockedApps", blockedApps)
        }
        Log.d(TAG, "Blocked apps saved")
    }

    // Function to get installed apps from the BlockedAppsManager based on the filters
    private fun getInstalledApps(): List<AppInfo> {
        Log.d(TAG, "Getting installed apps with blockGooglePlay: $blockGooglePlay")
        val apps = BlockedAppsManager(this).getInstalledApps(blockGooglePlay = blockGooglePlay)
        Log.d(TAG, "Installed apps retrieved: ${apps.size} apps found")
        return apps
    }
}