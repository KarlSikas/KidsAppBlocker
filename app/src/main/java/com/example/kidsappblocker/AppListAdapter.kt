package com.example.kidsappblocker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter to display a list of installed apps with checkboxes to block/unblock apps
class AppListAdapter(
    private var apps: List<AppInfo>, // List of apps to be displayed
    private val blockedAppsManager: BlockedAppsManager, // Manager to handle blocked apps
    private val onAppCheckedChangeListener: (String, Boolean) -> Unit // Callback for checkbox state change
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    // ViewHolder to hold references to views in the list item
    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val appName: TextView = view.findViewById(R.id.appName) // App name TextView
        val checkBox: CheckBox = view.findViewById(R.id.appCheckBox) // App block/unblock checkbox
        val appIcon: ImageView = view.findViewById(R.id.appIcon) // App icon ImageView
    }

    // Create new view for each app item
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app_list, parent, false) // Inflate the item layout
        return AppViewHolder(view)
    }

    // Bind data (app name, icon, checkbox) to the view
    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.appName.text = app.appName // Set app name
        holder.appIcon.setImageDrawable(app.appIcon) // Set app icon (ensure appIcon is non-null)

        // Check if the app can be blocked
        val isBlockable = isAppBlockable(app.packageName)

        // Disable checkbox for non-blockable apps
        holder.checkBox.isEnabled = isBlockable

        // If the app is blockable, update checkbox state based on blockedAppsManager
        holder.checkBox.setOnCheckedChangeListener(null) // Remove any existing listener
        holder.checkBox.isChecked = blockedAppsManager.getBlockedApps().contains(app.packageName)

        // Set the checkbox change listener
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isBlockable) {
                onAppCheckedChangeListener(app.packageName, isChecked) // Notify if the checkbox state changed
                if (isChecked) {
                    blockedAppsManager.blockApp(app.packageName)
                } else {
                    blockedAppsManager.unblockApp(app.packageName)
                }
            }
        }
    }

    // Return the number of apps in the list
    override fun getItemCount(): Int = apps.size

    // Check if the app is blockable (exclude system apps or certain apps)
    private fun isAppBlockable(packageName: String): Boolean {
        // List of non-blockable apps (system apps, etc.)
        val nonBlockableApps = listOf(
            "com.android.settings",  // Settings app (non-blockable)
            "com.google.android.gms",  // Google Play Services (non-blockable)
            "com.android.vending"  // Google Play Store (non-blockable)
        )
        return !nonBlockableApps.contains(packageName) // Return true if app is blockable
    }

    // Update the apps list when it changes (e.g., new apps installed)
    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged() // Notify adapter that the data set has changed
    }
}