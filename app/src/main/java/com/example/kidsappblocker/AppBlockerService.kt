package com.example.kidsappblocker

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.util.Log

class AppBlockerService : NotificationListenerService() {

    private val handler = Handler()
    private val blockedApps = mutableSetOf<String>()

    companion object {
        private const val TAG = "AppBlockerService"
        private const val CHECK_INTERVAL = 2000L // Interval to check running apps in milliseconds
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AppBlockerService created")
        loadBlockedApps()
        if (isUsageStatsPermissionGranted() && isNotificationListenerPermissionGranted()) {
            startAppMonitoring()
        } else {
            Log.e(TAG, "Required permissions not granted")
            requestPermissions()
        }
    }

    private fun loadBlockedApps() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        blockedApps.clear()
        blockedApps.addAll(prefs.getStringSet("BlockedApps", emptySet()) ?: emptySet())
        Log.d(TAG, "Loaded blocked apps: $blockedApps")
    }

    private fun startAppMonitoring() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                Log.d(TAG, "Checking running apps...")
                loadBlockedApps() // Reload blocked apps to ensure the latest list
                checkRunningApps()
                handler.postDelayed(this, CHECK_INTERVAL) // Check every 2 seconds
            }
        }, CHECK_INTERVAL)
    }

    private fun checkRunningApps() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        Log.d(TAG, "Querying usage stats from ${time - 3000} to $time")
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 3000, time)

        if (usageStats != null && usageStats.isNotEmpty()) {
            Log.d(TAG, "Usage stats retrieved: ${usageStats.size} entries")
            val recentApp = usageStats.maxByOrNull { it.lastTimeUsed }
            if (recentApp != null) {
                Log.d(TAG, "Recent app: ${recentApp.packageName}, last time used: ${recentApp.lastTimeUsed}")
                recentApp.packageName?.let {
                    if (blockedApps.contains(it)) {
                        Log.d(TAG, "Blocked app accessed: $it")
                        launchPasswordPrompt()
                    } else {
                        Log.d(TAG, "App not blocked: $it")
                    }
                }
            } else {
                Log.d(TAG, "No recent app found")
            }
        } else {
            Log.d(TAG, "No usage stats available or permission not granted")
        }
    }

    private fun launchPasswordPrompt() {
        Log.d(TAG, "Launching password prompt")
        try {
            val intent = PasswordPromptActivity.newIntent(this)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            launchHomeScreen() // Immediately launch home screen to block access
        } catch (e: Exception) {
            Log.e(TAG, "Error launching password prompt", e)
        }
    }

    private fun launchHomeScreen() {
        Log.d(TAG, "Launching home screen")
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN)
            homeIntent.addCategory(Intent.CATEGORY_HOME)
            homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(homeIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Error launching home screen", e)
        }
    }

    private fun isUsageStatsPermissionGranted(): Boolean {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val appList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, 0, System.currentTimeMillis())
        val granted = appList != null && appList.isNotEmpty()
        Log.d(TAG, "Usage stats permission granted: $granted")
        return granted
    }

    private fun isNotificationListenerPermissionGranted(): Boolean {
        val contentResolver = contentResolver
        val enabledNotificationListeners = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        val granted = enabledNotificationListeners != null && enabledNotificationListeners.contains(packageName)
        Log.d(TAG, "Notification listener permission granted: $granted")
        return granted
    }

    private fun requestPermissions() {
        requestUsageStatsPermission()
        requestNotificationListenerPermission()
    }

    private fun requestUsageStatsPermission() {
        Log.d(TAG, "Requesting usage stats permission")
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun requestNotificationListenerPermission() {
        Log.d(TAG, "Requesting notification listener permission")
        val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.d(TAG, "Notification listener service connected")
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        Log.d(TAG, "Notification listener service disconnected")
    }
}