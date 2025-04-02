package com.example.kidsappblocker

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.util.Log

class BlockedAppsManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_BLOCKED_APPS = "BlockedApps"
        private const val TAG = "BlockedAppsManager"
    }

    // Get a list of installed apps (only user-installed apps, excluding system apps and optionally Google Play Store)
    fun getInstalledApps(blockGooglePlay: Boolean = false): List<AppInfo> {
        val packageManager = context.packageManager
        val apps = mutableListOf<AppInfo>()

        // Get all installed applications (both system and user apps)
        val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)

        for (app in installedApps) {
            // Determine if the app is a system app
            val isSystemApp = app.flags and ApplicationInfo.FLAG_SYSTEM != 0
            val isUpdatedSystemApp = app.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
            val isUserApp = !isSystemApp && !isUpdatedSystemApp

            // Log details for debugging
            Log.d(TAG, "Checking app: ${app.packageName}, isSystemApp: $isSystemApp, isUpdatedSystemApp: $isUpdatedSystemApp, isUserApp: $isUserApp")

            // Skip Google Play Store if blockGooglePlay is true
            if (blockGooglePlay && app.packageName == "com.android.vending") {
                Log.d(TAG, "Skipping Google Play Store: ${app.packageName}")
                continue
            }

            // Only include user-installed apps (this includes apps installed via APK or from Play Store)
            if (isUserApp) {
                try {
                    val appName = packageManager.getApplicationLabel(app).toString()
                    val appIcon: Drawable = packageManager.getApplicationIcon(app) // Get the app icon
                    apps.add(AppInfo(appName, app.packageName, appIcon))
                    Log.d(TAG, "User App found: $appName (Package: ${app.packageName})")
                } catch (e: Exception) {
                    Log.e(TAG, "Error retrieving app information for package: ${app.packageName}", e)
                }
            } else {
                Log.d(TAG, "Skipping system app: ${app.packageName}")
            }
        }

        // Sort the list by app name and return
        return apps.sortedBy { it.appName }
    }

    // Get the set of blocked apps from SharedPreferences
    fun getBlockedApps(): Set<String> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getStringSet(KEY_BLOCKED_APPS, emptySet()) ?: emptySet()
    }

    // Save the set of blocked apps to SharedPreferences
    fun setBlockedApps(blockedApps: Set<String>) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        with(prefs.edit()) {
            putStringSet(KEY_BLOCKED_APPS, blockedApps)
            apply()
        }
    }

    // Add an app to the blocked list
    fun blockApp(packageName: String) {
        val blockedApps = getBlockedApps().toMutableSet()
        blockedApps.add(packageName)
        setBlockedApps(blockedApps)
    }

    // Remove an app from the blocked list
    fun unblockApp(packageName: String) {
        val blockedApps = getBlockedApps().toMutableSet()
        blockedApps.remove(packageName)
        setBlockedApps(blockedApps)
    }
}