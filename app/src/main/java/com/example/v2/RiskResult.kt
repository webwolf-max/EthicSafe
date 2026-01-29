package com.example.v2

data class RiskResult(
    val score: Int,              // 0–100
    val level: RiskLevel,
    val reasons: List<String>
)

enum class RiskLevel {
    LOW,
    MODERATE,
    HIGH
}
