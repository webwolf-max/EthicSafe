package com.example.v2

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ParentDashboardScreen(
    modifier: Modifier = Modifier,
    childId: String
) {

    var statsList by remember { mutableStateOf<List<DailyStats>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 🔥 FETCH FROM FIREBASE
    LaunchedEffect(childId) {
        if (childId.isBlank()) {
            error = "No child ID provided"
            isLoading = false
            Log.e("PARENT_UI", "Child ID is blank!")
            return@LaunchedEffect
        }

        Log.d("PARENT_UI", "Fetching data for child: $childId")

        FirebaseStatsRepository.fetchDailyStats(childId) { result ->
            try {
                Log.d("PARENT_UI", "Received ${result.size} records from Firebase")

                if (result.isEmpty()) {
                    error = "No data found for child ID: $childId"
                    Log.w("PARENT_UI", "Empty result for child: $childId")
                } else {
                    statsList = result.sortedByDescending { it.date }
                    Log.d("PARENT_UI", "Successfully loaded ${statsList.size} records")
                }

                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
                Log.e("PARENT_UI", "UI error", e)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        // ---- LOGO ----
        Image(
            painter = painterResource(id = R.drawable.loggggoo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Monitoring Child",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Child ID: $childId",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = error ?: "Unknown error",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            statsList.isEmpty() -> {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "No data available",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "The child hasn't used the app yet or no data has been uploaded.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            else -> {
                val latest = statsList.first()

                Text(
                    text = "Latest Activity",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ---- CYBER RISK GAUGE ----
                CyberRiskGauges(
                    riskScore = latest.riskScore,
                    riskLevel = latest.riskLevel
                )

                Spacer(modifier = Modifier.height(16.dp))

                StatCard("Date", latest.date)
                StatCard("Screen Time", "${latest.screenTimeMinutes} mins")
                StatCard("Social Notifications", latest.socialNotificationCount.toString())
                StatCard("Risk Score", "${latest.riskScore}/100")
                StatCard("Risk Level", latest.riskLevel.name)

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (latest.riskLevel) {
                            RiskLevel.LOW -> MaterialTheme.colorScheme.primaryContainer
                            RiskLevel.MODERATE -> MaterialTheme.colorScheme.tertiaryContainer
                            RiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Risk Reasons",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (latest.reasons.isEmpty()) {
                            Text("No risk factors detected")
                        } else {
                            latest.reasons.forEach {
                                Text("• $it", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }

                // Show history if available
                if (statsList.size > 1) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Recent History",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    statsList.drop(1).take(5).forEach { stats ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = stats.date,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${stats.screenTimeMinutes}m",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${stats.socialNotificationCount} notifs",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = "${stats.riskLevel.name}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CyberRiskGauges(
    riskScore: Int,
    riskLevel: RiskLevel
) {
    val clampedScore = riskScore.coerceIn(0, 100)
    val sweepAngle = (clampedScore / 100f) * 180f

    val gaugeColor = when {
        clampedScore < 30 -> Color(0xFF2E7D32)
        clampedScore < 60 -> Color(0xFFF9A825)
        clampedScore < 80 -> Color(0xFFEF6C00)
        else -> Color(0xFFC62828)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Cyber Risk Level",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(4.dp))

        Canvas(
            modifier = Modifier
                .width(240.dp)
                .height(140.dp)
        ) {
            val strokeWidth = 28f
            val radius = size.width / 2 - strokeWidth
            val center = Offset(size.width / 2, size.height)

            // Background arc
            drawArc(
                color = Color.LightGray,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Active arc
            drawArc(
                color = gaugeColor,
                startAngle = 180f,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(strokeWidth, cap = StrokeCap.Round)
            )

            // Needle
            val needleAngleRad =
                Math.toRadians((180 + sweepAngle).toDouble())

            val needleLength = radius - 12

            val needleEnd = Offset(
                x = center.x + needleLength * cos(needleAngleRad).toFloat(),
                y = center.y + needleLength * sin(needleAngleRad).toFloat()
            )

            drawLine(
                color = Color.Black,
                start = center,
                end = needleEnd,
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            drawCircle(
                color = Color.Black,
                radius = 10f,
                center = center
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "${riskLevel.name} • $riskScore/100",
            style = MaterialTheme.typography.titleMedium,
            color = gaugeColor
        )
    }
}

@Composable
fun StatCard(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(title, style = MaterialTheme.typography.labelLarge)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}