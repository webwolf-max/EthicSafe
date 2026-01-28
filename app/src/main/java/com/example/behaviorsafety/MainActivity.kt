package com.example.behaviorsafety

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.behaviorsafety.ui.theme.BehaviorSafetyAppTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.sin



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

    // ---- LIVE DATA ----
    val usageStats = UsageStatsHelper.getTodayUsageStats(context)
    val riskResultToday = RiskEngine.analyze(context, usageStats)
    val totalTimeTodayMs = UsageStatsHelper.getTotalScreenTime(usageStats)

    // ---- SAVE TODAY ----
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
    }

    // ---- LOAD SELECTED ----
    val selectedStats = remember(selectedDate) {
        DailyStatsStore.getStatsForDate(context, selectedDate)
    }

    val screenTime = selectedStats?.screenTimeMinutes ?: 0
    val notificationCount = selectedStats?.socialNotificationCount ?: 0
    val riskScore = selectedStats?.riskScore ?: 0
    val riskLevel = selectedStats?.riskLevel ?: RiskLevel.LOW
    val riskReasons = selectedStats?.reasons ?: emptyList()

    // ---- UI ----
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
            text = "Behavior Safety Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.align(Alignment.CenterHorizontally)
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

        Spacer(modifier = Modifier.height(16.dp))

        // ---- CYBER RISK GAUGE ----
        CyberRiskGauge(
            riskScore = riskScore,
            riskLevel = riskLevel
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ---- RISK REASONS ----
        RiskSummaryCard(
            riskResult = RiskResult(
                score = riskScore,
                level = riskLevel,
                reasons = riskReasons
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

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

// ---- MOST USED APP (TODAY ONLY) ----
        if (selectedDate == todayDate) {

            val mostUsed = UsageStatsHelper.getMostUsedApp(usageStats)

            val appName = mostUsed?.let {
                AppInfoResolver.getAppName(context, it.packageName)
            } ?: "Unknown"

            val appIconBitmap = mostUsed?.let {
                AppInfoResolver.getAppIconBitmap(context, it.packageName)
            }

            MostUsedAppCard(
                appName = appName,
                appIcon = appIconBitmap
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                context.startActivity(
                    Intent(context, CyberSafetyActivity::class.java)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Stay Safe Online")
        }


    }
}

@Composable
fun CyberRiskGauge(
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

        Spacer(modifier = Modifier.height(4.dp)) // smaller gap

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
        // 👇 This now sits CLOSE to the gauge
        Text(
            text = "${riskLevel.name} • $riskScore/100",
            style = MaterialTheme.typography.titleMedium,
            color = gaugeColor
        )
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
