package com.example.v2

import android.content.Context

object NotificationStatsStore {

    private const val PREFS_NAME = "notification_stats"
    private const val KEY_TOTAL = "total_social_notifications"
    private const val KEY_LAST_TIME = "last_notification_time"

    private const val DUPLICATE_WINDOW_MS = 1500L // 1.5 seconds

    fun incrementIfNew(
        context: Context,
        packageName: String,
        postTime: Long
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val lastTime = prefs.getLong(KEY_LAST_TIME, 0L)

        // Ignore duplicate callbacks
        if (postTime - lastTime < DUPLICATE_WINDOW_MS) return

        val current = prefs.getInt(KEY_TOTAL, 0)

        prefs.edit()
            .putInt(KEY_TOTAL, current + 1)
            .putLong(KEY_LAST_TIME, postTime)
            .apply()
    }

    fun getTotalSocialNotifications(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_TOTAL, 0)
    }

    fun reset(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }
}
