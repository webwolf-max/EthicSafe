package com.example.behaviorsafety

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.behaviorsafety.ui.theme.BehaviorSafetyAppTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BehaviorSafetyAppTheme {

                val lifecycleOwner = LocalLifecycleOwner.current

                var hasUsagePermission by remember { mutableStateOf(false) }
                var hasNotificationPermission by remember { mutableStateOf(false) }

                // ✅ THIS is the missing piece
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            hasUsagePermission =
                                PermissionUtils.hasUsageStatsPermission(this@MainActivity)

                            hasNotificationPermission =
                                NotificationPermissionUtils.hasNotificationAccess(this@MainActivity)
                        }
                    }

                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (hasUsagePermission && hasNotificationPermission) {
                        DashboardScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
//                        ParentDashboardScreen(
//                            childId = "child_001"
//                        )
                    } else {
                        PermissionScreen(
                            modifier = Modifier.padding(innerPadding),
                            onGrantUsageClick = {
                                startActivity(
                                    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                                )
                            },
                            onGrantNotificationClick = {
                                startActivity(
                                    Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // ---- DATE STATE ----
    val todayDate = java.time.LocalDate.now().toString()
    var selectedDate by remember { mutableStateOf(todayDate) }

    val availableDates = remember(context) {
        DailyStatsStore.getLast7Days(context)
    }

    // ---- LIVE DATA (ONLY FOR TODAY) ----
    val usageStats = UsageStatsHelper.getTodayUsageStats(context)
    val riskResultToday = RiskEngine.analyze(context, usageStats)
    val totalTimeTodayMs = UsageStatsHelper.getTotalScreenTime(usageStats)

    // ---- SAVE TODAY STATS ----
    LaunchedEffect(todayDate) {
        val todayStats = DailyStats(
            date = todayDate,
            screenTimeMinutes = (totalTimeTodayMs / 60000).toInt(),
            socialNotificationCount = NotificationStatsStore.getTotalSocialNotifications(context),
            riskScore = riskResultToday.score,
            riskLevel = riskResultToday.level,
            reasons = riskResultToday.reasons
        )
        DailyStatsStore.saveTodayStats(context, todayStats)

        FirebaseStatsRepository.uploadDailyStats(
            childId = "child_001",
            stats = todayStats
        )
    }

    // ---- LOAD STATS FOR SELECTED DATE ----
    val selectedStats = remember(selectedDate) {
        DailyStatsStore.getStatsForDate(context, selectedDate)
    }

    // ---- FALLBACKS ----
    val screenTime =
        selectedStats?.screenTimeMinutes ?: 0

    val notificationCount =
        if (selectedDate == todayDate) {
            NotificationStatsStore.getTotalSocialNotifications(context)
        } else {
            selectedStats?.socialNotificationCount ?: 0
        }


    val riskScore =
        selectedStats?.riskScore ?: 0

    val riskLevel =
        selectedStats?.riskLevel ?: RiskLevel.LOW

    val riskReasons =
        selectedStats?.reasons ?: emptyList()

    // ---- MOST USED APP (TODAY ONLY – HISTORICAL NOT POSSIBLE) ----
    val mostUsed = UsageStatsHelper.getMostUsedApp(usageStats)

    val appName = mostUsed?.let {
        AppInfoResolver.getAppName(context, it.packageName)
    } ?: "Unknown"

    val appIconBitmap = mostUsed?.let {
        AppInfoResolver.getAppIconBitmap(context, it.packageName)
    }

    LaunchedEffect(Unit) {
        FirebaseStatsRepository.fetchDailyStats("child_001") { stats ->
            stats.forEach {
                android.util.Log.d("PARENT_FETCH", it.toString())
            }
        }
    }


    // ---- UI ----
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Behavior Safety Dashboard",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ---- DATE DROPDOWN ----
        var expanded by remember { mutableStateOf(false) }

        Box {
            OutlinedButton(onClick = { expanded = true }) {
                Text("Viewing: $selectedDate")
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                availableDates.forEach { date ->
                    DropdownMenuItem(
                        text = { Text(date) },
                        onClick = {
                            selectedDate = date
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ---- RISK SUMMARY ----
        RiskSummaryCard(
            riskResult = selectedStats?.let {
                RiskResult(
                    score = it.riskScore,
                    level = it.riskLevel,
                    reasons = it.reasons
                )
            } ?: RiskResult(0, RiskLevel.LOW, emptyList())
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ---- INFO CARDS ----
        InfoCard(
            title = "Screen Time",
            value = "$screenTime mins",
            subtitle = selectedDate
        )

        Spacer(modifier = Modifier.height(12.dp))

        InfoCard(
            title = "Social Notifications",
            value = notificationCount.toString(),
            subtitle = selectedDate
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ---- MOST USED APP (TODAY) ----
        if (selectedDate == todayDate) {
            MostUsedAppCard(
                appName = appName,
                appIcon = appIconBitmap
            )
        }
    }
}

@Composable
fun RiskSummaryCard(riskResult: RiskResult) {

    val containerColor = when (riskResult.level) {
        RiskLevel.LOW -> MaterialTheme.colorScheme.primaryContainer
        RiskLevel.MODERATE -> MaterialTheme.colorScheme.tertiaryContainer
        RiskLevel.HIGH -> MaterialTheme.colorScheme.errorContainer
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = "Overall Cyber Risk",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${riskResult.level} • ${riskResult.score}/100",
                style = MaterialTheme.typography.headlineSmall
            )

            if (riskResult.reasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                riskResult.reasons.take(3).forEach { reason ->
                    Text(
                        text = "• $reason",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun InfoCard(
    title: String,
    value: String,
    subtitle: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.headlineSmall)
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun MostUsedAppCard(
    appName: String,
    appIcon: androidx.compose.ui.graphics.ImageBitmap?
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = "App Icon",
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column {
                Text("Most Used App", style = MaterialTheme.typography.labelLarge)
                Text(appName, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun PermissionScreen(
    modifier: Modifier = Modifier,
    onGrantUsageClick: () -> Unit,
    onGrantNotificationClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Permissions Required",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text =
                "To protect children, this app needs access to usage statistics " +
                        "and notification counts from social applications. " +
                        "We never read messages or notification content."
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onGrantUsageClick) {
            Text("Grant Usage Access")
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(onClick = onGrantNotificationClick) {
            Text("Grant Notification Access")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DashboardPreview() {
    BehaviorSafetyAppTheme {
        DashboardScreen()
    }
}
