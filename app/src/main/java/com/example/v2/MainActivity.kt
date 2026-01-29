package com.example.v2

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.content.ClipData
import android.content.ClipboardManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.v2.ui.theme.BehaviorSafetyAppTheme
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BehaviorSafetyAppTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Loading) }
                var currentUser by remember { mutableStateOf<User?>(null) }

                // Check if user is already logged in
                LaunchedEffect(Unit) {
                    val user = SessionManager.getCurrentUser(this@MainActivity)
                    if (user != null) {
                        currentUser = user
                        currentScreen = Screen.Main
                    } else {
                        currentScreen = Screen.Login
                    }
                }

                when (currentScreen) {
                    Screen.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    Screen.Login -> {
                        LoginScreen(
                            onLoginSuccess = { user ->
                                currentUser = user
                                currentScreen = Screen.Main
                            },
                            onNavigateToRegister = {
                                currentScreen = Screen.Register
                            }
                        )
                    }

                    Screen.Register -> {
                        RegisterScreen(
                            onRegisterSuccess = {
                                currentScreen = Screen.Login
                            },
                            onNavigateToLogin = {
                                currentScreen = Screen.Login
                            }
                        )
                    }

                    Screen.Main -> {
                        MainScreen(
                            user = currentUser!!,
                            onLogout = {
                                SessionManager.clearSession(this@MainActivity)
                                currentUser = null
                                currentScreen = Screen.Login
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun UserIdCard(userId: String) {
    val context = LocalContext.current
    var showCopied by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Your User ID",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = userId,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }

            IconButton(
                onClick = {
                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("User ID", userId)
                    clipboard.setPrimaryClip(clip)

                    Toast.makeText(context, "User ID copied!", Toast.LENGTH_SHORT).show()
                    showCopied = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Copy User ID",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }

    if (showCopied) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            showCopied = false
        }
    }
}

enum class Screen {
    Loading,
    Login,
    Register,
    Main
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    user: User,
    onLogout: () -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current

    var hasUsagePermission by remember { mutableStateOf(false) }
    var hasNotificationPermission by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasUsagePermission =
                    PermissionUtils.hasUsageStatsPermission(context)

                hasNotificationPermission =
                    NotificationPermissionUtils.hasNotificationAccess(context)
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (user.role == UserRole.PARENT) {
                            "Parent Dashboard"
                        } else {
                            "Child Dashboard"
                        }
                    )
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (hasUsagePermission && hasNotificationPermission) {
            // Show appropriate dashboard based on role
            when (user.role) {
                UserRole.CHILD -> {
                    DashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        userId = user.id
                    )
                }

                UserRole.PARENT -> {
                    ParentDashboardScreen(
                        modifier = Modifier.padding(innerPadding),
                        childId = user.childId
                    )
                }
            }
        } else {
            PermissionScreen(
                modifier = Modifier.padding(innerPadding),
                onGrantUsageClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    )
                },
                onGrantNotificationClick = {
                    context.startActivity(
                        Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    )
                }
            )
        }
    }
}

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    userId: String
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

    // ---- SAVE TODAY TO FIREBASE ----
    LaunchedEffect(todayDate) {
        val todayStats = DailyStats(
            date = todayDate,
            screenTimeMinutes = (totalTimeTodayMs / 60000).toInt(),
            socialNotificationCount = NotificationStatsStore.getTotalSocialNotifications(context),
            riskScore = riskResultToday.score,
            riskLevel = riskResultToday.level,
            reasons = riskResultToday.reasons
        )

        // Save locally
        DailyStatsStore.saveTodayStats(context, todayStats)

        // Upload to Firebase for parent monitoring
        FirebaseStatsRepository.uploadDailyStats(userId, todayStats)
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

        // User ID Card with Copy Button
        UserIdCard(userId = userId)

        Spacer(modifier = Modifier.height(16.dp))

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
