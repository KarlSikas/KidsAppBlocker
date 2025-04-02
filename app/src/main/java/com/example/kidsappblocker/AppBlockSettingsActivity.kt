package com.example.kidsappblocker

import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.edit
import com.google.android.material.textfield.TextInputEditText

class AppBlockSettingsActivity : AppCompatActivity() {

    private lateinit var blockedApps: MutableSet<String>
    private lateinit var appListRecyclerView: RecyclerView
    private lateinit var appListAdapter: AppListAdapter
    private var blockGooglePlay: Boolean = true // Default true to include Google Play
    private lateinit var passwordManager: PasswordManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_block_settings)

        passwordManager = PasswordManager(this)

        // Check if a password is already set, if not prompt for password creation
        if (!passwordManager.isPasswordSet()) {
            promptForPasswordCreation()
        }

        // Initialize SharedPreferences to store blocked apps
        blockedApps = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            .getStringSet("BlockedApps", mutableSetOf()) ?: mutableSetOf()

        // Initialize RecyclerView
        appListRecyclerView = findViewById(R.id.appListRecyclerView)
        appListRecyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter for the RecyclerView
        appListAdapter = AppListAdapter(getInstalledApps(), BlockedAppsManager(this)) { appPackageName, isChecked ->
            if (isChecked) {
                blockedApps.add(appPackageName)
            } else {
                blockedApps.remove(appPackageName)
            }
        }
        appListRecyclerView.adapter = appListAdapter

        // Get the list of installed apps based on the current filter settings
        updateAppList()

        // Button to save the selected blocked apps
        val saveButton = findViewById<Button>(R.id.saveBlockedAppsButton)
        saveButton.setOnClickListener {
            saveBlockedApps()
            Toast.makeText(this, "Settings saved!", Toast.LENGTH_SHORT).show()

            // Optionally, show the saved message for a few seconds and then close the activity
            Handler(Looper.getMainLooper()).postDelayed({
                finish()  // Close the activity after a few seconds
            }, 3000)
        }
    }

    private fun promptForPasswordCreation() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Create Password")
        val input = TextInputEditText(this)
        input.hint = "Enter password"
        builder.setView(input)

        builder.setPositiveButton("OK") { dialog, _ ->
            val password = input.text.toString()
            if (password.isNotEmpty()) {
                passwordManager.setPassword(password)
                Toast.makeText(this, "Password set successfully", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show()
                promptForPasswordCreation()
            }
        }
        builder.setCancelable(false)
        builder.show()
    }

    // Function to get the list of installed apps based on the Google Play exclusion
    private fun updateAppList() {
        val apps = BlockedAppsManager(this).getInstalledApps(blockGooglePlay = blockGooglePlay)
        appListAdapter.updateApps(apps)
    }

    // Function to save the blocked apps to SharedPreferences
    private fun saveBlockedApps() {
        getSharedPreferences("AppPrefs", Context.MODE_PRIVATE).edit {
            putStringSet("BlockedApps", blockedApps)
        }
    }

    // Function to get installed apps from the BlockedAppsManager based on the filters
    private fun getInstalledApps(): List<AppInfo> {
        return BlockedAppsManager(this).getInstalledApps(blockGooglePlay = blockGooglePlay)
    }
}