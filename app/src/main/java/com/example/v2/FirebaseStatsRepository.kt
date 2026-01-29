package com.example.v2

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseStatsRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val COLLECTION_NAME = "child_stats"

    /**
     * Upload daily stats for a child (called by child's device)
     */
    fun uploadDailyStats(childId: String, stats: DailyStats) {
        try {
            val docId = "${childId}_${stats.date}"

            val data = hashMapOf(
                "childId" to childId,
                "date" to stats.date,
                "screenTimeMinutes" to stats.screenTimeMinutes,
                "socialNotificationCount" to stats.socialNotificationCount,
                "riskScore" to stats.riskScore,
                "riskLevel" to stats.riskLevel.name,
                "reasons" to stats.reasons,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection(COLLECTION_NAME)
                .document(docId)
                .set(data)
                .addOnSuccessListener {
                    Log.d("FIREBASE_UPLOAD", "Stats uploaded for child $childId on ${stats.date}")
                }
                .addOnFailureListener { e ->
                    Log.e("FIREBASE_UPLOAD", "Failed to upload stats", e)
                }
        } catch (e: Exception) {
            Log.e("FIREBASE_UPLOAD", "Exception during upload", e)
        }
    }

    /**
     * Fetch daily stats for a child (called by parent's device)
     * FIXED: Removed .orderBy() to avoid Firestore index requirement
     */
    fun fetchDailyStats(
        childId: String,
        onResult: (List<DailyStats>) -> Unit
    ) {
        Log.d("FIREBASE_FETCH", "Starting fetch for child: $childId")

        db.collection(COLLECTION_NAME)
            .whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { snapshot ->
                try {
                    Log.d("FIREBASE_FETCH", "Query successful. Documents found: ${snapshot.size()}")

                    val statsList = snapshot.documents.mapNotNull { doc ->
                        try {
                            Log.d("FIREBASE_FETCH", "Processing doc: ${doc.id}")
                            DailyStats(
                                date = doc.getString("date") ?: "",
                                screenTimeMinutes = doc.getLong("screenTimeMinutes")?.toInt() ?: 0,
                                socialNotificationCount = doc.getLong("socialNotificationCount")?.toInt() ?: 0,
                                riskScore = doc.getLong("riskScore")?.toInt() ?: 0,
                                riskLevel = RiskLevel.valueOf(
                                    doc.getString("riskLevel") ?: "LOW"
                                ),
                                reasons = (doc.get("reasons") as? List<*>)
                                    ?.mapNotNull { it as? String }
                                    ?: emptyList()
                            )
                        } catch (e: Exception) {
                            Log.e("FIREBASE_FETCH", "Failed to parse document: ${doc.id}", e)
                            null
                        }
                    }.sortedByDescending { it.date } // Sort in code instead of Firestore query

                    Log.d("FIREBASE_FETCH", "Successfully fetched ${statsList.size} records for child $childId")
                    onResult(statsList)
                } catch (e: Exception) {
                    Log.e("FIREBASE_FETCH", "Failed to process documents", e)
                    onResult(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_FETCH", "Query failed for child $childId", e)
                onResult(emptyList())
            }
    }

    /**
     * Delete all stats for a child (for testing/cleanup)
     */
    fun deleteAllStatsForChild(childId: String, onComplete: (Boolean) -> Unit) {
        db.collection(COLLECTION_NAME)
            .whereEqualTo("childId", childId)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("FIREBASE_DELETE", "Deleted all stats for child $childId")
                        onComplete(true)
                    }
                    .addOnFailureListener { e ->
                        Log.e("FIREBASE_DELETE", "Failed to delete stats", e)
                        onComplete(false)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FIREBASE_DELETE", "Failed to query stats for deletion", e)
                onComplete(false)
            }
    }
}
