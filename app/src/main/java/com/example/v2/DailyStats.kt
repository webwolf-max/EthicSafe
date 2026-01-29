package com.example.v2

data class DailyStats(
    val date: String = "",                    // yyyy-MM-dd
    val screenTimeMinutes: Int = 0,
    val socialNotificationCount: Int = 0,
    val riskScore: Int = 0,
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val reasons: List<String> = emptyList()
)
