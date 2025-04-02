package com.example.kidsappblocker

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.util.Log

class AppBlockerService : NotificationListenerService() {

    private val handler = Handler()
    private val blockedApps = mutableSetOf<String>()

    companion object {
        private const val TAG = "AppBlockerService"
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "AppBlockerService created")
        loadBlockedApps()
        startAppMonitoring()
    }

    private fun loadBlockedApps() {
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        blockedApps.addAll(prefs.getStringSet("BlockedApps", emptySet()) ?: emptySet())
        Log.d(TAG, "Loaded blocked apps: $blockedApps")
    }

    private fun startAppMonitoring() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                Log.d(TAG, "Checking running apps...")
                checkRunningApps()
                handler.postDelayed(this, 2000) // Check every 2 seconds
            }
        }, 2000)
    }

    private fun checkRunningApps() {
        val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val time = System.currentTimeMillis()
        val usageStats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 10000, time)

        if (usageStats != null) {
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
            Log.d(TAG, "No usage stats available")
        }
    }

    private fun launchPasswordPrompt() {
        Log.d(TAG, "Launching password prompt")
        val intent = PasswordPromptActivity.newIntent(this)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        launchHomeScreen() // Immediately launch home screen to block access
    }

    private fun launchHomeScreen() {
        Log.d(TAG, "Launching home screen")
        val homeIntent = Intent(Intent.ACTION_MAIN)
        homeIntent.addCategory(Intent.CATEGORY_HOME)
        homeIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(homeIntent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }
}