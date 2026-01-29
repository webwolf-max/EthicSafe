package com.example.v2

import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.Calendar

object UsageStatsHelper {

    fun getTodayUsageStats(context: Context): List<UsageStats> {
        val usageStatsManager =
            context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

        val startTime = startOfToday()
        val endTime = System.currentTimeMillis() // now (safe, accurate)

        return usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            startTime,
            endTime
        )
    }

    fun getTotalScreenTime(usageStats: List<UsageStats>): Long {
        return usageStats.sumOf { it.totalTimeInForeground }
    }

    fun getMostUsedApp(usageStats: List<UsageStats>): UsageStats? {
        return usageStats.maxByOrNull { it.totalTimeInForeground }
    }

    // ---------- Time helpers (single responsibility) ----------

    private fun startOfToday(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}
