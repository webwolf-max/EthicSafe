package com.example.v2

import android.app.usage.UsageStats
import android.content.Context

object RiskEngine {

    fun analyze(
        context: Context,
        usageStats: List<UsageStats>
    ): RiskResult {

        var score = 0
        val reasons = mutableListOf<String>()
        val demoMode = true



        if (demoMode || BehaviorAnalyzer.hasLateNightSocialUsage(usageStats)) {
            score += 30
            reasons.add("Late-night social app usage detected")
        }

        if (demoMode || BehaviorAnalyzer.hasRepetitiveShortBursts(usageStats)) {
            score += 25
            reasons.add("Frequent short social app sessions")
        }

        if (BehaviorAnalyzer.hasSingleAppDominance(usageStats)) {
            score += 25
            reasons.add("Heavy focus on a single social application")
        }

        if ( BehaviorAnalyzer.hasHighSocialNotificationVolume(context)) {
            score += 20
            reasons.add("High number of social notifications today")
        }

        val level = when {
            score >= 60 -> RiskLevel.HIGH
            score >= 30 -> RiskLevel.MODERATE
            else -> RiskLevel.LOW
        }

        return RiskResult(
            score = score,
            level = level,
            reasons = reasons
        )
    }
}
