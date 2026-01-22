package com.example.behaviorsafety

import android.app.usage.UsageStats
import android.content.Context
import java.util.Calendar

object BehaviorAnalyzer {

    private val socialAppKeywords = listOf(
        "whatsapp",
        "instagram",
        "facebook",
        "messenger",
        "snapchat",
        "telegram",
        "signal",
        "discord",
        "twitter",
        "reddit",
        "tiktok"
    )
    fun isSocialOrChatApp(packageName: String): Boolean {
        return socialAppKeywords.any { keyword ->
            packageName.contains(keyword, ignoreCase = true)
        }
    }

    fun hasLateNightSocialUsage(
        usageStats: List<UsageStats>
    ): Boolean {

        val calendar = Calendar.getInstance()

        for (stat in usageStats) {
            val lastUsed = stat.lastTimeUsed
            if (lastUsed == 0L) continue

            val packageName = stat.packageName
            if (!isSocialOrChatApp(packageName)) continue

            calendar.timeInMillis = lastUsed
            val hour = calendar.get(Calendar.HOUR_OF_DAY)

            // 11 PM – 5 AM
            if (hour >= 23 || hour <= 5) {
                return true
            }
        }

        return false
    }

    fun hasRepetitiveShortBursts(
        usageStats: List<UsageStats>
    ): Boolean {

        val appUsageCount = mutableMapOf<String, Int>()
        val appUsageTime = mutableMapOf<String, Long>()

        for (stat in usageStats) {
            val packageName = stat.packageName
            if (!isSocialOrChatApp(packageName)) continue

            appUsageCount[packageName] =
                (appUsageCount[packageName] ?: 0) + 1

            appUsageTime[packageName] =
                (appUsageTime[packageName] ?: 0L) + stat.totalTimeInForeground
        }

        for (packageName in appUsageCount.keys) {
            val count = appUsageCount[packageName] ?: 0
            val totalTime = appUsageTime[packageName] ?: 0L

            val avgSessionTimeMs =
                if (count > 0) totalTime / count else 0

            // Heuristic thresholds (demo-safe)
            if (count >= 10 && avgSessionTimeMs < 60_000) {
                return true
            }
        }

        return false
    }

    fun hasSingleAppDominance(
        usageStats: List<UsageStats>
    ): Boolean {

        val appUsageTime = mutableMapOf<String, Long>()
        var totalSocialTime = 0L

        for (stat in usageStats) {
            val packageName = stat.packageName
            if (!isSocialOrChatApp(packageName)) continue

            val time = stat.totalTimeInForeground
            if (time <= 0L) continue

            appUsageTime[packageName] =
                (appUsageTime[packageName] ?: 0L) + time

            totalSocialTime += time
        }

        if (totalSocialTime == 0L) return false

        for ((_, appTime) in appUsageTime) {
            val dominanceRatio =
                appTime.toDouble() / totalSocialTime.toDouble()

            // ≥ 60% of total social app time
            if (dominanceRatio >= 0.6) {
                return true
            }
        }

        return false
    }

    fun hasHighSocialNotificationVolume(context: Context): Boolean {
        val total = NotificationStatsStore.getTotalSocialNotifications(context)
        return total >= 20   // demo threshold
    }


}
