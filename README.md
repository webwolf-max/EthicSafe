# EthicSafe

EthicSafe is a privacy-preserving Android application designed to assess cyber risk in children using behavioral metadata rather than personal content.

# Key Features
- Metadata-based behavioral risk analysis
- Cyber Risk Score (0–100)
- Dual-device architecture (Child device & Parent dashboard)
- No message or content inspection

# Technology Stack
- Android (Kotlin)
- Firebase (for secure aggregation and sync)
- Android UsageStatsManager
- NotificationListenerService

# Architecture
The system follows a three-layer architecture:
1. Data Collection Layer
2. Analysis Layer
3. Presentation Layer

# Privacy Considerations
EthicSafe operates exclusively on system-level metadata and does not access personal messages, media, or communications.
