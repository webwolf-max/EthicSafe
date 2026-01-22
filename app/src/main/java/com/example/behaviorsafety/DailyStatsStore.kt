package com.example.behaviorsafety

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DailyStatsStore {

    private const val PREFS_NAME = "daily_stats_store"
    private const val MAX_DAYS = 7

    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE

    private fun prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // ---------------- SAVE ----------------

    fun saveTodayStats(context: Context, stats: DailyStats) {
        val editor = prefs(context).edit()
        editor.putString(stats.date, serialize(stats))
        editor.apply()

        cleanupOldDays(context)
    }

    // ---------------- LOAD ----------------

    fun getStatsForDate(context: Context, date: String): DailyStats? {
        val data = prefs(context).getString(date, null) ?: return null
        return deserialize(data)
    }

    fun getLast7Days(context: Context): List<String> {
        return prefs(context).all.keys
            .sortedDescending()
            .take(MAX_DAYS)
    }

    // ---------------- CLEANUP ----------------

    private fun cleanupOldDays(context: Context) {
        val preferences = prefs(context)
        val cutoffDate = LocalDate.now().minusDays(MAX_DAYS.toLong())
        val editor = preferences.edit()

        for (key in preferences.all.keys) {
            val date = runCatching { LocalDate.parse(key, formatter) }.getOrNull()
            if (date != null && date.isBefore(cutoffDate)) {
                editor.remove(key)
            }
        }

        editor.apply()
    }

    // ---------------- SERIALIZATION ----------------

    private fun serialize(stats: DailyStats): String {
        return listOf(
            stats.date,
            stats.screenTimeMinutes.toString(),
            stats.socialNotificationCount.toString(),
            stats.riskScore.toString(),
            stats.riskLevel.name,
            stats.reasons.joinToString("|")
        ).joinToString(";")
    }

    private fun deserialize(data: String): DailyStats {
        val parts = data.split(";")

        return DailyStats(
            date = parts[0],
            screenTimeMinutes = parts[1].toInt(),
            socialNotificationCount = parts[2].toInt(),
            riskScore = parts[3].toInt(),
            riskLevel = RiskLevel.valueOf(parts[4]),
            reasons =
                if (parts.size > 5 && parts[5].isNotBlank())
                    parts[5].split("|")
                else emptyList()
        )
    }
}
