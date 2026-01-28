package com.example.behaviorsafety

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import com.example.behaviorsafety.ui.theme.BehaviorSafetyAppTheme

class CyberSOSActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BehaviorSafetyAppTheme {
                CyberSOSScreen(
                    onBack = { finish() },
                    onCall1930 = {
                        startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:1930")
                            )
                        )
                    },
                    onCall1098 = {
                        startActivity(
                            Intent(
                                Intent.ACTION_DIAL,
                                Uri.parse("tel:1098")
                            )
                        )
                    },
                    onOpenPortal = {
                        startActivity(
                            Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://cybercrime.gov.in")
                            )
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CyberSOSScreen(
    onBack: () -> Unit,
    onCall1930: () -> Unit,
    onCall1098: () -> Unit,
    onOpenPortal: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cyber SOS") },
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
                .background(Color(0xFFFFF5F3))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ---- LOGO ----
            Image(
                painter = painterResource(id = R.drawable.loggggoo),
                contentDescription = "App Logo",
                modifier = Modifier.size(96.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Need Help?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Tap to call for help",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ================= FINANCIAL FRAUD =================
            HelpCard(
                title = "Financial Fraud & Money Scams",
                description = "For online money frauds, fake links, UPI scams, investment scams",
                buttonText = "Call 1930",
                buttonColor = Color(0xFF7B1E3A), // soft maroon
                onClick = onCall1930,
                cases = listOf(
                    "WhatsApp Fake Cousin Scam (2024): ₹50,000 was transferred to a scammer posing as a cousin. The victim called 1930 immediately and the cyber cell recovered the full amount.",
                    "Digital Arrest Scam (2025): A retired engineer was threatened using fake CBI video calls. Reporting to 1930 helped freeze remaining funds and start recovery.",
                    "Mumbai Investment Scam (2025): Over ₹1.49 crore was frozen in 24 hours after victims reported fake Telegram trading groups to 1930."
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ================= CHILD SAFETY =================
            HelpCard(
                title = "Child Abuse, Bullying & Online Grooming",
                description = "For children under 18 facing harassment, abuse, blackmail or fear",
                buttonText = "Call 1098",
                buttonColor = Color(0xFF6A1B9A), // calm purple
                onClick = onCall1098,
                cases = listOf(
                    "Fake Profile Harassment (Varanasi): A cousin created a fake account and posted obscene content. 1098 helped remove content and supported the child.",
                    "Online Grooming Case (Bengaluru): A 14-year-old was blackmailed using images. Childline intervened and provided legal and emotional support.",
                    "Cyberbullying Case (Pune, 2025): Edited images were circulated online. 1098 coordinated with police to stop the harassment."
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ================= ONLINE PORTAL =================
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "File a Complaint Online",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "You can also report cyber crimes directly on the official government portal.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = onOpenPortal,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Open cybercrime.gov.in")
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun HelpCard(
    title: String,
    description: String,
    buttonText: String,
    buttonColor: Color,
    onClick: () -> Unit,
    cases: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF9F9F9)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClick,
                colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(buttonText, color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Real-life incidents:",
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            cases.forEach {
                Text(
                    text = "• $it",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(6.dp))
            }
        }
    }
}
