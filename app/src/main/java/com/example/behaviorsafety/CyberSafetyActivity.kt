package com.example.behaviorsafety

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import com.example.behaviorsafety.ui.theme.BehaviorSafetyAppTheme

class CyberSafetyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BehaviorSafetyAppTheme {
                CyberSafetyScreen()
            }
        }
    }
}

@Composable
fun CyberSafetyScreen() {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {

        Image(
            painter = painterResource(id = R.drawable.loggggoo),
            contentDescription = "App Logo",
            modifier = Modifier
                .size(96.dp)
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cyber Safety Awareness",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        AwarenessCard(
            title = "Online Strangers & Fake Profiles",
            cardColor = Color(0xFFE3F2FD),
            shortText = "Not everyone online is who they say they are.",
            details = listOf(
                "Some people create fake profiles using stolen photos",
                "They may pretend to be your age or a friend",
                "Their goal is to gain trust or get personal information"
            ),
            example = "Riya accepted a request from a fake classmate. When asked for her phone number, she told her mom."
        )

        AwarenessCard(
            title = "Oversharing Personal Information",
            cardColor = Color(0xFFE0F2F1),
            shortText = "Posting too much online can be risky.",
            details = listOf(
                "Avoid sharing school name or home address",
                "Daily routines can be tracked",
                "Old posts never fully disappear"
            ),
            example = "Arjun posted his school uniform daily. A stranger guessed his school correctly."
        )

        AwarenessCard(
            title = "Cyberbullying & Online Harassment",
            cardColor = Color(0xFFFDECEA),
            shortText = "Mean messages online are not okay.",
            details = listOf(
                "Includes teasing, threats, and spreading rumors",
                "Do not reply angrily",
                "Block the person and tell an adult"
            ),
            example = "Sneha blocked a bully and told her teacher. The messages stopped."
        )

        AwarenessCard(
            title = "Screen Time & Phone Addiction",
            cardColor = Color(0xFFF3E5F5),
            shortText = "Too much screen time affects health.",
            details = listOf(
                "Can cause eye strain and poor sleep",
                "Reduces focus in school",
                "Balance online and offline activities"
            ),
            example = "Karthik reduced phone use at night and felt more energetic in school."
        )

        AwarenessCard(
            title = "Scams, Fake Rewards & Links",
            cardColor = Color(0xFFFFF3E0),
            shortText = "If it sounds too good, it probably is.",
            details = listOf(
                "Free rewards are common scams",
                "Never share OTPs or passwords",
                "Do not click unknown links"
            ),
            example = "Ayaan clicked a free reward link and lost access to his account."
        )

        AwarenessCard(
            title = "Privacy Settings & App Permissions",
            cardColor = Color(0xFFE8F5E9),
            shortText = "Apps should not control everything.",
            details = listOf(
                "Check app permissions regularly",
                "Remove access for unused apps",
                "Ask an adult before allowing sensitive permissions"
            ),
            example = "Meera’s parent removed unnecessary permissions from an app."
        )

        AwarenessCard(
            title = "Talking to Trusted Adults",
            cardColor = Color(0xFFFCE4EC),
            shortText = "Asking for help is smart, not weak.",
            details = listOf(
                "Speak up if something feels wrong",
                "Adults can help safely",
                "You will not get into trouble"
            ),
            example = "Rahul told his dad about an uncomfortable chat. The issue was handled calmly."
        )
        Spacer(modifier = Modifier.height(24.dp))

        val context = androidx.compose.ui.platform.LocalContext.current

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            onClick = {
                val intent = android.content.Intent(
                    context,
                    CyberSOSActivity::class.java
                )
                context.startActivity(intent)
            }
        ) {
            Text(
                text = "Report an Issue",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
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
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(text = shortText)

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = { expanded = !expanded }) {
                Text(if (expanded) "Show Less" else "Read More")
            }

            if (expanded) {

                Spacer(modifier = Modifier.height(8.dp))

                details.forEach {
                    Text(text = "• $it")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Real-life example:",
                    fontWeight = FontWeight.SemiBold
                )

                Text(text = example)
            }
        }
    }
}
