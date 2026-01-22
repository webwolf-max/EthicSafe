package com.example.behaviorsafety

import com.google.firebase.firestore.FirebaseFirestore

object FirebaseStatsRepository {

    private val db = FirebaseFirestore.getInstance()

    fun uploadDailyStats(
        childId: String,
        stats: DailyStats
    ) {
        db.collection("children")
            .document(childId)
            .collection("dailyStats")
            .document(stats.date)
            .set(stats)
    }

    fun fetchDailyStats(
        childId: String,
        onResult: (List<DailyStats>) -> Unit
    ) {
        android.util.Log.d("PARENT_FETCH", "Fetch started for $childId")

        db.collection("children")
            .document(childId)
            .collection("dailyStats")
            .get()
            .addOnSuccessListener { snapshot ->
                android.util.Log.d(
                    "PARENT_FETCH",
                    "Fetch success. Docs count = ${snapshot.size()}"
                )

                val stats = snapshot.documents.mapNotNull {
                    it.toObject(DailyStats::class.java)
                }

                android.util.Log.d(
                    "PARENT_FETCH",
                    "Parsed stats count = ${stats.size}"
                )

                onResult(stats)
            }
            .addOnFailureListener { e ->
                android.util.Log.e(
                    "PARENT_FETCH",
                    "Fetch FAILED",
                    e
                )
            }
    }

}
