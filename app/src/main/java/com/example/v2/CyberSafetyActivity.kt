package com.example.v2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.v2.ui.theme.BehaviorSafetyAppTheme

class CyberSafetyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BehaviorSafetyAppTheme {
                CyberSafetyScreen(onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberSafetyScreen(onBack: () -> Unit) {

    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cyber Safety Awareness") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                    .size(88.dp)
                    .align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cyber Safety Awareness",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(20.dp))

            AwarenessCard(
                title = "Online Strangers & Fake Profiles",
                cardColor = Color(0xFFF1F5F9),
                shortText = "Not everyone online is who they say they are.",
                details = listOf(
                    "Some people create fake profiles using stolen photos",
                    "They may pretend to be your age or a friend",
                    "Their goal is to gain trust or personal information"
                ),
                example = "Riya received a friend request from a fake classmate. When money was asked, she told her parent and stayed safe."
            )

            AwarenessCard(
                title = "Oversharing Personal Information",
                cardColor = Color(0xFFEFF6F3),
                shortText = "Posting too much online can be risky.",
                details = listOf(
                    "Avoid sharing school name or home address",
                    "Daily routines can be tracked",
                    "Old posts never fully disappear"
                ),
                example = "Arjun posted his school uniform regularly. A stranger correctly guessed his school."
            )

            AwarenessCard(
                title = "Cyberbullying & Online Harassment",
                cardColor = Color(0xFFFDF1F0),
                shortText = "Mean messages online are not okay.",
                details = listOf(
                    "Includes teasing, threats, and spreading rumors",
                    "Do not reply angrily",
                    "Block the person and tell an adult"
                ),
                example = "Sneha blocked a bully and reported it. The harassment stopped."
            )

            AwarenessCard(
                title = "Screen Time & Phone Addiction",
                cardColor = Color(0xFFF4F0FA),
                shortText = "Too much screen time affects health.",
                details = listOf(
                    "Can cause eye strain and poor sleep",
                    "Reduces focus in school",
                    "Balance online and offline activities"
                ),
                example = "Karthik reduced late-night phone use and felt more focused."
            )

            AwarenessCard(
                title = "Scams, Fake Rewards & Links",
                cardColor = Color(0xFFFFF7ED),
                shortText = "If it sounds too good, it probably is.",
                details = listOf(
                    "Free rewards are common scams",
                    "Never share OTPs or passwords",
                    "Do not click unknown links"
                ),
                example = "Ayaan clicked a fake reward link and lost access to his account."
            )

            AwarenessCard(
                title = "Privacy Settings & App Permissions",
                cardColor = Color(0xFFF1F8F4),
                shortText = "Apps should not control everything.",
                details = listOf(
                    "Check app permissions regularly",
                    "Remove access for unused apps",
                    "Ask an adult before allowing permissions"
                ),
                example = "Meera’s parent removed unnecessary permissions from an app."
            )

            AwarenessCard(
                title = "Talking to Trusted Adults",
                cardColor = Color(0xFFFCEFF4),
                shortText = "Asking for help is smart, not weak.",
                details = listOf(
                    "Speak up if something feels wrong",
                    "Adults can help safely",
                    "You will not get into trouble"
                ),
                example = "Rahul told his parent about an uncomfortable chat. It was handled safely."
            )

            Spacer(modifier = Modifier.height(28.dp))

            // ---- REPORT BUTTON ----
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF7A1F2B) // calm maroon
                ),
                onClick = {
                    context.startActivity(
                        Intent(context, CyberSOSActivity::class.java)
                    )
                }
            ) {
                Text(
                    text = "Report an Issue",
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun AwarenessCard(
    title: String,
    cardColor: Color,
    shortText: String,
    details: List<String>,
    example: String
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = shortText,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Show Less" else "Read More")
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))

                details.forEach {
                    Text(text = "• $it", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Real-life example",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = example,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
