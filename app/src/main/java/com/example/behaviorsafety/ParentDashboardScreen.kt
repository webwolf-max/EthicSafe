package com.example.behaviorsafety

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ParentDashboardScreen(
    childId: String = "child_001"
) {

    var statsList by remember { mutableStateOf<List<DailyStats>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 🔥 FETCH FROM FIREBASE
    LaunchedEffect(Unit) {
        FirebaseStatsRepository.fetchDailyStats(childId) { result ->
            try {
                statsList = result.sortedByDescending { it.date }
                isLoading = false
            } catch (e: Exception) {
                error = e.message
                isLoading = false
                Log.e("PARENT_UI", "UI error", e)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Parent Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
            }

            error != null -> {
                Text(
                    text = "Error: $error",
                    color = MaterialTheme.colorScheme.error
                )
            }

            statsList.isEmpty() -> {
                Text("No data available for this child.")
            }

            else -> {
                val latest = statsList.first()

                StatCard("Date", latest.date)
                StatCard("Screen Time", "${latest.screenTimeMinutes} mins")
                StatCard("Social Notifications", latest.socialNotificationCount.toString())
                StatCard("Risk Score", latest.riskScore.toString())
                StatCard("Risk Level", latest.riskLevel.name)

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Risk Reasons",
                    style = MaterialTheme.typography.titleMedium
                )

                latest.reasons.forEach {
                    Text("• $it")
                }
            }
        }
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
