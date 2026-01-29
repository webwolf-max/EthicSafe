package com.example.v2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.v2.ui.theme.BehaviorSafetyAppTheme

class ParentalGuideActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val riskLevel = intent.getStringExtra("RISK_LEVEL") ?: "MODERATE"
        val riskScore = intent.getIntExtra("RISK_SCORE", 50)

        setContent {
            BehaviorSafetyAppTheme {
                ParentalGuideScreen(
                    riskLevel = riskLevel,
                    riskScore = riskScore,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentalGuideScreen(
    riskLevel: String,
    riskScore: Int,
    onBackPressed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Friendly Next Steps") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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

            Spacer(modifier = Modifier.height(16.dp))

            // Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (riskLevel) {
                        "LOW" -> MaterialTheme.colorScheme.primaryContainer
                        "MODERATE" -> MaterialTheme.colorScheme.tertiaryContainer
                        else -> MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (riskLevel) {
                                "LOW" -> Icons.Default.CheckCircle
                                "MODERATE" -> Icons.Default.Info
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Current Risk Level: $riskLevel",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Score: $riskScore/100",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Introduction
            Text(
                text = "A Supportive Approach",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "These guidelines are designed to help you support your child's online safety while maintaining trust and open communication. Remember, the goal is to guide, not punish.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Step 1: Start a Conversation
            GuideSection(
                icon = Icons.Default.Info,
                title = "1. Start with Understanding",
                content = listOf(
                    "Choose a calm, private moment when you both have time to talk",
                    "Express curiosity rather than concern: 'I noticed you've been spending more time online. Tell me about what you enjoy doing'",
                    "Listen without interrupting. Let them share their perspective first",
                    "Avoid accusatory language or immediately jumping to restrictions",
                    "Show genuine interest in their online activities and friendships"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 2: Discuss Together
            GuideSection(
                icon = Icons.Default.Info,
                title = "2. Discuss the Data Together",
                content = listOf(
                    "Show them the app data in a non-judgmental way",
                    "Ask: 'What do you think about this screen time? Does it feel right to you?'",
                    "Discuss how they feel after spending time online - energized or tired?",
                    "Talk about the quality vs. quantity of online time",
                    "Acknowledge positive online activities (learning, creativity, connecting with friends)"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Step 3: Collaborative Solutions
            GuideSection(
                icon = Icons.Default.CheckCircle,
                title = "3. Create Solutions Together",
                content = listOf(
                    "Ask your child what boundaries they think would be helpful",
                    "Set tech-free times together (family meals, before bedtime)",
                    "Create a family media agreement that applies to everyone, including parents",
                    "Encourage offline activities they enjoy - hobbies, sports, reading",
                    "Suggest 'phone holidays' or digital detox weekends as a family activity",
                    "Focus on adding positive activities rather than just removing screen time"
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Age-Appropriate Guidance
            when (riskLevel) {
                "HIGH" -> HighRiskGuidance()
                "MODERATE" -> ModerateRiskGuidance()
                else -> LowRiskGuidance()
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Warning Signs Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "When to Seek Professional Help",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    val warningSignsList = listOf(
                        "Significant changes in mood, sleep, or eating habits",
                        "Withdrawal from family, friends, and activities they used to enjoy",
                        "Declining academic performance",
                        "Secretive behavior or extreme reactions when asked about online activity",
                        "Signs of cyberbullying (as victim or perpetrator)",
                        "Evidence of contact with strangers online",
                        "Your child expresses feelings of anxiety or depression related to social media"
                    )

                    warningSignsList.forEach { sign ->
                        Text(
                            text = "• $sign",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Consider consulting with a school counselor, pediatrician, or mental health professional who specializes in adolescent issues.",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Key Principles Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Key Principles to Remember",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    PrincipleItem("Trust is more important than control")
                    PrincipleItem("Model healthy tech habits yourself")
                    PrincipleItem("Focus on connection, not perfection")
                    PrincipleItem("Celebrate progress, not just compliance")
                    PrincipleItem("Keep communication open and judgment-free")
                    PrincipleItem("Remember that mistakes are learning opportunities")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Resources
            Text(
                text = "Additional Resources",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            ResourceCard(
                title = "Common Sense Media",
                description = "Age-based reviews and advice on media and technology"
            )

            ResourceCard(
                title = "American Academy of Pediatrics",
                description = "Family media plan tools and guidelines"
            )

            ResourceCard(
                title = "Internet Matters",
                description = "Expert advice on keeping children safe online"
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun GuideSection(
    icon: ImageVector,
    title: String,
    content: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            content.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text(
                        text = "•",
                        modifier = Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun HighRiskGuidance() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "High Risk - Immediate Supportive Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )

            Spacer(modifier = Modifier.height(12.dp))

            val actions = listOf(
                "Have a heart-to-heart conversation within the next 24-48 hours",
                "Consider implementing temporary boundaries while you both create a long-term plan together",
                "Look for signs of cyberbullying, inappropriate content, or concerning contacts",
                "Spend quality offline time together daily",
                "Consult with a school counselor or therapist if behavior changes are significant",
                "Create a 'digital wellness plan' together with achievable weekly goals",
                "Consider family counseling if communication has broken down"
            )

            actions.forEach { action ->
                Text(
                    text = "• $action",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun ModerateRiskGuidance() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Moderate Risk - Proactive Steps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val actions = listOf(
                "Schedule a casual conversation this week about online habits",
                "Introduce tech-free family activities (game nights, outdoor activities)",
                "Discuss and set reasonable screen time limits together",
                "Help them identify one offline hobby or activity to pursue",
                "Review privacy settings on social media together",
                "Check in weekly about how they're feeling about their screen time",
                "Lead by example - put your own phone away during family time"
            )

            actions.forEach { action ->
                Text(
                    text = "• $action",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun LowRiskGuidance() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Low Risk - Maintaining Healthy Habits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            val actions = listOf(
                "Continue open conversations about their online experiences",
                "Acknowledge and praise their healthy tech habits",
                "Stay informed about apps and platforms they use",
                "Maintain tech-free zones and times (bedrooms, dinner table)",
                "Encourage continued balance between online and offline activities",
                "Keep lines of communication open without micromanaging",
                "Periodically review and update family media agreements together"
            )

            actions.forEach { action ->
                Text(
                    text = "• $action",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 3.dp)
                )
            }
        }
    }
}

@Composable
fun PrincipleItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(20.dp)
                .padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun ResourceCard(
    title: String,
    description: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
